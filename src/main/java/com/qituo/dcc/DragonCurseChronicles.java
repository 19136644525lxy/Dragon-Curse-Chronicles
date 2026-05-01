package com.qituo.dcc;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(DragonCurseChronicles.MODID)
public class DragonCurseChronicles {
    public static final String MODID = "dcc";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);

    // 游戏规则
    public static final GameRules.Key<GameRules.BooleanValue> RULE_ALLOW_TALISMAN_EXTRACTION = GameRules.register("allowTalismanExtraction", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }
    
    public DragonCurseChronicles() {
        // 确保配置文件在游戏加载前就生成
        com.qituo.dcc.config.TalismanConfig.loadConfig();
        
        TalismanItems.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
        TalismanCreativeTab.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
        com.qituo.dcc.effects.TalismanEffects.EFFECTS.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
        com.qituo.dcc.sounds.ModSounds.SOUNDS.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
        com.qituo.dcc.enchantments.ModEnchantments.ENCHANTMENTS.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
        com.qituo.dcc.entity.EntityTypes.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
        MENUS.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
        
        // 注册热重载监听器
        MinecraftForge.EVENT_BUS.addListener(this::onReload);
        
        // 注册命令
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }
    
    private void onReload(AddReloadListenerEvent event) {
        // 重新加载配置文件
        com.qituo.dcc.items.TalismanExtractorItem.reloadConfigs();
        com.qituo.dcc.config.TalismanConfig.reloadConfig();
        LOGGER.info("Reloaded Talisman Power Extractor configuration");
    }
    
    private void onRegisterCommands(RegisterCommandsEvent event) {
        com.qituo.dcc.commands.TalismanConfigCommand.register(event.getDispatcher());
    }
}
