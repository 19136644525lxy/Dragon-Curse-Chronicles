package com.qituo.dcc;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import com.qituo.dcc.enchantments.ModEnchantments;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class TalismanCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DragonCurseChronicles.MODID);
    
    // 龙咒异闻录物品 - 包含所有符咒和老爹的河豚干
    public static final RegistryObject<CreativeModeTab> ITEMS_TAB = CREATIVE_MODE_TABS.register("dragon_curse_chronicles_items", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.dcc.dragon_curse_chronicles_items"))
            .icon(() -> new ItemStack(TalismanItems.DRAGON_TALISMAN.get()))
            .displayItems((parameters, output) -> {
                output.accept(TalismanItems.MOUSE_TALISMAN.get());
                output.accept(TalismanItems.COW_TALISMAN.get());
                output.accept(TalismanItems.TIGER_TALISMAN.get());
                output.accept(TalismanItems.RABBIT_TALISMAN.get());
                output.accept(TalismanItems.DRAGON_TALISMAN.get());
                output.accept(TalismanItems.SNAKE_TALISMAN.get());
                output.accept(TalismanItems.HORSE_TALISMAN.get());
                output.accept(TalismanItems.SHEEP_TALISMAN.get());
                output.accept(TalismanItems.MONKEY_TALISMAN.get());
                output.accept(TalismanItems.CHICKEN_TALISMAN.get());
                output.accept(TalismanItems.DOG_TALISMAN.get());
                output.accept(TalismanItems.PIG_TALISMAN.get());
                output.accept(TalismanItems.UNCLES_DRIED_PUFFER_FISH.get());
                output.accept(TalismanItems.TALISMAN_BOX.get());
            })
            .build());
    
    // 龙咒异闻录附魔 - 包含10本不同等级的始源之力附魔书
    public static final RegistryObject<CreativeModeTab> ENCHANTMENTS_TAB = CREATIVE_MODE_TABS.register("dragon_curse_chronicles_enchantments", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.dcc.dragon_curse_chronicles_enchantments"))
            .icon(() -> createEnchantedBook(ModEnchantments.ORIGIN_POWER.get(), 10))
            .displayItems((parameters, output) -> {
                for (int i = 1; i <= 10; i++) {
                    output.accept(createEnchantedBook(ModEnchantments.ORIGIN_POWER.get(), i));
                }
            })
            .build());
    
    // 龙咒异闻录材料 - 包含符咒基等材料
    public static final RegistryObject<CreativeModeTab> MATERIALS_TAB = CREATIVE_MODE_TABS.register("dragon_curse_chronicles_materials", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.dcc.dragon_curse_chronicles_materials"))
            .icon(() -> new ItemStack(TalismanItems.TALISMAN_BASE.get()))
            .displayItems((parameters, output) -> {
                output.accept(TalismanItems.TALISMAN_BASE.get());
            })
            .build());
    
    // 创建带有指定附魔的附魔书
    private static ItemStack createEnchantedBook(Enchantment enchantment, int level) {
        ItemStack book = new ItemStack(net.minecraft.world.item.Items.ENCHANTED_BOOK);
        EnchantedBookItem.addEnchantment(book, new net.minecraft.world.item.enchantment.EnchantmentInstance(enchantment, level));
        return book;
    }
    
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}