package com.qituo.dcc.talismans;

import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerLevel;
import java.util.Random;

public class MonkeyTalisman extends TalismanBase {
    public MonkeyTalisman(Item.Properties properties) {
        super(properties);
    }
    
    @Override
    protected void useTalisman(ServerLevel level, Player player, InteractionHand hand) {
        // 猴符咒效果暂未实现，后续将添加变形之力功能
        sendMessage(player, "猴符咒：暂未实现，后续将添加变形之力功能");
    }
}