package com.qituo.dcc.talismans;

import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerLevel;

public class ChickenTalisman extends TalismanBase {
    public ChickenTalisman(Item.Properties properties) {
        super(properties);
    }
    
    @Override
    protected void useTalisman(ServerLevel level, Player player, InteractionHand hand) {
        // 切换飞行模式
        if (player.getAbilities().mayfly) {
            // 关闭飞行
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.removeEffect(com.qituo.dcc.effects.TalismanEffects.CHICKEN_POWER.get());
        } else {
            // 开启飞行
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            // 鸡之力效果：飞行效果
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                com.qituo.dcc.effects.TalismanEffects.CHICKEN_POWER.get(),
                Integer.MAX_VALUE, // 无限时间
                0, // 等级I
                false, // ambient
                false, // 不显示粒子
                true // 显示图标
            ));
        }
        // 同步能力变更
        player.onUpdateAbilities();
    }
}