package com.qituo.dcc.talismans;

import net.minecraft.item.Item;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import com.qituo.dcc.effects.TalismanEffects;
import java.util.List;

public class DogTalisman extends TalismanBase {
    public DogTalisman(Item.Settings settings) {
        super(settings);
    }
    
    @Override
    protected void useTalisman(ServerWorld world, PlayerEntity player, Hand hand) {
        // 狗之力效果：永生效果
        player.addStatusEffect(new StatusEffectInstance(
            TalismanEffects.DOG_POWER,
            60 * 20, // 1分钟
            0, // 等级I
            false, // ambient
            false, // 不显示粒子
            true // 显示图标
        ));
        
        // 立即恢复半血
        player.setHealth(player.getMaxHealth() * 0.5f);
        
        // 清除所有负面效果
        List<StatusEffect> harmfulEffects = List.of(
            StatusEffects.POISON,
            StatusEffects.WITHER,
            StatusEffects.WEAKNESS,
            StatusEffects.SLOWNESS,
            StatusEffects.BLINDNESS,
            StatusEffects.HUNGER,
            StatusEffects.LEVITATION,
            StatusEffects.GLOWING,
            StatusEffects.BAD_OMEN,
            StatusEffects.DARKNESS
        );
        
        for (StatusEffect effect : harmfulEffects) {
            if (player.hasStatusEffect(effect)) {
                player.removeStatusEffect(effect);
            }
        }
        
        // 狗之力效果：持续伤害吸收（30秒）
        player.addStatusEffect(new StatusEffectInstance(
            TalismanEffects.DOG_POWER,
            30 * 20, // 30秒
            0,
            false,
            false,
            true
        ));
    }
}