package com.qituo.dcc.damage;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 实体绕过伤害辅助器
 * 实现五重击杀链，确保实体死亡并正常掉落物品
 * 
 * 五重击杀链：
 * 1. setLastHurtByPlayer - 确保实体记录伤害来源为玩家
 * 2. hurt - 正常伤害调用
 * 3. actuallyHurt - 跳过某些检查直接伤害（反射调用）
 * 4. die - 强制死亡
 * 5. dropFromLootTable - 手动兜底掉落物（反射调用）
 */
public class EntityBypassHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityBypassHelper.class);
    
    // 防止无限递归的标签
    private static final String BYPASS_TAG = "dcc$bypass_processing";
    private static final String PROCESSED_TAG = "dcc$bypass_done";
    
    // 最大重试次数
    private static final int MAX_RETRIES = 3;
    
    /**
     * 对实体执行击杀，确保死亡并掉落物品
     * @param entity 目标实体
     * @param damageSource 伤害来源
     * @param damage 伤害值
     */
    public static void killEntity(LivingEntity entity, net.minecraft.world.damagesource.DamageSource damageSource, float damage) {
        if (entity == null || damageSource == null) {
            return;
        }
        
        // 检查是否已经在处理中，防止递归
        if (entity.getTags().contains(BYPASS_TAG)) {
            return;
        }
        
        entity.addTag(BYPASS_TAG);
        
        try {
            // 如果已经死亡，直接处理掉落
            if (entity.isDeadOrDying()) {
                ensureDrops(entity, damageSource);
                return;
            }
            
            // 第一重：确保伤害来源记录为玩家
            if (damageSource.getEntity() instanceof Player player) {
                try {
                    entity.setLastHurtByPlayer(player);
                    entity.setLastHurtByMob(player);
                } catch (Exception e) {
                    LOGGER.debug("Failed to set last hurt by player: {}", e.getMessage());
                }
            }
            
            // 重置无敌时间
            resetInvulnerability(entity);
            
            // 第二重：正常伤害
            boolean damaged = tryNormalDamage(entity, damageSource, damage);
            
            // 如果正常伤害没有生效，尝试第三重
            if (!damaged && !entity.isDeadOrDying()) {
                // 第三重：直接调用actuallyHurt
                tryActuallyHurt(entity, damageSource, damage);
            }
            
            // 如果还是没死，尝试第四重
            if (!entity.isDeadOrDying()) {
                // 第四重：强制die
                forceDie(entity, damageSource);
            }
            
            // 第五重：确保掉落物
            ensureDrops(entity, damageSource);
            
        } catch (Exception e) {
            LOGGER.error("Error in EntityBypassHelper.killEntity: {}", e.getMessage());
        } finally {
            entity.removeTag(BYPASS_TAG);
        }
    }
    
    /**
     * 批量击杀多个实体
     */
    public static void killEntities(List<LivingEntity> entities, net.minecraft.world.damagesource.DamageSource damageSource, float damage) {
        for (LivingEntity entity : entities) {
            killEntity(entity, damageSource, damage);
        }
    }
    
    /**
     * 对多个实体应用伤害，返回成功击杀的数量
     */
    public static int damageEntities(List<LivingEntity> entities, net.minecraft.world.damagesource.DamageSource damageSource, float damage) {
        AtomicInteger killedCount = new AtomicInteger(0);
        List<LivingEntity> failedEntities = new ArrayList<>();
        
        for (LivingEntity entity : entities) {
            try {
                killEntity(entity, damageSource, damage);
                if (entity.isDeadOrDying()) {
                    killedCount.incrementAndGet();
                } else {
                    failedEntities.add(entity);
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to damage entity {}: {}", entity.getName().getString(), e.getMessage());
                failedEntities.add(entity);
            }
        }
        
        // 对失败的实体进行重试
        for (LivingEntity entity : failedEntities) {
            if (!entity.isDeadOrDying()) {
                for (int i = 0; i < MAX_RETRIES; i++) {
                    try {
                        entity.setHealth(0.0f);
                        entity.die(damageSource);
                        killedCount.incrementAndGet();
                        break;
                    } catch (Exception e) {
                        LOGGER.warn("Retry {} failed for {}: {}", i + 1, entity.getName().getString(), e.getMessage());
                    }
                }
            }
        }
        
        return killedCount.get();
    }
    
    /**
     * 重置实体的无敌时间
     */
    private static void resetInvulnerability(LivingEntity entity) {
        try {
            entity.invulnerableTime = 0;
        } catch (Exception e) {
            LOGGER.debug("Failed to reset invulnerability: {}", e.getMessage());
        }
    }
    
    /**
     * 尝试正常伤害
     */
    private static boolean tryNormalDamage(LivingEntity entity, net.minecraft.world.damagesource.DamageSource damageSource, float damage) {
        try {
            float originalHealth = entity.getHealth();
            entity.hurt(damageSource, damage);
            return entity.getHealth() < originalHealth || entity.isDeadOrDying();
        } catch (Exception e) {
            LOGGER.debug("Normal hurt failed for {}: {}", entity.getName().getString(), e.getMessage());
            return false;
        }
    }
    
    /**
     * 尝试直接调用actuallyHurt（protected方法，需反射）
     */
    private static void tryActuallyHurt(LivingEntity entity, net.minecraft.world.damagesource.DamageSource damageSource, float damage) {
        try {
            java.lang.reflect.Method method = LivingEntity.class.getDeclaredMethod("actuallyHurt", 
                net.minecraft.world.damagesource.DamageSource.class, float.class);
            method.setAccessible(true);
            method.invoke(entity, damageSource, damage);
            LOGGER.debug("Successfully called actuallyHurt on {}", entity.getName().getString());
        } catch (Exception e) {
            LOGGER.debug("actuallyHurt failed for {}: {}", entity.getName().getString(), e.getMessage());
            // 备选方案：直接设置生命值
            try {
                float newHealth = Math.max(0, entity.getHealth() - damage);
                entity.setHealth(newHealth);
            } catch (Exception ex) {
                LOGGER.debug("Set health failed for {}: {}", entity.getName().getString(), ex.getMessage());
            }
        }
    }
    
    /**
     * 强制实体死亡
     */
    private static void forceDie(LivingEntity entity, net.minecraft.world.damagesource.DamageSource damageSource) {
        try {
            entity.setHealth(0.0f);
            entity.die(damageSource);
            
            if (!entity.isDeadOrDying()) {
                try {
                    java.lang.reflect.Field deathTimeField = LivingEntity.class.getDeclaredField("deathTime");
                    deathTimeField.setAccessible(true);
                    deathTimeField.setInt(entity, 100);
                } catch (Exception e) {
                    LOGGER.debug("Failed to set deathTime: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Force die failed for {}: {}", entity.getName().getString(), e.getMessage());
        }
    }
    
    /**
     * 确保掉落物生成（反射调用protected方法）
     */
    private static void ensureDrops(LivingEntity entity, net.minecraft.world.damagesource.DamageSource damageSource) {
        if (entity.level().isClientSide) {
            return;
        }
        
        try {
            if (entity.isDeadOrDying() && !entity.getTags().contains(PROCESSED_TAG)) {
                entity.addTag(PROCESSED_TAG);
                
                // 反射调用 dropFromLootTable（protected方法）
                try {
                    java.lang.reflect.Method dropMethod = LivingEntity.class.getDeclaredMethod(
                        "dropFromLootTable", 
                        net.minecraft.world.damagesource.DamageSource.class, 
                        boolean.class);
                    dropMethod.setAccessible(true);
                    
                    // 反射读取 lastHurtByPlayerTime（protected字段）
                    boolean lastHurtByPlayer = false;
                    try {
                        java.lang.reflect.Field field = LivingEntity.class.getDeclaredField("lastHurtByPlayerTime");
                        field.setAccessible(true);
                        lastHurtByPlayer = field.getInt(entity) > 0;
                    } catch (Exception e) {
                        LOGGER.debug("Failed to read lastHurtByPlayerTime: {}", e.getMessage());
                    }
                    
                    dropMethod.invoke(entity, damageSource, lastHurtByPlayer);
                } catch (Exception e) {
                    LOGGER.debug("dropFromLootTable failed: {}", e.getMessage());
                    manualDropItems(entity, damageSource);
                }
            }
        } catch (Exception e) {
            LOGGER.error("ensureDrops failed for {}: {}", entity.getName().getString(), e.getMessage());
        }
    }
    
    /**
     * 手动处理掉落物（兜底方案）
     */
    private static void manualDropItems(LivingEntity entity, net.minecraft.world.damagesource.DamageSource damageSource) {
        try {
            // 掉落装备
            for (net.minecraft.world.entity.EquipmentSlot slot : net.minecraft.world.entity.EquipmentSlot.values()) {
                if (slot.getType() == net.minecraft.world.entity.EquipmentSlot.Type.ARMOR ||
                    slot == net.minecraft.world.entity.EquipmentSlot.MAINHAND ||
                    slot == net.minecraft.world.entity.EquipmentSlot.OFFHAND) {
                    ItemStack itemStack = entity.getItemBySlot(slot);
                    if (!itemStack.isEmpty()) {
                        entity.spawnAtLocation(itemStack, 0.0F);
                    }
                }
            }
            
            LOGGER.debug("Manual drop completed for {}", entity.getName().getString());
        } catch (Exception e) {
            LOGGER.error("Manual drop failed for {}: {}", entity.getName().getString(), e.getMessage());
        }
    }
    
    /**
     * 清理标签
     */
    public static void cleanupTags(Entity entity) {
        entity.removeTag(BYPASS_TAG);
        entity.removeTag(PROCESSED_TAG);
    }
    
    /**
     * 获取实体附近的生物实体
     */
    public static List<LivingEntity> getNearbyLivingEntities(net.minecraft.world.level.Level level, Vec3 center, double radius) {
        List<LivingEntity> result = new ArrayList<>();
        AABB searchArea = new AABB(center, center).inflate(radius);
        
        List<Entity> entities = level.getEntities(null, searchArea);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingEntity && !entity.isSpectator()) {
                result.add(livingEntity);
            }
        }
        
        return result;
    }
    
    /**
     * 检查实体是否可以被伤害
     */
    public static boolean canBeDamaged(LivingEntity entity, net.minecraft.world.damagesource.DamageSource damageSource) {
        if (entity == null || damageSource == null) {
            return false;
        }
        
        if (entity.isDeadOrDying()) {
            return false;
        }
        
        // 对于始源伤害类型，绕过免疫检查
        if (damageSource instanceof ModDamageSources.OriginEndDamageSource) {
            return true;
        }
        
        return !entity.isInvulnerableTo(damageSource);
    }
}
