package com.qituo.dcc.client;

import com.qituo.dcc.DragonCurseChronicles;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * 模组按键绑定注册
 *
 * 当前注册：
 * - TOGGLE_AURA：切换始源光环开关（默认无按键绑定，玩家可在控制设置中自行绑定）
 *
 * 原理：KeyMapping 构造时第二个参数传入 -1（GLFW 无按键值）即为"默认无绑定"，
 * 启动后玩家在"选项→控制→按键绑定"中找到对应类别即可手动绑定按键。
 */
@EventBusSubscriber(modid = DragonCurseChronicles.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModKeyBindings {
    /** 按键分类键（语言文件中的 key.categories.dcc） */
    public static final String CATEGORY_KEY = "key.categories.dcc";

    /** 切换始源光环按键（默认无绑定，值为 -1） */
    public static KeyMapping TOGGLE_AURA;

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        TOGGLE_AURA = new KeyMapping(
                "key.dcc.toggle_aura",
                -1, // 默认无按键绑定，玩家可在控制设置中自行绑定
                CATEGORY_KEY
        );
        event.register(TOGGLE_AURA);
    }
}
