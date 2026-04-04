package com.qituo.dcc.talismans;

import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerLevel;

public class SnakeTalisman extends TalismanBase {
    public SnakeTalisman(Item.Properties properties) {
        super(properties);
    }
    
    @Override
    protected void useTalisman(ServerLevel level, Player player, InteractionHand hand) {
        // 蛇之力效果：增强隐身效果
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            com.qituo.dcc.effects.TalismanEffects.SNAKE_POWER.get(),
            5 * 60 * 20, // 5分钟
            0, // 等级I
            false, // ambient
            false, // 不显示粒子
            true // 显示图标
        ));
        
        // 增强的隐形效果
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.INVISIBILITY,
            5 * 60 * 20, // 5分钟
            1, // 等级II（增强效果）
            false, // ambient
            false, // 不显示粒子
            false // 不显示图标
        ));
    }
}