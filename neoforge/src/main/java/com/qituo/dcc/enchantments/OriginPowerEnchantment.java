package com.qituo.dcc.enchantments;

import com.qituo.dcc.DragonCurseChronicles;
import com.qituo.dcc.damage.EntityBypassHelper;
import com.qituo.dcc.damage.ModDamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 始源之力附魔
 *
 * 攻击效果（本类）：
 * - 攻击时触发始源终结伤害（无视护甲/护盾/无敌帧/附魔保护）
 * - 伤害值随附魔等级递增（1级66 → 10级5201314）
 * - 破盾能力
 *
 * 盔甲效果（OriginPowerArmorHandler）：
 * - 多件叠加，总等级驱动
 * - 反弹/减伤/护盾/再生/免疫击退/光环，阶梯式解锁
 */
@EventBusSubscriber(modid = DragonCurseChronicles.MODID, bus = EventBusSubscriber.Bus.GAME)
public class OriginPowerEnchantment {
    private static final Logger LOGGER = LoggerFactory.getLogger(OriginPowerEnchantment.class);

    /** 附魔台/村民交易可获取的最大等级，超过此等级只能通过合成获得 */
    public static final int MAX_TABLE_LEVEL = 5;

    private static final String TAG_TARGET_PROCESSED = "temp$OriginPowerTargetProcessed";

    // ==================== 攻击效果 ====================

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        try {
            // 避免处理自己造成的伤害，防止无限递归
            if (event.getSource() instanceof ModDamageSources.OriginEndDamageSource) {
                return;
            }

            if (event.getSource().getEntity() instanceof Player player && event.getEntity() instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) event.getEntity();

                // 避免同一目标在同一帧内被多次处理
                if (target.getTags().contains(TAG_TARGET_PROCESSED)) {
                    return;
                }

                // 取主手/副手中附魔等级最高的一把武器（直接读NBT，避免JSON附魔未就位）
                int mainHandLevel = ModEnchantments.getOriginPowerLevel(player.getMainHandItem());
                int offHandLevel = ModEnchantments.getOriginPowerLevel(player.getOffhandItem());
                int maxLevel = Math.max(mainHandLevel, offHandLevel);

                if (maxLevel > 0) {
                    target.addTag(TAG_TARGET_PROCESSED);
                    try {
                        applyOriginDamage(player, target, maxLevel);
                    } finally {
                        target.removeTag(TAG_TARGET_PROCESSED);
                    }
                }
            }
        } catch (Throwable e) {
            LOGGER.error("OriginPower attack failed: {}", e.getMessage());
        }
    }

    /**
     * 应用始源终结伤害
     * 基础伤害 + 100%真伤 + 强化加成 + 五重击杀链保障死亡
     */
    private static void applyOriginDamage(Player player, LivingEntity target, int level) {
        try {
            if (level > 0 && level <= 10) {
                float damage = com.qituo.dcc.damage.DamagePresets.getDamage(level);
                var damageSource = ModDamageSources.causeOriginEndDamage(player, level);

                float totalDamage = damage;
                if (damageSource instanceof ModDamageSources.OriginEndDamageSource originEndDamageSource) {
                    try {
                        float trueDamage = originEndDamageSource.getTrueDamage(damage);
                        if (trueDamage > 0) {
                            totalDamage += trueDamage;
                        }
                        totalDamage = originEndDamageSource.applyEnhancements(target, totalDamage);
                    } catch (Throwable e) {
                        LOGGER.debug("Enhancement failed: {}", e.getMessage());
                    }
                }

                // 五重击杀链确保伤害生效并掉落物品
                EntityBypassHelper.killEntity(target, damageSource, totalDamage);

                // 破盾
                try {
                    if (target.isUsingItem() && target.getUseItem().getItem() instanceof net.minecraft.world.item.ShieldItem) {
                        if (target instanceof Player targetPlayer) {
                            targetPlayer.disableShield(); // 1.21：无参，自动冷却100tick
                        }
                    }
                } catch (Throwable e) {
                    LOGGER.debug("Shield disable failed: {}", e.getMessage());
                }
            }
        } catch (Throwable e) {
            LOGGER.error("applyOriginDamage failed: {}", e.getMessage());
        }
    }
}
