package com.qituo.dcc.talismans;

import com.qituo.dcc.entity.DragonFireball;
import net.minecraft.item.Item;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import java.util.Random;

public class DragonTalisman extends TalismanBase {
    private static final Random random = new Random();
    private static final int COOLDOWN_TICKS = 20; // 1秒冷却
    
    public DragonTalisman(Item.Settings settings) {
        super(settings);
    }
    
    @Override
    protected void useTalisman(ServerWorld world, PlayerEntity player, Hand hand) {
        // 检查冷却时间
        if (player.getItemCooldownManager().isCoolingDown(this)) {
            return;
        }
        
        // 只发射1个火球
        int fireballCount = 1;
        
        // 获取玩家视线方向
        Vec3d lookVec = getLookVec(player);
        Vec3d eyePos = player.getEyePos();
        
        // 播放火焰弹发射音效
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F + random.nextFloat() * 0.2F);
        
        // 计算基础速度向量
        double speedMultiplier = 0.8 + random.nextDouble() * 0.4;
        double motionX = lookVec.x * speedMultiplier;
        double motionY = lookVec.y * speedMultiplier;
        double motionZ = lookVec.z * speedMultiplier;
        
        // 计算火焰弹的生成位置（向前移动1.0格，远离玩家）
        Vec3d spawnPos = eyePos.add(lookVec.x * 1.0, lookVec.y * 1.0, lookVec.z * 1.0);
        
        // 发射自定义火球（使用自定义伤害类型和伤害值）
        DragonFireball fireball = new DragonFireball(
            world,
            player,
            motionX,
            motionY,
            motionZ,
            4
        );
        
        // 设置火焰弹位置
        fireball.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        
        // 设置火焰弹不会与同一批次的其他火球碰撞（虽然现在只有1个）
        fireball.setInvulnerable(true);
        
        // 添加火焰弹到世界
        world.spawnEntity(fireball);
        
        // 添加原版粒子效果
        addFireballParticles(world, spawnPos, new Vec3d(motionX, motionY, motionZ));
        
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
     * 添加原版火焰弹粒子效果
     */
    private void addFireballParticles(ServerWorld world, Vec3d eyePos, Vec3d lookVec) {
        // 在发射位置添加火焰粒子
        for (int i = 0; i < 20; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * 0.5;
            double offsetY = (world.random.nextDouble() - 0.5) * 0.5;
            double offsetZ = (world.random.nextDouble() - 0.5) * 0.5;
            
            world.spawnParticles(
                ParticleTypes.FLAME,
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
            double offsetX = (world.random.nextDouble() - 0.5) * 0.5;
            double offsetY = (world.random.nextDouble() - 0.5) * 0.5;
            double offsetZ = (world.random.nextDouble() - 0.5) * 0.5;
            
            world.spawnParticles(
                ParticleTypes.LAVA,
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
            Vec3d trailPos = eyePos.add(lookVec.x * i * 0.2, lookVec.y * i * 0.2, lookVec.z * i * 0.2);
            world.spawnParticles(
                ParticleTypes.SMOKE,
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
            Vec3d trailPos = eyePos.add(lookVec.x * i * 0.2, lookVec.y * i * 0.2, lookVec.z * i * 0.2);
            double offsetX = (world.random.nextDouble() - 0.5) * 0.3;
            double offsetY = (world.random.nextDouble() - 0.5) * 0.3;
            double offsetZ = (world.random.nextDouble() - 0.5) * 0.3;
            
            world.spawnParticles(
                ParticleTypes.FLAME,
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
            Vec3d trailPos = eyePos.add(lookVec.x * i * 0.15, lookVec.y * i * 0.15, lookVec.z * i * 0.15);
            world.spawnParticles(
                ParticleTypes.SMOKE,
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
            Vec3d trailPos = eyePos.add(lookVec.x * i * 0.25, lookVec.y * i * 0.25, lookVec.z * i * 0.25);
            double offsetX = (world.random.nextDouble() - 0.5) * 0.4;
            double offsetY = (world.random.nextDouble() - 0.5) * 0.4;
            double offsetZ = (world.random.nextDouble() - 0.5) * 0.4;
            
            world.spawnParticles(
                ParticleTypes.LAVA,
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