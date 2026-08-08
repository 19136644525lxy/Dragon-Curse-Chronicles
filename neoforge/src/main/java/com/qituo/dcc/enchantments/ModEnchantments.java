package com.qituo.dcc.enchantments;

import com.qituo.dcc.DragonCurseChronicles;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 模组附魔注册类
 *
 * 【NeoForge 1.21.1 迁移说明】
 * Enchantment 从 class 改为 record，不再支持代码里 new XxxEnchantment() 注册。
 * 注册流程改为：
 *   1. 先在 resources/data/dcc/enchantment/origin_power.json 里写核心 JSON 数据驱动定义
 *   2. 效果逻辑迁移到 LivingIncomingDamageEvent/LivingDamageEvent（已在 OriginPowerEnchantment/OriginPowerArmorHandler 中）
 *
 * 当前为了让代码先编译并确保游戏内附魔 NBT 能被正常读取，
 * getOriginPowerLevel(ItemStack) 直接读取物品 NBT 的 "Enchantments" 列表，
 * 不依赖 DeferredHolder<Enchantment> 是否完成注册。等 JSON 数据补上后，
 * 可以再改回 ModEnchantments.ORIGIN_POWER Holder 方式。
 */
public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(Registries.ENCHANTMENT, DragonCurseChronicles.MODID);

    /** 始源之力附魔的命名空间:路径 —— 与 JSON 文件 (data/dcc/enchantment/origin_power.json) 一致 */
    public static final String ORIGIN_POWER_ID = "origin_power";
    public static final ResourceLocation ORIGIN_POWER_KEY =
            ResourceLocation.fromNamespaceAndPath(DragonCurseChronicles.MODID, ORIGIN_POWER_ID);

    // 暂按NeoForge 1.21.1数据驱动附魔方式迁移，具体注册留待下一步写 JSON
    // 待 resources/data/dcc/enchantment/origin_power.json 就位后，再使用：
    // public static final DeferredHolder<Enchantment, Enchantment> ORIGIN_POWER =
    //         ENCHANTMENTS.register(ORIGIN_POWER_ID, () -> ...);

    /**
     * 直接从物品 NBT 读取始源之力附魔等级
     * 原理：兼容还未完成 JSON 附魔注册的过渡阶段，确保附魔 NBT 依然能够被识别并生效
     */
    public static int getOriginPowerLevel(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = cd != null ? cd.copyTag() : new CompoundTag();
        if (tag.isEmpty()) return 0;

        ListTag enchantments = tag.getList("Enchantments", Tag.TAG_COMPOUND);
        String fullId = DragonCurseChronicles.MODID + ":" + ORIGIN_POWER_ID;
        for (int i = 0; i < enchantments.size(); i++) {
            CompoundTag ench = enchantments.getCompound(i);
            String id = ench.getString("id");
            if (fullId.equals(id) || ORIGIN_POWER_ID.equals(id)) {
                return ench.getInt("lvl");
            }
        }
        return 0;
    }
}
