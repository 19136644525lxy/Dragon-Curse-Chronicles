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
    public static final ResourceKey<DamageType> ORIGIN_END_SOURCE = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(DragonCurseChronicles.MODID, "origin_end"));
    public static final TagKey<DamageType> ORIGIN_WEAKNESS = TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(DragonCurseChronicles.MODID, "origin_weakness"));
    public static final TagKey<DamageType> IS_ABSOLUTE = TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(DragonCurseChronicles.MODID, "is_absolute"));
    
    public static DamageSource causeOriginEndDamage(Entity attacker) {
        return new OriginEndDamageSource(attacker);
    }
    
    public static DamageSource causeOriginEndDamage(Entity attacker, int level) {
        return new OriginEndDamageSource(attacker).setLevel(level);
    }
    
    /**
     * 检查伤害类型是否为绝对伤害
     * 绝对伤害会无视所有防御和免疫效果
     */
    public static boolean isAbsolute(net.minecraft.world.damagesource.DamageSource damageSource) {
        if (damageSource == null) {
            return false;
        }
        return damageSource.typeHolder().is(IS_ABSOLUTE);
    }
    
    /**
     * 检查伤害类型是否具有始源弱点效果
     * 始源弱点效果会对特定实体造成额外伤害
     */
    public static boolean isOriginWeakness(net.minecraft.world.damagesource.DamageSource damageSource) {
        if (damageSource == null) {
            return false;
        }
        return damageSource.typeHolder().is(ORIGIN_WEAKNESS);
    }
    
    /**
     * 检查伤害类型是否为始源终结伤害
     */
    public static boolean isOriginEndDamage(net.minecraft.world.damagesource.DamageSource damageSource) {
        if (damageSource == null) {
            return false;
        }
        return damageSource.typeHolder().is(ORIGIN_END_SOURCE);
    }
    
    public static class OriginEndDamageSource extends DamageSource {
        private int level = 1;
        private boolean absoluteDamage = true;
        
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
        
        /**
         * 设置是否为绝对伤害
         * 绝对伤害会无视所有防御和免疫效果
         */
        public OriginEndDamageSource setAbsolute(boolean absolute) {
            this.absoluteDamage = absolute;
            return this;
        }
        
        /**
         * 检查是否为绝对伤害
         */
        public boolean isAbsolute() {
            return absoluteDamage;
        }
        
        /**
         * 检查是否具有始源弱点效果
         */
        public boolean isOriginWeakness() {
            return this.typeHolder().is(ORIGIN_WEAKNESS);
        }
        
        /**
         * 获取伤害等级
         */
        public int getLevel() {
            return level;
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
        
        @Override
        public float getFoodExhaustion() {
            return 0.0F; // 不消耗饱食度
        }
        
        /**
         * 获取真伤部分
         * @param baseDamage 基础伤害
         * @return 真伤部分
         */
        public float getTrueDamage(float baseDamage) {
            // 100%的伤害作为绝对真伤
            return baseDamage;
        }
        
        /**
         * 应用强化效果
         * @param entity 目标实体
         * @param damage 伤害值
         * @return 实际造成的伤害
         */
        public float applyEnhancements(LivingEntity entity, float damage) {
            float finalDamage = damage;
            
            // 如果目标对始源伤害有弱点，增加伤害
            if (isOriginWeakness()) {
                finalDamage *= 1.5f; // 弱点加成50%
            }
            
            // 根据等级增加额外伤害
            if (level > 1) {
                finalDamage *= (1.0f + (level - 1) * 0.1f); // 每级增加10%伤害
            }
            
            return finalDamage;
        }
    }
}