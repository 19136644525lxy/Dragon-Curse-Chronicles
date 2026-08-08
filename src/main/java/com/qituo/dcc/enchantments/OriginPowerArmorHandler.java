package com.qituo.dcc.enchantments;

import com.qituo.dcc.damage.EntityBypassHelper;
import com.qituo.dcc.damage.ModDamageSources;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 始源之力附魔 - 盔甲效果处理器
 * 负责所有盔甲相关的被动效果：反弹、减伤、护盾、再生、免疫击退、光环
 *
 * 效果阶梯（按总等级）：
 * 1-5:   反弹+减伤（基础）
 * 6-10:  解锁始源护盾
 * 11-20: 解锁始源再生
 * 21-30: 解锁免疫击退
 * 31-40: 解锁始源光环
 */
@Mod.EventBusSubscriber
public class OriginPowerArmorHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(OriginPowerArmorHandler.class);

    private static final String TAG_ANTI_LOOP = "temp$OriginPowerEnchantTag";

    // 玩家tick计数器（用于周期性效果的间隔控制）
    private static final Map<UUID, Integer> tickCounters = new HashMap<>();

    /**
     * 计算玩家护甲上的始源之力总等级
     */
    public static int getTotalArmorLevel(Player player) {
        int total = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                total += player.getItemBySlot(slot).getEnchantmentLevel(ModEnchantments.ORIGIN_POWER.get());
            }
        }
        return total;
    }

    // ==================== 受伤事件：反弹 + 减伤 ====================

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) {
            return;
        }

        int totalLevel = getTotalArmorLevel(player);
        if (totalLevel <= 0) {
            return;
        }

        // 减伤
        float damageReduction = getDamageReduction(totalLevel);
        if (damageReduction > 0) {
            event.setAmount(event.getAmount() * (1.0F - damageReduction));
        }

        // 反弹
        applyDamageReflection(player, event, totalLevel);
    }

    /**
     * 伤害反弹概率 = 总等级/100 * 1.5（非线性加成）
     * 满级40 → 60%
     */
    private static float getReflectionChance(int totalLevel) {
        return Math.min(0.6F, totalLevel / 100.0F * 1.5F);
    }

    /**
     * 减伤比例 = 总等级/80，封顶50%
     * 满级40 → 50%
     */
    private static float getDamageReduction(int totalLevel) {
        return Math.min(0.5F, totalLevel / 80.0F);
    }

    private static void applyDamageReflection(Player player, LivingHurtEvent event, int totalLevel) {
        if (player.getTags().contains(TAG_ANTI_LOOP)) {
            return;
        }

        float chance = getReflectionChance(totalLevel);
        if (player.getRandom().nextFloat() >= chance) {
            return;
        }

        Entity source = event.getSource().getEntity();
        if (source == null || source == player) {
            return;
        }

        player.addTag(TAG_ANTI_LOOP);
        try {
            float damage = event.getAmount();
            var damageSource = ModDamageSources.causeOriginEndDamage(player);

            if (source instanceof LivingEntity livingSource) {
                EntityBypassHelper.killEntity(livingSource, damageSource, damage);
            } else {
                source.hurt(damageSource, damage);
            }
        } catch (Throwable e) {
            LOGGER.error("Damage reflection failed: {}", e.getMessage());
        } finally {
            player.removeTag(TAG_ANTI_LOOP);
        }
    }

    // ==================== 周期事件：护盾 + 再生 + 光环 ====================

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) {
            return;
        }

        int totalLevel = getTotalArmorLevel(player);
        if (totalLevel <= 0) {
            tickCounters.remove(player.getUUID());
            return;
        }

        int tick = tickCounters.getOrDefault(player.getUUID(), 0) + 1;
        tickCounters.put(player.getUUID(), tick);

        // 始源护盾（总等级≥6）
        if (totalLevel >= 6) {
            applyOriginShield(player, totalLevel, tick);
        }

        // 始源再生（总等级≥11）
        if (totalLevel >= 11) {
            applyOriginRegeneration(player, totalLevel, tick);
        }

        // 始源光环（总等级≥31）
        if (totalLevel >= 31) {
            applyOriginAura(player, totalLevel, tick);
        }

        // 清理过期计数器防止内存泄漏
        if (tick > 10000) {
            tickCounters.put(player.getUUID(), 1);
        }
    }

    /**
     * 始源护盾 - 周期性给予吸收效果
     * 吸收量 = 总等级 × 系数，冷却随等级递减
     */
    private static void applyOriginShield(Player player, int totalLevel, int tick) {
        int interval;
        float absorptionAmount;

        if (totalLevel <= 10) {
            interval = 200;     // 10秒
            absorptionAmount = totalLevel * 1.0F;
        } else if (totalLevel <= 20) {
            interval = 150;     // 7.5秒
            absorptionAmount = totalLevel * 1.5F;
        } else if (totalLevel <= 30) {
            interval = 120;     // 6秒
            absorptionAmount = totalLevel * 2.0F;
        } else {
            interval = 100;     // 5秒
            absorptionAmount = totalLevel * 3.0F;
        }

        if (tick % interval == 0) {
            // 只在当前吸收量低于阈值时补充
            float currentAbsorption = player.getAbsorptionAmount();
            float threshold = absorptionAmount * 0.5F;
            if (currentAbsorption < threshold) {
                player.setAbsorptionAmount(Math.min(absorptionAmount, absorptionAmount - currentAbsorption + player.getAbsorptionAmount()));
            }
        }
    }

    /**
     * 始源再生 - 周期性恢复生命值
     * 基准间隔2秒（40tick），高等级递减
     */
    private static void applyOriginRegeneration(Player player, int totalLevel, int tick) {
        int interval;
        int healAmount;

        if (totalLevel <= 15) {
            interval = 40;      // 2秒
            healAmount = 1;
        } else if (totalLevel <= 25) {
            interval = 30;      // 1.5秒
            healAmount = 1;
        } else if (totalLevel <= 35) {
            interval = 25;      // 1.25秒
            healAmount = 2;
        } else {
            interval = 20;      // 1秒
            healAmount = 2;
        }

        if (tick % interval == 0) {
            if (player.getHealth() < player.getMaxHealth()) {
                player.heal(healAmount);
            }
        }
    }

    /**
     * 始源光环 - 对周围实体造成周期性始源伤害
     *
     * 开关：玩家可通过按键绑定（默认无绑定，需在控制设置中手动绑定）切换光环启用状态。
     * 状态存储在 player.getPersistentData() 的 dcc_aura_disabled 标签，默认启用（保持原行为）。
     */
    private static void applyOriginAura(Player player, int totalLevel, int tick) {
        // 检查光环开关：被禁用则跳过（默认启用，保持原行为）
        if (player.getPersistentData().getBoolean(com.qituo.dcc.network.ModNetwork.TAG_AURA_DISABLED)) {
            return;
        }

        if (tick % 40 != 0) {
            return;
        }

        // 半径统一为 10 格（用户要求）
        double radius = 10.0;
        float damage = (float) (totalLevel * (totalLevel >= 36 ? 0.5 : 0.3));

        AABB searchArea = player.getBoundingBox().inflate(radius);
        List<LivingEntity> entities = player.level().getEntitiesOfClass(LivingEntity.class, searchArea,
                e -> e != player && !e.isSpectator() && e.isAlive());

        if (entities.isEmpty()) {
            return;
        }

        var damageSource = ModDamageSources.causeOriginEndDamage(player);
        for (LivingEntity entity : entities) {
            EntityBypassHelper.killEntity(entity, damageSource, damage);
        }
    }

    // ==================== 击退事件：免疫击退 ====================

    @SubscribeEvent
    public static void onLivingKnockBack(LivingKnockBackEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) {
            return;
        }

        int totalLevel = getTotalArmorLevel(player);
        if (totalLevel < 21) {
            return;
        }

        // 21-30级：击退力度降至30%
        if (totalLevel <= 30) {
            event.setStrength(event.getStrength() * 0.3F);
        } else {
            // 31-40级：完全免疫击退
            event.setCanceled(true);
        }
    }
}
