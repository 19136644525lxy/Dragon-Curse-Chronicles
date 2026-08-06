package com.qituo.dcc.recipes;

import com.qituo.dcc.DragonCurseChronicles;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * 模组自定义配方序列化器注册（Fabric 版）
 * 当前仅注册：始源之力附魔书升级配方（4本N级→1本N+1级）
 *
 * 注意：Fabric 使用 Registry.register 直接注册，而非 Forge 的 DeferredRegister。
 */
public class ModRecipeSerializers {
    /** 始源之力附魔书升级配方序列化器 */
    public static final RecipeSerializer<OriginPowerBookUpgradeRecipe> ORIGIN_POWER_BOOK_UPGRADE =
            Registry.register(Registries.RECIPE_SERIALIZER,
                    new Identifier(DragonCurseChronicles.MOD_ID, OriginPowerBookUpgradeRecipe.TYPE_ID),
                    new OriginPowerBookUpgradeRecipe.Serializer());

    /**
     * 在主类 onInitialize 中调用（实际通过静态初始化已注册，此方法确保类被加载）
     */
    public static void initialize() {
        // 静态字段已触发注册，此方法仅作初始化入口
    }
}
