package com.qituo.dcc.talismans;

import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import com.qituo.dcrapi.particles.DcRenderApiParticleManager;
import com.qituo.dcrapi.particles.ServerParticleGroup;
import com.qituo.dcrapi.particles.ServerParticleGroupManager;
import com.qituo.dcrapi.particles.ParticleAnimationExample;

public class DragonTalisman extends TalismanBase {
    // 龙符咒粒子效果ID
    public static final ResourceLocation DRAGON_FIREBALL_EFFECT = new ResourceLocation("dcc", "dragon_fireball");
    
    public DragonTalisman(Item.Properties properties) {
        super(properties);
    }
    
    @Override
    protected void useTalisman(ServerLevel level, Player player, InteractionHand hand) {
        // 获取玩家视线方向
        Vec3 lookVec = player.getViewVector(1.0F);
        Vec3 eyePos = player.getEyePosition(1.0F);
        
        // 发射恶魂风格的火焰弹
        net.minecraft.world.entity.projectile.LargeFireball fireball = new net.minecraft.world.entity.projectile.LargeFireball(
            level,
            player,
            lookVec.x,
            lookVec.y,
            lookVec.z,
            4 // 爆炸威力
        );
        
        // 设置火焰弹位置
        fireball.setPos(
            eyePos.x,
            eyePos.y - 0.1,
            eyePos.z
        );
        
        // 添加火焰弹到世界
        level.addFreshEntity(fireball);
        
        // 添加原版粒子效果
        addFireballParticles(level, eyePos, lookVec);
        
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
        for (int i = 0; i < 10; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 0.5;
            double offsetY = (level.random.nextDouble() - 0.5) * 0.5;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.5;
            
            level.addParticle(
                net.minecraft.core.particles.ParticleTypes.FLAME,
                eyePos.x + offsetX,
                eyePos.y + offsetY,
                eyePos.z + offsetZ,
                lookVec.x * 0.1,
                lookVec.y * 0.1,
                lookVec.z * 0.1
            );
        }
        
        // 在轨迹上添加烟雾粒子
        for (int i = 1; i <= 5; i++) {
            Vec3 trailPos = eyePos.add(lookVec.x * i * 0.2, lookVec.y * i * 0.2, lookVec.z * i * 0.2);
            level.addParticle(
                net.minecraft.core.particles.ParticleTypes.SMOKE,
                trailPos.x,
                trailPos.y,
                trailPos.z,
                0.0,
                0.0,
                0.0
            );
        }
    }
    

}