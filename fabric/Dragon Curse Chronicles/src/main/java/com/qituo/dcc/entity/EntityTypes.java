package com.qituo.dcc.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;

import com.qituo.dcc.DragonCurseChronicles;

public class EntityTypes {

    public static final EntityType<DragonFireball> DRAGON_FIREBALL = Registry.register(Registries.ENTITY_TYPE,
            DragonCurseChronicles.id("dragon_fireball"),
            FabricEntityTypeBuilder.<DragonFireball>create(SpawnGroup.MISC, DragonFireball::new)
                    .dimensions(EntityDimensions.fixed(1.0F, 1.0F))
                    .trackable(10, 4, true)
                    .build()
    );

    public static void initialize() {
    }
}
