package com.qituo.dcc.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.qituo.dcc.DragonCurseChronicles;
import com.qituo.dcc.enchantments.ModEnchantments;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

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

    public OriginPowerBookUpgradeRecipe(ResourceLocation id, String group, CraftingBookCategory category,
                                        int inputLevel, int outputLevel,
                                        NonNullList<Ingredient> ingredients, ItemStack result) {
        super(id, group, category, result, ingredients);
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
    public boolean matches(CraftingContainer container, Level level) {
        if (!super.matches(container, level)) {
            return false;
        }
        int matchedBooks = 0;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;
            // 必须是附魔书
            if (!(stack.getItem() instanceof EnchantedBookItem)) return false;
            // 必须是指定等级的始源之力附魔
            int enchantLevel = net.minecraft.world.item.enchantment.EnchantmentHelper
                    .getItemEnchantmentLevel(ModEnchantments.ORIGIN_POWER.get(), stack);
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
    public ItemStack assemble(CraftingContainer container, RegistryAccess access) {
        ItemStack result = super.assemble(container, access);
        EnchantedBookItem.addEnchantment(result,
                new EnchantmentInstance(ModEnchantments.ORIGIN_POWER.get(), outputLevel));
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
        public OriginPowerBookUpgradeRecipe fromJson(ResourceLocation id, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            CraftingBookCategory category = CraftingBookCategory.CODEC.byName(
                    GsonHelper.getAsString(json, "category", null), CraftingBookCategory.MISC);

            int inputLevel = GsonHelper.getAsInt(json, "input_level");
            int outputLevel = GsonHelper.getAsInt(json, "output_level");

            // 解析 ingredients（与原版 ShapelessRecipe 保持一致的解析方式）
            JsonArray ingredientsArray = GsonHelper.getAsJsonArray(json, "ingredients");
            NonNullList<Ingredient> ingredients = NonNullList.create();
            for (JsonElement e : ingredientsArray) {
                if (e.isJsonArray()) {
                    // 多重备选 ingredient
                    for (JsonElement sub : e.getAsJsonArray()) {
                        ingredients.add(Ingredient.fromJson(sub.getAsJsonObject()));
                    }
                } else {
                    ingredients.add(Ingredient.fromJson(e.getAsJsonObject()));
                }
            }

            // 解析 result（仅用作占位，实际结果由 assemble() 写入NBT生成）
            JsonObject resultJson = GsonHelper.getAsJsonObject(json, "result");
            ItemStack result = new ItemStack(Items.ENCHANTED_BOOK);

            return new OriginPowerBookUpgradeRecipe(id, group, category,
                    inputLevel, outputLevel, ingredients, result);
        }

        @Override
        public OriginPowerBookUpgradeRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            String group = buf.readUtf();
            CraftingBookCategory category = buf.readEnum(CraftingBookCategory.class);
            int inputLevel = buf.readVarInt();
            int outputLevel = buf.readVarInt();
            int size = buf.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(size, Ingredient.EMPTY);
            for (int i = 0; i < size; i++) {
                ingredients.set(i, Ingredient.fromNetwork(buf));
            }
            ItemStack result = buf.readItem();
            return new OriginPowerBookUpgradeRecipe(id, group, category,
                    inputLevel, outputLevel, ingredients, result);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, OriginPowerBookUpgradeRecipe recipe) {
            buf.writeUtf(recipe.getGroup());
            buf.writeEnum(recipe.category());
            buf.writeVarInt(recipe.inputLevel);
            buf.writeVarInt(recipe.outputLevel);
            buf.writeVarInt(recipe.getIngredients().size());
            for (Ingredient ing : recipe.getIngredients()) {
                ing.toNetwork(buf);
            }
            buf.writeItem(recipe.getResultItem(RegistryAccess.EMPTY));
        }
    }
}
