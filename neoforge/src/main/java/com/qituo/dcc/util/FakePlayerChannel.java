package com.qituo.dcc.util;

import io.netty.channel.embedded.EmbeddedChannel;

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