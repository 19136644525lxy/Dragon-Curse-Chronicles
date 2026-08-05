package com.qituo.dcc.mixin;

import com.qituo.dcc.damage.ModDamageSources;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * LivingEntity Mixin
 * 拦截实体的免疫检查，确保始源终结伤害能够无视免疫效果
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    
    /**
     * 拦截isInvulnerableTo方法
     * 对于始源终结伤害类型，强制返回false（即不免疫）
     */
    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void dcc$bypassInvulnerability(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        // 检查是否为始源终结伤害
        if (damageSource != null) {
            // 检查是否为OriginEndDamageSource实例
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
                return;
            }
        }
    }
    
    /**
     * 拦截hurt方法，确保伤害能够正确应用
     * 即使实体处于无敌状态也能受到伤害
     */
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void dcc$forceHurt(DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (damageSource != null && (damageSource instanceof ModDamageSources.OriginEndDamageSource || ModDamageSources.isOriginEndDamage(damageSource))) {
            LivingEntity entity = (LivingEntity) (Object) this;
            
            // 检查是否已经在处理中
            if (entity.getTags().contains("dcc$bypass_processing")) {
                return;
            }
            
            // 重置无敌时间
            entity.invulnerableTime = 0;
            
            // 对于始源终结伤害，即使实体"免疫"也强制造成伤害
            // 这会在EntityBypassHelper中进一步处理
        }
    }
}
