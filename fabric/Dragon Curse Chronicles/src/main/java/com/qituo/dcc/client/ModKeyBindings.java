package com.qituo.dcc.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

/**
 * 模组按键绑定注册（Fabric 版）
 *
 * 当前注册：
 * - TOGGLE_AURA：切换始源光环开关（默认无按键绑定，玩家可在控制设置中自行绑定）
 *
 * 原理：KeyBinding 构造时传入 InputUtil.UNKNOWN_KEY.getCode()（值为 -1）即为"默认无绑定"，
 * 启动后玩家在"选项→控制→按键绑定"中找到对应类别即可手动绑定按键。
 *
 * 注意：Fabric 使用 KeyBindingHelper.registerKeyBinding() 注册按键，
 * 而非 Forge 的 RegisterKeyMappingsEvent 事件。
 */
public class ModKeyBindings {
    /** 按键分类键（语言文件中的 key.categories.dcc） */
    public static final String CATEGORY_KEY = "key.categories.dcc";

    /** 切换始源光环按键（默认无绑定） */
    public static KeyBinding TOGGLE_AURA;

    /**
     * 在 ClientInit.onInitializeClient 中调用，注册按键绑定
     */
    public static void register() {
        TOGGLE_AURA = new KeyBinding(
                "key.dcc.toggle_aura",
                InputUtil.UNKNOWN_KEY.getCode(), // 默认无按键绑定
                CATEGORY_KEY
        );
        KeyBindingHelper.registerKeyBinding(TOGGLE_AURA);
    }
}
