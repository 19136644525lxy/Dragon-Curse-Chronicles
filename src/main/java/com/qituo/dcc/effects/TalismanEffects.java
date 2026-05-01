package com.qituo.dcc.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Abilities;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.qituo.dcc.DragonCurseChronicles;
import com.qituo.dcc.config.TalismanConfig;
import java.lang.reflect.Field;

public class TalismanEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, DragonCurseChronicles.MODID);
    
    // 牛之力
    public static final RegistryObject<MobEffect> COW_POWER = EFFECTS.register("cow_power", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xFF0000) {
        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            // 增加攻击力、防御力、移动速度、攻击范围
        }
        
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }
    });
    
    // 虎之力
    public static final RegistryObject<MobEffect> TIGER_POWER = EFFECTS.register("tiger_power", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xFF0000) {
        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            // 增加攻击力、防御力、伤害吸收
        }
        
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }
    });
    
    // 兔之力
    public static final RegistryObject<MobEffect> RABBIT_POWER = EFFECTS.register("rabbit_power", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xFF0000) {
        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            // 增加移动速度
        }
        
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }
    });
    
    // 蛇之力
    public static final RegistryObject<MobEffect> SNAKE_POWER = EFFECTS.register("snake_power", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xFF0000) {
        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            // 增强隐身效果
        }
        
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }
    });
    
    // 马之力
    public static final RegistryObject<MobEffect> HORSE_POWER = EFFECTS.register("horse_power", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xFF0000) {
        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            // 治愈效果
        }
        
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }
    });
    
    // 鸡之力
    public static final RegistryObject<MobEffect> CHICKEN_POWER = EFFECTS.register("chicken_power", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xFF0000) {
        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            // 飞行效果
        }
        
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }
    });
    
    // 狗之力
    public static final RegistryObject<MobEffect> DOG_POWER = EFFECTS.register("dog_power", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xFF0000) {
        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            // 永生效果
        }
        
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }
    });
    
    // 羊之力 - 灵魂出窍（包含隐身、夜视、飞行、穿墙等效果）
    public static final RegistryObject<MobEffect> SHEEP_POWER = EFFECTS.register("sheep_power", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xFFFFFF) {
        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            if (entity instanceof Player player) {
                // 穿墙能力
                player.noPhysics = true;
                
                // 无敌状态
                player.setInvulnerable(true);
                
                // 设置飞行模式（持续保持）
                Abilities abilities = player.getAbilities();
                if (!abilities.mayfly) {
                    abilities.mayfly = true;
                }
                if (!abilities.flying) {
                    abilities.flying = true;
                }
                // 使用配置文件中的飞行速度（通过反射访问私有字段）
                try {
                    float speed = (float) TalismanConfig.getSheepTalismanFlySpeed();
                    
                    // 设置飞行速度
                    Field flyingSpeedField = Abilities.class.getDeclaredField("flyingSpeed");
                    flyingSpeedField.setAccessible(true);
                    flyingSpeedField.set(abilities, speed);
                    
                    // 设置行走速度（确保疾跑时速度正确）
                    Field walkSpeedField = Abilities.class.getDeclaredField("walkSpeed");
                    walkSpeedField.setAccessible(true);
                    walkSpeedField.set(abilities, speed);
                    
                    // 直接设置玩家的移动速度属性，确保即时生效
                    player.setSpeed(1.0f);
                    
                    // 设置玩家为在地面上，确保疾跑可以生效
                    player.setOnGround(true);
                } catch (Exception e) {
                    DragonCurseChronicles.LOGGER.warn("Failed to set movement speed", e);
                }
                player.onUpdateAbilities();
                
                // 清除掉落伤害
                player.fallDistance = 0;
            }
        }
        
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }
    });
}
