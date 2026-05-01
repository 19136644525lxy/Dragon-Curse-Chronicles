package com.qituo.dcc.talismans;

import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import com.qituo.dcc.effects.TalismanEffects;
import com.qituo.dcc.config.TalismanConfig;

public class SheepTalisman extends TalismanBase {
    private static final String SOUL_MODE_KEY = "SoulModeActive";
    private static final String BODY_POS_X = "BodyPosX";
    private static final String BODY_POS_Y = "BodyPosY";
    private static final String BODY_POS_Z = "BodyPosZ";

    public SheepTalisman(Item.Properties properties) {
        super(properties);
    }

    @Override
    protected void useTalisman(ServerLevel level, Player player, InteractionHand hand) {
        if (isInSoulMode(player)) {
            deactivateSoulMode(player);
            sendMessage(player, Component.translatable("dcc.message.sheep_talisman.exit"));
        } else {
            activateSoulMode(player);
            sendMessage(player, Component.translatable("dcc.message.sheep_talisman.enter"));
        }
    }

    private boolean isInSoulMode(Player player) {
        return player.getPersistentData().getBoolean(SOUL_MODE_KEY);
    }

    private void activateSoulMode(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            ServerLevel level = serverPlayer.serverLevel();
            Vec3 pos = serverPlayer.position();

            serverPlayer.getPersistentData().putBoolean(SOUL_MODE_KEY, true);

            serverPlayer.getPersistentData().putDouble(BODY_POS_X, pos.x);
            serverPlayer.getPersistentData().putDouble(BODY_POS_Y, pos.y);
            serverPlayer.getPersistentData().putDouble(BODY_POS_Z, pos.z);

            spawnSoulParticles(level, pos);

            serverPlayer.addEffect(new MobEffectInstance(TalismanEffects.SHEEP_POWER.get(), 999999, 0, false, false, true));
            serverPlayer.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 999999, 0, false, false, true));
            serverPlayer.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 999999, 0, false, false, true));

            // 在激活时就设置好所有属性，确保疾跑立即生效
            serverPlayer.noPhysics = true;
            serverPlayer.setOnGround(true);
            serverPlayer.setSpeed(1.0f);

            // 设置飞行能力
            Abilities abilities = serverPlayer.getAbilities();
            abilities.mayfly = true;
            abilities.flying = true;
            // 使用配置文件中的飞行速度
            try {
                java.lang.reflect.Field flyingSpeedField = Abilities.class.getDeclaredField("flyingSpeed");
                flyingSpeedField.setAccessible(true);
                flyingSpeedField.set(abilities, (float) TalismanConfig.getSheepTalismanFlySpeed());
                java.lang.reflect.Field walkSpeedField = Abilities.class.getDeclaredField("walkSpeed");
                walkSpeedField.setAccessible(true);
                walkSpeedField.set(abilities, (float) TalismanConfig.getSheepTalismanFlySpeed());
            } catch (Exception e) {
                // 忽略错误，使用默认值
            }
            serverPlayer.onUpdateAbilities();

            double soulHeight = 2.0;
            serverPlayer.connection.teleport(pos.x, pos.y + soulHeight, pos.z, serverPlayer.getYRot(), serverPlayer.getXRot());

            spawnSoulAscentParticles(level, pos, pos.y + soulHeight);
        }
    }

    private void deactivateSoulMode(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            ServerLevel level = serverPlayer.serverLevel();
            Vec3 currentPos = serverPlayer.position();

            spawnReturnParticles(level, currentPos);

            serverPlayer.getPersistentData().remove(SOUL_MODE_KEY);

            serverPlayer.removeEffect(TalismanEffects.SHEEP_POWER.get());
            serverPlayer.removeEffect(MobEffects.INVISIBILITY);
            serverPlayer.removeEffect(MobEffects.NIGHT_VISION);

            serverPlayer.noPhysics = false;
            serverPlayer.setInvulnerable(false);
            serverPlayer.getAbilities().mayfly = serverPlayer.isCreative() || serverPlayer.isSpectator();
            serverPlayer.getAbilities().flying = serverPlayer.getAbilities().mayfly && serverPlayer.getAbilities().flying;
            serverPlayer.onUpdateAbilities();

            if (serverPlayer.getPersistentData().contains(BODY_POS_X)) {
                double x = serverPlayer.getPersistentData().getDouble(BODY_POS_X);
                double y = serverPlayer.getPersistentData().getDouble(BODY_POS_Y);
                double z = serverPlayer.getPersistentData().getDouble(BODY_POS_Z);

                spawnEnterBodyParticles(level, new Vec3(x, y, z), currentPos);

                serverPlayer.connection.teleport(x, y, z, serverPlayer.getYRot(), serverPlayer.getXRot());

                serverPlayer.getPersistentData().remove(BODY_POS_X);
                serverPlayer.getPersistentData().remove(BODY_POS_Y);
                serverPlayer.getPersistentData().remove(BODY_POS_Z);
            }

            if (serverPlayer.fallDistance > 0) {
                serverPlayer.fallDistance = 0;
            }
        }
    }

    private void spawnSoulParticles(ServerLevel level, Vec3 pos) {
        for (int i = 0; i < 30; i++) {
            double dx = (level.random.nextDouble() - 0.5) * 3;
            double dy = (level.random.nextDouble() - 0.5) * 3;
            double dz = (level.random.nextDouble() - 0.5) * 3;
            level.sendParticles(ParticleTypes.SOUL, pos.x, pos.y + 1, pos.z, 1, dx, dy, dz, 0.1);
        }

        for (int i = 0; i < 15; i++) {
            double dx = (level.random.nextDouble() - 0.5) * 0.3;
            double dz = (level.random.nextDouble() - 0.5) * 0.3;
            level.sendParticles(ParticleTypes.SOUL, pos.x, pos.y + 1, pos.z, 1, dx, 0.8, dz, 0.05);
        }
    }

    private void spawnSoulAscentParticles(ServerLevel level, Vec3 startPos, double endY) {
        double step = (endY - startPos.y) / 20;
        for (int i = 0; i < 20; i++) {
            double y = startPos.y + step * i;
            for (int j = 0; j < 3; j++) {
                double dx = (level.random.nextDouble() - 0.5) * 0.2;
                double dz = (level.random.nextDouble() - 0.5) * 0.2;
                level.sendParticles(ParticleTypes.SOUL, startPos.x + dx, y, startPos.z + dz, 1, 0, 0, 0, 0);
            }
        }
    }

    private void spawnReturnParticles(ServerLevel level, Vec3 pos) {
        for (int i = 0; i < 25; i++) {
            double dx = (level.random.nextDouble() - 0.5) * 4;
            double dy = (level.random.nextDouble() - 0.5) * 4;
            double dz = (level.random.nextDouble() - 0.5) * 4;
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.x + dx, pos.y + dy + 1, pos.z + dz, 1, -dx * 0.1, -dy * 0.1, -dz * 0.1, 0.1);
        }
    }

    private void spawnEnterBodyParticles(ServerLevel level, Vec3 bodyPos, Vec3 soulPos) {
        Vec3 direction = bodyPos.subtract(soulPos).normalize();
        for (int i = 0; i < 30; i++) {
            Vec3 start = soulPos.add(
                (level.random.nextDouble() - 0.5) * 2,
                (level.random.nextDouble() - 0.5) * 2,
                (level.random.nextDouble() - 0.5) * 2
            );
            level.sendParticles(ParticleTypes.SOUL,
                start.x, start.y, start.z,
                1,
                direction.x * 0.3, direction.y * 0.3, direction.z * 0.3,
                0.05);
        }
    }

    private void sendMessage(Player player, Component message) {
        player.sendSystemMessage(message);
    }
}