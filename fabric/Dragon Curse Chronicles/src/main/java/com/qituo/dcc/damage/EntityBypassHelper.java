package com.qituo.dcc.damage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 实体绕过伤害辅助器（Fabric版）
 * 实现五重击杀链，确保实体死亡并正常掉落物品
 * 
 * 五重击杀链：
 * 1. setAttacker - 确保实体记录伤害来源为玩家
 * 2. damage - 正常伤害调用
 * 3. applyDamage - 跳过某些检查直接伤害（反射调用）
 * 4. onDeath - 强制死亡
 * 5. dropLoot - 手动兜底掉落物（反射调用）
 */
public class EntityBypassHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityBypassHelper.class);
    
    private static final String BYPASS_TAG = "dcc$bypass_processing";
    private static final String PROCESSED_TAG = "dcc$bypass_done";
    
    private static final int MAX_RETRIES = 3;
    
    /**
     * 对实体执行击杀，确保死亡并掉落物品
     */
    public static void killEntity(LivingEntity entity, net.minecraft.entity.damage.DamageSource damageSource, float damage) {
        if (entity == null || damageSource == null) {
            return;
        }
        
        if (entity.getCommandTags().contains(BYPASS_TAG)) {
            return;
        }
        
        entity.addCommandTag(BYPASS_TAG);
        
        try {
            // 如果已经死亡，直接处理掉落
            if (entity.isDead()) {
                ensureDrops(entity, damageSource);
                return;
            }
            
            // 第一重：确保伤害来源记录为玩家
            if (damageSource.getAttacker() instanceof PlayerEntity player) {
                try {
                    entity.setAttacker(player);
                    // 直接设置attackingPlayer字段（protected）
                    try {
                        java.lang.reflect.Field field = LivingEntity.class.getDeclaredField("attackingPlayer");
                        field.setAccessible(true);
                        field.set(entity, player);
                    } catch (Exception e) {
                        LOGGER.debug("Failed to set attackingPlayer: {}", e.getMessage());
                    }
                } catch (Exception e) {
                    LOGGER.debug("Failed to set attacker: {}", e.getMessage());
                }
            }
            
            // 重置无敌时间
            resetInvulnerability(entity);
            
            // 第二重：正常伤害
            boolean damaged = tryNormalDamage(entity, damageSource, damage);
            
            // 如果正常伤害没有生效，尝试第三重
            if (!damaged && !entity.isDead()) {
                tryApplyDamage(entity, damageSource, damage);
            }
            
            // 如果还是没死，尝试第四重
            if (!entity.isDead()) {
                forceDie(entity, damageSource);
            }
            
            // 第五重：确保掉落物
            ensureDrops(entity, damageSource);
            
        } catch (Exception e) {
            LOGGER.error("Error in EntityBypassHelper.killEntity: {}", e.getMessage());
        } finally {
            entity.getCommandTags().remove(BYPASS_TAG);
        }
    }
    
    /**
     * 批量击杀多个实体
     */
    public static void killEntities(List<LivingEntity> entities, net.minecraft.entity.damage.DamageSource damageSource, float damage) {
        for (LivingEntity entity : entities) {
            killEntity(entity, damageSource, damage);
        }
    }
    
    /**
     * 对多个实体应用伤害，返回成功击杀的数量
     */
    public static int damageEntities(List<LivingEntity> entities, net.minecraft.entity.damage.DamageSource damageSource, float damage) {
        AtomicInteger killedCount = new AtomicInteger(0);
        List<LivingEntity> failedEntities = new ArrayList<>();
        
        for (LivingEntity entity : entities) {
            try {
                killEntity(entity, damageSource, damage);
                if (entity.isDead()) {
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
            if (!entity.isDead()) {
                for (int i = 0; i < MAX_RETRIES; i++) {
                    try {
                        entity.setHealth(0.0f);
                        entity.onDeath(damageSource);
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
     * Yarn映射中字段名为 timeUntilRegen
     */
    private static void resetInvulnerability(LivingEntity entity) {
        try {
            java.lang.reflect.Field field = LivingEntity.class.getDeclaredField("timeUntilRegen");
            field.setAccessible(true);
            field.setInt(entity, 0);
        } catch (Exception e) {
            LOGGER.debug("Failed to reset timeUntilRegen: {}", e.getMessage());
        }
    }
    
    /**
     * 尝试正常伤害
     */
    private static boolean tryNormalDamage(LivingEntity entity, net.minecraft.entity.damage.DamageSource damageSource, float damage) {
        try {
            float originalHealth = entity.getHealth();
            entity.damage(damageSource, damage);
            return entity.getHealth() < originalHealth || entity.isDead();
        } catch (Exception e) {
            LOGGER.debug("Normal damage failed for {}: {}", entity.getName().getString(), e.getMessage());
            return false;
        }
    }
    
    /**
     * 尝试直接调用applyDamage（protected方法，需反射）
     * Yarn映射中 actuallyHurt 对应 applyDamage
     */
    private static void tryApplyDamage(LivingEntity entity, net.minecraft.entity.damage.DamageSource damageSource, float damage) {
        try {
            java.lang.reflect.Method method = LivingEntity.class.getDeclaredMethod("applyDamage", 
                net.minecraft.entity.damage.DamageSource.class, float.class);
            method.setAccessible(true);
            method.invoke(entity, damageSource, damage);
            LOGGER.debug("Successfully called applyDamage on {}", entity.getName().getString());
        } catch (Exception e) {
            LOGGER.debug("applyDamage failed for {}: {}", entity.getName().getString(), e.getMessage());
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
    private static void forceDie(LivingEntity entity, net.minecraft.entity.damage.DamageSource damageSource) {
        try {
            entity.setHealth(0.0f);
            entity.onDeath(damageSource);
            
            if (!entity.isDead()) {
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
     * Yarn映射中 dropFromLootTable 对应 dropLoot
     */
    private static void ensureDrops(LivingEntity entity, net.minecraft.entity.damage.DamageSource damageSource) {
        if (entity.getWorld().isClient) {
            return;
        }
        
        try {
            if (entity.isDead() && !entity.getCommandTags().contains(PROCESSED_TAG)) {
                entity.addCommandTag(PROCESSED_TAG);
                
                // 反射调用 dropLoot（protected方法）
                try {
                    java.lang.reflect.Method dropMethod = LivingEntity.class.getDeclaredMethod(
                        "dropLoot", 
                        net.minecraft.entity.damage.DamageSource.class, 
                        boolean.class);
                    dropMethod.setAccessible(true);
                    
                    // 反射读取 attackingPlayer（protected字段）判断是否由玩家击杀
                    boolean causedByPlayer = false;
                    try {
                        java.lang.reflect.Field field = LivingEntity.class.getDeclaredField("attackingPlayer");
                        field.setAccessible(true);
                        causedByPlayer = field.get(entity) != null;
                    } catch (Exception e) {
                        LOGGER.debug("Failed to read attackingPlayer: {}", e.getMessage());
                    }
                    
                    dropMethod.invoke(entity, damageSource, causedByPlayer);
                } catch (Exception e) {
                    LOGGER.debug("dropLoot failed: {}", e.getMessage());
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
    private static void manualDropItems(LivingEntity entity, net.minecraft.entity.damage.DamageSource damageSource) {
        try {
            // 掉落装备
            for (net.minecraft.entity.EquipmentSlot slot : net.minecraft.entity.EquipmentSlot.values()) {
                if (slot.getType() == net.minecraft.entity.EquipmentSlot.Type.ARMOR ||
                    slot == net.minecraft.entity.EquipmentSlot.MAINHAND ||
                    slot == net.minecraft.entity.EquipmentSlot.OFFHAND) {
                    ItemStack itemStack = entity.getEquippedStack(slot);
                    if (!itemStack.isEmpty()) {
                        entity.dropStack(itemStack);
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
        entity.getCommandTags().remove(BYPASS_TAG);
        entity.getCommandTags().remove(PROCESSED_TAG);
    }
    
    /**
     * 获取实体附近的生物实体
     */
    public static List<LivingEntity> getNearbyLivingEntities(World world, Vec3d center, double radius) {
        List<LivingEntity> result = new ArrayList<>();
        Box searchArea = new Box(center, center).expand(radius);
        
        List<Entity> entities = world.getOtherEntities(null, searchArea);
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
    public static boolean canBeDamaged(LivingEntity entity, net.minecraft.entity.damage.DamageSource damageSource) {
        if (entity == null || damageSource == null) {
            return false;
        }
        
        if (entity.isDead()) {
            return false;
        }
        
        // 对于始源伤害类型，绕过免疫检查
        if (damageSource instanceof ModDamageSources.OriginEndDamageSource) {
            return true;
        }
        
        return !entity.isInvulnerableTo(damageSource);
    }
}
