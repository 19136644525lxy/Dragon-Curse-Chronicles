package com.qituo.dcc.items;

import com.qituo.dcc.damage.DamagePresets;
import com.qituo.dcc.damage.EntityBypassHelper;
import com.qituo.dcc.damage.ModDamageSources;
import com.qituo.dcc.enchantments.ModEnchantments;
import com.qituo.dcc.particles.SmartParticleDispatcher;
import com.qituo.dcc.sounds.ModSounds;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 老爹的河豚干（Fabric 1.20.1）
 *
 * 行为规格（与 Forge / NeoForge 对齐）：
 * - 右键立即发射绿色激光，无需蓄力；发射后 5 秒冷却
 * - 命中判定：光束轴线 15 格半径内的全部实体，不做遮挡判定
 * - 伤害走自定义 origin_end 伤害类型 + EntityBypassHelper 五重击杀链
 * - 粒子走 SmartParticleDispatcher：1200 候选 → LOD 衰减 → 近到远渐进射出，
 *   并通过服务端向 512 格内玩家发包，保证旁观者可见
 */
public class UnclesDriedPufferFish extends Item {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnclesDriedPufferFish.class);

    /** 冷却时间（5 秒） */
    private static final int COOLDOWN_TICKS = 100;
    /** 激光最大长度（格） */
    private static final double LASER_MAX_DISTANCE = 50.0;
    /** 命中半径（格）：光束周围该半径内全部命中，无遮挡判定 */
    private static final double HIT_RADIUS = 15.0;

    public UnclesDriedPufferFish(Item.Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack stack = super.getDefaultStack();
        // 仅对可附魔物品写入附魔，否则 EnchantmentHelper.set 会直接抛异常
        if (stack.isEnchantable()) {
            java.util.Map<Enchantment, Integer> enchantments =
                    java.util.Map.of(ModEnchantments.ORIGIN_POWER, 10);
            EnchantmentHelper.set(enchantments, stack);
        } else {
            LOGGER.warn("老爹的河豚干不可附魔，跳过默认始源之力附魔写入");
        }
        return stack;
    }

    // ================================================
    // 核心入口：右键立即触发（不蓄力）
    // ================================================
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        // 1) 冷却判断 + 立即加冷却
        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }
        user.getItemCooldownManager().set(this, COOLDOWN_TICKS);

        // 2) 施法音效：客户端本机播，服务端广播给其他人
        final float volume = 2.0F;
        final float pitch = 1.0F;
        if (world.isClient) {
            user.playSound(ModSounds.MADGAQ, volume, pitch);
        } else {
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    ModSounds.MADGAQ, SoundCategory.PLAYERS, volume, pitch);
        }

        Vec3d lookVec = user.getRotationVec(1.0F);
        Vec3d start = user.getEyePos();
        Vec3d end = start.add(lookVec.multiply(LASER_MAX_DISTANCE));

        // 3) 仅服务端：伤害结算 + 粒子入队 + 冲击音效
        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            applyLaserDamage(serverWorld, user, start, end);
            submitLaserParticles(serverWorld, start, lookVec);

            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.PLAYERS, 1.0F, 1.0F);
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }

        return TypedActionResult.consume(stack);
    }

    /**
     * 激光伤害：光束轴线 HIT_RADIUS 半径内的全部实体命中（无遮挡判定）
     */
    private void applyLaserDamage(ServerWorld world, PlayerEntity player, Vec3d start, Vec3d end) {
        Box searchBox = new Box(start, end).expand(HIT_RADIUS);
        List<Entity> entities = world.getOtherEntities(player, searchBox);
        float damage = DamagePresets.getDamage(10);

        for (Entity target : entities) {
            Vec3d targetCenter = target.getPos().add(0, target.getHeight() * 0.5, 0);
            if (!isPointNearSegment(start, end, targetCenter, HIT_RADIUS)) continue;
            if (!(target instanceof LivingEntity living)) continue;

            var damageSource = ModDamageSources.causeOriginEndDamage(player, 10);

            // Draconic Guardian 专用处理（反射调用，避免直接引用类加载）
            if (target.getClass().getName()
                    .equals("com.brandon3055.draconicevolution.entity.guardian.DraconicGuardianEntity")) {
                try {
                    java.lang.reflect.Method setShield = target.getClass()
                            .getMethod("setShieldPower", float.class);
                    setShield.invoke(target, 0.0F);
                    java.lang.reflect.Method getParts = target.getClass().getMethod("getDragonParts");
                    Object[] parts = (Object[]) getParts.invoke(target);
                    if (parts != null && parts.length > 0) {
                        Object head = parts[0];
                        for (int i = 0; i < 5 && !living.isDead(); i++) {
                            java.lang.reflect.Method attack = target.getClass()
                                    .getMethod("attackEntityPartFrom",
                                            Class.forName("com.brandon3055.draconicevolution.entity.guardian.DraconicGuardianPartEntity"),
                                            Class.forName("net.minecraft.entity.damage.DamageSource"),
                                            float.class);
                            attack.invoke(target, head,
                                    player.getDamageSources().playerAttack(player), 1000.0F);
                        }
                    }
                    if (!living.isDead()) {
                        EntityBypassHelper.killEntity(living, damageSource, damage);
                    }
                } catch (Exception e) {
                    LOGGER.error("[PufferFish] DraconicGuardian special handle err: {}", e.getMessage());
                    EntityBypassHelper.killEntity(living, damageSource, damage);
                }
            } else {
                // 普通实体：走通用五重击杀链，确保死亡且掉落
                EntityBypassHelper.killEntity(living, damageSource, damage);
            }
        }
    }

    /**
     * 激光粒子：1200 候选（主束 + 双反向螺旋）交给调度器做 LOD 与渐进射出
     */
    private void submitLaserParticles(ServerWorld world, Vec3d start, Vec3d lookVec) {
        Vec3d up = Math.abs(lookVec.dotProduct(new Vec3d(0, 1, 0))) > 0.99
                ? new Vec3d(1, 0, 0) : new Vec3d(0, 1, 0);
        Vec3d normal = lookVec.crossProduct(up).normalize();
        Vec3d binormal = lookVec.crossProduct(normal).normalize();

        final double spiralR = 0.35;
        final double spiralFreq = 0.8;
        final int segments = 400;
        final float pSize = 1.5F;
        final double stepLen = LASER_MAX_DISTANCE / segments;

        Vector3f mainS = new Vector3f(0.2F, 0.8F, 0.2F);
        Vector3f mainE = new Vector3f(0.3F, 1.0F, 0.4F);
        Vector3f spAS = new Vector3f(0.2F, 1.0F, 0.3F);
        Vector3f spAE = new Vector3f(0.0F, 1.0F, 1.0F);
        Vector3f spBS = new Vector3f(0.8F, 0.2F, 1.0F);
        Vector3f spBE = new Vector3f(1.0F, 0.4F, 0.8F);

        List<SmartParticleDispatcher.LaserParticleCandidate> list = new ArrayList<>(segments * 3);
        int idx = 0;
        for (int i = 0; i < segments; i++) {
            double dist = i * stepLen;
            Vec3d center = start.add(lookVec.multiply(dist));
            float ratio = (float) i / (segments - 1);

            // 主激光（直线渐变）
            Vector3f mc = new Vector3f(mainS).lerp(mainE, ratio);
            list.add(new SmartParticleDispatcher.LaserParticleCandidate(
                    idx++, dist, mc, pSize, center.x, center.y, center.z));

            // 环绕A：顺时针
            double aA = dist * spiralFreq;
            Vec3d oA = normal.multiply(Math.cos(aA) * spiralR)
                    .add(binormal.multiply(Math.sin(aA) * spiralR));
            Vector3f cA = new Vector3f(spAS).lerp(spAE, ratio);
            list.add(new SmartParticleDispatcher.LaserParticleCandidate(
                    idx++, dist, cA, pSize,
                    center.x + oA.x, center.y + oA.y, center.z + oA.z));

            // 环绕B：逆时针（相差 π）
            double aB = dist * spiralFreq + Math.PI;
            Vec3d oB = normal.multiply(Math.cos(aB) * spiralR)
                    .add(binormal.multiply(Math.sin(aB) * spiralR));
            Vector3f cB = new Vector3f(spBS).lerp(spBE, ratio);
            list.add(new SmartParticleDispatcher.LaserParticleCandidate(
                    idx++, dist, cB, pSize,
                    center.x + oB.x, center.y + oB.y, center.z + oB.z));
        }
        SmartParticleDispatcher.submitLaserParticles(world, list, true);
    }

    // ================================================
    // 蓄力系统彻底停用：以下方法仅留空实现兼容
    // ================================================
    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 0;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.NONE;
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseDuration) {
        // no-op：发射已改由 use() 完成
    }

    // 注意：1.20.1 的 Item 没有 finishUsingItem 方法（那是 Forge 端 1.20.1 的扩展），
    // 因此这里不能覆写它；发射逻辑全部在 use()，不会进入"用完"分支。

    // ================================================
    // 辅助：点到线段最近距离 ≤ radius
    // ================================================
    private static boolean isPointNearSegment(Vec3d segStart, Vec3d segEnd, Vec3d point, double radius) {
        Vec3d lineVec = segEnd.subtract(segStart);
        Vec3d pointVec = point.subtract(segStart);
        double lenSq = lineVec.lengthSquared();
        if (lenSq == 0) {
            return point.squaredDistanceTo(segStart) <= radius * radius;
        }
        double t = Math.max(0, Math.min(1, pointVec.dotProduct(lineVec) / lenSq));
        Vec3d closest = segStart.add(lineVec.multiply(t));
        return point.squaredDistanceTo(closest) <= radius * radius;
    }
}
