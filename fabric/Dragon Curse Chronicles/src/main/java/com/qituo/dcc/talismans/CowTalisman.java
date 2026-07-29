package com.qituo.dcc.talismans;

import net.minecraft.item.Item;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import com.qituo.dcc.effects.TalismanEffects;

public class CowTalisman extends TalismanBase {
    public CowTalisman(Item.Settings settings) {
        super(settings);
    }
    
    @Override
    protected void useTalisman(ServerWorld world, PlayerEntity player, Hand hand) {
        // 牛之力效果：增加攻击力、防御力、移动速度、攻击范围
        player.addStatusEffect(new StatusEffectInstance(
            TalismanEffects.COW_POWER,
            3 * 60 * 20, // 3分钟
            0, // 等级I
            false, // ambient
            false, // 不显示粒子
            true // 显示图标
        ));
        
        // 增加攻击力
        player.addStatusEffect(new StatusEffectInstance(
            StatusEffects.STRENGTH,
            3 * 60 * 20, // 3分钟
            2, // 等级III
            false, // ambient
            false, // 不显示粒子
            false // 不显示图标
        ));
        
        // 增加防御力
        player.addStatusEffect(new StatusEffectInstance(
            StatusEffects.RESISTANCE,
            3 * 60 * 20, // 3分钟
            2, // 等级III
            false, // ambient
            false, // 不显示粒子
            false // 不显示图标
        ));
        
        // 增加移动速度
        player.addStatusEffect(new StatusEffectInstance(
            StatusEffects.SPEED,
            3 * 60 * 20, // 3分钟
            1, // 等级II
            false, // ambient
            false, // 不显示粒子
            false // 不显示图标
        ));
        
        // 增加攻击范围（通过力量效果间接实现）
    }
}