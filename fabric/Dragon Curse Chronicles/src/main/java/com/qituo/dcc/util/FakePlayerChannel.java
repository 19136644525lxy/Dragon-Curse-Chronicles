package com.qituo.dcc.util;

import io.netty.channel.embedded.EmbeddedChannel;

/**
 * 假玩家通道（Fabric 版）
 *
 * 原理：基于 Netty 的 EmbeddedChannel，平台无关。
 * Forge 与 Fabric 共用同一实现，无需适配。
 * 用于 FakeClientConnection 模拟网络通道，避免真实网络 IO。
 */
public class FakePlayerChannel extends EmbeddedChannel {
    @Override
    protected void handleOutboundMessage(Object msg) {
        this.flushOutbound();
    }

    @Override
    protected void handleInboundMessage(Object msg) {
        this.flushInbound();
    }
}
