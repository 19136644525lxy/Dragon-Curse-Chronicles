package com.qituo.dcc.talismans;

import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.ClipContext;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import com.qituo.dcrapi.particles.DcRenderApiParticleManager;
import com.qituo.dcrapi.particles.ServerParticleGroup;
import com.qituo.dcrapi.particles.ServerParticleGroupManager;
import com.qituo.dcrapi.particles.ParticleAnimationExample;

import java.util.Random;

public class PigTalisman extends TalismanBase {
    // 猪符咒粒子效果ID
    public static final ResourceLocation PIG_LASER_EFFECT = new ResourceLocation("dcc", "pig_laser");
    private static final Random random = new Random();
    // 冷却时间（以刻为单位，20刻=1秒）
    private static final int COOLDOWN_TICKS = 40; // 2秒
    
    public PigTalisman(Item.Properties properties) {
        super(properties);
    }
    
    @Override
    protected void useTalisman(ServerLevel level, Player player, InteractionHand hand) {
        // 播放激光发射音效
        level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        
        // 获取玩家视线方向
        Vec3 lookVec = player.getViewVector(1.0F);
        Vec3 eyePos = player.getEyePosition(1.0F);
        
        // 创建射线检测
        double reachDistance = 50.0;
        Vec3 endPos = eyePos.add(lookVec.x * reachDistance, lookVec.y * reachDistance, lookVec.z * reachDistance);
        
        // 先检测方块
        HitResult blockHitResult = level.clip(
            new ClipContext(
                eyePos,
                endPos,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
            )
        );
        
        // 计算激光长度
        double laserLength = reachDistance;
        if (blockHitResult.getType() != HitResult.Type.MISS) {
            laserLength = eyePos.distanceTo(blockHitResult.getLocation());
        }
        
        // 检测实体
        HitResult hitResult = blockHitResult;
        net.minecraft.world.entity.LivingEntity targetEntity = null;
        
        // 使用 AABB 检测实体
        net.minecraft.world.phys.AABB aabb = new net.minecraft.world.phys.AABB(
            eyePos.x - 0.5, eyePos.y - 0.5, eyePos.z - 0.5,
            endPos.x + 0.5, endPos.y + 0.5, endPos.z + 0.5
        );
        
        java.util.List<net.minecraft.world.entity.Entity> entities = level.getEntities(
            player,
            aabb,
            entity -> entity instanceof net.minecraft.world.entity.LivingEntity && entity.isAlive()
        );
        
        double closestDistance = reachDistance;
        for (net.minecraft.world.entity.Entity entity : entities) {
            net.minecraft.world.phys.AABB entityAabb = entity.getBoundingBox();
            
            // 检查射线是否与实体的 AABB 相交
            net.minecraft.world.phys.Vec3 intersection = eyePos;
            net.minecraft.world.phys.Vec3 direction = endPos.subtract(eyePos);
            double length = direction.length();
            direction = direction.normalize();
            
            boolean hit = false;
            for (double t = 0.0; t <= length; t += 0.1) {
                Vec3 currentPos = eyePos.add(direction.scale(t));
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
                    targetEntity = (net.minecraft.world.entity.LivingEntity) entity;
                    hitResult = new net.minecraft.world.phys.EntityHitResult(entity, intersection);
                    laserLength = distance;
                }
            }
        }
        
        // 添加原版激光粒子效果
        addLaserParticles(level, eyePos, lookVec, laserLength);
        
        // 添加Twelve Render API粒子效果
        addTrapiParticles(level, player, eyePos, lookVec, laserLength, hitResult);
        
        // 对击中的实体造成伤害
        if (targetEntity != null) {
            // 造成99点伤害
            targetEntity.hurt(level.damageSources().playerAttack(player), 99.0f);
            
            // 点燃目标
            targetEntity.setSecondsOnFire(5);
            
            // 在目标位置创建爆炸粒子效果
            createExplosionEffect(level, targetEntity.position());
        } else if (hitResult.getType() == HitResult.Type.BLOCK) {
            // 对击中的方块点燃
            BlockPos hitPos = ((BlockHitResult) hitResult).getBlockPos();
            BlockPos firePos = hitPos.relative(((BlockHitResult) hitResult).getDirection());
            
            if (level.getBlockState(firePos).isAir()) {
                level.setBlock(firePos, Blocks.FIRE.defaultBlockState(), 11);
            }
            
            // 在击中位置创建爆炸粒子效果
            createExplosionEffect(level, hitResult.getLocation());
        }
        
        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
    }
    
    /**
     * 添加Twelve Render API粒子效果
     */
    private void addTrapiParticles(ServerLevel level, Player player, Vec3 eyePos, Vec3 lookVec, double laserLength, HitResult hitResult) {
        // 在激光起点创建圆形轨道粒子效果
        ParticleAnimationExample.createCircleOrbitEffect(
            level,
            eyePos,
            1.0, // 半径
            15   // 粒子数量
        );
        
        // 在激光终点创建螺旋粒子效果
        Vec3 endPos = eyePos.add(lookVec.x * laserLength, lookVec.y * laserLength, lookVec.z * laserLength);
        ParticleAnimationExample.createSpiralEffect(
            level,
            endPos,
            1.5, // 半径
            2.0, // 高度
            20   // 粒子数量
        );
        
        // 在激光路径上创建波浪粒子效果
        if (laserLength > 3) {
            ParticleAnimationExample.createWaveEffect(
                level,
                eyePos.add(lookVec.x * 2, lookVec.y * 2, lookVec.z * 2),
                Math.min(laserLength - 2, 15),  // 长度
                0.4,  // 振幅
                20    // 粒子数量
            );
        }
    }
    
    /**
     * 添加原版激光粒子效果
     */
    private void addLaserParticles(ServerLevel level, Vec3 eyePos, Vec3 lookVec, double laserLength) {
        // 生成激光粒子效果
        int particleCount = (int) (laserLength * 3);
        for (int i = 0; i < particleCount; i++) {
            double progress = (double) i / particleCount;
            // 添加一些随机偏移，使激光更加自然
            double randomOffset = (random.nextDouble() - 0.5) * 0.1;
            Vec3 particlePos = eyePos.add(
                lookVec.x * laserLength * progress + randomOffset,
                lookVec.y * laserLength * progress + randomOffset,
                lookVec.z * laserLength * progress + randomOffset
            );
            
            // 根据进度选择不同的粒子类型，创造颜色渐变效果
            if (progress < 0.3) {
                // 起点使用红色系粒子
                level.sendParticles(
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
                level.sendParticles(
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
                level.sendParticles(
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
                level.sendParticles(
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
            level.getServer().execute(() -> {
                double pulseProgress = (double) pulseIndex / 3;
                double pulseLength = laserLength * (0.5 + pulseProgress * 0.5);
                int pulseParticles = (int) (pulseLength * 2);
                
                for (int j = 0; j < pulseParticles; j++) {
                    double progress = (double) j / pulseParticles;
                    Vec3 pulsePos = eyePos.add(lookVec.x * pulseLength * progress, lookVec.y * pulseLength * progress, lookVec.z * pulseLength * progress);
                    
                    level.sendParticles(
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
    private void createExplosionEffect(ServerLevel level, Vec3 position) {
        // 播放爆炸音效
        level.playSound(null, position.x, position.y, position.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.5F, 1.0F);
        
        // 创建爆炸粒子效果
        for (int i = 0; i < 20; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double distance = random.nextDouble() * 1.5;
            double x = position.x + Math.cos(angle) * distance;
            double y = position.y + random.nextDouble() * 1.5;
            double z = position.z + Math.sin(angle) * distance;
            
            level.sendParticles(
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
                level.sendParticles(
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