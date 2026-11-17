package com.qituo.dcc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.qituo.dcc.DragonCurseChronicles;
import com.qituo.dcc.events.MeteorShowerEvent;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Set;

/**
 * 模组通用指令（Fabric 1.20.1）
 *
 * 当前提供：
 * - /dcc tp —— 传送到流星雨宝箱坐标
 *
 * 注意：本指令**故意不使用** .requires(hasPermissionLevel(2))。
 * 因为普通玩家点击聊天栏坐标时，若指令被 .requires 过滤掉，客户端只会提示"未知的指令"，
 * 无法给出"权限不足"的明确提示。因此这里在 executes 内部手动判定权限，
 * 并分别发送"权限不足"或"没有可传送坐标"的提示。
 */
public class DccCommand {

    /** 可传送流星雨坐标所需的权限等级（对应 OP / 作弊） */
    private static final int TELEPORT_PERMISSION_LEVEL = 2;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("dcc")
                .then(CommandManager.literal("tp")
                        .executes(DccCommand::teleportToMeteorShower)));
    }

    /**
     * 传送到流星雨宝箱坐标
     */
    private static int teleportToMeteorShower(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        // 仅限玩家执行
        if (!source.isExecutedByPlayer() || source.getPlayer() == null) {
            source.sendError(Text.translatable("dcc.command.dcc.tp.player_only"));
            return 0;
        }

        // 权限校验：不足时给出明确提示（而非"未知的指令"）
        if (!source.hasPermissionLevel(TELEPORT_PERMISSION_LEVEL)) {
            source.sendError(Text.translatable("dcc.command.dcc.tp.no_permission")
                    .formatted(Formatting.RED));
            return 0;
        }

        // 必须有已记录的流星雨宝箱坐标
        BlockPos target = MeteorShowerEvent.getTreasureChestPos();
        if (target == null) {
            source.sendError(Text.translatable("dcc.command.dcc.tp.no_target"));
            return 0;
        }

        ServerPlayerEntity player = source.getPlayer();
        if (player == null || source.getServer() == null) {
            source.sendError(Text.translatable("dcc.command.dcc.tp.failed"));
            return 0;
        }

        ServerWorld overworld = source.getServer().getWorld(World.OVERWORLD);
        if (overworld == null) {
            source.sendError(Text.translatable("dcc.command.dcc.tp.failed"));
            return 0;
        }

        try {
            // 落在宝箱上方一格，避免卡在方块里
            double x = target.getX() + 0.5D;
            double y = target.getY() + 1.0D;
            double z = target.getZ() + 0.5D;
            player.teleport(overworld, x, y, z, Set.<PositionFlag>of(), player.getYaw(), player.getPitch());
            source.sendFeedback(() -> Text.translatable(
                    "dcc.command.dcc.tp.success",
                    target.getX(), target.getY(), target.getZ()), false);
            return 1;
        } catch (Throwable e) {
            DragonCurseChronicles.LOGGER.error("流星雨坐标传送失败", e);
            source.sendError(Text.translatable("dcc.command.dcc.tp.failed"));
            return 0;
        }
    }

    /**
     * 构建带"点击传送"交互的坐标文本
     * 点击后执行 /dcc tp，由指令内部做权限判定与提示
     *
     * @param pos 流星雨宝箱坐标
     */
    public static MutableText buildClickableCoords(BlockPos pos) {
        return Text.literal(pos.getX() + ", " + pos.getY() + ", " + pos.getZ())
                .styled(style -> style
                        .withColor(Formatting.AQUA)
                        .withUnderline(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/dcc tp"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Text.translatable("dcc.message.meteor_shower.teleport_hint")))
                );
    }
}
