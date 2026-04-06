package com.qituo.dcc.damage;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;

public class OriginEndDamageSource extends DamageSource {
    public OriginEndDamageSource(Entity source) {
        super(source.level().registryAccess().registry(Registries.DAMAGE_TYPE).get().getHolderOrThrow(DamageTypes.GENERIC), source);
    }
    
    @Override
    public Component getLocalizedDeathMessage(LivingEntity entity) {
        Entity sourceEntity = this.getDirectEntity();
        if (sourceEntity != null) {
            String s = "death.attack.origin_end.player";
            return Component.translatable(s, entity.getDisplayName(), sourceEntity.getDisplayName());
        } else {
            String s = "death.attack.origin_end";
            return Component.translatable(s, entity.getDisplayName());
        }
    }
    
    @Override
    public boolean scalesWithDifficulty() {
        return false;
    }
    
    public boolean isBypassArmor() {
        return true; // 无视护甲
    }
    
    public boolean isBypassMagic() {
        return true; // 无视魔法抗性
    }
    
    public boolean isBypassInvulnerable() {
        return true; // 无视无敌状态
    }
    
    public boolean isBypassShield() {
        return true; // 无视护盾
    }
    
    @Override
    public float getFoodExhaustion() {
        return 0.0F; // 不消耗饱食度
    }
    
    public boolean isFire() {
        return false; // 不是火焰伤害
    }
    
    public boolean isExplosion() {
        return false; // 不是爆炸伤害
    }
}