package com.qituo.dcc.effects;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import com.qituo.dcc.DragonCurseChronicles;
import com.qituo.dcc.config.TalismanConfig;
import java.lang.reflect.Field;

public class TalismanEffects {

    // 牛之力
    public static final StatusEffect COW_POWER = Registry.register(Registries.STATUS_EFFECT, DragonCurseChronicles.id("cow_power"), new StatusEffect(StatusEffectCategory.BENEFICIAL, 0xFF0000) {
        @Override
        public void applyUpdateEffect(LivingEntity entity, int amplifier) {
            // 增加攻击力、防御力、移动速度、攻击范围
        }

        @Override
        public boolean canApplyUpdateEffect(int duration, int amplifier) {
            return true;
        }
    });

    // 虎之力
    public static final StatusEffect TIGER_POWER = Registry.register(Registries.STATUS_EFFECT, DragonCurseChronicles.id("tiger_power"), new StatusEffect(StatusEffectCategory.BENEFICIAL, 0xFF0000) {
        @Override
        public void applyUpdateEffect(LivingEntity entity, int amplifier) {
            // 增加攻击力、防御力、伤害吸收
        }

        @Override
        public boolean canApplyUpdateEffect(int duration, int amplifier) {
            return true;
        }
    });

    // 兔之力
    public static final StatusEffect RABBIT_POWER = Registry.register(Registries.STATUS_EFFECT, DragonCurseChronicles.id("rabbit_power"), new StatusEffect(StatusEffectCategory.BENEFICIAL, 0xFF0000) {
        @Override
        public void applyUpdateEffect(LivingEntity entity, int amplifier) {
            // 增加移动速度
        }

        @Override
        public boolean canApplyUpdateEffect(int duration, int amplifier) {
            return true;
        }
    });

    // 蛇之力
    public static final StatusEffect SNAKE_POWER = Registry.register(Registries.STATUS_EFFECT, DragonCurseChronicles.id("snake_power"), new StatusEffect(StatusEffectCategory.BENEFICIAL, 0xFF0000) {
        @Override
        public void applyUpdateEffect(LivingEntity entity, int amplifier) {
            // 增强隐身效果
        }

        @Override
        public boolean canApplyUpdateEffect(int duration, int amplifier) {
            return true;
        }
    });

    // 马之力
    public static final StatusEffect HORSE_POWER = Registry.register(Registries.STATUS_EFFECT, DragonCurseChronicles.id("horse_power"), new StatusEffect(StatusEffectCategory.BENEFICIAL, 0xFF0000) {
        @Override
        public void applyUpdateEffect(LivingEntity entity, int amplifier) {
            // 治愈效果
        }

        @Override
        public boolean canApplyUpdateEffect(int duration, int amplifier) {
            return true;
        }
    });

    // 鸡之力
    public static final StatusEffect CHICKEN_POWER = Registry.register(Registries.STATUS_EFFECT, DragonCurseChronicles.id("chicken_power"), new StatusEffect(StatusEffectCategory.BENEFICIAL, 0xFF0000) {
        @Override
        public void applyUpdateEffect(LivingEntity entity, int amplifier) {
            // 飞行效果
        }

        @Override
        public boolean canApplyUpdateEffect(int duration, int amplifier) {
            return true;
        }
    });

    // 狗之力 - 持续伤害吸收
    public static final StatusEffect DOG_POWER = Registry.register(Registries.STATUS_EFFECT, DragonCurseChronicles.id("dog_power"), new StatusEffect(StatusEffectCategory.BENEFICIAL, 0xFF0000) {
        @Override
        public void applyUpdateEffect(LivingEntity entity, int amplifier) {
            // 持续给予伤害吸收效果（等级V，持续2秒，刷新持续时间）
            if (entity instanceof PlayerEntity player && !player.getWorld().isClient) {
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.ABSORPTION,
                    2 * 20,
                    4,
                    false,
                    false,
                    true
                ));
            }
        }

        @Override
        public boolean canApplyUpdateEffect(int duration, int amplifier) {
            return true;
        }
    });

    // 羊之力 - 灵魂出窍（包含隐身、夜视、飞行、穿墙等效果）
    public static final StatusEffect SHEEP_POWER = Registry.register(Registries.STATUS_EFFECT, DragonCurseChronicles.id("sheep_power"), new StatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFFFFF) {
        @Override
        public void applyUpdateEffect(LivingEntity entity, int amplifier) {
            if (entity instanceof PlayerEntity player) {
                // 穿墙能力
                player.noClip = true;

                // 无敌状态
                player.setInvulnerable(true);

                // 设置飞行模式（持续保持）
                PlayerAbilities abilities = player.getAbilities();
                if (!abilities.allowFlying) {
                    abilities.allowFlying = true;
                }
                if (!abilities.flying) {
                    abilities.flying = true;
                }
                // 使用配置文件中的飞行速度（通过反射访问私有字段）
                try {
                    float speed = (float) TalismanConfig.getSheepTalismanFlySpeed();

                    // 设置飞行速度
                    abilities.setFlySpeed(speed);

                    // 设置行走速度（确保疾跑时速度正确）
                    abilities.setWalkSpeed(speed);

                    // 设置玩家为在地面上，确保疾跑可以生效
                    player.setOnGround(true);
                } catch (Exception e) {
                    DragonCurseChronicles.LOGGER.warn("Failed to set movement speed", e);
                }
                player.sendAbilitiesUpdate();

                // 清除掉落伤害
                player.fallDistance = 0;
            }
        }

        @Override
        public boolean canApplyUpdateEffect(int duration, int amplifier) {
            return true;
        }
    });

    public static void initialize() {
    }
}