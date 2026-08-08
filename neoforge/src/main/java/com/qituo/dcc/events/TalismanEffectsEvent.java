package com.qituo.dcc.events;

import com.qituo.dcc.DragonCurseChronicles;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import com.qituo.dcc.talismans.RabbitTalisman;
import com.qituo.dcc.effects.TalismanEffects;

@EventBusSubscriber(modid = DragonCurseChronicles.MODID, bus = EventBusSubscriber.Bus.GAME)
public class TalismanEffectsEvent {
    
    private static final int REFRESH_INTERVAL = 200;
    private static int tickCounter = 0;
    
    @SubscribeEvent
    public static void onPlayerTickPost(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        
        if (RabbitTalisman.isChickenRabbitPowerActive(player)) {
            tickCounter++;
            
            if (tickCounter >= REFRESH_INTERVAL) {
                tickCounter = 0;
                
                if (!player.hasEffect(TalismanEffects.CHICKEN_POWER)) {
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        TalismanEffects.CHICKEN_POWER,
                        300,
                        1,
                        false,
                        false,
                        true
                    ));
                }
                
                if (!player.hasEffect(MobEffects.MOVEMENT_SPEED)) {
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        MobEffects.MOVEMENT_SPEED,
                        300,
                        1,
                        false,
                        false,
                        false
                    ));
                }
                
                if (!player.hasEffect(MobEffects.JUMP)) {
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        MobEffects.JUMP,
                        300,
                        2,
                        false,
                        false,
                        false
                    ));
                }
                
                if (!player.getAbilities().mayfly) {
                    player.getAbilities().mayfly = true;
                    player.onUpdateAbilities();
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        
        if (RabbitTalisman.isChickenRabbitPowerActive(player)) {
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
            
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                TalismanEffects.CHICKEN_POWER,
                20 * 60 * 20,
                1,
                false,
                false,
                true
            ));
            
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                MobEffects.MOVEMENT_SPEED,
                20 * 60 * 20,
                1,
                false,
                false,
                false
            ));
            
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                MobEffects.JUMP,
                20 * 60 * 20,
                2,
                false,
                false,
                false
            ));
        }
    }
    
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        
        if (RabbitTalisman.isChickenRabbitPowerActive(player)) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }
    }
}
