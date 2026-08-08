package com.qituo.dcc.items;

import com.qituo.dcc.damage.DamagePresets;
import com.qituo.dcc.damage.EntityBypassHelper;
import com.qituo.dcc.damage.ModDamageSources;
import com.qituo.dcc.enchantments.ModEnchantments;
import com.qituo.dcc.particles.SmartParticleDispatcher;
import com.qituo.dcc.sounds.ModSounds;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3f;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 老爹的河豚干
 * 行为：右键点击立即发射绿色激光（50格距离，双环绕螺旋渐变粒子，始源终结伤害），
 *      发射后进入5秒冷却（100 tick），不蓄力。
 */
public class UnclesDriedPufferFish extends Item {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnclesDriedPufferFish.class);
    private static final int COOLDOWN_TICKS = 100;   // 5秒冷却
    private static final double LASER_MAX_DISTANCE = 50.0;
    private static final double HIT_RADIUS = 2.0;     // 激光命中半径（格）

    public UnclesDriedPufferFish(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        // 过渡方案：通过 NBT 直接写入始源之力 10 级附魔
        CompoundTag tag = new CompoundTag();
        ListTag enchantments = new ListTag();
        CompoundTag ench = new CompoundTag();
        ench.putString("id", ModEnchantments.ORIGIN_POWER_KEY.toString());
        ench.putInt("lvl", 10);
        enchantments.add(ench);
        tag.put("Enchantments", enchantments);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    // ================================================
    // 核心入口：右键立即触发（不蓄力，无 startUsingItem）
    // ================================================
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // ---------- 1) 冷却判断 + 加冷却（放在最前面，确保生效） ----------
        if (player.getCooldowns().isOnCooldown(this)) {
            LOGGER.debug("[PufferFish] on cooldown, skip");
            return InteractionResultHolder.fail(stack);
        }
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        Vec3 lookVec = player.getLookAngle();
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(lookVec.scale(LASER_MAX_DISTANCE));

        LOGGER.info("[PufferFish] fire! player={} isClient={}", player.getName().getString(), level.isClientSide);

        // ---------- 2) 施法音效（客户端+服务器双端分开播） ----------
        final float vol = 2.0F;
        final float pitch = 1.0F;
        if (level.isClientSide) {
            player.playSound(ModSounds.MADGAQ.get(), vol, pitch);
        } else {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.MADGAQ.get(), SoundSource.PLAYERS, vol, pitch);
        }

        // ---------- 3) 仅服务端：伤害 + 粒子入队 + 冲击音效 ----------
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            // 3.1 激光伤害：AABB 初筛 + 点到线段二次过滤
            AABB laserBox = new AABB(start, end).inflate(5.0);
            List<Entity> entities = level.getEntities(player, laserBox);
            LOGGER.info("[PufferFish] candidate entities: {}", entities.size());
            float damage = DamagePresets.getDamage(10);

            for (Entity target : entities) {
                if (target == player) continue;
                Vec3 targetCenter = target.position().add(0, target.getBbHeight() * 0.5, 0);
                if (!isPointNearSegment(start, end, targetCenter, HIT_RADIUS)) continue;

                if (target instanceof LivingEntity living) {
                    var dmgSrc = ModDamageSources.causeOriginEndDamage(player, 10);

                    // Draconic Guardian 专用处理（反射调用，避免直接引用类加载）
                    if (target.getClass().getName()
                            .equals("com.brandon3055.draconicevolution.entity.guardian.DraconicGuardianEntity")) {
                        try {
                            java.lang.reflect.Method setShield = target.getClass()
                                    .getMethod("setShieldPower", float.class);
                            setShield.invoke(target, 0.0F);
                            java.lang.reflect.Method getParts = target.getClass()
                                    .getMethod("getDragonParts");
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
                                EntityBypassHelper.killEntity(living, dmgSrc, damage);
                            }
                        } catch (Exception e) {
                            LOGGER.error("[PufferFish] DraconicGuardian special handle err: {}", e.getMessage());
                            EntityBypassHelper.killEntity(living, dmgSrc, damage);
                        }
                    } else {
                        // 普通实体：走通用"五重击杀链"，确保死亡且掉落
                        EntityBypassHelper.killEntity(living, dmgSrc, damage);
                    }
                    LOGGER.info("[PufferFish] damaged: {}", living.getName().getString());
                }
            }

            // 3.2 粒子：SmartParticleDispatcher 入队（渐进式射出 + LOD + 对象池）
            LOGGER.info("[PufferFish] particle submit begin: pendingBefore={}", SmartParticleDispatcher.pendingSize());
            Vec3 up = Math.abs(lookVec.dot(new Vec3(0, 1, 0))) > 0.99 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
            Vec3 normal = lookVec.cross(up).normalize();
            Vec3 binormal = lookVec.cross(normal).normalize();

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

            java.util.ArrayList<SmartParticleDispatcher.LaserParticleCandidate> list =
                    new java.util.ArrayList<>(segments * 3);
            int idx = 0;
            for (int i = 0; i < segments; i++) {
                double dist = i * stepLen;
                Vec3 center = start.add(lookVec.scale(dist));
                float ratio = (float) i / (segments - 1);

                // 主激光（直线渐变）
                Vector3f mc = new Vector3f(mainS).lerp(mainE, ratio);
                list.add(new SmartParticleDispatcher.LaserParticleCandidate(
                        idx++, dist, mc, pSize, center.x, center.y, center.z));

                // 环绕A：顺时针
                double aA = dist * spiralFreq;
                Vec3 oA = normal.scale(Math.cos(aA) * spiralR)
                        .add(binormal.scale(Math.sin(aA) * spiralR));
                Vector3f cA = new Vector3f(spAS).lerp(spAE, ratio);
                list.add(new SmartParticleDispatcher.LaserParticleCandidate(
                        idx++, dist, cA, pSize,
                        center.x + oA.x, center.y + oA.y, center.z + oA.z));

                // 环绕B：逆时针（相差 π）
                double aB = dist * spiralFreq + Math.PI;
                Vec3 oB = normal.scale(Math.cos(aB) * spiralR)
                        .add(binormal.scale(Math.sin(aB) * spiralR));
                Vector3f cB = new Vector3f(spBS).lerp(spBE, ratio);
                list.add(new SmartParticleDispatcher.LaserParticleCandidate(
                        idx++, dist, cB, pSize,
                        center.x + oB.x, center.y + oB.y, center.z + oB.z));
            }
            SmartParticleDispatcher.submitLaserParticles(serverLevel, list, true);
            LOGGER.info("[PufferFish] particle submit end: submitted={}, pendingNow={}",
                    list.size(), SmartParticleDispatcher.pendingSize());

            // 3.3 冲击音效（只在服务端发，自动同步所有客户端）
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.0F, 1.0F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        return InteractionResultHolder.consume(stack);
    }

    // ================================================
    // 蓄力系统彻底停用：不使用 startUsingItem，以下方法仅留空实现兼容
    // ================================================
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 0;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        // no-op
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        return stack;
    }

    // ================================================
    // 辅助：判断点到线段最近距离 ≤ radius
    // ================================================
    private static boolean isPointNearSegment(Vec3 segStart, Vec3 segEnd, Vec3 point, double radius) {
        Vec3 lineVec = segEnd.subtract(segStart);
        Vec3 pointVec = point.subtract(segStart);
        double lenSq = lineVec.lengthSqr();
        if (lenSq == 0) return point.distanceToSqr(segStart) <= radius * radius;
        double t = Math.max(0, Math.min(1, pointVec.dot(lineVec) / lenSq));
        Vec3 closest = segStart.add(lineVec.scale(t));
        return point.distanceToSqr(closest) <= radius * radius;
    }
}
