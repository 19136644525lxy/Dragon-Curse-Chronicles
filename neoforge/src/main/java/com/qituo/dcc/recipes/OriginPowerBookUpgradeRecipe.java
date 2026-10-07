package com.qituo.dcc.recipes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.qituo.dcc.enchantments.ModEnchantments;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * 始源之力附魔书升级配方（NeoForge 1.21.1）
 *
 * 合成规则：4 本相同等级（inputLevel）的始源之力附魔书 → 1 本 inputLevel+1 等级的始源之力附魔书
 * 用途：6-10 级附魔书的唯一获取途径（1-5 级可由附魔台/村民交易获得）
 *
 * 1.21 实现要点：
 * - 附魔存放在 DataComponents.STORED_ENCHANTMENTS（附魔书），通过 EnchantmentHelper 统一读写；
 * - 附魔是数据驱动的 Holder，assemble 时从 HolderLookup.Provider 解析，因此不需要在 codec 里解析；
 * - 序列化用 MapCodec + StreamCodec（1.21 取代了 1.20 的 fromJson/fromNetwork）。
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
     * 占位结果（空白附魔书）：仅用于序列化往返与配方书展示。
     * 真正带有 outputLevel 附魔的结果由 {@link #assemble} 生成。
     */
    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(Items.ENCHANTED_BOOK);
    }

    /**
     * 校验：恰好 4 本、且每本都是 inputLevel 级的始源之力附魔书
     * 先由父类完成无序合成的材料匹配，再逐槽校验附魔等级
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
            if (!stack.is(Items.ENCHANTED_BOOK)) return false;
            // 必须是指定等级的始源之力附魔
            if (ModEnchantments.getOriginPowerLevel(stack) != inputLevel) return false;
            matchedBooks++;
        }
        // 必须正好4本
        return matchedBooks == 4;
    }

    /**
     * 生成带 outputLevel 级始源之力附魔的附魔书
     * 结果由附魔 Holder 动态写入，不依赖 result 占位物品
     */
    @Override
    public ItemStack assemble(CraftingInput container, HolderLookup.Provider provider) {
        ItemStack result = new ItemStack(Items.ENCHANTED_BOOK);
        try {
            Holder<Enchantment> holder = provider.lookupOrThrow(Registries.ENCHANTMENT)
                    .getOrThrow(ModEnchantments.ORIGIN_POWER_KEY);
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            mutable.set(holder, outputLevel);
            // 附魔书走 STORED_ENCHANTMENTS，由 EnchantmentHelper.setEnchantments 自动选择组件类型
            EnchantmentHelper.setEnchantments(result, mutable.toImmutable());
        } catch (Throwable e) {
            com.qituo.dcc.DragonCurseChronicles.LOGGER.error(
                    "始源之力附魔书合成失败（附魔 {} 未解析）: {}",
                    ModEnchantments.ORIGIN_POWER_KEY.location(), e.toString());
        }
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
     * 自定义序列化器：解析 group / category / input_level / output_level / ingredients / result
     */
    public static class Serializer implements RecipeSerializer<OriginPowerBookUpgradeRecipe> {

        /** 仅用于读取占位结果物品；不涉及任何注册表查找 */
        private static final HolderLookup.Provider EMPTY_PROVIDER =
                HolderLookup.Provider.create(java.util.stream.Stream.empty());
        private static final MapCodec<OriginPowerBookUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        com.mojang.serialization.Codec.STRING.optionalFieldOf("group", "")
                                .forGetter(OriginPowerBookUpgradeRecipe::getGroup),
                        CraftingBookCategory.CODEC.fieldOf("category")
                                .orElse(CraftingBookCategory.MISC)
                                .forGetter(OriginPowerBookUpgradeRecipe::category),
                        com.mojang.serialization.Codec.INT.fieldOf("input_level")
                                .forGetter(OriginPowerBookUpgradeRecipe::getInputLevel),
                        com.mojang.serialization.Codec.INT.fieldOf("output_level")
                                .forGetter(OriginPowerBookUpgradeRecipe::getOutputLevel),
                        Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients")
                                .forGetter(r -> r.getIngredients()),
                        ItemStack.STRICT_CODEC.fieldOf("result")
                                .forGetter(r -> r.getResultItem(EMPTY_PROVIDER))
                ).apply(instance, (group, category, inputLevel, outputLevel, ingredients, result) ->
                        new OriginPowerBookUpgradeRecipe(group, category, inputLevel, outputLevel,
                                NonNullList.of(Ingredient.EMPTY, ingredients.toArray(Ingredient[]::new)), result))
        );

        private static final StreamCodec<RegistryFriendlyByteBuf, OriginPowerBookUpgradeRecipe> STREAM_CODEC =
                StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        @Override
        public MapCodec<OriginPowerBookUpgradeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, OriginPowerBookUpgradeRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static OriginPowerBookUpgradeRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
            int inputLevel = buffer.readVarInt();
            int outputLevel = buffer.readVarInt();
            int size = buffer.readVarInt();
            List<Ingredient> ingredients = new java.util.ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                ingredients.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
            }
            ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
            return new OriginPowerBookUpgradeRecipe(group, category, inputLevel, outputLevel,
                    NonNullList.of(Ingredient.EMPTY, ingredients.toArray(Ingredient[]::new)), result);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, OriginPowerBookUpgradeRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeEnum(recipe.category());
            buffer.writeVarInt(recipe.getInputLevel());
            buffer.writeVarInt(recipe.getOutputLevel());
            NonNullList<Ingredient> ingredients = recipe.getIngredients();
            buffer.writeVarInt(ingredients.size());
            for (Ingredient ingredient : ingredients) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
            }
            ItemStack.STREAM_CODEC.encode(buffer, recipe.getResultItem(EMPTY_PROVIDER));
        }
    }
}
