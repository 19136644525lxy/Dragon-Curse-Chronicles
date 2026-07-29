package com.qituo.dcc.talismans;

import net.minecraft.item.Item;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.text.Text;
import com.qituo.dcc.effects.TalismanEffects;
import com.qituo.dcc.util.PersistentDataHelper;

public class ChickenTalisman extends TalismanBase {
    // 效果类型：0-漂浮之力，1-缓降之力
    private static final String CHICKEN_TALISMAN_MODE = "chicken_talisman_mode";
    
    public ChickenTalisman(Item.Settings settings) {
        super(settings);
    }
    
    @Override
    protected void useTalisman(ServerWorld world, PlayerEntity player, Hand hand) {
        // 检查是否按下Shift键
        if (player.isSneaking()) {
            // 切换效果模式
            int currentMode = getCurrentMode(player);
            int newMode = (currentMode + 1) % 2;
            setCurrentMode(player, newMode);
            
            // 发送切换消息
            String modeKey = newMode == 0 ? "dcc.levitation_power" : "dcc.slow_falling_power";
            Text modeName = Text.translatable(modeKey);
            Text message = Text.translatable("dcc.switched_to_mode", modeName);
            Text prefix = Text.translatable("dcc.mod_prefix");
            player.sendMessage(Text.literal("").append(prefix).append(message));

        } else {
            // 检查是否已经有效果
            if (player.hasStatusEffect(StatusEffects.LEVITATION) || 
                player.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
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
    private void deactivateEffects(PlayerEntity player) {
        // 移除漂浮效果
        player.removeStatusEffect(StatusEffects.LEVITATION);
        // 移除缓降效果
        player.removeStatusEffect(StatusEffects.SLOW_FALLING);
        // 移除鸡之力效果图标
        player.removeStatusEffect(TalismanEffects.CHICKEN_POWER);
        
        Text message = Text.translatable("dcc.chicken_talisman_deactivated");
        Text prefix = Text.translatable("dcc.mod_prefix");
        player.sendMessage(Text.literal("").append(prefix).append(message));

    }
    
    /**
     * 获取当前效果模式
     */
    private int getCurrentMode(PlayerEntity player) {
        return PersistentDataHelper.getInt(player, CHICKEN_TALISMAN_MODE);
    }
    
    /**
     * 设置当前效果模式
     */
    private void setCurrentMode(PlayerEntity player, int mode) {
        PersistentDataHelper.putInt(player, CHICKEN_TALISMAN_MODE, mode);
    }
    
    /**
     * 激活漂浮之力
     */
    private void activateLevitation(PlayerEntity player) {
        // 移除缓降效果（如果存在）
        player.removeStatusEffect(StatusEffects.SLOW_FALLING);
        
        // 添加漂浮效果
        player.addStatusEffect(new StatusEffectInstance(
            StatusEffects.LEVITATION,
            Integer.MAX_VALUE, // 无限时间
            0, // 等级I
            false, // ambient
            false, // 不显示粒子
            true // 显示图标
        ));
        
        // 添加鸡之力效果图标
        player.addStatusEffect(new StatusEffectInstance(
            TalismanEffects.CHICKEN_POWER,
            Integer.MAX_VALUE, // 无限时间
            0, // 等级I
            false, // ambient
            false, // 不显示粒子
            true // 显示图标
        ));
        
        Text message = Text.translatable("dcc.levitation_activated");
        Text prefix = Text.translatable("dcc.mod_prefix");
        player.sendMessage(Text.literal("").append(prefix).append(message));

    }
    
    /**
     * 激活缓降之力
     */
    private void activateSlowFalling(PlayerEntity player) {
        // 移除漂浮效果（如果存在）
        player.removeStatusEffect(StatusEffects.LEVITATION);
        
        // 添加缓降效果
        player.addStatusEffect(new StatusEffectInstance(
            StatusEffects.SLOW_FALLING,
            Integer.MAX_VALUE, // 无限时间
            0, // 等级I
            false, // ambient
            false, // 不显示粒子
            true // 显示图标
        ));
        
        // 添加鸡之力效果图标
        player.addStatusEffect(new StatusEffectInstance(
            TalismanEffects.CHICKEN_POWER,
            Integer.MAX_VALUE, // 无限时间
            0, // 等级I
            false, // ambient
            false, // 不显示粒子
            true // 显示图标
        ));
        
        Text message = Text.translatable("dcc.slow_falling_activated");
        Text prefix = Text.translatable("dcc.mod_prefix");
        player.sendMessage(Text.literal("").append(prefix).append(message));

    }
}