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
        if (level.getBlockState(newPos).isAir() && level.getBlockState(newPos.above()).isAir()) {
            player.teleportTo(newPos.getX() + 0.5, newPos.getY(), newPos.getZ() + 0.5);
        }
    }
}