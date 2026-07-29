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

public class HorseTalisman extends TalismanBase {
    public HorseTalisman(Item.Settings settings) {
        super(settings);
    }
    
    @Override
    protected void useTalisman(ServerWorld world, PlayerEntity player, Hand hand) {
        // 马之力效果：治愈效果
        player.addStatusEffect(new StatusEffectInstance(
            TalismanEffects.HORSE_POWER,
            60 * 20, // 1分钟
            0, // 等级I
            false, // ambient
            false, // 不显示粒子
            true // 显示图标
        ));
        
        // 恢复满生命值
        player.setHealth(player.getMaxHealth());
        
        // 只清除负面效果，保留正面效果
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
    }
}