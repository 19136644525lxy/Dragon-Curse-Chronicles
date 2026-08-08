package com.qituo.dcc.recipes;

import com.qituo.dcc.DragonCurseChronicles;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.Registries;

/**
 * 模组自定义配方序列化器注册
 * 当前仅注册：始源之力附魔书升级配方（4本N级→1本N+1级）
 */
public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, DragonCurseChronicles.MODID);

    /** 始源之力附魔书升级配方序列化器 */
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<OriginPowerBookUpgradeRecipe>> ORIGIN_POWER_BOOK_UPGRADE =
            RECIPE_SERIALIZERS.register(OriginPowerBookUpgradeRecipe.TYPE_ID,
                    OriginPowerBookUpgradeRecipe.Serializer::new);
}
