package com.qituo.dcc.enchantments;

import com.qituo.dcc.damage.EntityBypassHelper;
import com.qituo.dcc.damage.ModDamageSources;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantment.Rarity;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class OriginPowerEnchantment extends Enchantment {

    public OriginPowerEnchantment(Rarity rarity, EnchantmentTarget target, EquipmentSlot... slots) {
        super(rarity, target, slots);
    }

    @Override
    public int getMinLevel() {
        return 20;
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }

    @Override
    public boolean isTreasure() {
        return true;
    }

    @Override
    public boolean isAvailableForEnchantedBookOffer() {
        return false;
    }

    private static final String TAG_ANTI_LOOP = "temp$OriginPowerEnchantTag";
    private static final String TAG_TARGET_PROCESSED = "temp$OriginPowerTargetProcessed";

    public static void init() {
        // 使用AttackEntityCallback检测玩家攻击事件
        net.fabricmc.fabric.api.event.player.AttackEntityCallback.EVENT.register(OriginPowerEnchantment::onPlayerAttack);
        // 使用ALLOW_DAMAGE检测伤害反射
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.ALLOW_DAMAGE.register(OriginPowerEnchantment::onLivingDamage);
    }

    /**
     * 玩家攻击实体回调
     */
    private static ActionResult onPlayerAttack(PlayerEntity player, World world, Hand hand, Entity entity, HitResult hitResult) {
        try {
            if (entity instanceof LivingEntity target) {
                if (target.getCommandTags().contains(TAG_TARGET_PROCESSED)) {
                    return ActionResult.PASS;
                }

                int mainHandLevel = EnchantmentHelper.getLevel(ModEnchantments.ORIGIN_POWER, player.getMainHandStack());
                int offHandLevel = EnchantmentHelper.getLevel(ModEnchantments.ORIGIN_POWER, player.getOffHandStack());

                int maxLevel = Math.max(mainHandLevel, offHandLevel);
                if (maxLevel > 0) {
                    target.addCommandTag(TAG_TARGET_PROCESSED);
                    try {
                        applyOriginDamage(player, target, maxLevel);
                    } finally {
                        target.getCommandTags().remove(TAG_TARGET_PROCESSED);
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return ActionResult.PASS;
    }

    /**
     * 伤害回调 - 处理伤害反射
     */
    private static boolean onLivingDamage(LivingEntity entity, net.minecraft.entity.damage.DamageSource source, float amount) {
        try {
            if (entity instanceof PlayerEntity player && !player.getWorld().isClient) {
                // 检查是否来自OriginEnd伤害源，避免无限循环
                if (source instanceof ModDamageSources.OriginEndDamageSource) {
                    return true;
                }

                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                        ItemStack stack = player.getEquippedStack(slot);
                        int level = EnchantmentHelper.getLevel(ModEnchantments.ORIGIN_POWER, stack);
                        if (level > 0) {
                            applyDamageReflection(player, source, amount, level);
                            break;
                        }
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return true;
    }

    private static void applyDamageReflection(PlayerEntity player, net.minecraft.entity.damage.DamageSource source, float amount, int level) {
        try {
            if (Math.random() >= level / 100.0F) return;

            Entity sourceEntity = source.getAttacker();
            if (sourceEntity == null || sourceEntity == player || player.getCommandTags().contains(TAG_ANTI_LOOP)) return;

            player.addCommandTag(TAG_ANTI_LOOP);

            try {
                float damage = amount;
                var damageSource = ModDamageSources.causeOriginEndDamage(player);
                
                // 使用EntityBypassHelper确保伤害生效
                if (sourceEntity instanceof LivingEntity livingSource) {
                    EntityBypassHelper.killEntity(livingSource, damageSource, damage);
                } else {
                    sourceEntity.damage(damageSource, damage);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                player.getCommandTags().remove(TAG_ANTI_LOOP);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static void applyOriginDamage(PlayerEntity player, LivingEntity target, int level) {
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
                        // 应用强化效果
                        totalDamage = originEndDamageSource.applyEnhancements(target, totalDamage);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }

                // 使用EntityBypassHelper确保伤害生效并掉落物品
                EntityBypassHelper.killEntity(target, damageSource, totalDamage);

                try {
                    if (target.isUsingItem() && target.getActiveItem().getItem() instanceof net.minecraft.item.ShieldItem) {
                        if (target instanceof PlayerEntity targetPlayer) {
                            targetPlayer.disableShield(true);
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.getItem() instanceof net.minecraft.item.SwordItem ||
               stack.getItem() instanceof net.minecraft.item.AxeItem ||
               stack.getItem() instanceof net.minecraft.item.BowItem ||
               stack.getItem() instanceof net.minecraft.item.CrossbowItem ||
               stack.getItem() instanceof net.minecraft.item.ArmorItem;
    }
}
