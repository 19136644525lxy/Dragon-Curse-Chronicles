package com.qituo.dcc.damage;

import com.qituo.dcc.DragonCurseChronicles;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class ModDamageSources {
    public static final ResourceKey<DamageType> ORIGIN_END_SOURCE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(DragonCurseChronicles.MODID, "origin_end"));
    public static final TagKey<DamageType> ORIGIN_WEAKNESS = TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(DragonCurseChronicles.MODID, "origin_weakness"));
    
    public static DamageSource causeOriginEndDamage(Entity attacker) {
        return new OriginEndDamageSource(attacker);
    }
    
    public static DamageSource causeOriginEndDamage(Entity attacker, int level) {
        return new OriginEndDamageSource(attacker).setLevel(level);
    }
    
    public static class OriginEndDamageSource extends DamageSource {
        private int level = 1;
        
        public OriginEndDamageSource(Entity source) {
            super(getDamageTypeHolder(source), source);
        }
        
        private static net.minecraft.core.Holder<DamageType> getDamageTypeHolder(Entity source) {
            var registry = source.level().registryAccess().registry(Registries.DAMAGE_TYPE);
            if (registry.isPresent()) {
                var holder = registry.get().getHolder(ORIGIN_END_SOURCE);
                if (holder.isPresent()) {
                    return holder.get();
                }
            }
            // 如果伤害类型不存在，使用默认的通用伤害类型
            return source.level().damageSources().generic().typeHolder();
        }
        
        public OriginEndDamageSource setLevel(int level) {
            this.level = level;
            return this;
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
        
        // 额外的伤害效果
        public float getTrueDamage(float baseDamage) {
            // 50%的伤害作为绝对真伤
            return baseDamage * 0.5F;
        }
    }
}