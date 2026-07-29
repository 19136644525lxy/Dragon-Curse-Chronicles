package com.qituo.dcc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import com.qituo.dcc.config.MeteorShowerConfig;
import com.qituo.dcc.events.MeteorShowerEvent;

public class MeteorShowerCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var meteorShower = CommandManager.literal("meteorshower")
            .requires(source -> source.hasPermissionLevel(2));

        meteorShower.then(CommandManager.literal("start")
            .executes(MeteorShowerCommand::startMeteorShower));

        meteorShower.then(CommandManager.literal("status")
            .executes(MeteorShowerCommand::checkStatus));

        meteorShower.then(CommandManager.literal("config")
            .then(CommandManager.literal("enable")
                .executes(MeteorShowerCommand::enableMeteorShower))
            .then(CommandManager.literal("disable")
                .executes(MeteorShowerCommand::disableMeteorShower))
            .then(CommandManager.literal("get")
                .executes(MeteorShowerCommand::getConfigStatus)));

        dispatcher.register(meteorShower);
    }

    private static int startMeteorShower(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (source.getPlayer() == null) {
            source.sendError(Text.translatable("dcc.message.meteor_shower.command.dimension_blocked"));
            return 0;
        }

        World playerWorld = source.getPlayer().getWorld();
        if (playerWorld.getRegistryKey() != World.OVERWORLD) {
            source.sendError(Text.translatable("dcc.message.meteor_shower.command.dimension_blocked"));
            return 0;
        }

        net.minecraft.server.world.ServerWorld level = source.getServer().getWorld(World.OVERWORLD);
        if (level != null) {
            if (MeteorShowerEvent.canStartMeteorShower()) {
                MeteorShowerEvent.forceStartMeteorShower(level);
                source.sendFeedback(() -> Text.translatable("dcc.message.meteor_shower.command.started"), true);
                return 1;
            } else {
                source.sendError(Text.translatable("dcc.message.meteor_shower.command.already_active"));
                return 0;
            }
        }
        source.sendError(Text.translatable("dcc.message.meteor_shower.command.error"));
        return 0;
    }

    private static int checkStatus(CommandContext<ServerCommandSource> context) {
        if (MeteorShowerEvent.isMeteorShowerActive()) {
            context.getSource().sendFeedback(() -> Text.translatable("dcc.message.meteor_shower.command.status.active"), false);
        } else {
            context.getSource().sendFeedback(() -> Text.translatable("dcc.message.meteor_shower.command.status.inactive"), false);
        }
        return 1;
    }

    private static int enableMeteorShower(CommandContext<ServerCommandSource> context) {
        MeteorShowerConfig.setMeteorShowerEnabled(true);
        context.getSource().sendFeedback(() -> Text.translatable("dcc.message.meteor_shower.command.config.enabled"), true);
        return 1;
    }

    private static int disableMeteorShower(CommandContext<ServerCommandSource> context) {
        MeteorShowerConfig.setMeteorShowerEnabled(false);
        context.getSource().sendFeedback(() -> Text.translatable("dcc.message.meteor_shower.command.config.disabled"), true);
        return 1;
    }

    private static int getConfigStatus(CommandContext<ServerCommandSource> context) {
        boolean enabled = MeteorShowerConfig.isMeteorShowerEnabled();
        context.getSource().sendFeedback(() -> Text.translatable(
            "dcc.message.meteor_shower.command.config.status",
            enabled ? Text.translatable("dcc.message.meteor_shower.command.config.enabled_short")
                    : Text.translatable("dcc.message.meteor_shower.command.config.disabled_short")
        ), false);
        return 1;
    }
}
