package com.qituo.dcc.sounds;

import com.qituo.dcc.DragonCurseChronicles;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.Registries;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, DragonCurseChronicles.MODID);
    
    // 注册老爹的河豚干施法音效
    public static final DeferredHolder<SoundEvent, SoundEvent> MADGAQ = SOUNDS.register("madgaq", 
        () -> SoundEvent.createVariableRangeEvent(DragonCurseChronicles.id("madgaq"))
    );
    
    // 注册激光音效
    public static final DeferredHolder<SoundEvent, SoundEvent> LASER = SOUNDS.register("laser", 
        () -> SoundEvent.createVariableRangeEvent(DragonCurseChronicles.id("laser"))
    );
}
