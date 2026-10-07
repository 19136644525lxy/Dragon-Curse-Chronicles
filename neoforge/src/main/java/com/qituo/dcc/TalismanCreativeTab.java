package com.qituo.dcc;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.qituo.dcc.enchantments.ModEnchantments;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class TalismanCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DragonCurseChronicles.MODID);
    
    // 龙咒异闻录物品 - 包含所有符咒和老爹的河豚干
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ITEMS_TAB = CREATIVE_MODE_TABS.register("dragon_curse_chronicles_items", () -> CreativeModeTab.builder()
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
                output.accept(TalismanItems.CUBE_OF_TANG_SHAN.get());
                output.accept(TalismanItems.TALISMAN_POWER_EXTRACTOR.get());
            })
            .build());
    
    // 龙咒异闻录附魔 - 包含10本不同等级的始源之力附魔书（过渡方案：NBT方式，无RegistryAccess依赖）
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ENCHANTMENTS_TAB = CREATIVE_MODE_TABS.register("dragon_curse_chronicles_enchantments", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.dcc.dragon_curse_chronicles_enchantments"))
            .icon(() -> createEnchantedBookNbt(10))
            .displayItems((parameters, output) -> {
                for (int i = 1; i <= 10; i++) {
                    output.accept(createEnchantedBookNbt(i));
                }
            })
            .build());
    
    // 龙咒异闻录材料 - 包含符咒基等材料
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MATERIALS_TAB = CREATIVE_MODE_TABS.register("dragon_curse_chronicles_materials", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.dcc.dragon_curse_chronicles_materials"))
            .icon(() -> new ItemStack(TalismanItems.TALISMAN_BASE.get()))
            .displayItems((parameters, output) -> {
                output.accept(TalismanItems.TALISMAN_BASE.get());
            })
            .build());
    
    /**
     * 创建带有指定等级始源之力附魔的附魔书
     *
     * 1.21 附魔是数据驱动的 Holder，必须写入 DataComponents.STORED_ENCHANTMENTS 组件，
     * 而不是手搓 NBT（旧实现写 CUSTOM_DATA，原版附魔系统读不到）。
     * Holder 在服务端启动后才解析；创造栏可能在解析前构建，此时返回空白附魔书兜底。
     */
    private static ItemStack createEnchantedBookNbt(int level) {
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> holder =
                ModEnchantments.getOriginPowerHolder();
        if (holder == null) {
            return book;
        }
        var mutable = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(
                net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
        mutable.set(holder, Math.max(1, Math.min(10, level)));
        net.minecraft.world.item.enchantment.EnchantmentHelper.setEnchantments(book, mutable.toImmutable());
        return book;
    }
    
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}