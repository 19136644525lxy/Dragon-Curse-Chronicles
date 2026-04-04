package com.qituo.dcc.talismans;

import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerLevel;
import java.util.List;

public class DogTalisman extends TalismanBase {
    public DogTalisman(Item.Properties properties) {
        super(properties);
    }
    
    @Override
    protected void useTalisman(ServerLevel level, Player player, InteractionHand hand) {
        // 狗之力效果：永生效果
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            com.qituo.dcc.effects.TalismanEffects.DOG_POWER.get(),
            60 * 20, // 1分钟
            0, // 等级I
            false, // ambient
            false, // 不显示粒子
            true // 显示图标
        ));
        
        // 立即恢复半血
        player.setHealth(player.getMaxHealth() * 0.5f);
        
        // 清除所有负面效果
        List<net.minecraft.world.effect.MobEffect> harmfulEffects = List.of(
            net.minecraft.world.effect.MobEffects.POISON,
            net.minecraft.world.effect.MobEffects.WITHER,
            net.minecraft.world.effect.MobEffects.WEAKNESS,
            net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
            net.minecraft.world.effect.MobEffects.BLINDNESS,
            net.minecraft.world.effect.MobEffects.HUNGER,
            net.minecraft.world.effect.MobEffects.LEVITATION,
            net.minecraft.world.effect.MobEffects.GLOWING,
            net.minecraft.world.effect.MobEffects.BAD_OMEN,
            net.minecraft.world.effect.MobEffects.DARKNESS
        );
        
        for (net.minecraft.world.effect.MobEffect effect : harmfulEffects) {
            if (player.hasEffect(effect)) {
                player.removeEffect(effect);
            }
        }
        
        // 获得短暂无敌时间（2秒）
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.ABSORPTION,
            2 * 20, // 2秒
            4, // 等级V
            false, // 不显示粒子
            false // 不显示图标
        ));
    }
}