package com.qituo.dcc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import com.qituo.dcc.config.MeteorShowerConfig;
import com.qituo.dcc.events.MeteorShowerEvent;

public class MeteorShowerCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var meteorShower = Commands.literal("meteorshower")
            .requires(source -> source.hasPermission(2));
        
        meteorShower.then(Commands.literal("start")
            .executes(MeteorShowerCommand::startMeteorShower));
        
        meteorShower.then(Commands.literal("status")
            .executes(MeteorShowerCommand::checkStatus));
        
        meteorShower.then(Commands.literal("config")
            .then(Commands.literal("enable")
                .executes(MeteorShowerCommand::enableMeteorShower))
            .then(Commands.literal("disable")
                .executes(MeteorShowerCommand::disableMeteorShower))
            .then(Commands.literal("get")
                .executes(MeteorShowerCommand::getConfigStatus)));
        
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
        } else {
            context.getSource().sendSuccess(() -> Component.translatable("dcc.message.meteor_shower.command.status.inactive"), false);
        }
        return 1;
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
        context.getSource().sendSuccess(() -> Component.translatable(
            "dcc.message.meteor_shower.command.config.status", 
            enabled ? Component.translatable("dcc.message.meteor_shower.command.config.enabled_short") 
                    : Component.translatable("dcc.message.meteor_shower.command.config.disabled_short")
        ), false);
        return 1;
    }
}
