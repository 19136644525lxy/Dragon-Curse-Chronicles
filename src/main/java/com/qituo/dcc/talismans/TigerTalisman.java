package com.qituo.dcc.talismans;

import com.qituo.dcc.ai.EvilCloneAI;
import com.qituo.dcc.util.MojangSkinAPI;
import com.qituo.dcc.util.NetHandlerPlayServerFake;
import com.qituo.dcc.util.ReflectionCache;
import com.qituo.dcc.util.ExceptionHandler;
import net.minecraft.world.item.Item.Properties;

public class TigerTalisman extends TalismanBase {
    public TigerTalisman(Properties properties) {
        super(properties);
    }
    
    @Override
    protected void useTalisman(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        // 虎符咒效果暂未实现，后续将添加善恶分离功能
        sendMessage(player, "虎符咒：暂未实现，后续将添加善恶分离功能");
    }
}