package com.qituo.dcc.talismans;

import net.minecraft.item.Item;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import com.qituo.dcc.effects.TalismanEffects;
import com.qituo.dcc.util.PersistentDataHelper;

public class RabbitTalisman extends TalismanBase {
    
    private static final String CHICKEN_RABBIT_POWER_KEY = "dcc_chicken_rabbit_power_active";
    private static final String CHICKEN_RABBIT_POWER_LEVEL = "dcc_chicken_rabbit_power_level";
    
    public RabbitTalisman(Item.Settings settings) {
        super(settings);
    }
    
    @Override
    protected void useTalisman(ServerWorld world, PlayerEntity player, Hand hand) {
        if (isChickenRabbitPowerActive(player)) {
            deactivateChickenRabbitPower(player);
        } else if (player.hasStatusEffect(StatusEffects.LEVITATION)) {
            activateChickenRabbitPower(player);
        } else {
            activateRabbitPower(player);
        }
    }
    
    /**
     * 检查玩家是否激活了鸡兔之力
     */
    public static boolean isChickenRabbitPowerActive(PlayerEntity player) {
        return PersistentDataHelper.getBoolean(player, CHICKEN_RABBIT_POWER_KEY);
    }
    
    /**
     * 获取鸡兔之力等级
     */
    public static int getChickenRabbitPowerLevel(PlayerEntity player) {
        return PersistentDataHelper.getInt(player, CHICKEN_RABBIT_POWER_LEVEL);
    }
    
    /**
     * 取消鸡兔之力
     */
    private void deactivateChickenRabbitPower(PlayerEntity player) {
        PersistentDataHelper.putBoolean(player, CHICKEN_RABBIT_POWER_KEY, false);
        
        PlayerAbilities abilities = player.getAbilities();
        // 恢复创造/旁观模式的飞行权限，非创造模式才关闭
        abilities.allowFlying = player.isCreative() || player.isSpectator();
        abilities.flying = abilities.allowFlying && abilities.flying;
        player.removeStatusEffect(TalismanEffects.CHICKEN_POWER);
        if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
            serverPlayer.sendAbilitiesUpdate();
        }
        
        sendMessage(player, "dcc.chicken_rabbit_power_deactivated");
    }
    
    /**
     * 激活普通兔之力
     */
    private void activateRabbitPower(PlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(
            TalismanEffects.RABBIT_POWER,
            2 * 60 * 20,
            0,
            false,
            false,
            true
        ));
        
        player.addStatusEffect(new StatusEffectInstance(
            StatusEffects.SPEED,
            2 * 60 * 20,
            3,
            false,
            false,
            false
        ));
        
        Direction direction = player.getHorizontalFacing();
        BlockPos currentPos = player.getBlockPos();
        BlockPos newPos = currentPos.offset(direction, 5);
        
        if (player.getWorld().getBlockState(newPos).isAir() && player.getWorld().getBlockState(newPos.up()).isAir()) {
            player.teleport(newPos.getX() + 0.5, newPos.getY(), newPos.getZ() + 0.5);
        }
    }
    
    /**
     * 激活鸡兔之力（创造飞行）
     */
    private void activateChickenRabbitPower(PlayerEntity player) {
        PersistentDataHelper.putBoolean(player, CHICKEN_RABBIT_POWER_KEY, true);
        PersistentDataHelper.putInt(player, CHICKEN_RABBIT_POWER_LEVEL, 1);
        
        player.removeStatusEffect(StatusEffects.LEVITATION);
        
        PlayerAbilities abilities = player.getAbilities();
        abilities.allowFlying = true;
        abilities.flying = true;

        player.addStatusEffect(new StatusEffectInstance(
            TalismanEffects.CHICKEN_POWER,
            20 * 60 * 20,
            1,
            false,
            false,
            true
        ));

        player.addStatusEffect(new StatusEffectInstance(
            StatusEffects.SPEED,
            20 * 60 * 20,
            1,
            false,
            false,
            false
        ));

        player.addStatusEffect(new StatusEffectInstance(
            StatusEffects.JUMP_BOOST,
            20 * 60 * 20,
            2,
            false,
            false,
            false
        ));

        if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
            serverPlayer.sendAbilitiesUpdate();
        }
        
        sendMessage(player, "dcc.chicken_rabbit_power_activated");
    }
    
    /**
     * 发送消息给玩家
     */
    protected void sendMessage(PlayerEntity player, String key) {
        Text message = Text.translatable(key);
        Text prefix = Text.translatable("dcc.mod_prefix");
        player.sendMessage(Text.literal("").append(prefix).append(message));
    }
}