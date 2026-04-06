package com.qituo.dcc.enchantments;

import com.qituo.dcc.DragonCurseChronicles;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = 
        DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, DragonCurseChronicles.MODID);

    // 注册始源之力附魔
    public static final RegistryObject<Enchantment> ORIGIN_POWER = ENCHANTMENTS.register("origin_power", 
        () -> new OriginPowerEnchantment(Enchantment.Rarity.VERY_RARE, 
            EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND, 
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)
    );
}
