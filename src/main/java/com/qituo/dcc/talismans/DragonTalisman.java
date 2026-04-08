package com.qituo.dcc.talismans;

import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import java.util.Random;

import com.qituo.dcrapi.particles.DcRenderApiParticleManager;
import com.qituo.dcrapi.particles.ServerParticleGroup;
import com.qituo.dcrapi.particles.ServerParticleGroupManager;
import com.qituo.dcrapi.particles.ParticleAnimationExample;

public class DragonTalisman extends TalismanBase {
    // 龙符咒粒子效果ID
    public static final ResourceLocation DRAGON_FIREBALL_EFFECT = new ResourceLocation("dcc", "dragon_fireball");
    private static final Random random = new Random();
    
    public DragonTalisman(Item.Properties properties) {
        super(properties);
    }
    
    @Override
    protected void useTalisman(ServerLevel level, Player player, InteractionHand hand) {
        // 随机生成 1-5 个火焰弹
        int fireballCount = random.nextInt(5) + 1;
        
        // 获取玩家视线方向
        Vec3 lookVec = player.getViewVector(1.0F);
        Vec3 eyePos = player.getEyePosition(1.0F);
        
        // 发射多个火焰弹
        for (int i = 0; i < fireballCount; i++) {
            // 播放火焰弹发射音效
            level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F + random.nextFloat() * 0.2F);
            
            // 为每个火焰弹添加随机偏移，使它们分散发射
            double offsetX = (random.nextDouble() - 0.5) * 0.3;
            double offsetY = (random.nextDouble() - 0.5) * 0.3;
            double offsetZ = (random.nextDouble() - 0.5) * 0.3;
            
            // 发射恶魂风格的火焰弹
            net.minecraft.world.entity.projectile.LargeFireball fireball = new net.minecraft.world.entity.projectile.LargeFireball(
                level,
                player,
                lookVec.x + offsetX * 0.5,
                lookVec.y + offsetY * 0.5,
                lookVec.z + offsetZ * 0.5,
                4 // 爆炸威力
            );
            
            // 设置火焰弹位置
            fireball.setPos(
                eyePos.x + offsetX,
                eyePos.y - 0.1 + offsetY,
                eyePos.z + offsetZ
            );
            
            // 添加火焰弹到世界
            level.addFreshEntity(fireball);
            
            // 添加原版粒子效果
            addFireballParticles(level, eyePos.add(offsetX, offsetY, offsetZ), lookVec.add(offsetX * 0.5, offsetY * 0.5, offsetZ * 0.5));
        }
        
        // 添加Twelve Render API粒子效果
        addTrapiParticles(level, player, eyePos, lookVec);
    }
    
    /**
     * 添加Twelve Render API粒子效果
     */
    private void addTrapiParticles(ServerLevel level, Player player, Vec3 eyePos, Vec3 lookVec) {
        // 创建圆形轨道粒子效果
        ParticleAnimationExample.createCircleOrbitEffect(
            level,
            eyePos,
            1.5, // 半径
            15   // 粒子数量
        );
        
        // 创建螺旋粒子效果
        ParticleAnimationExample.createSpiralEffect(
            level,
            eyePos,
            1.0, // 半径
            3.0, // 高度
            10   // 粒子数量
        );
        
        // 创建波浪粒子效果
        ParticleAnimationExample.createWaveEffect(
            level,
            eyePos.add(lookVec.x * 2, lookVec.y * 2, lookVec.z * 2),
            3.0,  // 长度
            0.5,  // 振幅
            10    // 粒子数量
        );
    }
    
    /**
     * 添加原版火焰弹粒子效果
     */
    private void addFireballParticles(ServerLevel level, Vec3 eyePos, Vec3 lookVec) {
        // 在发射位置添加火焰粒子
        for (int i = 0; i < 20; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 0.5;
            double offsetY = (level.random.nextDouble() - 0.5) * 0.5;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.5;
            
            level.sendParticles(
                net.minecraft.core.particles.ParticleTypes.FLAME,
                eyePos.x + offsetX,
                eyePos.y + offsetY,
                eyePos.z + offsetZ,
                1,
                lookVec.x * 0.1,
                lookVec.y * 0.1,
                lookVec.z * 0.1,
                0.05
            );
        }
        
        // 添加熔岩粒子，增强火焰效果
        for (int i = 0; i < 10; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 0.5;
            double offsetY = (level.random.nextDouble() - 0.5) * 0.5;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.5;
            
            level.sendParticles(
                net.minecraft.core.particles.ParticleTypes.LAVA,
                eyePos.x + offsetX,
                eyePos.y + offsetY,
                eyePos.z + offsetZ,
                1,
                lookVec.x * 0.05,
                lookVec.y * 0.05,
                lookVec.z * 0.05,
                0.05
            );
        }
        
        // 在轨迹上添加烟雾粒子
        for (int i = 1; i <= 10; i++) {
            Vec3 trailPos = eyePos.add(lookVec.x * i * 0.2, lookVec.y * i * 0.2, lookVec.z * i * 0.2);
            level.sendParticles(
                net.minecraft.core.particles.ParticleTypes.SMOKE,
                trailPos.x,
                trailPos.y,
                trailPos.z,
                1,
                0.0,
                0.0,
                0.0,
                0.0
            );
        }
        
        // 在轨迹上添加火焰粒子
        for (int i = 1; i <= 15; i++) {
            Vec3 trailPos = eyePos.add(lookVec.x * i * 0.2, lookVec.y * i * 0.2, lookVec.z * i * 0.2);
            double offsetX = (level.random.nextDouble() - 0.5) * 0.3;
            double offsetY = (level.random.nextDouble() - 0.5) * 0.3;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.3;
            
            level.sendParticles(
                net.minecraft.core.particles.ParticleTypes.FLAME,
                trailPos.x + offsetX,
                trailPos.y + offsetY,
                trailPos.z + offsetZ,
                1,
                lookVec.x * 0.1,
                lookVec.y * 0.1,
                lookVec.z * 0.1,
                0.05
            );
        }
        
        // 在轨迹上添加更多烟雾粒子，增强轨迹效果
        for (int i = 1; i <= 12; i++) {
            Vec3 trailPos = eyePos.add(lookVec.x * i * 0.15, lookVec.y * i * 0.15, lookVec.z * i * 0.15);
            level.sendParticles(
                net.minecraft.core.particles.ParticleTypes.SMOKE,
                trailPos.x,
                trailPos.y,
                trailPos.z,
                1,
                0.02,
                0.02,
                0.02,
                0.05
            );
        }
        
        // 添加火星粒子，增强火焰效果
        for (int i = 1; i <= 10; i++) {
            Vec3 trailPos = eyePos.add(lookVec.x * i * 0.25, lookVec.y * i * 0.25, lookVec.z * i * 0.25);
            double offsetX = (level.random.nextDouble() - 0.5) * 0.4;
            double offsetY = (level.random.nextDouble() - 0.5) * 0.4;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.4;
            
            level.sendParticles(
                net.minecraft.core.particles.ParticleTypes.LAVA,
                trailPos.x + offsetX,
                trailPos.y + offsetY,
                trailPos.z + offsetZ,
                1,
                lookVec.x * 0.08,
                lookVec.y * 0.08,
                lookVec.z * 0.08,
                0.05
            );
        }
    }
    

}