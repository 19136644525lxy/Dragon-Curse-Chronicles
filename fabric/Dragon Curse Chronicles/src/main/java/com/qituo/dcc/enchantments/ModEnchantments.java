package com.qituo.dcc.enchantments;

import com.qituo.dcc.DragonCurseChronicles;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModEnchantments {

    // 注册始源之力附魔
    public static final Enchantment ORIGIN_POWER = Registry.register(Registries.ENCHANTMENT,
            DragonCurseChronicles.id("origin_power"),
            new OriginPowerEnchantment(Enchantment.Rarity.VERY_RARE, EnchantmentTarget.WEAPON,
                    EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND,
                    EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)
    );

    public static void initialize() {
    }
}