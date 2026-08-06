package com.qituo.dcc.particles;

import com.qituo.dcc.DragonCurseChronicles;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

/**
 * SmartParticleDispatcher —— 自研智能粒子调度器
 *
 * 与市面上 Embeddium/Sodium 的核心差异：
 * 1. 不做激进剔除 → 确保粒子100%渲染，而不是被丢弃
 * 2. 智能分帧 → 每帧最多 120 个网络包，避免单帧爆卡 + 客户端卡顿
 * 3. LOD 距离衰减 → 0-10格全密度 / 10-30格半密度 / 30格以上1/3密度
 * 4. 渐进式生成 → 粒子从玩家向远处逐帧延伸，形成"激光射出"动画
 * 5. 颜色对象池 → 复用 DustParticleOptions，减少 GC
 * 6. 线程安全 → PriorityBlockingQueue 带优先级 + ConcurrentHashMap 对象池
 */
@Mod.EventBusSubscriber(modid = DragonCurseChronicles.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SmartParticleDispatcher {

    /** 每 tick 最多发送的粒子网络包数（单帧限流，避免网络拥塞和客户端卡顿） */
    private static final int MAX_PACKETS_PER_TICK = 120;
    /** LOD 分级距离阈值 */
    private static final double LOD_NEAR = 10.0;
    private static final double LOD_MID = 30.0;

    /**
     * 带优先级的待发送队列
     * 优先级规则：order 越小越先发（=距离越近先发，形成渐进式延伸效果）
     * order 由 (提交顺序序号 + 距离微调) 保证先提交的整体先出，近的在同批次中先出
     */
    private static final PriorityBlockingQueue<PendingParticle> PENDING_QUEUE =
            new PriorityBlockingQueue<>(512, (a, b) -> Double.compare(a.order, b.order));

    /**
     * DustParticleOptions 颜色对象池
     * key 由 RGB 各 16 位 + size 打包成 long，避免同色 new 多个对象
     */
    private static final ConcurrentHashMap<Long, DustParticleOptions> PARTICLE_POOL = new ConcurrentHashMap<>();

    /** 全局提交序号，保证多批次提交时整体按时间顺序出队 */
    private static final java.util.concurrent.atomic.AtomicLong SUBMIT_SEQ = new java.util.concurrent.atomic.AtomicLong(0);

    // ============ 颜色对象池：RGB(16bit)*3 + size(16bit) 打包 ============

    private static long colorKey(Vector3f color, float size) {
        int r = (int) (color.x * 65535F) & 0xFFFF;
        int g = (int) (color.y * 65535F) & 0xFFFF;
        int b = (int) (color.z * 65535F) & 0xFFFF;
        int s = (int) (size * 1000F) & 0xFFFF;
        return ((long) r << 48) | ((long) g << 32) | ((long) b << 16) | s;
    }

    /** 从对象池获取或创建 DustParticleOptions */
    public static DustParticleOptions getOrCreateParticle(Vector3f color, float size) {
        long key = colorKey(color, size);
        return PARTICLE_POOL.computeIfAbsent(key, k -> new DustParticleOptions(new Vector3f(color), size));
    }

    // ============ 激光粒子提交入口（LOD 筛选 + 入队） ============

    /**
     * 批量提交激光粒子
     *
     * @param level       服务端世界
     * @param candidates  粒子候选列表（按距离从近到远排序即可，提交后由队列保证优先级）
     * @param progressive true=从近到远逐帧延伸，有"激光射出"动画；false=密集按序分发
     */
    public static void submitLaserParticles(ServerLevel level,
                                            List<LaserParticleCandidate> candidates,
                                            boolean progressive) {
        long seq = SUBMIT_SEQ.getAndIncrement();
        // 全局批次基础值：seq << 20 保证不同批次之间按提交时间出队，同批次内用 distance 微调
        double seqBase = (double) (seq << 20);

        for (LaserParticleCandidate cand : candidates) {
            // LOD 密度分级步长
            int step;
            if (cand.distance <= LOD_NEAR) {
                step = 1;       // 0-10格：全密度
            } else if (cand.distance <= LOD_MID) {
                step = 2;       // 10-30格：半密度
            } else {
                step = 3;       // 30+格：1/3密度
            }
            if (cand.index % step != 0) continue;

            // order = 批次基础值 + (progressive ? distance : 乱序微调)
            double order;
            if (progressive) {
                order = seqBase + cand.distance;
            } else {
                order = seqBase + cand.index * 0.0001 + ThreadLocalRandom.current().nextDouble(0.00001);
            }

            PENDING_QUEUE.add(new PendingParticle(level, cand.color, cand.size,
                    cand.x, cand.y, cand.z, order));
        }
    }

    // ============ 单粒子直接提交（快捷方法） ============

    /**
     * 直接提交单个粒子（不走 LOD，直接入队）
     * 用于数量少、立即要看到的粒子
     */
    public static void submitParticle(ServerLevel level, Vector3f color, float size,
                                      double x, double y, double z) {
        double order = (double) (SUBMIT_SEQ.getAndIncrement() << 20);
        PENDING_QUEUE.add(new PendingParticle(level, color, size, x, y, z, order));
    }

    // ============ 服务器 tick 消费队列 ============

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (PENDING_QUEUE.isEmpty()) return;

        int sent = 0;
        while (sent < MAX_PACKETS_PER_TICK) {
            PendingParticle p = PENDING_QUEUE.poll();
            if (p == null) break;
            p.dispatch();
            sent++;
        }
    }

    // ============ 待发送粒子包（带优先级比较） ============

    private static class PendingParticle {
        final ServerLevel level;
        final Vector3f color;
        final float size;
        final double x, y, z;
        final double order;

        PendingParticle(ServerLevel level, Vector3f color, float size,
                        double x, double y, double z, double order) {
            this.level = level;
            this.color = color;
            this.size = size;
            this.x = x;
            this.y = y;
            this.z = z;
            this.order = order;
        }

        void dispatch() {
            DustParticleOptions opts = getOrCreateParticle(color, size);
            ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(
                    opts, true, x, y, z, 0F, 0F, 0F, 0F, 1);
            for (ServerPlayer sp : level.players()) {
                if (sp.level() == level) {
                    sp.connection.send(packet);
                }
            }
        }
    }

    // ============ 激光粒子候选 POJO ============

    public static class LaserParticleCandidate {
        public final int index;
        public final double distance;
        public final Vector3f color;
        public final float size;
        public final double x, y, z;

        public LaserParticleCandidate(int index, double distance, Vector3f color, float size,
                                      double x, double y, double z) {
            this.index = index;
            this.distance = distance;
            this.color = color;
            this.size = size;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    // ============ 调试 / 生命周期方法 ============

    public static void clear() {
        PENDING_QUEUE.clear();
    }

    public static int pendingSize() {
        return PENDING_QUEUE.size();
    }

    public static int poolSize() {
        return PARTICLE_POOL.size();
    }
}
