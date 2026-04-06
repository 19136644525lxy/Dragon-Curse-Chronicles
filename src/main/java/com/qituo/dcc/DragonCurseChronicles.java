package com.qituo.dcc;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;

@Mod(DragonCurseChronicles.MODID)
public class DragonCurseChronicles {
    public static final String MODID = "dcc";
    
    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }
    
    public DragonCurseChronicles() {
        TalismanItems.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
        TalismanCreativeTab.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
        com.qituo.dcc.effects.TalismanEffects.EFFECTS.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
        com.qituo.dcc.sounds.ModSounds.SOUNDS.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
        com.qituo.dcc.enchantments.ModEnchantments.ENCHANTMENTS.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
    }
}