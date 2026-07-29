package com.qituo.dcc;

import com.qituo.dcc.config.MeteorShowerConfig;
import com.qituo.dcc.config.TalismanConfig;
import com.qituo.dcc.effects.TalismanEffects;
import com.qituo.dcc.enchantments.ModEnchantments;
import com.qituo.dcc.enchantments.OriginPowerEnchantment;
import com.qituo.dcc.entity.EntityTypes;
import com.qituo.dcc.TalismanItems;
import com.qituo.dcc.sounds.ModSounds;
import com.qituo.dcc.talismans.ChickenTalisman;
import com.qituo.dcc.talismans.CowTalisman;
import com.qituo.dcc.talismans.DogTalisman;
import com.qituo.dcc.talismans.DragonTalisman;
import com.qituo.dcc.talismans.HorseTalisman;
import com.qituo.dcc.talismans.MonkeyTalisman;
import com.qituo.dcc.talismans.MouseTalisman;
import com.qituo.dcc.talismans.PigTalisman;
import com.qituo.dcc.talismans.RabbitTalisman;
import com.qituo.dcc.talismans.SheepTalisman;
import com.qituo.dcc.talismans.SnakeTalisman;
import com.qituo.dcc.talismans.TigerTalisman;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.world.World;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 龙咒异闻录 主类（Fabric 版本）
 *
 * 原理：
 * 1. Forge 用 @Mod 注解 + 构造函数，Fabric 改为 ModInitializer.onInitialize()。
 * 2. Forge 的 DeferredRegister 在主类构造时注册，Fabric 改为直接在 onInitialize 中静态注册。
 * 3. Forge 的 MinecraftForge.EVENT_BUS.addListener 改为 Fabric API 回调注册。
 * 4. Forge 的 AddReloadListenerEvent 改为 ServerLifecycleEvents 或自定义资源重载。
 * 5. Forge 的 RegisterCommandsEvent 改为 CommandRegistrationCallback。
 */
public class DragonCurseChronicles implements ModInitializer {
    public static final String MOD_ID = "dcc";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    // 允许符咒提取的游戏规则
    public static final net.minecraft.world.GameRules.Key<net.minecraft.world.GameRules.BooleanRule> RULE_ALLOW_TALISMAN_EXTRACTION =
            net.minecraft.world.GameRules.register("allowTalismanExtraction",
                    net.minecraft.world.GameRules.Category.MISC,
                    net.minecraft.world.GameRules.BooleanRule.create(true));

    @Override
    public void onInitialize() {
        // 加载配置
        MeteorShowerConfig.loadConfig();
        TalismanConfig.loadConfig();

        // 注册系统（物品、附魔、效果、实体、声音等）
        TalismanItems.initialize();
        TalismanCreativeTab.initialize();
        TalismanEffects.initialize();
        ModEnchantments.initialize();
        OriginPowerEnchantment.init();
        EntityTypes.initialize();
        ModSounds.initialize();

        // 注册命令
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            com.qituo.dcc.commands.TalismanConfigCommand.register(dispatcher);
            com.qituo.dcc.commands.MeteorShowerCommand.register(dispatcher);
        });

        // 服务器 tick 回调（替代 Forge 的 TickEvent.ServerTickEvent）
        // Fabric的END_SERVER_TICK提供MinecraftServer，需遍历世界获取ServerWorld
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ServerWorld overworld = server.getWorld(World.OVERWORLD);
            if (overworld != null) {
                com.qituo.dcc.events.MeteorShowerEvent.onServerTick(overworld);
                com.qituo.dcc.events.TalismanEffectsEvent.onServerTick(overworld);
            }
        });

        // 服务器启动事件（用于资源重载）
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            com.qituo.dcc.items.TalismanExtractorItem.reloadConfigs();
            TalismanConfig.reloadConfig();
            LOGGER.info("Reloaded Talisman Power Extractor configuration");
        });

        LOGGER.info("Dragon Curse Chronicles initialized");
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}
