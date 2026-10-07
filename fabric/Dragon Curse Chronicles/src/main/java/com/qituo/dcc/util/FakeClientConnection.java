package com.qituo.dcc.util;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;

/**
 * 假客户端连接（Fabric 版）
 *
 * 原理与适配：
 * 1. Forge 继承 Connection + PacketFlow；Fabric Yarn 改为 ClientConnection + NetworkSide。
 * 2. Forge 的 setReadOnly() 在 Yarn 中对应 disableAutoRead()；
 *    Forge 的 public channel() 在 Yarn 中无对应方法（channel 字段为 private），
 *    通过反射注入 FakePlayerChannel 即可，无需重写 channel()。
 * 3. 仍然提供空实现的 handleDisconnection()，与 Forge 保持语义一致。
 */
public class FakeClientConnection extends ClientConnection {
    public FakeClientConnection(NetworkSide side) {
        super(side);
        // 通过反射设置 channel 字段（Fabric Yarn 中字段名仍为 "channel"）
        try {
            java.lang.reflect.Field channelField = ReflectionCache.getField(ClientConnection.class, "channel");
            ReflectionCache.setFieldValue(this, channelField, new FakePlayerChannel());
        } catch (Exception e) {
            // 反射失败不影响主流程，记录日志后继续
            ExceptionHandler.handleReflectionException("设置channel字段", e);
        }
    }

    @Override
    public void disableAutoRead() {
        // 假连接不需要禁止读取，空实现保持连接可用
    }

    @Override
    public void handleDisconnection() {
        // 假连接无需处理断开
    }
}
