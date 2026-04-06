package com.qituo.dcc.damage;

import com.qituo.dcc.DragonCurseChronicles;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public class DamageTypes {
    // 创建伤害类型的资源键
    public static final ResourceKey<DamageType> ORIGIN_END_KEY = ResourceKey.create(
        Registries.DAMAGE_TYPE, 
        new ResourceLocation(DragonCurseChronicles.MODID, DamagePresets.DAMAGE_TYPE_NAME)
    );
}