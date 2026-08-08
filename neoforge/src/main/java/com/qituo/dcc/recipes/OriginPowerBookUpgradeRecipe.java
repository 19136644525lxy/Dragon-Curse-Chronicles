package com.qituo.dcc.recipes;

import com.mojang.serialization.MapCodec;
import com.qituo.dcc.enchantments.ModEnchantments;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;

/**
 * 始源之力附魔书升级配方
 *
 * 合成规则：4本相同等级（inputLevel）的始源之力附魔书 → 1本 inputLevel+1 等级的始源之力附魔书
 * 用途：实现 6-10级附魔书的合成获取途径（5级及以下可由附魔台/村民交易获得）
 *
 * 由于附魔书的等级存储在NBT中（StoredEnchantments），原版JSON配方无法匹配NBT也无法生成带特定等级结果，
 * 故自定义 Recipe 类在 matches() 中校验等级、在 assemble() 中写入等级。
 */
public class OriginPowerBookUpgradeRecipe extends ShapelessRecipe {
    /** 自定义配方类型ID */
    public static final String TYPE_ID = "origin_power_book_upgrade";

    /** 输入附魔书等级（4本均需为此等级） */
    private final int inputLevel;
    /** 输出附魔书等级（=inputLevel+1） */
    private final int outputLevel;

    public OriginPowerBookUpgradeRecipe(String group, CraftingBookCategory category,
                                        int inputLevel, int outputLevel,
                                        NonNullList<Ingredient> ingredients, ItemStack result) {
        super(group, category, result, ingredients);
        this.inputLevel = inputLevel;
        this.outputLevel = outputLevel;
    }

    public int getInputLevel() {
        return inputLevel;
    }

    public int getOutputLevel() {
        return outputLevel;
    }

    /**
     * 校验：4本相同等级的始源之力附魔书
     * 先用父类无序合成匹配（数量/形状），再逐个槽位检查NBT等级
     */
    @Override
    public boolean matches(CraftingInput container, Level level) {
        if (!super.matches(container, level)) {
            return false;
        }
        int matchedBooks = 0;
        for (int i = 0; i < container.size(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;
            // 必须是附魔书
            if (!(stack.getItem() instanceof EnchantedBookItem)) return false;
            // 必须是指定等级的始源之力附魔
            int enchantLevel = ModEnchantments.getOriginPowerLevel(stack);
            if (enchantLevel != inputLevel) return false;
            matchedBooks++;
        }
        // 必须正好4本
        return matchedBooks == 4;
    }

    /**
     * 生成带指定等级附魔的附魔书
     * 父类返回result.copy()（一本空白附魔书），此处向其写入NBT
     */
    @Override
    public ItemStack assemble(CraftingInput container, net.minecraft.core.HolderLookup.Provider provider) {
        ItemStack result = super.assemble(container, provider);
        // TODO 1.21迁移：Enchantment需Holder，暂跳过合成附魔，待JSON注册后补
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.ORIGIN_POWER_BOOK_UPGRADE.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }

    /**
     * 自定义序列化器：解析 input_level / output_level / ingredients / result
     */
    public static class Serializer implements RecipeSerializer<OriginPowerBookUpgradeRecipe> {
        @Override
        public MapCodec<OriginPowerBookUpgradeRecipe> codec() {
            throw new UnsupportedOperationException("codec暂未实现，待打磨");
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, OriginPowerBookUpgradeRecipe> streamCodec() {
            throw new UnsupportedOperationException("streamCodec暂未实现");
        }
    }
}
