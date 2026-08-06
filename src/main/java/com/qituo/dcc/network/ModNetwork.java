package com.qituo.dcc.network;

import com.qituo.dcc.DragonCurseChronicles;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

/**
 * 模组网络通道
 *
 * 当前注册消息：
 * - ToggleAuraMessage：客户端→服务端，切换始源光环开关
 *
 * 状态存储：使用 Forge 的 player.getPersistentData()（持久化，随玩家存档保存）
 * Key: "dcc_aura_disabled"
 * - 不存在/false = 光环启用（默认行为，保持原有体验）
 * - true = 光环禁用
 */
public class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";

    /** 主通道 */
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(DragonCurseChronicles.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    /** 玩家持久化数据中的光环禁用标志键名 */
    public static final String TAG_AURA_DISABLED = "dcc_aura_disabled";

    private static int nextId = 0;

    /** 在主类构造时调用，注册所有消息 */
    public static void register() {
        CHANNEL.registerMessage(nextId++,
                ToggleAuraMessage.class,
                ToggleAuraMessage::encode,
                ToggleAuraMessage::decode,
                ToggleAuraMessage::handle);
    }

    /**
     * 切换始源光环开关的消息
     * 空包（无字段），仅作为触发信号
     */
    public static class ToggleAuraMessage {
        public ToggleAuraMessage() {}

        public static void encode(ToggleAuraMessage msg, FriendlyByteBuf buf) {
            // 无数据需要编码
        }

        public static ToggleAuraMessage decode(FriendlyByteBuf buf) {
            return new ToggleAuraMessage();
        }

        public static void handle(ToggleAuraMessage msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player == null) return;

                CompoundTag data = player.getPersistentData();
                boolean currentlyDisabled = data.getBoolean(TAG_AURA_DISABLED);
                boolean newState = !currentlyDisabled;
                data.putBoolean(TAG_AURA_DISABLED, newState);

                // 发送状态提示给玩家（action bar 显示）
                String key = newState ? "dcc.message.aura.disabled" : "dcc.message.aura.enabled";
                player.displayClientMessage(
                        Component.translatable(key).withStyle(ChatFormatting.GREEN), true);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
