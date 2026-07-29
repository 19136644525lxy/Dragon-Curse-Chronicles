package com.qituo.dcc.talismans;

import net.minecraft.item.Item;
import net.minecraft.item.Item.Settings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.server.world.ServerWorld;

public class TigerTalisman extends TalismanBase {
    public TigerTalisman(Settings settings) {
        super(settings);
    }
    
    @Override
    protected void useTalisman(ServerWorld world, PlayerEntity player, Hand hand) {
        // 虎符咒效果暂未实现，后续将添加善恶分离功能
        sendMessage(player, "虎符咒：暂未实现，后续将添加善恶分离功能");
    }
}