package com.qituo.dcc.talismans;

import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerLevel;

public class ChickenTalisman extends TalismanBase {
    // 效果类型：0-漂浮之力，1-缓降之力
    private static final String CHICKEN_TALISMAN_MODE = "chicken_talisman_mode";
    
    public ChickenTalisman(Item.Properties properties) {
        super(properties);
    }
    
    @Override
    protected void useTalisman(ServerLevel level, Player player, InteractionHand hand) {
        // 检查是否按下Shift键
        if (player.isShiftKeyDown()) {
            // 切换效果模式
            int currentMode = getCurrentMode(player);
            int newMode = (currentMode + 1) % 2;
            setCurrentMode(player, newMode);
            
            // 发送切换消息
            String modeKey = newMode == 0 ? "dcc.levitation_power" : "dcc.slow_falling_power";
            net.minecraft.network.chat.Component modeName = net.minecraft.network.chat.Component.translatable(modeKey);
            net.minecraft.network.chat.Component message = net.minecraft.network.chat.Component.translatable("dcc.switched_to_mode", modeName);
            net.minecraft.network.chat.Component prefix = net.minecraft.network.chat.Component.translatable("dcc.mod_prefix");
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("").append(prefix).append(message));

        } else {
            // 检查是否已经有效果
            if (player.hasEffect(net.minecraft.world.effect.MobEffects.LEVITATION) || 
                player.hasEffect(net.minecraft.world.effect.MobEffects.SLOW_FALLING)) {
                // 取消效果
                deactivateEffects(player);
            } else {
                // 启动当前选择的效果
                int currentMode = getCurrentMode(player);
                
                if (currentMode == 0) {
                    // 漂浮之力
                    activateLevitation(player);
                } else {
                    // 缓降之力
                    activateSlowFalling(player);
                }
            }
        }
    }
    
    /**
     * 取消所有鸡符咒效果
     */
    private void deactivateEffects(Player player) {
        // 移除漂浮效果
        player.removeEffect(net.minecraft.world.effect.MobEffects.LEVITATION);
        // 移除缓降效果
        player.removeEffect(net.minecraft.world.effect.MobEffects.SLOW_FALLING);
        // 移除鸡之力效果图标
        player.removeEffect(com.qituo.dcc.effects.TalismanEffects.CHICKEN_POWER.get());
        
        net.minecraft.network.chat.Component message = net.minecraft.network.chat.Component.translatable("dcc.chicken_talisman_deactivated");
        net.minecraft.network.chat.Component prefix = net.minecraft.network.chat.Component.translatable("dcc.mod_prefix");
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("").append(prefix).append(message));

    }
    
    /**
     * 获取当前效果模式
     */
    private int getCurrentMode(Player player) {
        return player.getPersistentData().getInt(CHICKEN_TALISMAN_MODE);
    }
    
    /**
     * 设置当前效果模式
     */
    private void setCurrentMode(Player player, int mode) {
        player.getPersistentData().putInt(CHICKEN_TALISMAN_MODE, mode);
    }
    
    /**
     * 激活漂浮之力
     */
    private void activateLevitation(Player player) {
        // 移除缓降效果（如果存在）
        player.removeEffect(net.minecraft.world.effect.MobEffects.SLOW_FALLING);
        
        // 添加漂浮效果
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.LEVITATION,
            Integer.MAX_VALUE, // 无限时间
            0, // 等级I
            false, // ambient
            false, // 不显示粒子
            true // 显示图标
        ));
        
        // 添加鸡之力效果图标
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            com.qituo.dcc.effects.TalismanEffects.CHICKEN_POWER.get(),
            Integer.MAX_VALUE, // 无限时间
            0, // 等级I
            false, // ambient
            false, // 不显示粒子
            true // 显示图标
        ));
        
        net.minecraft.network.chat.Component message = net.minecraft.network.chat.Component.translatable("dcc.levitation_activated");
        net.minecraft.network.chat.Component prefix = net.minecraft.network.chat.Component.translatable("dcc.mod_prefix");
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("").append(prefix).append(message));

    }
    
    /**
     * 激活缓降之力
     */
    private void activateSlowFalling(Player player) {
        // 移除漂浮效果（如果存在）
        player.removeEffect(net.minecraft.world.effect.MobEffects.LEVITATION);
        
        // 添加缓降效果
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.SLOW_FALLING,
            Integer.MAX_VALUE, // 无限时间
            0, // 等级I
            false, // ambient
            false, // 不显示粒子
            true // 显示图标
        ));
        
        // 添加鸡之力效果图标
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            com.qituo.dcc.effects.TalismanEffects.CHICKEN_POWER.get(),
            Integer.MAX_VALUE, // 无限时间
            0, // 等级I
            false, // ambient
            false, // 不显示粒子
            true // 显示图标
        ));
        
        net.minecraft.network.chat.Component message = net.minecraft.network.chat.Component.translatable("dcc.slow_falling_activated");
        net.minecraft.network.chat.Component prefix = net.minecraft.network.chat.Component.translatable("dcc.mod_prefix");
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("").append(prefix).append(message));

    }
}