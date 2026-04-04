package com.qituo.dcc;

import net.minecraftforge.fml.common.Mod;

@Mod(DragonCurseChronicles.MODID)
public class DragonCurseChronicles {
    public static final String MODID = "dcc";
    
    public DragonCurseChronicles() {
        TalismanItems.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
        TalismanCreativeTab.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
        com.qituo.dcc.effects.TalismanEffects.EFFECTS.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
    }
}