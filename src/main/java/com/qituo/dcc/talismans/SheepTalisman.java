package com.qituo.dcc.talismans;

import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerLevel;

public class SheepTalisman extends TalismanBase {
    public SheepTalisman(Item.Properties properties) {
        super(properties);
    }
    
    @Override
    protected void useTalisman(ServerLevel level, Player player, InteractionHand hand) {
        sendMessage(player, "羊符咒：灵体之力功能暂时禁用，后续将实现灵魂出窍功能");
    }
}