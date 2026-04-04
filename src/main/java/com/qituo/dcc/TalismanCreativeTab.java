package com.qituo.dcc;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class TalismanCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DragonCurseChronicles.MODID);
    
    public static final RegistryObject<CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("dragon_curse_chronicles", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.dcc.dragon_curse_chronicles"))
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
            })
            .build());
    
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}