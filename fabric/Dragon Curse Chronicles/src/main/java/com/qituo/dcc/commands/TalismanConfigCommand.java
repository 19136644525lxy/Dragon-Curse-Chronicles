package com.qituo.dcc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.fabricmc.loader.api.FabricLoader;

import com.qituo.dcc.config.TalismanConfig;
import com.qituo.dcc.DragonCurseChronicles;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.io.File;

public class TalismanConfigCommand {
    private static final File CONFIG_DIR = new File(FabricLoader.getInstance().getConfigDir().toFile(), "dcc");
    private static final File CONFIG_FILE = new File(CONFIG_DIR, "talisman_config.properties");

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var talismanConfig = CommandManager.literal("talismanconfig").requires(source -> source.hasPermissionLevel(2));

        var flyspeed = CommandManager.literal("flyspeed");
        flyspeed.then(CommandManager.literal("get").executes(TalismanConfigCommand::getFlySpeed));
        flyspeed.then(CommandManager.literal("set").then(CommandManager.argument("speed", DoubleArgumentType.doubleArg(0.01, 10.0)).executes(TalismanConfigCommand::setFlySpeed)));

        talismanConfig.then(flyspeed);
        talismanConfig.then(CommandManager.literal("reload").executes(TalismanConfigCommand::reloadConfig));

        dispatcher.register(talismanConfig);
    }

    private static int setFlySpeed(CommandContext<ServerCommandSource> context) {
        double speed = DoubleArgumentType.getDouble(context, "speed");

        try {
            java.lang.reflect.Field field = TalismanConfig.class.getDeclaredField("sheepTalismanFlySpeed");
            field.setAccessible(true);
            field.set(null, speed);

            saveConfig(speed);

        } catch (Exception e) {
            context.getSource().sendError(Text.translatable("dcc.message.config.fails"));
            DragonCurseChronicles.LOGGER.error("Failed to set fly speed", e);
            return 0;
        }

        context.getSource().sendFeedback(() -> Text.translatable("dcc.message.config.flyspeed.set", speed), true);
        return 1;
    }

    private static void saveConfig(double flySpeed) throws Exception {
        if (!CONFIG_DIR.exists()) {
            CONFIG_DIR.mkdirs();
        }

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(CONFIG_FILE), StandardCharsets.UTF_8))) {
            writer.println("# ==========================================");
            writer.println("# Dragon Curse Chronicles - Talisman Config");
            writer.println("# ==========================================");
            writer.println("#");
            writer.println("# 羊符咒配置 (Sheep Talisman Configuration)");
            writer.println("#");
            writer.println("# sheep_talisman.fly_speed:");
            writer.println("#   - 羊符咒灵魂出窍模式下的飞行速度");
            writer.println("#   - 默认值: 0.5");
            writer.println("#   - 范围: 0.01 - 10.0");
            writer.println("#   - 创造模式默认飞行速度是 0.05");
            writer.println("#");
            writer.println("sheep_talisman.fly_speed=" + flySpeed);
        }
    }

    private static int getFlySpeed(CommandContext<ServerCommandSource> context) {
        double speed = TalismanConfig.getSheepTalismanFlySpeed();
        context.getSource().sendFeedback(() -> Text.translatable("dcc.message.config.flyspeed.get", speed), false);
        return 1;
    }

    private static int reloadConfig(CommandContext<ServerCommandSource> context) {
        TalismanConfig.reloadConfig();
        context.getSource().sendFeedback(() -> Text.translatable("dcc.message.config.reloaded"), true);
        return 1;
    }
}
