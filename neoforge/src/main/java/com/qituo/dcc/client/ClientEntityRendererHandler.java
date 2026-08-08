package com.qituo.dcc.client;

import com.qituo.dcc.entity.EntityTypes;
import com.qituo.dcc.client.renderer.DragonFireballRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import com.qituo.dcc.DragonCurseChronicles;

@EventBusSubscriber(modid = DragonCurseChronicles.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEntityRendererHandler {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityTypes.DRAGON_FIREBALL.get(), DragonFireballRenderer::new);
    }
}