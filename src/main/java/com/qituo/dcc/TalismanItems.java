package com.qituo.dcc;

import com.qituo.dcc.items.UnclesDriedPufferFish;
import com.qituo.dcc.talismans.MouseTalisman;
import com.qituo.dcc.talismans.TigerTalisman;
import com.qituo.dcc.talismans.CowTalisman;
import com.qituo.dcc.talismans.RabbitTalisman;
import com.qituo.dcc.talismans.DragonTalisman;
import com.qituo.dcc.talismans.SnakeTalisman;
import com.qituo.dcc.talismans.HorseTalisman;
import com.qituo.dcc.talismans.SheepTalisman;
import com.qituo.dcc.talismans.MonkeyTalisman;
import com.qituo.dcc.talismans.ChickenTalisman;
import com.qituo.dcc.talismans.DogTalisman;
import com.qituo.dcc.talismans.PigTalisman;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TalismanItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, DragonCurseChronicles.MODID);
    
    public static final RegistryObject<Item> MOUSE_TALISMAN = ITEMS.register("mouse_talisman", () -> new MouseTalisman(new Item.Properties().stacksTo(1)));
    
    public static final RegistryObject<Item> COW_TALISMAN = ITEMS.register("cow_talisman", () -> new CowTalisman(new Item.Properties().stacksTo(1)));
    
    public static final RegistryObject<Item> TIGER_TALISMAN = ITEMS.register("tiger_talisman", () -> new TigerTalisman(new Item.Properties().stacksTo(1)));
    
    public static final RegistryObject<Item> RABBIT_TALISMAN = ITEMS.register("rabbit_talisman", () -> new RabbitTalisman(new Item.Properties().stacksTo(1)));
    
    public static final RegistryObject<Item> DRAGON_TALISMAN = ITEMS.register("dragon_talisman", () -> new DragonTalisman(new Item.Properties().stacksTo(1)));
    
    public static final RegistryObject<Item> SNAKE_TALISMAN = ITEMS.register("snake_talisman", () -> new SnakeTalisman(new Item.Properties().stacksTo(1)));
    
    public static final RegistryObject<Item> HORSE_TALISMAN = ITEMS.register("horse_talisman", () -> new HorseTalisman(new Item.Properties().stacksTo(1)));
    
    public static final RegistryObject<Item> SHEEP_TALISMAN = ITEMS.register("sheep_talisman", () -> new SheepTalisman(new Item.Properties().stacksTo(1)));
    
    public static final RegistryObject<Item> MONKEY_TALISMAN = ITEMS.register("monkey_talisman", () -> new MonkeyTalisman(new Item.Properties().stacksTo(1)));
    
    public static final RegistryObject<Item> CHICKEN_TALISMAN = ITEMS.register("chicken_talisman", () -> new ChickenTalisman(new Item.Properties().stacksTo(1)));
    
    public static final RegistryObject<Item> DOG_TALISMAN = ITEMS.register("dog_talisman", () -> new DogTalisman(new Item.Properties().stacksTo(1)));
    
    public static final RegistryObject<Item> PIG_TALISMAN = ITEMS.register("pig_talisman", () -> new PigTalisman(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> UNCLES_DRIED_PUFFER_FISH = ITEMS.register("uncles_dried_puffer_fish", () -> new UnclesDriedPufferFish(new Item.Properties().stacksTo(1)));
    
    public static final RegistryObject<Item> TALISMAN_BASE = ITEMS.register("talisman_base", () -> new Item(new Item.Properties().stacksTo(64)));
    
    public static final RegistryObject<Item> TALISMAN_BOX = ITEMS.register("talisman_box", () -> new Item(new Item.Properties().stacksTo(64)));
    
    public static final RegistryObject<Item> CUBE_OF_TANG_SHAN = ITEMS.register("cube_of_tang_shan", () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> TALISMAN_POWER_EXTRACTOR = ITEMS.register("talisman_power_extractor", () -> new Item(new Item.Properties().stacksTo(1)));
    
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}