package com.qituo.dcc.client;

import com.qituo.dcc.client.renderer.TalismanGlowRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/**
 * 符咒物品客户端扩展
 * 为所有符咒物品提供自定义 BEWLR 渲染器，实现生肖边缘光效
 *
 * 使用方式：TalismanBase.initializeClient() 中调用 consumer.accept(TalismanClientExtensions.INSTANCE)
 */
public class TalismanClientExtensions {

    private static TalismanGlowRenderer glowRenderer;

    /**
     * 获取共享的 BEWLR 实例（单例，避免重复创建）
     */
    private static BlockEntityWithoutLevelRenderer getGlowRenderer() {
        if (glowRenderer == null) {
            glowRenderer = new TalismanGlowRenderer();
        }
        return glowRenderer;
    }

    /**
     * IClientItemExtensions 实现
     * 返回自定义 BEWLR，接管符咒物品的渲染
     */
    public static final IClientItemExtensions INSTANCE = new IClientItemExtensions() {
        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            return getGlowRenderer();
        }
    };
}
