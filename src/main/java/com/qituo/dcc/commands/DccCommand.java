package com.qituo.dcc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.qituo.dcc.DragonCurseChronicles;
import com.qituo.dcc.events.MeteorShowerEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * 模组通用指令
 *
 * 当前提供：
 * - /dcc tp —— 传送到流星雨宝箱坐标
 *
 * 注意：本指令**故意不使用** .requires(hasPermission(2))。
 * 因为普通玩家点击聊天栏坐标时，若指令被 .requires 过滤掉，客户端只会提示"未知的指令"，
 * 无法给出"权限不足"的明确提示。因此这里在 executes 内部手动判定权限，
 * 并分别发送"权限不足"或"没有可传送坐标"的提示。
 */
public class DccCommand {

    /** 可传送流星雨坐标所需的权限等级（对应 OP / 作弊） */
    private static final int TELEPORT_PERMISSION_LEVEL = 2;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("dcc")
                .then(Commands.literal("tp")
                        .executes(DccCommand::teleportToMeteorShower)));
    }

    /**
     * 传送到流星雨宝箱坐标
     */
    private static int teleportToMeteorShower(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        // 仅限玩家执行
        if (!source.isPlayer()) {
            source.sendFailure(Component.translatable("dcc.command.dcc.tp.player_only"));
            return 0;
        }

        // 权限校验：不足时给出明确提示（而非"未知的指令"）
        if (!source.hasPermission(TELEPORT_PERMISSION_LEVEL)) {
            source.sendFailure(Component.translatable("dcc.command.dcc.tp.no_permission")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        // 必须有已记录的流星雨宝箱坐标
        BlockPos target = MeteorShowerEvent.getTreasureChestPos();
        if (target == null) {
            source.sendFailure(Component.translatable("dcc.command.dcc.tp.no_target"));
            return 0;
        }

        ServerPlayer player = source.getPlayer();
        ServerLevel overworld = source.getServer().getLevel(Level.OVERWORLD);
        if (player == null || overworld == null) {
            source.sendFailure(Component.translatable("dcc.command.dcc.tp.failed"));
            return 0;
        }

        try {
            // 落在宝箱上方一格，避免卡在方块里
            double x = target.getX() + 0.5D;
            double y = target.getY() + 1.0D;
            double z = target.getZ() + 0.5D;
            player.teleportTo(overworld, x, y, z, player.getYRot(), player.getXRot());
            source.sendSuccess(() -> Component.translatable(
                    "dcc.command.dcc.tp.success",
                    target.getX(), target.getY(), target.getZ()), false);
            return 1;
        } catch (Throwable e) {
            DragonCurseChronicles.LOGGER.error("流星雨坐标传送失败", e);
            source.sendFailure(Component.translatable("dcc.command.dcc.tp.failed"));
            return 0;
        }
    }

    /**
     * 构建带"点击传送"交互的坐标文本
     * 点击后执行 /dcc tp，由指令内部做权限判定与提示
     *
     * @param pos 流星雨宝箱坐标
     */
    public static MutableComponent buildClickableCoords(BlockPos pos) {
        MutableComponent coords = Component.literal(pos.getX() + ", " + pos.getY() + ", " + pos.getZ())
                .withStyle(style -> style
                        .withColor(ChatFormatting.AQUA)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/dcc tp"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("dcc.message.meteor_shower.teleport_hint")))
                );
        return coords;
    }
}
