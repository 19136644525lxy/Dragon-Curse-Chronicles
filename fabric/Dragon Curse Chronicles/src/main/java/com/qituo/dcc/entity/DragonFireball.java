package com.qituo.dcc.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import com.qituo.dcc.damage.DamagePresets;
import com.qituo.dcc.damage.ModDamageSources;

/**
 * 龙之火球实体
 *
 * 原理：
 * yarn 1.20.1 中 LargeFireballEntity 对应 FireballEntity（大型火球），
 * SmallFireballEntity 对应小型火球（如恶魂火球）。
 * FireballEntity 构造函数需要额外的 explosionPower 参数。
 * 碰撞回调方法为 onCollision(HitResult)，需要通过 HitResult 类型判断命中目标。
 */
public class DragonFireball extends FireballEntity {
    // 父类 explosionPower 为 private，子类自行存储
    private final int dcc$explosionPower;

    public DragonFireball(EntityType<? extends FireballEntity> entityType, World world) {
        super(entityType, world);
        this.dcc$explosionPower = 4;
    }

    public DragonFireball(World world, LivingEntity shooter, double velocityX, double velocityY, double velocityZ, int explosionPower) {
        super(world, shooter, velocityX, velocityY, velocityZ, explosionPower);
        this.dcc$explosionPower = explosionPower;
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        if (!this.getWorld().isClient()) {
            // 触发爆炸效果（与 LargeFireball 默认行为一致）
            this.getWorld().createExplosion(
                this,
                this.getX(),
                this.getY(),
                this.getZ(),
                this.dcc$explosionPower,
                World.ExplosionSourceType.MOB
            );

            // 对命中实体施加自定义伤害
            if (hitResult.getType() == HitResult.Type.ENTITY) {
                net.minecraft.util.hit.EntityHitResult entityHitResult = (net.minecraft.util.hit.EntityHitResult) hitResult;
                if (entityHitResult.getEntity() instanceof LivingEntity livingEntity) {
                    DamageSource damageSource = ModDamageSources.causeOriginEndDamage(getOwner());
                    livingEntity.damage(damageSource, DamagePresets.LEVEL_5);
                }
            }
        }

        this.discard();
    }
}
