package com.qituo.dcc.talismans;

import net.minecraft.item.Item;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.server.world.ServerWorld;

public class MonkeyTalisman extends TalismanBase {
    public MonkeyTalisman(Item.Settings settings) {
        super(settings);
    }
    
    @Override
    protected void useTalisman(ServerWorld world, PlayerEntity player, Hand hand) {
        // 猴符咒效果暂未实现，后续将添加变形之力功能
        sendMessage(player, "猴符咒：暂未实现，后续将添加变形之力功能");
    }
}