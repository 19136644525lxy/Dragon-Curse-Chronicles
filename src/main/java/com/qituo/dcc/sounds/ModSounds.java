package com.qituo.dcc.sounds;

import com.qituo.dcc.DragonCurseChronicles;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, DragonCurseChronicles.MODID);
    
    // 注册老爹的河豚干施法音效
    public static final RegistryObject<SoundEvent> MADGAQ = SOUNDS.register("madgaq", 
        () -> SoundEvent.createVariableRangeEvent(DragonCurseChronicles.id("madgaq"))
    );
    
    // 注册激光音效
    public static final RegistryObject<SoundEvent> LASER = SOUNDS.register("laser", 
        () -> SoundEvent.createVariableRangeEvent(DragonCurseChronicles.id("laser"))
    );
}
