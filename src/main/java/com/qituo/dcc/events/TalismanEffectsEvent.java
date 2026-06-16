package com.qituo.dcc.events;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.qituo.dcc.talismans.RabbitTalisman;
import com.qituo.dcc.effects.TalismanEffects;

@Mod.EventBusSubscriber(modid = "dcc")
public class TalismanEffectsEvent {
    
    private static final int REFRESH_INTERVAL = 200;
    private static int tickCounter = 0;
    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        
        Player player = event.player;
        
        if (RabbitTalisman.isChickenRabbitPowerActive(player)) {
            tickCounter++;
            
            if (tickCounter >= REFRESH_INTERVAL) {
                tickCounter = 0;
                
                if (!player.hasEffect(TalismanEffects.CHICKEN_POWER.get())) {
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        TalismanEffects.CHICKEN_POWER.get(),
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
                TalismanEffects.CHICKEN_POWER.get(),
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
