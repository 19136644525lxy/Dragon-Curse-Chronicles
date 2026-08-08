package com.qituo.dcc.util;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.RelativeMovement;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class NetHandlerPlayServerFake extends ServerGamePacketListenerImpl {
    public static final Connection DUMMY_CONNECTION;
    
    // TODO 1.21迁移：ServerGamePacketListenerImpl 构造签名变更，后续补齐 CommonListenerCookie 参数
    public NetHandlerPlayServerFake(MinecraftServer server, net.minecraft.server.level.ServerPlayer playerIn) {
        // super(server, DUMMY_CONNECTION, playerIn);
        super(server, DUMMY_CONNECTION, playerIn, null);
        try {
            throw new UnsupportedOperationException("not ported yet");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void send(final Packet<?> packetIn) {
    }
    
    public void send(Packet<?> packet, @Nullable PacketSendListener sendListener) {
    }
    
    @Override
    public void disconnect(Component message) {
    }
    
    @Override
    public void teleport(double d, double e, double f, float g, float h, Set<RelativeMovement> set) {
        super.teleport(d, e, f, g, h, set);
        if (player.serverLevel().getPlayerByUUID(player.getUUID()) != null) {
            resetPosition();
            player.serverLevel().getChunkSource().move(player);
        }
    }
    
    static {
        DUMMY_CONNECTION = new FakeClientConnection(PacketFlow.SERVERBOUND);
    }
}