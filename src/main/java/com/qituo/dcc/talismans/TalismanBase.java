package com.qituo.dcc.talismans;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

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
}