package com.qituo.dcc.mixin;

import com.qituo.dcc.enchantments.OriginPowerArmorHandler;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * 始源之力盔甲效果 Mixin（Fabric 版）
 *
 * 处理 Forge 中由 LivingHurtEvent 和 LivingKnockBackEvent 负责的逻辑：
 * 1. 减伤：注入 applyDamage()，按总等级减免伤害比例
 * 2. 免疫击退：注入 takeKnockback()，21级以上降低力度或完全取消
 *
 * 原理：Fabric 没有对应的事件 API，需要通过 Mixin 修改方法参数。
 */
@Mixin(LivingEntity.class)
public abstract class OriginPowerArmorMixin {

    /**
     * 减伤：在 applyDamage() 中修改 amount 参数
     * 仅对玩家生效，按始源之力总等级减免伤害比例（封顶50%）
     */
    @ModifyVariable(method = "applyDamage", at = @At("HEAD"), argsOnly = true)
    private float dcc$reduceDamage(float amount, DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof PlayerEntity player && !self.getWorld().isClient) {
            int totalLevel = OriginPowerArmorHandler.getTotalArmorLevel(player);
            if (totalLevel > 0) {
                float reduction = OriginPowerArmorHandler.getDamageReduction(totalLevel);
                if (reduction > 0) {
                    return amount * (1.0F - reduction);
                }
            }
        }
        return amount;
    }

    /**
     * 免疫击退：在 takeKnockback() 中修改 strength 参数
     * 21-30级：击退力度降至30%
     * 31-40级：完全免疫击退（strength=0）
     */
    @ModifyVariable(method = "takeKnockback", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private double dcc$reduceKnockback(double strength) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof PlayerEntity player && !self.getWorld().isClient) {
            int totalLevel = OriginPowerArmorHandler.getTotalArmorLevel(player);
            if (totalLevel >= 21) {
                if (totalLevel >= 31) {
                    return 0.0; // 完全免疫击退
                }
                return strength * 0.3; // 击退力度降至30%
            }
        }
        return strength;
    }
}
