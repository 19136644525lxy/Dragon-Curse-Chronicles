package com.qituo.dcc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import com.qituo.dcc.events.MeteorShowerEvent;

public class MeteorShowerCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var meteorShower = Commands.literal("meteorshower")
            .requires(source -> source.hasPermission(2));
        
        meteorShower.then(Commands.literal("start")
            .executes(MeteorShowerCommand::startMeteorShower));
        
        meteorShower.then(Commands.literal("status")
            .executes(MeteorShowerCommand::checkStatus));
        
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
}
