package com.qituo.dcc.items;

import com.qituo.dcc.damage.DamagePresets;
import com.qituo.dcc.damage.EntityBypassHelper;
import com.qituo.dcc.damage.ModDamageSources;
import com.qituo.dcc.enchantments.ModEnchantments;
import com.qituo.dcc.particles.SmartParticleDispatcher;
import com.qituo.dcc.sounds.ModSounds;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 老爹的河豚干（Forge 1.20.1）
 *
 * 行为规格（与 Fabric / NeoForge 对齐）：
 * - 右键立即发射绿色激光，无需蓄力；发射后 5 秒冷却
 * - 命中判定：光束轴线 15 格半径内的全部实体，不做遮挡判定
 * - 伤害走自定义 origin_end 伤害类型 + EntityBypassHelper 五重击杀链
 * - 粒子走 SmartParticleDispatcher：1200 候选 → LOD 衰减 → 近到远渐进射出
 */
public class UnclesDriedPufferFish extends Item {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnclesDriedPufferFish.class);

    /** 冷却时间（5 秒） */
    private static final int COOLDOWN_TICKS = 100;
    /** 激光最大长度（格） */
    private static final double LASER_MAX_DISTANCE = 50.0;
    /** 命中半径（格）：光束周围该半径内全部命中，无遮挡判定 */
    private static final double HIT_RADIUS = 15.0;

    public UnclesDriedPufferFish(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        // 仅对可附魔物品写入附魔，否则 EnchantmentHelper.setEnchantments 会直接抛异常
        if (stack.isEnchantable()) {
            EnchantmentHelper.setEnchantments(
                    java.util.Map.of(ModEnchantments.ORIGIN_POWER.get(), 10),
                    stack);
        } else {
            LOGGER.warn("老爹的河豚干不可附魔，跳过默认始源之力附魔写入");
        }
        return stack;
    }

    // ================================================
    // 核心入口：右键立即触发（不蓄力）
    // ================================================
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 1) 冷却判断 + 立即加冷却
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        // 2) 施法音效：客户端本机播，服务端广播给其他人
        final float volume = 2.0F;
        final float pitch = 1.0F;
        if (level.isClientSide) {
            player.playSound(ModSounds.MADGAQ.get(), volume, pitch);
        } else {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.MADGAQ.get(), SoundSource.PLAYERS, volume, pitch);
        }

        Vec3 lookVec = player.getLookAngle();
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(lookVec.scale(LASER_MAX_DISTANCE));

        // 3) 仅服务端：伤害结算 + 粒子入队 + 冲击音效
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            applyLaserDamage(serverLevel, player, start, end);
            submitLaserParticles(serverLevel, start, lookVec);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.0F, 1.0F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        return InteractionResultHolder.consume(stack);
    }

    /**
     * 激光伤害：光束轴线 HIT_RADIUS 半径内的全部实体命中（无遮挡判定）
     */
    private void applyLaserDamage(ServerLevel level, Player player, Vec3 start, Vec3 end) {
        AABB searchBox = new AABB(start, end).inflate(HIT_RADIUS);
        List<Entity> entities = level.getEntities(player, searchBox);
        float damage = DamagePresets.getDamage(10);

        for (Entity target : entities) {
            if (target == player) continue;
            Vec3 targetCenter = target.position().add(0, target.getBbHeight() * 0.5, 0);
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
                        for (int i = 0; i < 5 && !living.isDeadOrDying(); i++) {
                            java.lang.reflect.Method attack = target.getClass()
                                    .getMethod("attackEntityPartFrom",
                                            Class.forName("com.brandon3055.draconicevolution.entity.guardian.DraconicGuardianPartEntity"),
                                            Class.forName("net.minecraft.world.damagesource.DamageSource"),
                                            float.class);
                            attack.invoke(target, head,
                                    player.damageSources().playerAttack(player), 1000.0F);
                        }
                    }
                    if (!living.isDeadOrDying()) {
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
    private void submitLaserParticles(ServerLevel level, Vec3 start, Vec3 lookVec) {
        Vec3 up = Math.abs(lookVec.dot(new Vec3(0, 1, 0))) > 0.99 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 normal = lookVec.cross(up).normalize();
        Vec3 binormal = lookVec.cross(normal).normalize();

        final double spiralRadius = 0.35;
        final double spiralFrequency = 0.8;
        final int segments = 400;
        final float particleSize = 1.5F;
        final double stepLen = LASER_MAX_DISTANCE / segments;

        Vector3f mainStart = new Vector3f(0.2F, 0.8F, 0.2F);
        Vector3f mainEnd = new Vector3f(0.3F, 1.0F, 0.4F);
        Vector3f spiralAStart = new Vector3f(0.2F, 1.0F, 0.3F);
        Vector3f spiralAEnd = new Vector3f(0.0F, 1.0F, 1.0F);
        Vector3f spiralBStart = new Vector3f(0.8F, 0.2F, 1.0F);
        Vector3f spiralBEnd = new Vector3f(1.0F, 0.4F, 0.8F);

        ArrayList<SmartParticleDispatcher.LaserParticleCandidate> candidates =
                new ArrayList<>(segments * 3);
        int candidateIndex = 0;

        for (int i = 0; i < segments; i++) {
            double distance = i * stepLen;
            Vec3 centerPos = start.add(lookVec.scale(distance));
            float ratio = (float) i / (segments - 1);

            // 主激光：直线，分段渐变
            Vector3f mainColor = new Vector3f(mainStart).lerp(mainEnd, ratio);
            candidates.add(new SmartParticleDispatcher.LaserParticleCandidate(
                    candidateIndex++, distance, mainColor, particleSize,
                    centerPos.x, centerPos.y, centerPos.z));

            // 环绕束A：顺时针螺旋
            double angleA = distance * spiralFrequency;
            Vec3 offsetA = normal.scale(Math.cos(angleA) * spiralRadius)
                    .add(binormal.scale(Math.sin(angleA) * spiralRadius));
            Vector3f colorA = new Vector3f(spiralAStart).lerp(spiralAEnd, ratio);
            candidates.add(new SmartParticleDispatcher.LaserParticleCandidate(
                    candidateIndex++, distance, colorA, particleSize,
                    centerPos.x + offsetA.x, centerPos.y + offsetA.y, centerPos.z + offsetA.z));

            // 环绕束B：逆时针螺旋（相位差180°）
            double angleB = distance * spiralFrequency + Math.PI;
            Vec3 offsetB = normal.scale(Math.cos(angleB) * spiralRadius)
                    .add(binormal.scale(Math.sin(angleB) * spiralRadius));
            Vector3f colorB = new Vector3f(spiralBStart).lerp(spiralBEnd, ratio);
            candidates.add(new SmartParticleDispatcher.LaserParticleCandidate(
                    candidateIndex++, distance, colorB, particleSize,
                    centerPos.x + offsetB.x, centerPos.y + offsetB.y, centerPos.z + offsetB.z));
        }

        // progressive=true：近→远逐帧延伸，形成"激光射出"动画
        SmartParticleDispatcher.submitLaserParticles(level, candidates, true);
    }

    // ================================================
    // 蓄力系统彻底停用：以下方法仅留空实现兼容
    // ================================================
    @Override
    public int getUseDuration(ItemStack stack) {
        return 0;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        // no-op：发射已改由 use() 完成
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        return stack;
    }

    // ================================================
    // 辅助：点到线段最近距离 ≤ radius
    // ================================================
    private static boolean isPointNearSegment(Vec3 segStart, Vec3 segEnd, Vec3 point, double radius) {
        Vec3 lineVec = segEnd.subtract(segStart);
        Vec3 pointVec = point.subtract(segStart);
        double lineLengthSqr = lineVec.lengthSqr();
        if (lineLengthSqr == 0) {
            return point.distanceToSqr(segStart) <= radius * radius;
        }
        double t = Math.max(0, Math.min(1, pointVec.dot(lineVec) / lineLengthSqr));
        Vec3 closestPoint = segStart.add(lineVec.scale(t));
        return point.distanceToSqr(closestPoint) <= radius * radius;
    }
}
