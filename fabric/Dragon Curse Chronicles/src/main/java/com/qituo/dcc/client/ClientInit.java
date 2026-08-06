package com.qituo.dcc.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

import com.qituo.dcc.entity.EntityTypes;
import com.qituo.dcc.client.renderer.DragonFireballRenderer;

/**
 * 客户端初始化器
 *
 * 原理：
 * 1. Forge 用 @Mod.EventBusSubscriber(bus = MOD, value = Dist.CLIENT) + FMLClientSetupEvent；
 *    Fabric 改为 ClientModInitializer.onInitializeClient()。
 * 2. EntityRenderers.register 改为 EntityRendererRegistry.register。
 */
@Environment(EnvType.CLIENT)
public class ClientInit implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // 注册自定义实体渲染器
        EntityRendererRegistry.register(EntityTypes.DRAGON_FIREBALL, DragonFireballRenderer::new);

        // 注册按键绑定（切换始源光环开关等）
        ModKeyBindings.register();

        // 注册客户端按键事件处理（检测按键按下并发包）
        ClientKeyEventHandler.register();
    }
}
