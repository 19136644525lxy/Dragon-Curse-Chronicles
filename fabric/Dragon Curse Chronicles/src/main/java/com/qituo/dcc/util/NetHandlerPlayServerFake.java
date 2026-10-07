package com.qituo.dcc.util;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * 假玩家服务端网络处理器（Fabric 版）
 *
 * 原理与适配：
 * 1. Forge 继承 ServerGamePacketListenerImpl，Fabric Yarn 改为 ServerPlayNetworkHandler。
 * 2. 类型映射：Connection→ClientConnection, PacketFlow→NetworkSide,
 *    PacketSendListener→PacketCallbacks, RelativeMovement→PositionFlag,
 *    ServerPlayer→ServerPlayerEntity, Component→Text, Packet<?> 包路径变更。
 * 3. 方法映射：teleport→requestTeleport, player.getUUID()→player.getUuid(),
 *    level.getPlayerByUUID→server.getPlayerManager().getPlayer,
 *    level.getChunkSource().move→world.getChunkManager().updatePosition,
 *    Forge 的 resetPosition() 在 Yarn 无直接对应，用 player.refreshPositionAndAngles 等价替代。
 * 4. 仍用反射注入 FakeClientConnection，避免真实网络 IO。
 */
public class NetHandlerPlayServerFake extends ServerPlayNetworkHandler {
    public static final ClientConnection DUMMY_CONNECTION;

    public NetHandlerPlayServerFake(MinecraftServer server, ServerPlayerEntity playerIn) {
        super(server, DUMMY_CONNECTION, playerIn);
    }

    // Forge: send(Packet<?>) → Yarn: sendPacket(Packet<?>)
    @Override
    public void sendPacket(Packet<?> packet) {
    }

    public void sendPacket(Packet<?> packet, @Nullable PacketCallbacks callbacks) {
    }

    @Override
    public void disconnect(Text message) {
    }

    @Override
    public void requestTeleport(double d, double e, double f, float g, float h, Set<PositionFlag> set) {
        super.requestTeleport(d, e, f, g, h, set);
        // Forge: player.serverLevel().getPlayerByUUID(player.getUUID())
        // Fabric: 通过 PlayerManager 查找在线玩家，确认玩家仍在线后再同步位置
        if (player.getServer().getPlayerManager().getPlayer(player.getUuid()) != null) {
            // Forge: resetPosition() —— Yarn 无直接对应方法，用 refreshPositionAndAngles 等价替代
            player.refreshPositionAndAngles(
                    player.getX(), player.getY(), player.getZ(),
                    player.getYaw(), player.getPitch());
            // Forge: player.serverLevel().getChunkSource().move(player)
            // Fabric: player.getServerWorld().getChunkManager().updatePosition(player)
            player.getServerWorld().getChunkManager().updatePosition(player);
        }
    }

    static {
        // Forge: PacketFlow.SERVERBOUND → Fabric: NetworkSide.SERVERBOUND
        DUMMY_CONNECTION = new FakeClientConnection(NetworkSide.SERVERBOUND);
    }
}
