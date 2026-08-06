package com.qituo.dcc.client;

import com.qituo.dcc.DragonCurseChronicles;
import com.qituo.dcc.network.ModNetwork;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 客户端按键事件处理
 *
 * 职责：检测按键按下，并通过网络包通知服务端切换光环状态
 * 注意：按键状态仅在客户端可见，必须发包到服务端才能修改持久化数据
 */
@Mod.EventBusSubscriber(modid = DragonCurseChronicles.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientKeyEventHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (ModKeyBindings.TOGGLE_AURA == null) return;

        // consumeClick() 返回 true 表示按键自上次tick以来被按下过一次（单击触发）
        if (ModKeyBindings.TOGGLE_AURA.consumeClick()) {
            ModNetwork.CHANNEL.sendToServer(new ModNetwork.ToggleAuraMessage());
        }
    }
}
