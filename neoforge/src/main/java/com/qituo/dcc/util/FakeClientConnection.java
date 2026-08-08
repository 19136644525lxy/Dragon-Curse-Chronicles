package com.qituo.dcc.util;

import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;

public class FakeClientConnection extends Connection {
    public FakeClientConnection(PacketFlow p) {
        super(p);
        // 尝试使用反射设置channel字段
        try {
            java.lang.reflect.Field channelField = ReflectionCache.getField(Connection.class, "channel");
            ReflectionCache.setFieldValue(this, channelField, new FakePlayerChannel());
        } catch (Exception e) {
            // 忽略反射错误，继续执行
            ExceptionHandler.handleReflectionException("设置channel字段", e);
        }
    }
    
    @Override
    public void setReadOnly() {
    }
    
    @Override
    public void handleDisconnection() {
    }
    
    @Override
    public Channel channel() {
        // 重写channel方法，返回一个假的channel
        return new FakePlayerChannel();
    }
}