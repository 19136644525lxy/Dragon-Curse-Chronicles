package com.qituo.dcc.damage;

import com.qituo.dcc.DragonCurseChronicles;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class DamageTypes {
    public static final RegistryKey<DamageType> ORIGIN_END_KEY = RegistryKey.of(
        RegistryKeys.DAMAGE_TYPE,
        new Identifier(DragonCurseChronicles.MOD_ID, DamagePresets.DAMAGE_TYPE_NAME)
    );
}
