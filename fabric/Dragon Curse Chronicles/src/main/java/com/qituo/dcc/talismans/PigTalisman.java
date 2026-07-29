package com.qituo.dcc.talismans;

import net.minecraft.item.Item;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PigTalisman extends TalismanBase {
    private static final Random random = new Random();
    // 冷却时间（以刻为单位，20刻=1秒）
    private static final int COOLDOWN_TICKS = 40; // 2秒

    public PigTalisman(Item.Settings settings) {
        super(settings);
    }

    @Override
    protected void useTalisman(ServerWorld world, PlayerEntity player, Hand hand) {
        // 播放激光发射音效
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F);

        // 获取玩家视线方向
        Vec3d lookVec = getLookVec(player);
        Vec3d eyePos = player.getEyePos();

        // 创建射线检测
        double reachDistance = 50.0;
        Vec3d endPos = eyePos.add(lookVec.x * reachDistance, lookVec.y * reachDistance, lookVec.z * reachDistance);

        // 先检测方块
        RaycastContext context = new RaycastContext(
            eyePos,
            endPos,
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.NONE,
            player
        );
        BlockHitResult blockHitResult = world.raycast(context);

        // 计算激光长度
        double laserLength = reachDistance;
        if (blockHitResult.getType() != HitResult.Type.MISS) {
            laserLength = eyePos.distanceTo(blockHitResult.getPos());
        }

        // 检测实体
        HitResult hitResult = blockHitResult;
        LivingEntity targetEntity = null;

        // 使用 AABB 检测实体
        Box aabb = new Box(
            eyePos.x - 0.5, eyePos.y - 0.5, eyePos.z - 0.5,
            endPos.x + 0.5, endPos.y + 0.5, endPos.z + 0.5
        );

        List<Entity> entities = world.getOtherEntities(
            player,
            aabb,
            entity -> entity instanceof LivingEntity && entity.isAlive()
        );

        double closestDistance = reachDistance;
        for (Entity entity : entities) {
            Box entityAabb = entity.getBoundingBox();

            // 检查射线是否与实体的 AABB 相交
            Vec3d intersection = eyePos;
            Vec3d direction = endPos.subtract(eyePos);
            double length = direction.length();
            direction = direction.normalize();

            boolean hit = false;
            for (double t = 0.0; t <= length; t += 0.1) {
                Vec3d currentPos = eyePos.add(direction.multiply(t));
                if (entityAabb.contains(currentPos)) {
                    intersection = currentPos;
                    hit = true;
                    break;
                }
            }

            if (hit) {
                double distance = eyePos.distanceTo(intersection);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    targetEntity = (LivingEntity) entity;
                    hitResult = new EntityHitResult(entity, intersection);
                    laserLength = distance;
                }
            }
        }

        // 添加原版激光粒子效果
        addLaserParticles(world, eyePos, lookVec, laserLength);

        // 对击中的实体造成伤害
        if (targetEntity != null) {
            // 造成99点伤害
            targetEntity.damage(world.getDamageSources().playerAttack(player), 99.0f);

            // 点燃目标
            targetEntity.setFireTicks(100);

            // 在目标位置创建爆炸粒子效果
            createExplosionEffect(world, targetEntity.getPos());
        } else if (hitResult.getType() == HitResult.Type.BLOCK) {
            // 对击中的方块点燃
            BlockPos hitPos = ((BlockHitResult) hitResult).getBlockPos();
            BlockPos firePos = hitPos.offset(((BlockHitResult) hitResult).getSide());

            if (world.getBlockState(firePos).isAir()) {
                world.setBlockState(firePos, Blocks.FIRE.getDefaultState(), 11);
            }

            // 在击中位置创建爆炸粒子效果
            createExplosionEffect(world, hitResult.getPos());
        }

        // 设置冷却时间
        player.getItemCooldownManager().set(this, COOLDOWN_TICKS);
    }

    /**
     * 获取玩家视线向量
     */
    private Vec3d getLookVec(PlayerEntity player) {
        float yawRad = player.getYaw() * ((float)Math.PI / 180.0f);
        float pitchRad = player.getPitch() * ((float)Math.PI / 180.0f);
        double x = -Math.sin(yawRad) * Math.cos(pitchRad);
        double y = -Math.sin(pitchRad);
        double z = Math.cos(yawRad) * Math.cos(pitchRad);
        return new Vec3d(x, y, z);
    }

    /**
     * 添加原版激光粒子效果
     */
    private void addLaserParticles(ServerWorld world, Vec3d eyePos, Vec3d lookVec, double laserLength) {
        // 生成激光粒子效果
        int particleCount = (int) (laserLength * 3);
        for (int i = 0; i < particleCount; i++) {
            double progress = (double) i / particleCount;
            // 添加一些随机偏移，使激光更加自然
            double randomOffset = (random.nextDouble() - 0.5) * 0.1;
            Vec3d particlePos = eyePos.add(
                lookVec.x * laserLength * progress + randomOffset,
                lookVec.y * laserLength * progress + randomOffset,
                lookVec.z * laserLength * progress + randomOffset
            );

            // 根据进度选择不同的粒子类型，创造颜色渐变效果
            if (progress < 0.3) {
                // 起点使用红色系粒子
                world.spawnParticles(
                    ParticleTypes.FLAME,
                    particlePos.x,
                    particlePos.y,
                    particlePos.z,
                    1,
                    0.01,
                    0.01,
                    0.01,
                    0.05
                );
            } else if (progress < 0.6) {
                // 中间使用黄色系粒子
                world.spawnParticles(
                    ParticleTypes.END_ROD,
                    particlePos.x,
                    particlePos.y,
                    particlePos.z,
                    1,
                    0.01,
                    0.01,
                    0.01,
                    0.05
                );
            } else {
                // 终点使用白色系粒子
                world.spawnParticles(
                    ParticleTypes.GLOW_SQUID_INK,
                    particlePos.x,
                    particlePos.y,
                    particlePos.z,
                    1,
                    0.01,
                    0.01,
                    0.01,
                    0.05
                );
            }

            // 每隔几个粒子添加一个烟雾粒子
            if (i % 4 == 0) {
                world.spawnParticles(
                    ParticleTypes.SMOKE,
                    particlePos.x,
                    particlePos.y,
                    particlePos.z,
                    1,
                    0.0,
                    0.0,
                    0.0,
                    0.02
                );
            }
        }

        // 添加激光脉冲效果
        for (int i = 0; i < 3; i++) {
            final int pulseIndex = i;
            world.getServer().execute(() -> {
                double pulseProgress = (double) pulseIndex / 3;
                double pulseLength = laserLength * (0.5 + pulseProgress * 0.5);
                int pulseParticles = (int) (pulseLength * 2);

                for (int j = 0; j < pulseParticles; j++) {
                    double progress = (double) j / pulseParticles;
                    Vec3d pulsePos = eyePos.add(lookVec.x * pulseLength * progress, lookVec.y * pulseLength * progress, lookVec.z * pulseLength * progress);

                    world.spawnParticles(
                        ParticleTypes.GLOW_SQUID_INK,
                        pulsePos.x,
                        pulsePos.y,
                        pulsePos.z,
                        1,
                        0.02,
                        0.02,
                        0.02,
                        0.1
                    );
                }
            });
        }
    }

    /**
     * 创建爆炸粒子效果
     */
    private void createExplosionEffect(ServerWorld world, Vec3d position) {
        // 播放爆炸音效
        world.playSound(null, position.x, position.y, position.z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 0.5F, 1.0F);

        // 创建爆炸粒子效果
        for (int i = 0; i < 20; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double distance = random.nextDouble() * 1.5;
            double x = position.x + Math.cos(angle) * distance;
            double y = position.y + random.nextDouble() * 1.5;
            double z = position.z + Math.sin(angle) * distance;

            world.spawnParticles(
                ParticleTypes.EXPLOSION,
                x,
                y,
                z,
                1,
                0.0,
                0.0,
                0.0,
                0.1
            );

            if (i % 2 == 0) {
                world.spawnParticles(
                    ParticleTypes.FLAME,
                    x,
                    y,
                    z,
                    1,
                    0.0,
                    0.1,
                    0.0,
                    0.1
                );
            }
        }
    }
}
