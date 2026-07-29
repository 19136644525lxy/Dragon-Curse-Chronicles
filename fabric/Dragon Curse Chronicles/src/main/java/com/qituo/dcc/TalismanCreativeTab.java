package com.qituo.dcc;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.item.ItemStack;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Items;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import com.qituo.dcc.enchantments.ModEnchantments;

public class TalismanCreativeTab {

    // 龙咒异闻录物品 - 包含所有符咒和老爹的河豚干
    public static final net.minecraft.item.ItemGroup ITEMS_TAB = Registry.register(Registries.ITEM_GROUP,
            DragonCurseChronicles.id("dragon_curse_chronicles_items"),
            FabricItemGroup.builder()
                    .displayName(Text.translatable("itemGroup.dcc.items"))
                    .icon(() -> new ItemStack(TalismanItems.DRAGON_TALISMAN))
                    .entries((parameters, output) -> {
                        output.add(TalismanItems.MOUSE_TALISMAN);
                        output.add(TalismanItems.COW_TALISMAN);
                        output.add(TalismanItems.TIGER_TALISMAN);
                        output.add(TalismanItems.RABBIT_TALISMAN);
                        output.add(TalismanItems.DRAGON_TALISMAN);
                        output.add(TalismanItems.SNAKE_TALISMAN);
                        output.add(TalismanItems.HORSE_TALISMAN);
                        output.add(TalismanItems.SHEEP_TALISMAN);
                        output.add(TalismanItems.MONKEY_TALISMAN);
                        output.add(TalismanItems.CHICKEN_TALISMAN);
                        output.add(TalismanItems.DOG_TALISMAN);
                        output.add(TalismanItems.PIG_TALISMAN);
                        output.add(TalismanItems.UNCLES_DRIED_PUFFER_FISH);
                        output.add(TalismanItems.TALISMAN_BOX);
                        output.add(TalismanItems.CUBE_OF_TANG_SHAN);
                        output.add(TalismanItems.TALISMAN_POWER_EXTRACTOR);
                    })
                    .build());

    // 龙咒异闻录附魔 - 包含10本不同等级的始源之力附魔书
    public static final net.minecraft.item.ItemGroup ENCHANTMENTS_TAB = Registry.register(Registries.ITEM_GROUP,
            DragonCurseChronicles.id("dragon_curse_chronicles_enchantments"),
            FabricItemGroup.builder()
                    .displayName(Text.translatable("itemGroup.dcc.enchantments"))
                    .icon(() -> createEnchantedBook(ModEnchantments.ORIGIN_POWER, 10))
                    .entries((parameters, output) -> {
                        for (int i = 1; i <= 10; i++) {
                            output.add(createEnchantedBook(ModEnchantments.ORIGIN_POWER, i));
                        }
                    })
                    .build());

    // 龙咒异闻录材料 - 包含符咒基等材料
    public static final net.minecraft.item.ItemGroup MATERIALS_TAB = Registry.register(Registries.ITEM_GROUP,
            DragonCurseChronicles.id("dragon_curse_chronicles_materials"),
            FabricItemGroup.builder()
                    .displayName(Text.translatable("itemGroup.dcc.materials"))
                    .icon(() -> new ItemStack(TalismanItems.TALISMAN_BASE))
                    .entries((parameters, output) -> {
                        output.add(TalismanItems.TALISMAN_BASE);
                    })
                    .build());

    // 创建带有指定附魔的附魔书
    private static ItemStack createEnchantedBook(Enchantment enchantment, int level) {
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantedBookItem.addEnchantment(book, new EnchantmentLevelEntry(enchantment, level));
        return book;
    }

    public static void initialize() {
    }
}
