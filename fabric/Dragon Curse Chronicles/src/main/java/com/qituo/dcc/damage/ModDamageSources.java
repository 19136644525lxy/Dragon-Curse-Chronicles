package com.qituo.dcc.damage;

import com.qituo.dcc.DragonCurseChronicles;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ModDamageSources {
    public static final RegistryKey<DamageType> ORIGIN_END_SOURCE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(DragonCurseChronicles.MOD_ID, "origin_end"));
    public static final TagKey<DamageType> ORIGIN_WEAKNESS = TagKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(DragonCurseChronicles.MOD_ID, "origin_weakness"));
    public static final TagKey<DamageType> IS_ABSOLUTE = TagKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(DragonCurseChronicles.MOD_ID, "is_absolute"));

    public static DamageSource causeOriginEndDamage(Entity attacker) {
        return new OriginEndDamageSource(attacker);
    }

    public static DamageSource causeOriginEndDamage(Entity attacker, int level) {
        return new OriginEndDamageSource(attacker).setLevel(level);
    }

    /**
     * 检查伤害类型是否为绝对伤害
     */
    public static boolean isAbsolute(DamageSource damageSource) {
        if (damageSource == null) {
            return false;
        }
        return damageSource.isIn(IS_ABSOLUTE);
    }

    /**
     * 检查伤害类型是否具有始源弱点效果
     */
    public static boolean isOriginWeakness(DamageSource damageSource) {
        if (damageSource == null) {
            return false;
        }
        return damageSource.isIn(ORIGIN_WEAKNESS);
    }

    /**
     * 检查伤害类型是否为始源终结伤害
     */
    public static boolean isOriginEndDamage(DamageSource damageSource) {
        if (damageSource == null) {
            return false;
        }
        return damageSource.isOf(ORIGIN_END_SOURCE);
    }

    public static class OriginEndDamageSource extends DamageSource {
        private int level = 1;
        private boolean absoluteDamage = true;

        public OriginEndDamageSource(Entity source) {
            super(getDamageTypeEntry(source), source);
        }

        private static RegistryEntry<DamageType> getDamageTypeEntry(Entity source) {
            World world = source.getWorld();
            var registry = world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE);
            if (registry != null) {
                var entry = registry.getEntry(ORIGIN_END_SOURCE);
                if (entry.isPresent()) {
                    return entry.get();
                }
            }
            return world.getDamageSources().generic().getTypeRegistryEntry();
        }

        public OriginEndDamageSource setLevel(int level) {
            this.level = level;
            return this;
        }

        /**
         * 设置是否为绝对伤害
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
            return this.isIn(ORIGIN_WEAKNESS);
        }

        /**
         * 获取伤害等级
         */
        public int getLevel() {
            return level;
        }

        @Override
        public Text getDeathMessage(LivingEntity entity) {
            Entity sourceEntity = this.getAttacker();
            if (sourceEntity != null) {
                String s = "death.attack.origin_end.player";
                return Text.translatable(s, entity.getDisplayName(), sourceEntity.getDisplayName());
            } else {
                String s = "death.attack.origin_end";
                return Text.translatable(s, entity.getDisplayName());
            }
        }

        public boolean isScaledWithDifficulty() {
            return false;
        }

        @Override
        public float getExhaustion() {
            return 0.0F;
        }

        /**
         * 获取真伤部分（100%伤害作为绝对真伤）
         */
        public float getTrueDamage(float baseDamage) {
            return baseDamage;
        }

        /**
         * 应用强化效果
         */
        public float applyEnhancements(LivingEntity entity, float damage) {
            float finalDamage = damage;
            
            if (isOriginWeakness()) {
                finalDamage *= 1.5f; // 弱点加成50%
            }
            
            if (level > 1) {
                finalDamage *= (1.0f + (level - 1) * 0.1f); // 每级增加10%伤害
            }
            
            return finalDamage;
        }
    }
}
