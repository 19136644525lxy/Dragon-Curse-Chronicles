package com.qituo.dcc.enchantments;

import com.qituo.dcc.DragonCurseChronicles;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModEnchantments {

    // 注册始源之力附魔
    // 注意：target 必须是 ARMOR。此前误用 WEAPON，导致附魔台只能给剑刷出，
    // 而盔甲（本附魔的实际生效部位）在附魔台永远刷不到，与 Forge 端行为不一致。
    public static final Enchantment ORIGIN_POWER = Registry.register(Registries.ENCHANTMENT,
            DragonCurseChronicles.id("origin_power"),
            new OriginPowerEnchantment(Enchantment.Rarity.VERY_RARE, EnchantmentTarget.ARMOR,
                    EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND,
                    EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)
    );

    public static void initialize() {
    }
}