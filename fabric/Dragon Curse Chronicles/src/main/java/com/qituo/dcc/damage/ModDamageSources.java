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
import org.jetbrains.annotations.NotNull;

public class ModDamageSources {
    public static final RegistryKey<DamageType> ORIGIN_END_SOURCE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(DragonCurseChronicles.MOD_ID, "origin_end"));
    public static final TagKey<DamageType> ORIGIN_WEAKNESS = TagKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(DragonCurseChronicles.MOD_ID, "origin_weakness"));

    public static DamageSource causeOriginEndDamage(Entity attacker) {
        return new OriginEndDamageSource(attacker);
    }

    public static DamageSource causeOriginEndDamage(Entity attacker, int level) {
        return new OriginEndDamageSource(attacker).setLevel(level);
    }

    public static class OriginEndDamageSource extends DamageSource {
        private int level = 1;

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

        public boolean scalesWithDifficulty() {
            return false;
        }

        public boolean isBypassArmor() {
            return true;
        }

        public boolean isBypassMagic() {
            return true;
        }

        public boolean isBypassInvulnerable() {
            return true;
        }

        public boolean isBypassShield() {
            return true;
        }

        @Override
        public float getExhaustion() {
            return 0.0F;
        }

        public boolean isFire() {
            return false;
        }

        public boolean isExplosion() {
            return false;
        }

        public float getTrueDamage(float baseDamage) {
            return baseDamage * 0.5F;
        }
    }
}
