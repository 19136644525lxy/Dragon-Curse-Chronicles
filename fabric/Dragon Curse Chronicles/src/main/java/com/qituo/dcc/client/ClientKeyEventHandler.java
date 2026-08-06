package com.qituo.dcc.client;

import com.qituo.dcc.network.ModNetwork;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;

/**
 * 客户端按键事件处理（Fabric 版）
 *
 * 职责：检测按键按下，并通过网络包通知服务端切换光环状态
 * 原理：按键状态仅在客户端可见，必须发包到服务端才能修改持久化数据
 */
public class ClientKeyEventHandler {

    /**
     * 在 ClientInit.onInitializeClient 中调用，注册客户端 tick 回调
     */
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (ModKeyBindings.TOGGLE_AURA == null) return;

            // wasPressed() 返回 true 表示按键自上次tick以来被按下过一次（单击触发）
            if (ModKeyBindings.TOGGLE_AURA.wasPressed()) {
                // 发送切换光环开关的网络包到服务端（空包，仅作触发信号）
                ClientPlayNetworking.send(ModNetwork.TOGGLE_AURA_ID,
                        new PacketByteBuf(Unpooled.buffer()));
            }
        });
    }
}
