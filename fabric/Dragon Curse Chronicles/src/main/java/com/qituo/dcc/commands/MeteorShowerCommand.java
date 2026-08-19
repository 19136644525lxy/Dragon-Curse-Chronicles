package com.qituo.dcc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import com.qituo.dcc.config.MeteorShowerConfig;
import com.qituo.dcc.events.MeteorShowerEvent;

/**
 * 流星雨指令系统
 * /meteorshower start         - 管理员强制启动（需权限2）
 * /meteorshower status         - 查看事件状态
 * /meteorshower confirm        - 玩家确认启动（由 ClickEvent 触发，无需权限）
 * /meteorshower cancel         - 玩家取消启动（由 ClickEvent 触发，无需权限）
 * /meteorshower config enable  - 启用事件（需权限2）
 * /meteorshower config disable - 禁用事件（需权限2）
 * /meteorshower config get     - 查看配置（需权限2）
 * /meteorshower config probability <0.0-1.0> - 设置触发概率（需权限2）
 * /meteorshower config cube <0.0-1.0>       - 设置唐扇魔方概率（需权限2）
 */
public class MeteorShowerCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // 根节点不设权限限制，让 confirm/cancel 可被普通玩家执行
        var meteorShower = CommandManager.literal("meteorshower");

        // 管理员指令（需权限2）
        meteorShower.then(CommandManager.literal("start")
            .requires(source -> source.hasPermissionLevel(2))
            .executes(MeteorShowerCommand::startMeteorShower));

        // 状态查询（无需权限）
        meteorShower.then(CommandManager.literal("status")
            .executes(MeteorShowerCommand::checkStatus));

        // 玩家确认/取消（无需权限，由 ClickEvent 触发）
        meteorShower.then(CommandManager.literal("confirm")
            .executes(MeteorShowerCommand::confirmMeteorShower));

        meteorShower.then(CommandManager.literal("cancel")
            .executes(MeteorShowerCommand::cancelMeteorShower));

        // 配置管理（需权限2）
        meteorShower.then(CommandManager.literal("config")
            .requires(source -> source.hasPermissionLevel(2))
            .then(CommandManager.literal("enable")
                .executes(MeteorShowerCommand::enableMeteorShower))
            .then(CommandManager.literal("disable")
                .executes(MeteorShowerCommand::disableMeteorShower))
            .then(CommandManager.literal("get")
                .executes(MeteorShowerCommand::getConfigStatus))
            .then(CommandManager.literal("probability")
                .then(CommandManager.argument("value", DoubleArgumentType.doubleArg(0.0, 1.0))
                    .executes(MeteorShowerCommand::setProbability)))
            .then(CommandManager.literal("cube")
                .then(CommandManager.argument("value", DoubleArgumentType.doubleArg(0.0, 1.0))
                    .executes(MeteorShowerCommand::setCubeProbability))));

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

        ServerWorld level = source.getServer().getWorld(World.OVERWORLD);
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
        } else if (MeteorShowerEvent.isPendingConfirmation()) {
            var pending = MeteorShowerEvent.getPendingPlayer();
            String name = pending != null ? pending.getName().getString() : "Unknown";
            context.getSource().sendFeedback(() -> Text.translatable(
                    "dcc.message.meteor_shower.command.status.pending", name), false);
        } else {
            context.getSource().sendFeedback(() -> Text.translatable("dcc.message.meteor_shower.command.status.inactive"), false);
        }
        return 1;
    }

    private static int confirmMeteorShower(CommandContext<ServerCommandSource> context) {
        if (context.getSource().getPlayer() == null) {
            context.getSource().sendError(Text.translatable("dcc.message.meteor_shower.command.player_only"));
            return 0;
        }

        var player = context.getSource().getPlayer();
        if (MeteorShowerEvent.confirmMeteorShower(player)) {
            context.getSource().sendFeedback(() -> Text.translatable("dcc.message.meteor_shower.command.confirmed"), true);
            return 1;
        } else {
            context.getSource().sendError(Text.translatable("dcc.message.meteor_shower.command.no_pending"));
            return 0;
        }
    }

    private static int cancelMeteorShower(CommandContext<ServerCommandSource> context) {
        if (context.getSource().getPlayer() == null) {
            context.getSource().sendError(Text.translatable("dcc.message.meteor_shower.command.player_only"));
            return 0;
        }

        var player = context.getSource().getPlayer();
        if (MeteorShowerEvent.cancelMeteorShower(player)) {
            context.getSource().sendFeedback(() -> Text.translatable("dcc.message.meteor_shower.command.cancelled"), true);
            return 1;
        } else {
            context.getSource().sendError(Text.translatable("dcc.message.meteor_shower.command.no_pending"));
            return 0;
        }
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
        double prob = MeteorShowerConfig.getTriggerProbability();
        double cubeProb = MeteorShowerConfig.getCubeSpawnProbability();
        context.getSource().sendFeedback(() -> Text.translatable(
            "dcc.message.meteor_shower.command.config.full_status",
            enabled ? Text.translatable("dcc.message.meteor_shower.command.config.enabled_short")
                    : Text.translatable("dcc.message.meteor_shower.command.config.disabled_short"),
            String.format("%.0f%%", prob * 100),
            String.format("%.0f%%", cubeProb * 100)
        ), false);
        return 1;
    }

    private static int setProbability(CommandContext<ServerCommandSource> context) {
        double value = DoubleArgumentType.getDouble(context, "value");
        MeteorShowerConfig.setTriggerProbability(value);
        context.getSource().sendFeedback(() -> Text.translatable(
            "dcc.message.meteor_shower.command.config.probability_set", String.format("%.0f%%", value * 100)), true);
        return 1;
    }

    private static int setCubeProbability(CommandContext<ServerCommandSource> context) {
        double value = DoubleArgumentType.getDouble(context, "value");
        MeteorShowerConfig.setCubeSpawnProbability(value);
        context.getSource().sendFeedback(() -> Text.translatable(
            "dcc.message.meteor_shower.command.config.cube_set", String.format("%.0f%%", value * 100)), true);
        return 1;
    }
}
