package com.qituo.dcc.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.damagesource.DamageSource;
import com.qituo.dcc.damage.DamagePresets;
import com.qituo.dcc.damage.ModDamageSources;

public class DragonFireball extends LargeFireball {
    public DragonFireball(EntityType<? extends LargeFireball> entityType, Level level) {
        super(entityType, level);
    }

    public DragonFireball(Level level, LivingEntity shooter, double x, double y, double z) {
        super(level, shooter, x, y, z, 4);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        // 不调用父类的伤害逻辑，使用我们的自定义伤害
        if (result.getEntity() instanceof LivingEntity livingEntity) {
            // 使用自定义伤害类型和伤害值（LEVEL_5 = 512.0）
            DamageSource damageSource = ModDamageSources.causeOriginEndDamage(getOwner());
            livingEntity.hurt(damageSource, DamagePresets.LEVEL_5);
        }
        
        // 移除火球
        this.discard();
    }
}