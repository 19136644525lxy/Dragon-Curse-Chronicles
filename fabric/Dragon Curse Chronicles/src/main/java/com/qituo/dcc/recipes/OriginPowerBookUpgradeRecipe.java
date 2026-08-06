package com.qituo.dcc.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.qituo.dcc.DragonCurseChronicles;
import com.qituo.dcc.enchantments.ModEnchantments;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

/**
 * 始源之力附魔书升级配方（Fabric 版）
 *
 * 合成规则：4本相同等级（inputLevel）的始源之力附魔书 → 1本 inputLevel+1 等级的始源之力附魔书
 * 用途：实现 6-10级附魔书的合成获取途径
 *
 * 由于附魔书等级存储在NBT中，原版JSON配方无法匹配也无法生成结果，
 * 故自定义 Recipe 类在 matches() 中校验NBT、在 craft() 中写入NBT。
 */
public class OriginPowerBookUpgradeRecipe extends ShapelessRecipe {
    /** 自定义配方类型ID */
    public static final String TYPE_ID = "origin_power_book_upgrade";

    /** 输入附魔书等级（4本均需为此等级） */
    private final int inputLevel;
    /** 输出附魔书等级（=inputLevel+1） */
    private final int outputLevel;

    public OriginPowerBookUpgradeRecipe(Identifier id, String group, CraftingRecipeCategory category,
                                        int inputLevel, int outputLevel,
                                        DefaultedList<Ingredient> ingredients, ItemStack result) {
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
    public boolean matches(RecipeInputInventory inventory, World world) {
        if (!super.matches(inventory, world)) {
            return false;
        }
        int matchedBooks = 0;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            // 必须是附魔书
            if (!(stack.getItem() instanceof EnchantedBookItem)) return false;
            // 必须是指定等级的始源之力附魔
            int enchantLevel = EnchantmentHelper.getLevel(ModEnchantments.ORIGIN_POWER, stack);
            if (enchantLevel != inputLevel) return false;
            matchedBooks++;
        }
        // 必须正好4本
        return matchedBooks == 4;
    }

    /**
     * 生成带指定等级附魔的附魔书
     * 父类返回 result.copy()，此处向其写入NBT
     */
    @Override
    public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registryManager) {
        ItemStack result = super.craft(inventory, registryManager);
        EnchantedBookItem.addEnchantment(result,
                new EnchantmentLevelEntry(ModEnchantments.ORIGIN_POWER, outputLevel));
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.ORIGIN_POWER_BOOK_UPGRADE;
    }

    /**
     * 自定义序列化器：解析 input_level / output_level / ingredients / result
     */
    public static class Serializer implements RecipeSerializer<OriginPowerBookUpgradeRecipe> {
        @Override
        public OriginPowerBookUpgradeRecipe read(Identifier id, JsonObject json) {
            String group = JsonHelper.getString(json, "group", "");
            CraftingRecipeCategory category = CraftingRecipeCategory.CODEC
                    .byId(JsonHelper.getString(json, "category", null), CraftingRecipeCategory.MISC);

            int inputLevel = JsonHelper.getInt(json, "input_level");
            int outputLevel = JsonHelper.getInt(json, "output_level");

            // 解析 ingredients（与原版 ShapelessRecipe.Serializer 保持一致）
            JsonArray ingredientsArray = JsonHelper.getArray(json, "ingredients");
            DefaultedList<Ingredient> ingredients = DefaultedList.of();
            for (JsonElement e : ingredientsArray) {
                if (e.isJsonArray()) {
                    for (JsonElement sub : e.getAsJsonArray()) {
                        ingredients.add(Ingredient.fromJson(sub, false));
                    }
                } else {
                    ingredients.add(Ingredient.fromJson(e, false));
                }
            }

            // result 仅作占位，实际结果由 craft() 写入NBT生成
            ItemStack result = new ItemStack(Items.ENCHANTED_BOOK);

            return new OriginPowerBookUpgradeRecipe(id, group, category,
                    inputLevel, outputLevel, ingredients, result);
        }

        @Override
        public OriginPowerBookUpgradeRecipe read(Identifier id, PacketByteBuf buf) {
            String group = buf.readString();
            CraftingRecipeCategory category = buf.readEnumConstant(CraftingRecipeCategory.class);
            int inputLevel = buf.readVarInt();
            int outputLevel = buf.readVarInt();
            int size = buf.readVarInt();
            DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(size, Ingredient.EMPTY);
            for (int i = 0; i < size; i++) {
                ingredients.set(i, Ingredient.fromPacket(buf));
            }
            ItemStack result = buf.readItemStack();
            return new OriginPowerBookUpgradeRecipe(id, group, category,
                    inputLevel, outputLevel, ingredients, result);
        }

        @Override
        public void write(PacketByteBuf buf, OriginPowerBookUpgradeRecipe recipe) {
            buf.writeString(recipe.getGroup());
            buf.writeEnumConstant(recipe.getCategory());
            buf.writeVarInt(recipe.inputLevel);
            buf.writeVarInt(recipe.outputLevel);
            buf.writeVarInt(recipe.getIngredients().size());
            for (Ingredient ing : recipe.getIngredients()) {
                ing.write(buf);
            }
            buf.writeItemStack(recipe.getOutput(DynamicRegistryManager.EMPTY));
        }
    }
}
