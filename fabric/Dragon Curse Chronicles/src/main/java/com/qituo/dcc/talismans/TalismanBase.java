package com.qituo.dcc.talismans;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.text.Text;

public abstract class TalismanBase extends Item {
    public TalismanBase(Settings settings) {
        super(settings);
    }
    
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient && world instanceof net.minecraft.server.world.ServerWorld serverWorld) {
            useTalisman(serverWorld, user, hand);
        }
        return super.use(world, user, hand);
    }
    
    protected abstract void useTalisman(net.minecraft.server.world.ServerWorld world, PlayerEntity user, Hand hand);
    
    protected void sendMessage(PlayerEntity player, String message) {
        player.sendMessage(Text.literal("[十二符咒] " + message));
    }
}