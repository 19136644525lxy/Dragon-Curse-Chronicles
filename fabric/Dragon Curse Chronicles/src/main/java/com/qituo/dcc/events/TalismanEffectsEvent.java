package com.qituo.dcc.events;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.player.PlayerAbilities;
import com.qituo.dcc.talismans.RabbitTalisman;
import com.qituo.dcc.effects.TalismanEffects;

public class TalismanEffectsEvent {
    
    private static final int REFRESH_INTERVAL = 200;
    private static int tickCounter = 0;
    
    /**
     * 服务器 tick 回调（由 DragonCurseChronicles 通过 ServerTickEvents.END_SERVER_TICK 注册）
     * 替代 Forge 的 TickEvent.PlayerTickEvent，在服务端遍历来实现玩家 tick 逻辑
     */
    public static void onServerTick(ServerWorld world) {
        tickCounter++;
        
        for (PlayerEntity player : world.getPlayers(p -> true)) {
            if (RabbitTalisman.isChickenRabbitPowerActive(player)) {
                if (tickCounter >= REFRESH_INTERVAL) {
                    tickCounter = 0;
                    
                    if (!player.hasStatusEffect(TalismanEffects.CHICKEN_POWER)) {
                        player.addStatusEffect(new StatusEffectInstance(
                            TalismanEffects.CHICKEN_POWER,
                            300,
                            1,
                            false,
                            false,
                            true
                        ));
                    }
                    
                    if (!player.hasStatusEffect(StatusEffects.SPEED)) {
                        player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.SPEED,
                            300,
                            1,
                            false,
                            false,
                            false
                        ));
                    }
                    
                    if (!player.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
                        player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.JUMP_BOOST,
                            300,
                            2,
                            false,
                            false,
                            false
                        ));
                    }
                    
                    if (!player.getAbilities().allowFlying) {
                        PlayerAbilities abilities = player.getAbilities();
                        abilities.allowFlying = true;
                        player.sendAbilitiesUpdate();
                    }
                }
            }
        }
    }
    
    // TODO: PlayerEvent.PlayerRespawnEvent 暂不实现
    // 可通过 Mixin 注入 ServerPlayerEvents.AFTER_RESPAWN 来处理
    // Forge 版本在此事件中重置鸡兔之力的飞行状态
    //
    // 替代方案：在 Mixin 中实现 ServerPlayerEvents.AFTER_RESPAWN
    // 或在玩家登录/传送时检查 PersistentDataHelper 中的标记并恢复状态
    //
    // private static void onPlayerRespawn(PlayerEntity player) {
    //     if (RabbitTalisman.isChickenRabbitPowerActive(player)) {
    //         PlayerAbilities abilities = player.getAbilities();
    //         abilities.mayfly = true;
    //         abilities.flying = true;
    //         player.updateAbilities();
    //         ...
    //     }
    // }
    
    // TODO: PlayerEvent.PlayerChangedDimensionEvent 暂不实现
    // 可通过 Mixin 或 DimensionChange 相关事件来处理
    // Forge 版本在此事件中恢复鸡兔之力的飞行能力
    //
    // 替代方案：在玩家切换维度时检查 PersistentDataHelper 并恢复飞行能力
    //
    // private static void onPlayerChangedDimension(PlayerEntity player) {
    //     if (RabbitTalisman.isChickenRabbitPowerActive(player)) {
    //         PlayerAbilities abilities = player.getAbilities();
    //         abilities.mayfly = true;
    //         player.updateAbilities();
    //     }
    // }
}