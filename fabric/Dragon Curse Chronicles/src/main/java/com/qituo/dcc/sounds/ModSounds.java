package com.qituo.dcc.sounds;

import com.qituo.dcc.DragonCurseChronicles;
import net.minecraft.sound.SoundEvent;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModSounds {

    // 注册老爹的河豚干施法音效
    public static final SoundEvent MADGAQ = Registry.register(Registries.SOUND_EVENT,
            DragonCurseChronicles.id("madgaq"),
            SoundEvent.of(DragonCurseChronicles.id("madgaq"))
    );

    // 注册激光音效
    public static final SoundEvent LASER = Registry.register(Registries.SOUND_EVENT,
            DragonCurseChronicles.id("laser"),
            SoundEvent.of(DragonCurseChronicles.id("laser"))
    );

    public static void initialize() {
    }
}