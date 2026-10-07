package com.qituo.dcc.talismans;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 符咒基类
 * 所有生肖符咒继承此类，提供施法入口和生肖颜色数据
 */
public abstract class TalismanBase extends Item {
    public TalismanBase(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            useTalisman(serverLevel, player, hand);
        }
        return super.use(level, player, hand);
    }

    protected abstract void useTalisman(net.minecraft.server.level.ServerLevel level, Player player, InteractionHand hand);

    protected void sendMessage(Player player, String message) {
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("[十二符咒] " + message));
    }

    // ===== 生肖边缘光效接口 =====

    /**
     * 获取该符咒对应生肖的名称（如 "mouse", "cow", "dragon" 等）
     * 默认根据类名自动推断（如 DragonTalisman -> "dragon"）
     * 子类可覆盖以返回自定义名称
     */
    public String getZodiacName() {
        String className = getClass().getSimpleName();
        // 移除 "Talisman" 后缀并转为小写
        if (className.endsWith("Talisman")) {
            return className.substring(0, className.length() - 8).toLowerCase();
        }
        return className.toLowerCase();
    }

    /**
     * 获取该符咒对应生肖的发光颜色（RGB）
     * 子类实现，返回各自生肖的代表色
     */
    public abstract float[] getZodiacColor();

    /**
     * 获取发光纹理路径
     * 根据生肖名称自动构建路径，子类可覆盖为自定义纹理
     */
    public String getGlowTexturePath() {
        return "textures/item/glow/" + getZodiacName() + "_glow.png";
    }

    /**
     * 客户端初始化：注册自定义 BEWLR 渲染器
     * Forge 在物品初始化时调用此方法，传入 consumer 接受 IClientItemExtensions
     */
    @Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        consumer.accept(com.qituo.dcc.client.TalismanClientExtensions.INSTANCE);
    }
}