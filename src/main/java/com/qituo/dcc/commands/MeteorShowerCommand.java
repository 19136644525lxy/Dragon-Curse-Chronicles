package com.qituo.dcc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var meteorShower = Commands.literal("meteorshower");

        // 管理员指令（需权限2）
        meteorShower.then(Commands.literal("start")
            .requires(source -> source.hasPermission(2))
            .executes(MeteorShowerCommand::startMeteorShower));

        // 状态查询（无需权限）
        meteorShower.then(Commands.literal("status")
            .executes(MeteorShowerCommand::checkStatus));

        // 玩家确认/取消（无需权限，由 ClickEvent 触发）
        meteorShower.then(Commands.literal("confirm")
            .executes(MeteorShowerCommand::confirmMeteorShower));

        meteorShower.then(Commands.literal("cancel")
            .executes(MeteorShowerCommand::cancelMeteorShower));

        // 配置管理（需权限2）
        meteorShower.then(Commands.literal("config")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("enable")
                .executes(MeteorShowerCommand::enableMeteorShower))
            .then(Commands.literal("disable")
                .executes(MeteorShowerCommand::disableMeteorShower))
            .then(Commands.literal("get")
                .executes(MeteorShowerCommand::getConfigStatus))
            .then(Commands.literal("probability")
                .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 1.0))
                    .executes(MeteorShowerCommand::setProbability)))
            .then(Commands.literal("cube")
                .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 1.0))
                    .executes(MeteorShowerCommand::setCubeProbability))));

        dispatcher.register(meteorShower);
    }

    private static int startMeteorShower(CommandContext<CommandSourceStack> context) {
        if (!context.getSource().isPlayer()) {
            context.getSource().sendFailure(Component.translatable("dcc.message.meteor_shower.command.dimension_blocked"));
            return 0;
        }

        net.minecraft.world.level.Level playerLevel = context.getSource().getPlayer().level();
        if (!playerLevel.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) {
            context.getSource().sendFailure(Component.translatable("dcc.message.meteor_shower.command.dimension_blocked"));
            return 0;
        }

        ServerLevel level = context.getSource().getServer().getLevel(net.minecraft.world.level.Level.OVERWORLD);
        if (level != null) {
            if (MeteorShowerEvent.canStartMeteorShower()) {
                MeteorShowerEvent.forceStartMeteorShower(level);
                context.getSource().sendSuccess(() -> Component.translatable("dcc.message.meteor_shower.command.started"), true);
                return 1;
            } else {
                context.getSource().sendFailure(Component.translatable("dcc.message.meteor_shower.command.already_active"));
                return 0;
            }
        }
        context.getSource().sendFailure(Component.translatable("dcc.message.meteor_shower.command.error"));
        return 0;
    }

    private static int checkStatus(CommandContext<CommandSourceStack> context) {
        if (MeteorShowerEvent.isMeteorShowerActive()) {
            context.getSource().sendSuccess(() -> Component.translatable("dcc.message.meteor_shower.command.status.active"), false);
        } else if (MeteorShowerEvent.isPendingConfirmation()) {
            ServerPlayer pending = MeteorShowerEvent.getPendingPlayer();
            String name = pending != null ? pending.getName().getString() : "Unknown";
            context.getSource().sendSuccess(() -> Component.translatable(
                    "dcc.message.meteor_shower.command.status.pending", name), false);
        } else {
            context.getSource().sendSuccess(() -> Component.translatable("dcc.message.meteor_shower.command.status.inactive"), false);
        }
        return 1;
    }

    private static int confirmMeteorShower(CommandContext<CommandSourceStack> context) {
        if (!context.getSource().isPlayer()) {
            context.getSource().sendFailure(Component.translatable("dcc.message.meteor_shower.command.player_only"));
            return 0;
        }

        ServerPlayer player = context.getSource().getPlayer();
        if (MeteorShowerEvent.confirmMeteorShower(player)) {
            context.getSource().sendSuccess(() -> Component.translatable("dcc.message.meteor_shower.command.confirmed"), true);
            return 1;
        } else {
            context.getSource().sendFailure(Component.translatable("dcc.message.meteor_shower.command.no_pending"));
            return 0;
        }
    }

    private static int cancelMeteorShower(CommandContext<CommandSourceStack> context) {
        if (!context.getSource().isPlayer()) {
            context.getSource().sendFailure(Component.translatable("dcc.message.meteor_shower.command.player_only"));
            return 0;
        }

        ServerPlayer player = context.getSource().getPlayer();
        if (MeteorShowerEvent.cancelMeteorShower(player)) {
            context.getSource().sendSuccess(() -> Component.translatable("dcc.message.meteor_shower.command.cancelled"), true);
            return 1;
        } else {
            context.getSource().sendFailure(Component.translatable("dcc.message.meteor_shower.command.no_pending"));
            return 0;
        }
    }

    private static int enableMeteorShower(CommandContext<CommandSourceStack> context) {
        MeteorShowerConfig.setMeteorShowerEnabled(true);
        context.getSource().sendSuccess(() -> Component.translatable("dcc.message.meteor_shower.command.config.enabled"), true);
        return 1;
    }

    private static int disableMeteorShower(CommandContext<CommandSourceStack> context) {
        MeteorShowerConfig.setMeteorShowerEnabled(false);
        context.getSource().sendSuccess(() -> Component.translatable("dcc.message.meteor_shower.command.config.disabled"), true);
        return 1;
    }

    private static int getConfigStatus(CommandContext<CommandSourceStack> context) {
        boolean enabled = MeteorShowerConfig.isMeteorShowerEnabled();
        double prob = MeteorShowerConfig.getTriggerProbability();
        double cubeProb = MeteorShowerConfig.getCubeSpawnProbability();
        context.getSource().sendSuccess(() -> Component.translatable(
            "dcc.message.meteor_shower.command.config.full_status",
            enabled ? Component.translatable("dcc.message.meteor_shower.command.config.enabled_short")
                    : Component.translatable("dcc.message.meteor_shower.command.config.disabled_short"),
            String.format("%.0f%%", prob * 100),
            String.format("%.0f%%", cubeProb * 100)
        ), false);
        return 1;
    }

    private static int setProbability(CommandContext<CommandSourceStack> context) {
        double value = DoubleArgumentType.getDouble(context, "value");
        MeteorShowerConfig.setTriggerProbability(value);
        context.getSource().sendSuccess(() -> Component.translatable(
            "dcc.message.meteor_shower.command.config.probability_set", String.format("%.0f%%", value * 100)), true);
        return 1;
    }

    private static int setCubeProbability(CommandContext<CommandSourceStack> context) {
        double value = DoubleArgumentType.getDouble(context, "value");
        MeteorShowerConfig.setCubeSpawnProbability(value);
        context.getSource().sendSuccess(() -> Component.translatable(
            "dcc.message.meteor_shower.command.config.cube_set", String.format("%.0f%%", value * 100)), true);
        return 1;
    }
}
