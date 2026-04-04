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

import com.qituo.dcrapi.particles.DcRenderApiParticleManager;
import com.qituo.dcrapi.particles.ServerParticleGroup;
import com.qituo.dcrapi.particles.ServerParticleGroupManager;
import com.qituo.dcrapi.particles.ParticleAnimationExample;

public class PigTalisman extends TalismanBase {
    // 猪符咒粒子效果ID
    public static final ResourceLocation PIG_LASER_EFFECT = new ResourceLocation("dcc", "pig_laser");
    
    public PigTalisman(Item.Properties properties) {
        super(properties);
    }
    
    @Override
    protected void useTalisman(ServerLevel level, Player player, InteractionHand hand) {
        // 获取玩家视线方向
        Vec3 lookVec = player.getViewVector(1.0F);
        Vec3 eyePos = player.getEyePosition(1.0F);
        
        // 创建射线检测
        Vec3 endPos = eyePos.add(lookVec.x * 50, lookVec.y * 50, lookVec.z * 50);
        HitResult hitResult = level.clip(
            new ClipContext(
                eyePos,
                endPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
            )
        );
        
        // 计算激光长度
        double laserLength = 50.0;
        if (hitResult.getType() != HitResult.Type.MISS) {
            laserLength = eyePos.distanceTo(hitResult.getLocation());
        }
        
        // 添加原版激光粒子效果
        addLaserParticles(level, eyePos, lookVec, laserLength);
        
        // 添加Twelve Render API粒子效果
        addTrapiParticles(level, player, eyePos, lookVec, laserLength, hitResult);
        
        // 对击中的实体造成伤害
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHitResult = (EntityHitResult) hitResult;
            net.minecraft.world.entity.Entity target = entityHitResult.getEntity();
            
            if (target instanceof net.minecraft.world.entity.LivingEntity livingTarget) {
                // 造成99点伤害
                livingTarget.hurt(level.damageSources().playerAttack(player), 99.0f);
                
                // 点燃目标
                livingTarget.setSecondsOnFire(5);
            }
        } else if (hitResult.getType() == HitResult.Type.BLOCK) {
            // 对击中的方块点燃
            BlockPos hitPos = ((BlockHitResult) hitResult).getBlockPos();
            BlockPos firePos = hitPos.relative(((BlockHitResult) hitResult).getDirection());
            
            if (level.getBlockState(firePos).isAir()) {
                level.setBlock(firePos, Blocks.FIRE.defaultBlockState(), 11);
            }
        }
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
            10   // 粒子数量
        );
        
        // 在激光终点创建螺旋粒子效果
        Vec3 endPos = eyePos.add(lookVec.x * laserLength, lookVec.y * laserLength, lookVec.z * laserLength);
        ParticleAnimationExample.createSpiralEffect(
            level,
            endPos,
            1.5, // 半径
            2.0, // 高度
            15   // 粒子数量
        );
        
        // 在激光路径上创建波浪粒子效果
        if (laserLength > 3) {
            ParticleAnimationExample.createWaveEffect(
                level,
                eyePos.add(lookVec.x * 2, lookVec.y * 2, lookVec.z * 2),
                Math.min(laserLength - 2, 10),  // 长度
                0.3,  // 振幅
                15    // 粒子数量
            );
        }
    }
    
    /**
     * 添加原版激光粒子效果
     */
    private void addLaserParticles(ServerLevel level, Vec3 eyePos, Vec3 lookVec, double laserLength) {
        // 生成激光粒子效果
        int particleCount = (int) (laserLength * 2);
        for (int i = 0; i < particleCount; i++) {
            double progress = (double) i / particleCount;
            Vec3 particlePos = eyePos.add(lookVec.x * laserLength * progress, lookVec.y * laserLength * progress, lookVec.z * laserLength * progress);
            
            // 添加火焰粒子
            level.addParticle(
                ParticleTypes.FLAME,
                particlePos.x,
                particlePos.y,
                particlePos.z,
                0.0,
                0.0,
                0.0
            );
            
            // 每隔几个粒子添加一个烟雾粒子
            if (i % 3 == 0) {
                level.addParticle(
                    ParticleTypes.SMOKE,
                    particlePos.x,
                    particlePos.y,
                    particlePos.z,
                    0.0,
                    0.0,
                    0.0
                );
            }
        }
    }
    

}