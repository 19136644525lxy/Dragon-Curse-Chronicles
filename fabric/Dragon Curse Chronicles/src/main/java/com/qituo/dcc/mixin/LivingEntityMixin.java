package com.qituo.dcc.mixin;

import com.qituo.dcc.damage.ModDamageSources;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Entity Mixin（Fabric版）
 * 拦截实体的免疫检查，确保始源终结伤害能够无视免疫效果
 * 
 * 注：isInvulnerableTo 定义在 Entity 类中，不是 LivingEntity
 */
@Mixin(Entity.class)
public abstract class LivingEntityMixin {
    
    /**
     * 拦截 isInvulnerableTo 方法
     * 对于始源终结伤害类型，强制返回 false（即不免疫）
     */
    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void dcc$bypassInvulnerability(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if (damageSource != null) {
            // 检查是否为 OriginEndDamageSource 实例
            if (damageSource instanceof ModDamageSources.OriginEndDamageSource) {
                cir.setReturnValue(false);
                return;
            }
            
            // 检查伤害类型标签
            if (ModDamageSources.isOriginEndDamage(damageSource)) {
                cir.setReturnValue(false);
                return;
            }
            
            // 检查是否为绝对伤害
            if (ModDamageSources.isAbsolute(damageSource)) {
                cir.setReturnValue(false);
            }
        }
    }
}
