package com.qituo.dcc.enchantments;

import com.qituo.dcc.damage.ModDamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantment.Rarity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class OriginPowerEnchantment extends Enchantment {

    public OriginPowerEnchantment(Rarity p_44676_, EquipmentSlot... p_44678_) {
        super(p_44676_, EnchantmentCategory.ARMOR, p_44678_);
    }

    @Override
    public int getMinCost(int level) {
        return 20 * level;
    }

    @Override
    public int getMaxCost(int level) {
        return super.getMinCost(level) + 50;
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    public boolean isTradeable() {
        return false;
    }

    private static final String TAG_ANTI_LOOP = "temp$OriginPowerEnchantTag";
    private static final String TAG_TARGET_PROCESSED = "temp$OriginPowerTargetProcessed";

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
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
                
                // 检查主手
                int mainHandLevel = player.getMainHandItem().getEnchantmentLevel(ModEnchantments.ORIGIN_POWER.get());
                int offHandLevel = player.getOffhandItem().getEnchantmentLevel(ModEnchantments.ORIGIN_POWER.get());
                
                // 取最高等级的附魔
                int maxLevel = Math.max(mainHandLevel, offHandLevel);
                if (maxLevel > 0) {
                    // 添加处理标签
                    target.addTag(TAG_TARGET_PROCESSED);
                    try {
                        applyOriginDamage(player, target, maxLevel);
                    } finally {
                        // 移除处理标签
                        target.removeTag(TAG_TARGET_PROCESSED);
                    }
                }
            }
        } catch (Throwable e) {
            // 忽略所有异常，防止与其他模组冲突导致崩溃
            e.printStackTrace();
        }
    }

    // 伤害反弹效果
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        try {
            if (event.getEntity() instanceof Player player && !player.level().isClientSide) {
                // 检查是否穿戴了带有始源之力附魔的护甲
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                        ItemStack stack = player.getItemBySlot(slot);
                        int level = stack.getEnchantmentLevel(ModEnchantments.ORIGIN_POWER.get());
                        if (level > 0) {
                            applyDamageReflection(player, event, level);
                            break;
                        }
                    }
                }
            }
        } catch (Throwable e) {
            // 忽略所有异常，防止与其他模组冲突导致崩溃
            e.printStackTrace();
        }
    }

    // 应用伤害反弹
    private static void applyDamageReflection(Player player, LivingHurtEvent event, int level) {
        try {
            if (Math.random() >= level / 100.0F) return; // 概率：等级/100
            
            Entity source = event.getSource().getEntity();
            if (source == null || source == player || player.getTags().contains(TAG_ANTI_LOOP)) return;
            
            player.addTag(TAG_ANTI_LOOP);
            
            try {
                // 反弹伤害
                float damage = event.getAmount();
                var damageSource = ModDamageSources.causeOriginEndDamage(player);
                source.hurt(damageSource, damage);
                
                // 抵消部分伤害
                event.setAmount(event.getAmount() * 0.75F);
            } catch (Throwable e) {
                // 忽略异常
                e.printStackTrace();
            } finally {
                player.removeTag(TAG_ANTI_LOOP);
            }
        } catch (Throwable e) {
            // 忽略所有异常，防止与其他模组冲突导致崩溃
            e.printStackTrace();
        }
    }

    private static void applyOriginDamage(Player player, LivingEntity target, int level) {
        try {
            if (level > 0 && level <= 10) {
                float damage = com.qituo.dcc.damage.DamagePresets.getDamage(level);
                var damageSource = ModDamageSources.causeOriginEndDamage(player, level);
                
                // 计算总伤害（包含真伤）
                float totalDamage = damage;
                if (damageSource instanceof ModDamageSources.OriginEndDamageSource originEndDamageSource) {
                    try {
                        float trueDamage = originEndDamageSource.getTrueDamage(damage);
                        if (trueDamage > 0) {
                            totalDamage += trueDamage;
                        }
                    } catch (Throwable e) {
                        // 忽略异常
                        e.printStackTrace();
                    }
                }
                
                // 一次性造成所有伤害
                target.hurt(damageSource, totalDamage);
                
                // 破盾能力
                try {
                    if (target.isUsingItem() && target.getUseItem().getItem() instanceof net.minecraft.world.item.ShieldItem) {
                        if (target instanceof Player targetPlayer) {
                            targetPlayer.disableShield(true);
                        }
                    }
                } catch (Throwable e) {
                    // 忽略异常
                    e.printStackTrace();
                }
                
                // 重置无敌时间，确保伤害能够完全生效
                try {
                    target.invulnerableTime = 0;
                } catch (Throwable e) {
                    // 忽略异常
                    e.printStackTrace();
                }
            }
        } catch (Throwable e) {
            // 忽略所有异常，防止与其他模组冲突导致崩溃
            e.printStackTrace();
        }
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 允许附魔在任何武器、弓弩和护甲上
        return stack.getItem() instanceof net.minecraft.world.item.SwordItem ||
               stack.getItem() instanceof net.minecraft.world.item.AxeItem ||
               stack.getItem() instanceof net.minecraft.world.item.BowItem ||
               stack.getItem() instanceof net.minecraft.world.item.CrossbowItem ||
               stack.getItem() instanceof net.minecraft.world.item.ArmorItem;
    }
}
