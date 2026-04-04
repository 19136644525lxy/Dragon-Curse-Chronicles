package com.qituo.dcc.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.qituo.dcc.DragonCurseChronicles;

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
}