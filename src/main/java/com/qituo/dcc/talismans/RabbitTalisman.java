package com.qituo.dcc.talismans;

import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;

public class RabbitTalisman extends TalismanBase {
    
    private static final String CHICKEN_RABBIT_POWER_KEY = "dcc_chicken_rabbit_power_active";
    private static final String CHICKEN_RABBIT_POWER_LEVEL = "dcc_chicken_rabbit_power_level";
    
    public RabbitTalisman(Item.Properties properties) {
        super(properties);
    }
    
    @Override
    protected void useTalisman(ServerLevel level, Player player, InteractionHand hand) {
        if (isChickenRabbitPowerActive(player)) {
            deactivateChickenRabbitPower(player);
        } else if (player.hasEffect(net.minecraft.world.effect.MobEffects.LEVITATION)) {
            activateChickenRabbitPower(player);
        } else {
            activateRabbitPower(player);
        }
    }
    
    /**
     * 检查玩家是否激活了鸡兔之力
     */
    public static boolean isChickenRabbitPowerActive(Player player) {
        CompoundTag persistentData = player.getPersistentData();
        return persistentData.getBoolean(CHICKEN_RABBIT_POWER_KEY);
    }
    
    /**
     * 获取鸡兔之力等级
     */
    public static int getChickenRabbitPowerLevel(Player player) {
        CompoundTag persistentData = player.getPersistentData();
        return persistentData.getInt(CHICKEN_RABBIT_POWER_LEVEL);
    }
    
    /**
     * 取消鸡兔之力
     */
    private void deactivateChickenRabbitPower(Player player) {
        CompoundTag persistentData = player.getPersistentData();
        persistentData.putBoolean(CHICKEN_RABBIT_POWER_KEY, false);
        
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.removeEffect(com.qituo.dcc.effects.TalismanEffects.CHICKEN_POWER.get());
        player.onUpdateAbilities();
        
        sendMessage(player, "dcc.chicken_rabbit_power_deactivated");
    }
    
    /**
     * 激活普通兔之力
     */
    private void activateRabbitPower(Player player) {
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            com.qituo.dcc.effects.TalismanEffects.RABBIT_POWER.get(),
            2 * 60 * 20,
            0,
            false,
            false,
            true
        ));
        
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED,
            2 * 60 * 20,
            3,
            false,
            false,
            false
        ));
        
        net.minecraft.core.Direction direction = player.getDirection();
        net.minecraft.core.BlockPos currentPos = player.blockPosition();
        net.minecraft.core.BlockPos newPos = currentPos.relative(direction, 5);
        
        if (player.level().getBlockState(newPos).isAir() && player.level().getBlockState(newPos.above()).isAir()) {
            player.teleportTo(newPos.getX() + 0.5, newPos.getY(), newPos.getZ() + 0.5);
        }
    }
    
    /**
     * 激活鸡兔之力（创造飞行）
     */
    private void activateChickenRabbitPower(Player player) {
        CompoundTag persistentData = player.getPersistentData();
        persistentData.putBoolean(CHICKEN_RABBIT_POWER_KEY, true);
        persistentData.putInt(CHICKEN_RABBIT_POWER_LEVEL, 1);
        
        player.removeEffect(net.minecraft.world.effect.MobEffects.LEVITATION);
        
        player.getAbilities().mayfly = true;
        player.getAbilities().flying = true;
        
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            com.qituo.dcc.effects.TalismanEffects.CHICKEN_POWER.get(),
            20 * 60 * 20,
            1,
            false,
            false,
            true
        ));
        
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED,
            20 * 60 * 20,
            1,
            false,
            false,
            false
        ));
        
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.JUMP,
            20 * 60 * 20,
            2,
            false,
            false,
            false
        ));
        
        player.onUpdateAbilities();
        
        sendMessage(player, "dcc.chicken_rabbit_power_activated");
    }
    
    /**
     * 发送消息给玩家
     */
    protected void sendMessage(Player player, String key) {
        net.minecraft.network.chat.Component message = net.minecraft.network.chat.Component.translatable(key);
        net.minecraft.network.chat.Component prefix = net.minecraft.network.chat.Component.translatable("dcc.mod_prefix");
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("").append(prefix).append(message));
    }
}
