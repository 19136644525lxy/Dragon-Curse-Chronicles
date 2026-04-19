package com.qituo.dcc.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.qituo.dcc.DragonCurseChronicles;

public class EntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, DragonCurseChronicles.MODID);

    public static final RegistryObject<EntityType<DragonFireball>> DRAGON_FIREBALL = ENTITY_TYPES.register(
        "dragon_fireball",
        () -> EntityType.Builder.<DragonFireball>of(DragonFireball::new, MobCategory.MISC)
            .sized(1.0F, 1.0F)
            .clientTrackingRange(4)
            .updateInterval(10)
            .fireImmune()
            .build("dragon_fireball")
    );

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}