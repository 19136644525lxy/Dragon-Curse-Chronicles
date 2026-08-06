package com.qituo.dcc.network;

import com.qituo.dcc.DragonCurseChronicles;
import com.qituo.dcc.util.PersistentDataAccess;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

/**
 * 模组网络通道（Fabric 版）
 *
 * 当前注册消息：
 * - ToggleAuraMessage：客户端→服务端，切换始源光环开关
 *
 * 状态存储：通过 PersistentDataAccess（Mixin 实现）存储在玩家持久化NBT中
 * Key: "dcc_aura_disabled"
 * - 不存在/false = 光环启用（默认行为）
 * - true = 光环禁用
 *
 * 原理：Fabric 使用 ServerPlayNetworking.registerGlobalReceiver 注册接收器，
 * 客户端通过 ClientPlayNetworking.send 发送自定义 payload。
 */
public class ModNetwork {
    /** 切换光环开关的网络包ID */
    public static final Identifier TOGGLE_AURA_ID =
            new Identifier(DragonCurseChronicles.MOD_ID, "toggle_aura");

    /** 玩家持久化数据中的光环禁用标志键名 */
    public static final String TAG_AURA_DISABLED = "dcc_aura_disabled";

    /**
     * 在主类 onInitialize 中调用，注册服务端网络接收器
     */
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(TOGGLE_AURA_ID,
                (server, player, handler, buf, responseSender) -> {
                    // 在服务端主线程执行切换逻辑
                    server.execute(() -> {
                        boolean currentlyDisabled = ((PersistentDataAccess) player)
                                .getDccPersistentData().getBoolean(TAG_AURA_DISABLED);
                        boolean newState = !currentlyDisabled;
                        ((PersistentDataAccess) player)
                                .getDccPersistentData().putBoolean(TAG_AURA_DISABLED, newState);

                        // 发送状态提示给玩家（action bar 显示）
                        String key = newState ? "dcc.message.aura.disabled" : "dcc.message.aura.enabled";
                        player.sendMessage(
                                Text.translatable(key).formatted(Formatting.GREEN), true);
                    });
                });
    }
}
