package com.qituo.dcc;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(DragonCurseChronicles.MODID)
public class DragonCurseChronicles {
    public static final String MODID = "dcc";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MODID);

    public static final GameRules.Key<GameRules.BooleanValue> RULE_ALLOW_TALISMAN_EXTRACTION =
            GameRules.register("allowTalismanExtraction", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public DragonCurseChronicles(IEventBus modEventBus, ModContainer modContainer) {
        // 加载配置
        com.qituo.dcc.config.MeteorShowerConfig.loadConfig();
        com.qituo.dcc.config.TalismanConfig.loadConfig();

        // 注册网络（始源光环开关等，后续迁移 CustomPayload）
        try {
            com.qituo.dcc.network.ModNetwork.register();
        } catch (Throwable t) {
            LOGGER.warn("ModNetwork.register() not yet ported to NeoForge", t);
        }

        // 绑定所有 DeferredRegister 到 modEventBus
        TalismanItems.ITEMS.register(modEventBus);
        TalismanCreativeTab.CREATIVE_MODE_TABS.register(modEventBus);
        com.qituo.dcc.effects.TalismanEffects.EFFECTS.register(modEventBus);
        com.qituo.dcc.sounds.ModSounds.SOUNDS.register(modEventBus);
        com.qituo.dcc.enchantments.ModEnchantments.ENCHANTMENTS.register(modEventBus);
        com.qituo.dcc.entity.EntityTypes.ENTITY_TYPES.register(modEventBus);
        MENUS.register(modEventBus);
        try {
            com.qituo.dcc.recipes.ModRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        } catch (Throwable t) {
            LOGGER.warn("ModRecipeSerializers not yet ported", t);
        }

        // 全局事件总线订阅
        NeoForge.EVENT_BUS.addListener(this::onReload);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void onReload(AddReloadListenerEvent event) {
        try {
            com.qituo.dcc.items.TalismanExtractorItem.reloadConfigs();
        } catch (Throwable ignore) {}
        try {
            com.qituo.dcc.config.TalismanConfig.reloadConfig();
        } catch (Throwable ignore) {}
        LOGGER.info("Reloaded Talisman Power Extractor configuration");
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        try {
            com.qituo.dcc.commands.TalismanConfigCommand.register(event.getDispatcher());
        } catch (Throwable ignore) {}
        try {
            com.qituo.dcc.commands.MeteorShowerCommand.register(event.getDispatcher());
        } catch (Throwable ignore) {}
    }
}
