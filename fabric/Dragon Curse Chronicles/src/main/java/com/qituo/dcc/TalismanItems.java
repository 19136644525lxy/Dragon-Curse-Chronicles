package com.qituo.dcc;

import com.qituo.dcc.items.UnclesDriedPufferFish;
import com.qituo.dcc.items.CubeOfTangShanItem;
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
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class TalismanItems {
    public static final Item MOUSE_TALISMAN = Registry.register(Registries.ITEM, DragonCurseChronicles.id("mouse_talisman"), new MouseTalisman(new Item.Settings().maxCount(1)));
    public static final Item COW_TALISMAN = Registry.register(Registries.ITEM, DragonCurseChronicles.id("cow_talisman"), new CowTalisman(new Item.Settings().maxCount(1)));
    public static final Item TIGER_TALISMAN = Registry.register(Registries.ITEM, DragonCurseChronicles.id("tiger_talisman"), new TigerTalisman(new Item.Settings().maxCount(1)));
    public static final Item RABBIT_TALISMAN = Registry.register(Registries.ITEM, DragonCurseChronicles.id("rabbit_talisman"), new RabbitTalisman(new Item.Settings().maxCount(1)));
    public static final Item DRAGON_TALISMAN = Registry.register(Registries.ITEM, DragonCurseChronicles.id("dragon_talisman"), new DragonTalisman(new Item.Settings().maxCount(1)));
    public static final Item SNAKE_TALISMAN = Registry.register(Registries.ITEM, DragonCurseChronicles.id("snake_talisman"), new SnakeTalisman(new Item.Settings().maxCount(1)));
    public static final Item HORSE_TALISMAN = Registry.register(Registries.ITEM, DragonCurseChronicles.id("horse_talisman"), new HorseTalisman(new Item.Settings().maxCount(1)));
    public static final Item SHEEP_TALISMAN = Registry.register(Registries.ITEM, DragonCurseChronicles.id("sheep_talisman"), new SheepTalisman(new Item.Settings().maxCount(1)));
    public static final Item MONKEY_TALISMAN = Registry.register(Registries.ITEM, DragonCurseChronicles.id("monkey_talisman"), new MonkeyTalisman(new Item.Settings().maxCount(1)));
    public static final Item CHICKEN_TALISMAN = Registry.register(Registries.ITEM, DragonCurseChronicles.id("chicken_talisman"), new ChickenTalisman(new Item.Settings().maxCount(1)));
    public static final Item DOG_TALISMAN = Registry.register(Registries.ITEM, DragonCurseChronicles.id("dog_talisman"), new DogTalisman(new Item.Settings().maxCount(1)));
    public static final Item PIG_TALISMAN = Registry.register(Registries.ITEM, DragonCurseChronicles.id("pig_talisman"), new PigTalisman(new Item.Settings().maxCount(1)));

    public static final Item UNCLES_DRIED_PUFFER_FISH = Registry.register(Registries.ITEM, DragonCurseChronicles.id("uncles_dried_puffer_fish"), new UnclesDriedPufferFish(new Item.Settings().maxCount(1)));
    public static final Item TALISMAN_BASE = Registry.register(Registries.ITEM, DragonCurseChronicles.id("talisman_base"), new Item(new Item.Settings().maxCount(64)));
    public static final Item TALISMAN_BOX = Registry.register(Registries.ITEM, DragonCurseChronicles.id("talisman_box"), new Item(new Item.Settings().maxCount(64)));
    public static final Item CUBE_OF_TANG_SHAN = Registry.register(Registries.ITEM, DragonCurseChronicles.id("cube_of_tang_shan"), new CubeOfTangShanItem(new Item.Settings().maxCount(1)));
    public static final Item TALISMAN_POWER_EXTRACTOR = Registry.register(Registries.ITEM, DragonCurseChronicles.id("talisman_power_extractor"), new com.qituo.dcc.items.TalismanExtractorItem(new Item.Settings().maxCount(1)));

    public static void initialize() {
    }
}