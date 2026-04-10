package com.qituo.dcc.talismans;

import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerLevel;

public class RabbitTalisman extends TalismanBase {
    public RabbitTalisman(Item.Properties properties) {
        super(properties);
    }
    
    @Override
    protected void useTalisman(ServerLevel level, Player player, InteractionHand hand) {
        // 检查是否有鸡兔之力（创造飞行）
        if (player.getAbilities().mayfly && player.hasEffect(com.qituo.dcc.effects.TalismanEffects.CHICKEN_POWER.get())) {
            // 取消鸡兔之力
            deactivateChickenRabbitPower(player);
        } else if (player.hasEffect(net.minecraft.world.effect.MobEffects.LEVITATION)) {
            // 激活鸡兔之力（创造飞行）
            activateChickenRabbitPower(player);
        } else {
            // 普通兔之力效果：增加移动速度
            activateRabbitPower(player);
        }
    }
    
    /**
     * 取消鸡兔之力
     */
    private void deactivateChickenRabbitPower(Player player) {
        // 关闭飞行
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        // 移除鸡之力效果图标
        player.removeEffect(com.qituo.dcc.effects.TalismanEffects.CHICKEN_POWER.get());
        // 同步能力变更
        player.onUpdateAbilities();
        
        net.minecraft.network.chat.Component message = net.minecraft.network.chat.Component.translatable("dcc.chicken_rabbit_power_deactivated");
        net.minecraft.network.chat.Component prefix = net.minecraft.network.chat.Component.translatable("dcc.mod_prefix");
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("").append(prefix).append(message));

    }
    
    /**
     * 激活普通兔之力
     */
    private void activateRabbitPower(Player player) {
        // 兔之力效果：增加移动速度
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            com.qituo.dcc.effects.TalismanEffects.RABBIT_POWER.get(),
            2 * 60 * 20, // 2分钟
            0, // 等级I
            false, // ambient
            false, // 不显示粒子
            true // 显示图标
        ));
        
        // 增加移动速度
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED,
            2 * 60 * 20, // 2分钟
            3, // 等级IV
            false, // ambient
            false, // 不显示粒子
            false // 不显示图标
        ));
        
        // 瞬间移动：向前移动5格
        net.minecraft.core.Direction direction = player.getDirection();
        net.minecraft.core.BlockPos currentPos = player.blockPosition();
        net.minecraft.core.BlockPos newPos = currentPos.relative(direction, 5);
        
        // 检查目标位置是否安全
        if (player.level().getBlockState(newPos).isAir() && player.level().getBlockState(newPos.above()).isAir()) {
            player.teleportTo(newPos.getX() + 0.5, newPos.getY(), newPos.getZ() + 0.5);
        }
    }
    
    /**
     * 激活鸡兔之力（创造飞行）
     */
    private void activateChickenRabbitPower(Player player) {
        // 移除漂浮效果
        player.removeEffect(net.minecraft.world.effect.MobEffects.LEVITATION);
        
        // 开启飞行
        player.getAbilities().mayfly = true;
        player.getAbilities().flying = true;
        
        // 添加鸡兔之力效果图标
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            com.qituo.dcc.effects.TalismanEffects.CHICKEN_POWER.get(),
            Integer.MAX_VALUE, // 无限时间
            1, // 等级II（区分普通鸡之力）
            false, // ambient
            false, // 不显示粒子
            true // 显示图标
        ));
        
        // 同步能力变更
        player.onUpdateAbilities();
        
        net.minecraft.network.chat.Component message = net.minecraft.network.chat.Component.translatable("dcc.chicken_rabbit_power_activated");
        net.minecraft.network.chat.Component prefix = net.minecraft.network.chat.Component.translatable("dcc.mod_prefix");
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("").append(prefix).append(message));

    }
}