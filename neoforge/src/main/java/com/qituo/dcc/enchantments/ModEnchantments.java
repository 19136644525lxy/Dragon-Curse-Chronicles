package com.qituo.dcc.enchantments;

import com.qituo.dcc.DragonCurseChronicles;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 模组附魔注册类（NeoForge 1.21.1）
 *
 * 【1.21 数据驱动说明】
 * Enchantment 在 1.21 变成 record，附魔实例由数据包 JSON 定义，代码无法 new 出来。
 * 因此本类的职责是：
 *   1. 保留 DeferredRegister（附魔注册表本身是数据驱动的，注册项由 JSON 产生）；
 *   2. 在服务端启动后（数据包已加载）解析出 dcc:origin_power 的 Holder 并缓存；
 *   3. 提供按物品栈读取附魔等级的统一入口。
 *
 * 对应数据包文件：data/dcc/enchantment/origin_power.json
 * 可获取性标签：data/minecraft/tags/enchantment/{non_treasure,tradeable}.json
 *   （in_enchanting_table 由原版 #non_treasure 派生，无需再写）
 */
public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(Registries.ENCHANTMENT, DragonCurseChronicles.MODID);

    /** 始源之力附魔的命名空间:路径 —— 与 JSON 文件 (data/dcc/enchantment/origin_power.json) 一致 */
    public static final String ORIGIN_POWER_ID = "origin_power";

    /** 附魔注册键，用于在数据包加载后取回 Holder */
    public static final ResourceKey<Enchantment> ORIGIN_POWER_KEY =
            ResourceKey.create(Registries.ENCHANTMENT,
                    ResourceLocation.fromNamespaceAndPath(DragonCurseChronicles.MODID, ORIGIN_POWER_ID));

    /** 已解析的附魔 Holder；服务端启动后填充，客户端为空（客户端效果不依赖它） */
    private static volatile Holder<Enchantment> originPowerHolder = null;

    /**
     * 服务端启动后由主类调用：从已加载的数据包注册表里取回附魔 Holder。
     * 此时数据包已完成加载，未取到说明 JSON 缺失，打印告警便于排查。
     */
    public static void syncHolder(MinecraftServer server) {
        try {
            var registry = server.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            originPowerHolder = registry.getHolder(ORIGIN_POWER_KEY).orElse(null);
            if (originPowerHolder == null) {
                DragonCurseChronicles.LOGGER.warn(
                        "未能找到附魔 {}，请确认 data/dcc/enchantment/origin_power.json 存在且格式正确",
                        ORIGIN_POWER_KEY.location());
                return;
            }

            // 自检：确认最大等级与可获取性标签已生效
            int maxLevel = originPowerHolder.value().getMaxLevel();
            boolean inTable = originPowerHolder.is(net.minecraft.tags.EnchantmentTags.IN_ENCHANTING_TABLE);
            boolean tradeable = originPowerHolder.is(net.minecraft.tags.EnchantmentTags.TRADEABLE);
            DragonCurseChronicles.LOGGER.info(
                    "始源之力附魔已就绪：{} 最大等级={} 附魔台可出={} 村民可交易={}",
                    ORIGIN_POWER_KEY.location(), maxLevel, inTable, tradeable);
        } catch (Throwable e) {
            DragonCurseChronicles.LOGGER.error("解析始源之力附魔失败: {}", e.toString());
        }
    }

    /** 获取已解析的附魔 Holder；未就绪时返回 null */
    public static Holder<Enchantment> getOriginPowerHolder() {
        return originPowerHolder;
    }

    /**
     * 读取物品上的始源之力附魔等级。
     * 1.21 中附魔存放在 DataComponents.ENCHANTMENTS（附魔书为 STORED_ENCHANTMENTS），
     * 通过 EnchantmentHelper.getEnchantmentsForCrafting 统一读取。
     *
     * @return 附魔等级；未附魔或 Holder 未就绪时返回 0
     */
    public static int getOriginPowerLevel(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0;
        Holder<Enchantment> holder = originPowerHolder;
        if (holder == null) return 0;
        try {
            ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
            return enchantments.getLevel(holder);
        } catch (Throwable e) {
            return 0;
        }
    }
}
