package com.qituo.dcc.items;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class TalismanItem extends Item {
    
    public TalismanItem(Properties pProperties) {
        super(pProperties);
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        if (!pLevel.isClientSide) {
            useTalisman((ServerLevel)pLevel, pPlayer, pHand);
        }
        return super.use(pLevel, pPlayer, pHand);
    }
    
    protected abstract void useTalisman(ServerLevel level, Player player, InteractionHand hand);
    
    protected void sendMessage(Player player, String message) {
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(message));
    }
}