package com.qituo.dcc.particles;

import com.qituo.dcc.DragonCurseChronicles;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

/**
 * SmartParticleDispatcher —— 自研智能粒子调度器
 *
 *  与市面上的粒子优化核心差异：
 *  1) 不做激进剔除 → 100% 保证渲染（服务端直接发包，绕过客户端粒子模组 addParticle 拦截）
 *  2) 智能分帧 → 每 tick 最多 120 个网络包，防单帧爆卡
 *  3) LOD 距离衰减 → 0-10格全密度 / 10-30格半密度 / 30+格 1/3密度
 *  4) 渐进式生成 → 近→远逐帧延伸，形成"激光射出"动画
 *  5) 颜色对象池 → 复用 DustParticleOptions，减少 GC
 *  6) 线程安全 → PriorityBlockingQueue + ConcurrentHashMap
 *  7) 双保险订阅：不依赖 bus=GAME 属性（NeoForge 21.x [removal]），
 *     直接在静态初始化块中手动注册到 NeoForge.EVENT_BUS，确保 ServerTickEvent 一定触发
 */
@EventBusSubscriber(modid = DragonCurseChronicles.MODID)
public class SmartParticleDispatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(SmartParticleDispatcher.class);

    private static final int MAX_PACKETS_PER_TICK = 120;
    private static final double LOD_NEAR = 10.0;
    private static final double LOD_MID  = 30.0;

    // 粒子包对象池（颜色键 → DustParticleOptions）
    private static final ConcurrentHashMap<Long, DustParticleOptions> PARTICLE_POOL = new ConcurrentHashMap<>();
    // 待发送队列（order 越小优先级越高：按批次+距离综合排序，实现渐进式延伸）
    private static final PriorityBlockingQueue<PendingParticle> PENDING_QUEUE =
            new PriorityBlockingQueue<>(512, (a, b) -> Double.compare(a.order, b.order));
    private static final java.util.concurrent.atomic.AtomicLong SUBMIT_SEQ = new java.util.concurrent.atomic.AtomicLong(0);

    // ========================== 双保险：类加载时再次手动注册 ==========================
    // 防止 NeoForge 21.x 对 @EventBusSubscriber(Bus.GAME) 的兼容性问题
    static {
        try {
            NeoForge.EVENT_BUS.register(SmartParticleDispatcher.class);
            LOGGER.info("[SPD] manual NeoForge.EVENT_BUS.register OK");
        } catch (Throwable t) {
            LOGGER.warn("[SPD] manual register failed (maybe double register), ignore: {}", t.getMessage());
        }
    }

    // ========================== 颜色对象池 ==========================
    private static long colorKey(Vector3f color, float size) {
        int r = (int)(color.x * 65535F) & 0xFFFF;
        int g = (int)(color.y * 65535F) & 0xFFFF;
        int b = (int)(color.z * 65535F) & 0xFFFF;
        int s = (int)(size    * 1000F)  & 0xFFFF;
        return ((long)r << 48) | ((long)g << 32) | ((long)b << 16) | s;
    }

    public static DustParticleOptions getOrCreateParticle(Vector3f color, float size) {
        return PARTICLE_POOL.computeIfAbsent(colorKey(color, size),
                k -> new DustParticleOptions(new Vector3f(color), size));
    }

    // ========================== 激光粒子批量入队 ==========================
    public static void submitLaserParticles(ServerLevel level,
                                            List<LaserParticleCandidate> candidates,
                                            boolean progressive) {
        long seq = SUBMIT_SEQ.getAndIncrement();
        double seqBase = (double)(seq << 20);

        int submitted = 0;
        for (LaserParticleCandidate cand : candidates) {
            // LOD 分级步长：近的密，远的疏
            int step;
            if      (cand.distance <= LOD_NEAR) step = 1;
            else if (cand.distance <= LOD_MID)  step = 2;
            else                                 step = 3;
            if (cand.index % step != 0) continue;

            double order = progressive
                    ? seqBase + cand.distance
                    : seqBase + cand.index * 0.0001 + ThreadLocalRandom.current().nextDouble(0.00001);
            PENDING_QUEUE.add(new PendingParticle(level, cand.color, cand.size,
                    cand.x, cand.y, cand.z, order));
            submitted++;
        }
        LOGGER.debug("[SPD] submitLaserParticles: candidates={} submitted={} pendingTotal={}",
                candidates.size(), submitted, PENDING_QUEUE.size());
    }

    // ========================== 单粒子直接入队 ==========================
    public static void submitParticle(ServerLevel level, Vector3f color, float size,
                                      double x, double y, double z) {
        double order = (double)(SUBMIT_SEQ.getAndIncrement() << 20);
        PENDING_QUEUE.add(new PendingParticle(level, color, size, x, y, z, order));
    }

    // ========================== 服务器 tick 消费队列 ==========================
    @SubscribeEvent
    public static void onServerTickPost(ServerTickEvent.Post event) {
        if (PENDING_QUEUE.isEmpty()) return;

        int sent = 0;
        while (sent < MAX_PACKETS_PER_TICK) {
            PendingParticle p = PENDING_QUEUE.poll();
            if (p == null) break;
            try {
                p.dispatch();
            } catch (Throwable t) {
                LOGGER.error("[SPD] dispatch one particle fail: {}", t.getMessage());
            }
            sent++;
        }
    }

    // ========================== 单个粒子包 ==========================
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
            this.size  = size;
            this.x = x; this.y = y; this.z = z;
            this.order = order;
        }

        /**
         * 【三保险发包】
         *  第一优先：调用 ServerLevel.sendParticles 公开 API（原版实现，内部自动遍历玩家 + 考虑距离）
         *  失败降级：直接构造 ClientboundLevelParticlesPacket，遍历所有 ServerPlayer 手动发送
         *     （手动发送时用 longDistance=true 允许 512 格内玩家都能看到，避开原版 32 格限制）
         */
        void dispatch() {
            DustParticleOptions opts = getOrCreateParticle(color, size);
            try {
                // 方式1：ServerLevel 原生 API（int sendParticles(T, x,y,z, count, xOff,yOff,zOff, speed)）
                //   1.21 原版签名：xOffset/yOffset/zOffset/speed 都是 double
                level.sendParticles(opts, x, y, z,
                        /*particleCount*/1,
                        /*xOffset*/0.0, /*yOffset*/0.0, /*zOffset*/0.0,
                        /*speed*/0.0);
            } catch (Throwable t1) {
                LOGGER.warn("[SPD] sendParticles API fail, downgrade direct packet: {}", t1.getMessage());
                // 方式2：直接手动发包（longDistance=true → 512 格内全部玩家）
                try {
                    ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(
                            opts, true, x, y, z, 0F, 0F, 0F, 0F, 1);
                    for (ServerPlayer sp : level.players()) {
                        if (sp.level() == level) {
                            sp.connection.send(packet);
                        }
                    }
                } catch (Throwable t2) {
                    LOGGER.error("[SPD] direct packet also fail: {}", t2.getMessage());
                }
            }
        }
    }

    // ========================== POJO ==========================
    public static class LaserParticleCandidate {
        public final int index;
        public final double distance;
        public final Vector3f color;
        public final float size;
        public final double x, y, z;
        public LaserParticleCandidate(int index, double distance, Vector3f color, float size,
                                      double x, double y, double z) {
            this.index = index; this.distance = distance;
            this.color = color; this.size = size;
            this.x = x; this.y = y; this.z = z;
        }
    }

    // ========================== 调试 ==========================
    public static void clear()               { PENDING_QUEUE.clear(); }
    public static int  pendingSize()         { return PENDING_QUEUE.size(); }
}
