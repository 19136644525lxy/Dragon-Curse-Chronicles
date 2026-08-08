package com.qituo.dcc.client;

import com.qituo.dcc.DragonCurseChronicles;
import com.qituo.dcc.network.ModNetwork;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 客户端按键事件处理
 *
 * 职责：检测按键按下，并通过网络包通知服务端切换光环状态
 * 注意：按键状态仅在客户端可见，必须发包到服务端才能修改持久化数据
 */
@EventBusSubscriber(modid = DragonCurseChronicles.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientKeyEventHandler {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (ModKeyBindings.TOGGLE_AURA == null) return;

        // consumeClick() 返回 true 表示按键自上次tick以来被按下过一次（单击触发）
        if (ModKeyBindings.TOGGLE_AURA.consumeClick()) {
            PacketDistributor.sendToServer(new ModNetwork.ToggleAuraPayload());
        }
    }
}
