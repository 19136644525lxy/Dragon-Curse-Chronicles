package com.qituo.dcc.talismans;

import net.minecraft.item.Item;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.util.Hand;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.particle.ParticleTypes;
import com.qituo.dcc.effects.TalismanEffects;
import com.qituo.dcc.config.TalismanConfig;
import com.qituo.dcc.util.PersistentDataHelper;
import java.lang.reflect.Field;

public class SheepTalisman extends TalismanBase {
    private static final String SOUL_MODE_KEY = "SoulModeActive";
    private static final String BODY_POS_X = "BodyPosX";
    private static final String BODY_POS_Y = "BodyPosY";
    private static final String BODY_POS_Z = "BodyPosZ";

    public SheepTalisman(Item.Settings settings) {
        super(settings);
    }

    @Override
    protected void useTalisman(ServerWorld world, PlayerEntity player, Hand hand) {
        if (isInSoulMode(player)) {
            deactivateSoulMode(player);
            sendMessage(player, Text.translatable("dcc.message.sheep_talisman.exit"));
        } else {
            activateSoulMode(player);
            sendMessage(player, Text.translatable("dcc.message.sheep_talisman.enter"));
        }
    }

    private boolean isInSoulMode(PlayerEntity player) {
        return PersistentDataHelper.getBoolean(player, SOUL_MODE_KEY);
    }

    private void activateSoulMode(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            ServerWorld world = serverPlayer.getServerWorld();
            Vec3d pos = serverPlayer.getPos();

            PersistentDataHelper.putBoolean(serverPlayer, SOUL_MODE_KEY, true);

            PersistentDataHelper.putDouble(serverPlayer, BODY_POS_X, pos.x);
            PersistentDataHelper.putDouble(serverPlayer, BODY_POS_Y, pos.y);
            PersistentDataHelper.putDouble(serverPlayer, BODY_POS_Z, pos.z);

            spawnSoulParticles(world, pos);

            serverPlayer.addStatusEffect(new StatusEffectInstance(TalismanEffects.SHEEP_POWER, 999999, 0, false, false, true));
            serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 999999, 0, false, false, true));
            serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 999999, 0, false, false, true));

            // 在激活时就设置好所有属性，确保疾跑立即生效
            serverPlayer.noClip = true;
            serverPlayer.setOnGround(true);
            // setSpeed在1.20.1中不存在，通过abilities设置行走速度
            serverPlayer.getAbilities().setWalkSpeed(1.0f);

            // 设置飞行能力
            PlayerAbilities abilities = serverPlayer.getAbilities();
            abilities.allowFlying = true;
            abilities.flying = true;
            // 使用配置文件中的飞行速度
            try {
                Field flyingSpeedField = PlayerAbilities.class.getDeclaredField("flyingSpeed");
                flyingSpeedField.setAccessible(true);
                flyingSpeedField.set(abilities, (float) TalismanConfig.getSheepTalismanFlySpeed());
                Field walkSpeedField = PlayerAbilities.class.getDeclaredField("walkSpeed");
                walkSpeedField.setAccessible(true);
                walkSpeedField.set(abilities, (float) TalismanConfig.getSheepTalismanFlySpeed());
            } catch (Exception e) {
                // 忽略错误，使用默认值
            }
            serverPlayer.sendAbilitiesUpdate();

            double soulHeight = 2.0;
            serverPlayer.networkHandler.requestTeleport(pos.x, pos.y + soulHeight, pos.z, serverPlayer.getYaw(), serverPlayer.getPitch());

            spawnSoulAscentParticles(world, pos, pos.y + soulHeight);
        }
    }

    private void deactivateSoulMode(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            ServerWorld world = serverPlayer.getServerWorld();
            Vec3d currentPos = serverPlayer.getPos();

            spawnReturnParticles(world, currentPos);

            PersistentDataHelper.remove(serverPlayer, SOUL_MODE_KEY);

            serverPlayer.removeStatusEffect(TalismanEffects.SHEEP_POWER);
            serverPlayer.removeStatusEffect(StatusEffects.INVISIBILITY);
            serverPlayer.removeStatusEffect(StatusEffects.NIGHT_VISION);

            serverPlayer.noClip = false;
            serverPlayer.setInvulnerable(false);
            serverPlayer.getAbilities().allowFlying = serverPlayer.isCreative() || serverPlayer.isSpectator();
            serverPlayer.getAbilities().flying = serverPlayer.getAbilities().allowFlying && serverPlayer.getAbilities().flying;
            serverPlayer.sendAbilitiesUpdate();

            if (PersistentDataHelper.contains(serverPlayer, BODY_POS_X)) {
                double x = PersistentDataHelper.getDouble(serverPlayer, BODY_POS_X);
                double y = PersistentDataHelper.getDouble(serverPlayer, BODY_POS_Y);
                double z = PersistentDataHelper.getDouble(serverPlayer, BODY_POS_Z);

                spawnEnterBodyParticles(world, new Vec3d(x, y, z), currentPos);

                serverPlayer.networkHandler.requestTeleport(x, y, z, serverPlayer.getYaw(), serverPlayer.getPitch());

                PersistentDataHelper.remove(serverPlayer, BODY_POS_X);
                PersistentDataHelper.remove(serverPlayer, BODY_POS_Y);
                PersistentDataHelper.remove(serverPlayer, BODY_POS_Z);
            }

            if (serverPlayer.fallDistance > 0) {
                serverPlayer.fallDistance = 0;
            }
        }
    }

    private void spawnSoulParticles(ServerWorld world, Vec3d pos) {
        for (int i = 0; i < 30; i++) {
            double dx = (world.random.nextDouble() - 0.5) * 3;
            double dy = (world.random.nextDouble() - 0.5) * 3;
            double dz = (world.random.nextDouble() - 0.5) * 3;
            world.spawnParticles(ParticleTypes.SOUL, pos.x, pos.y + 1, pos.z, 1, dx, dy, dz, 0.1);
        }

        for (int i = 0; i < 15; i++) {
            double dx = (world.random.nextDouble() - 0.5) * 0.3;
            double dz = (world.random.nextDouble() - 0.5) * 0.3;
            world.spawnParticles(ParticleTypes.SOUL, pos.x, pos.y + 1, pos.z, 1, dx, 0.8, dz, 0.05);
        }
    }

    private void spawnSoulAscentParticles(ServerWorld world, Vec3d startPos, double endY) {
        double step = (endY - startPos.y) / 20;
        for (int i = 0; i < 20; i++) {
            double y = startPos.y + step * i;
            for (int j = 0; j < 3; j++) {
                double dx = (world.random.nextDouble() - 0.5) * 0.2;
                double dz = (world.random.nextDouble() - 0.5) * 0.2;
                world.spawnParticles(ParticleTypes.SOUL, startPos.x + dx, y, startPos.z + dz, 1, 0, 0, 0, 0);
            }
        }
    }

    private void spawnReturnParticles(ServerWorld world, Vec3d pos) {
        for (int i = 0; i < 25; i++) {
            double dx = (world.random.nextDouble() - 0.5) * 4;
            double dy = (world.random.nextDouble() - 0.5) * 4;
            double dz = (world.random.nextDouble() - 0.5) * 4;
            world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.x + dx, pos.y + dy + 1, pos.z + dz, 1, -dx * 0.1, -dy * 0.1, -dz * 0.1, 0.1);
        }
    }

    private void spawnEnterBodyParticles(ServerWorld world, Vec3d bodyPos, Vec3d soulPos) {
        Vec3d direction = bodyPos.subtract(soulPos).normalize();
        for (int i = 0; i < 30; i++) {
            Vec3d start = soulPos.add(
                (world.random.nextDouble() - 0.5) * 2,
                (world.random.nextDouble() - 0.5) * 2,
                (world.random.nextDouble() - 0.5) * 2
            );
            world.spawnParticles(ParticleTypes.SOUL,
                start.x, start.y, start.z,
                1,
                direction.x * 0.3, direction.y * 0.3, direction.z * 0.3,
                0.05);
        }
    }

    private void sendMessage(PlayerEntity player, Text message) {
        player.sendMessage(message);
    }
}