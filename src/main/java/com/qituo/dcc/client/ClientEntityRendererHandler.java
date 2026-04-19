package com.qituo.dcc.client;

import com.qituo.dcc.entity.EntityTypes;
import com.qituo.dcc.client.renderer.DragonFireballRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import com.qituo.dcc.DragonCurseChronicles;

@Mod.EventBusSubscriber(modid = DragonCurseChronicles.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEntityRendererHandler {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 注册自定义实体渲染器
        EntityRenderers.register(EntityTypes.DRAGON_FIREBALL.get(), DragonFireballRenderer::new);
    }
}