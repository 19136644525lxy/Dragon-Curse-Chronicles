package com.qituo.dcc.talismans;

import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerLevel;

public class CowTalisman extends TalismanBase {
    public CowTalisman(Item.Properties properties) {
        super(properties);
    }
    
    @Override
    protected void useTalisman(ServerLevel level, Player player, InteractionHand hand) {
        // 牛之力效果：增加攻击力、防御力、移动速度、攻击范围
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            com.qituo.dcc.effects.TalismanEffects.COW_POWER.get(),
            3 * 60 * 20, // 3分钟
            0, // 等级I
            false, // ambient
            false, // 不显示粒子
            true // 显示图标
        ));
        
        // 增加攻击力
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.DAMAGE_BOOST,
            3 * 60 * 20, // 3分钟
            2, // 等级III
            false, // ambient
            false, // 不显示粒子
            false // 不显示图标
        ));
        
        // 增加防御力
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE,
            3 * 60 * 20, // 3分钟
            2, // 等级III
            false, // ambient
            false, // 不显示粒子
            false // 不显示图标
        ));
        
        // 增加移动速度
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED,
            3 * 60 * 20, // 3分钟
            1, // 等级II
            false, // ambient
            false, // 不显示粒子
            false // 不显示图标
        ));
        
        // 增加攻击范围（通过力量效果间接实现）
    }
}