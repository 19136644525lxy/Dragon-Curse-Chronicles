package com.qituo.dcc.ai;

import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.qituo.dcc.util.ExceptionHandler;

/**
 * 恶人格AI处理器
 * 控制恶人格的行为，包括追踪、攻击等
 */
@Mod.EventBusSubscriber(modid = "dcc")
public class EvilCloneAI {
    
    static {
        System.out.println("[恶人格AI] 类已加载，事件监听器已注册");
    }

    private static final Map<UUID, EvilCloneData> evilClones = new HashMap<>();
    private static final int ATTACK_COOLDOWN = 20; // 攻击冷却时间（tick）
    private static final double ATTACK_RANGE = 3.0; // 攻击范围
    private static final double FOLLOW_RANGE = 32.0; // 追踪范围
    private static final double AMBUSH_RANGE = 8.0; // 伏击范围
    private static final double DANGER_RANGE = 5.0; // 危险范围（需要躲避）
    private static final int AMBUSH_COOLDOWN = 120; // 伏击冷却时间（tick）
    private static final int DODGE_COOLDOWN = 20; // 躲避冷却时间（tick）
    private static final int WANDER_CHANCE = 30; // 游荡概率（百分比）

    /**
     * 恶人格数据类
     */
    private static class EvilCloneData {
        final ServerPlayer evilClone;
        final Player originalPlayer;
        LivingEntity target;
        int attackCooldown;
        int wanderTimer;
        int ambushCooldown;
        int dodgeCooldown;
        boolean isAmbushing;
        Vec3 ambushPos;

        EvilCloneData(ServerPlayer evilClone, Player originalPlayer) {
            this.evilClone = evilClone;
            this.originalPlayer = originalPlayer;
            this.target = originalPlayer;
            this.attackCooldown = 0;
            this.wanderTimer = 0;
            this.ambushCooldown = 0;
            this.dodgeCooldown = 0;
            this.isAmbushing = false;
            this.ambushPos = null;
        }
    }

    /**
     * 注册恶人格
     */
    public static void registerEvilClone(ServerPlayer evilClone, Player originalPlayer) {
        evilClones.put(evilClone.getUUID(), new EvilCloneData(evilClone, originalPlayer));
        System.out.println("[恶人格AI] 注册恶人格: " + evilClone.getName().getString());
    }

    /**
     * 移除恶人格
     */
    public static void removeEvilClone(UUID evilCloneUUID) {
        evilClones.remove(evilCloneUUID);
        System.out.println("[恶人格AI] 移除恶人格: " + evilCloneUUID);
    }

    /**
     * 每个tick更新恶人格的AI
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        try {
            System.out.println("[恶人格AI] 服务器tick事件触发");
            if (event.phase != TickEvent.Phase.END) {
                System.out.println("[恶人格AI] 事件阶段不是END，跳过");
                return;
            }

            if (evilClones.isEmpty()) {
                System.out.println("[恶人格AI] 没有恶人格，跳过");
                return;
            }

            System.out.println("[恶人格AI] 服务器tick，当前有 " + evilClones.size() + " 个恶人格");

            evilClones.entrySet().removeIf(entry -> {
                try {
                    UUID uuid = entry.getKey();
                    EvilCloneData data = entry.getValue();
                    ServerPlayer evilClone = data.evilClone;

                    if (!evilClone.isAlive()) {
                        System.out.println("[恶人格AI] 恶人格已死亡: " + evilClone.getName().getString());
                        return true;
                    }

                    updateEvilCloneAI(data);
                    return false;
                } catch (Exception e) {
                    ExceptionHandler.handleAIException("处理恶人格数据", e);
                    return true;
                }
            });
        } catch (Exception e) {
            ExceptionHandler.handleAIException("服务器tick事件", e);
        }
    }

    /**
     * 更新恶人格的AI行为
     */
    private static void updateEvilCloneAI(EvilCloneData data) {
        try {
            ServerPlayer evilClone = data.evilClone;
            Player originalPlayer = data.originalPlayer;

            if (!originalPlayer.isAlive()) {
                evilClone.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
                return;
            }

            // 更新冷却时间
            if (data.attackCooldown > 0) data.attackCooldown--;
            if (data.ambushCooldown > 0) data.ambushCooldown--;
            if (data.dodgeCooldown > 0) data.dodgeCooldown--;

            System.out.println("[恶人格AI] 更新AI: " + evilClone.getName().getString() + ", 目标: " + (data.target != null ? data.target.getName().getString() : "null"));

            updateTarget(data);
            updateMovement(data);
            updateAttack(data);
        } catch (Exception e) {
            ExceptionHandler.handleAIException("更新恶人格AI", e);
        }
    }

    /**
     * 更新目标
     */
    private static void updateTarget(EvilCloneData data) {
        ServerPlayer evilClone = data.evilClone;
        Player originalPlayer = data.originalPlayer;

        if (data.target == null || !data.target.isAlive()) {
            data.target = originalPlayer;
        }

        double distanceToOriginal = evilClone.distanceTo(originalPlayer);

        if (distanceToOriginal > FOLLOW_RANGE) {
            data.target = null;
        }
    }

    /**
     * 更新移动
     */
    private static void updateMovement(EvilCloneData data) {
        ServerPlayer evilClone = data.evilClone;
        LivingEntity target = data.target;

        if (target == null) {
            wander(data);
            return;
        }

        double distance = evilClone.distanceTo(target);

        // 检查是否需要躲避
        if (shouldDodge(data, target)) {
            dodge(data, target);
            return;
        }

        // 检查是否需要伏击
        if (shouldAmbush(data, target)) {
            ambush(data, target);
            return;
        }

        if (distance > ATTACK_RANGE) {
            moveToTarget(evilClone, target);
        } else {
            faceTarget(evilClone, target);
        }
    }
    
    /**
     * 检查是否需要躲避
     */
    private static boolean shouldDodge(EvilCloneData data, LivingEntity target) {
        ServerPlayer evilClone = data.evilClone;
        double distance = evilClone.distanceTo(target);
        
        // 如果在危险范围内且不在冷却中
        return distance <= DANGER_RANGE && data.dodgeCooldown <= 0;
    }
    
    /**
     * 躲避行为
     */
    private static void dodge(EvilCloneData data, LivingEntity target) {
        ServerPlayer evilClone = data.evilClone;
        Vec3 targetPos = target.position();
        Vec3 currentPos = evilClone.position();
        
        // 计算远离目标的方向
        Vec3 direction = currentPos.subtract(targetPos).normalize();
        double dodgeDistance = 1.5; // 躲避距离
        
        Vec3 dodgePos = currentPos.add(direction.x * dodgeDistance, 0, direction.z * dodgeDistance);
        
        // 移动到躲避位置
        double moveX = (dodgePos.x - currentPos.x) * 0.5;
        double moveZ = (dodgePos.z - currentPos.z) * 0.5;
        double moveY = evilClone.getDeltaMovement().y;
        
        evilClone.moveRelative(1.0F, new Vec3(moveX, 0.0, moveZ));
        evilClone.setDeltaMovement(evilClone.getDeltaMovement().x, moveY, evilClone.getDeltaMovement().z);
        evilClone.hasImpulse = true;
        
        System.out.println("[恶人格AI] " + evilClone.getName().getString() + " 躲避了攻击");
        data.dodgeCooldown = DODGE_COOLDOWN;
    }
    
    /**
     * 检查是否需要伏击
     */
    private static boolean shouldAmbush(EvilCloneData data, LivingEntity target) {
        ServerPlayer evilClone = data.evilClone;
        double distance = evilClone.distanceTo(target);
        
        // 如果在伏击范围内，不在冷却中，且不在伏击状态
        return distance <= AMBUSH_RANGE && distance > ATTACK_RANGE && data.ambushCooldown <= 0 && !data.isAmbushing;
    }
    
    /**
     * 伏击行为
     */
    private static void ambush(EvilCloneData data, LivingEntity target) {
        ServerPlayer evilClone = data.evilClone;
        
        // 随机选择一个伏击点
        double angle = evilClone.getRandom().nextDouble() * Math.PI * 2;
        double distance = AMBUSH_RANGE * 0.5 + evilClone.getRandom().nextDouble() * AMBUSH_RANGE * 0.5;
        
        Vec3 targetPos = target.position();
        Vec3 ambushPos = new Vec3(
            targetPos.x + Math.sin(angle) * distance,
            targetPos.y,
            targetPos.z + Math.cos(angle) * distance
        );
        
        // 移动到伏击点
        double moveX = (ambushPos.x - evilClone.getX()) * 0.3;
        double moveZ = (ambushPos.z - evilClone.getZ()) * 0.3;
        double moveY = evilClone.getDeltaMovement().y;
        
        evilClone.moveRelative(1.0F, new Vec3(moveX, 0.0, moveZ));
        evilClone.setDeltaMovement(evilClone.getDeltaMovement().x, moveY, evilClone.getDeltaMovement().z);
        evilClone.hasImpulse = true;
        
        data.isAmbushing = true;
        data.ambushPos = ambushPos;
        System.out.println("[恶人格AI] " + evilClone.getName().getString() + " 开始伏击");
    }

    /**
     * 移动到目标
     */
    private static void moveToTarget(ServerPlayer evilClone, LivingEntity target) {
        Vec3 targetPos = target.position();
        Vec3 currentPos = evilClone.position();
        
        double dx = targetPos.x - currentPos.x;
        double dz = targetPos.z - currentPos.z;
        
        double distance = Math.sqrt(dx * dx + dz * dz);
        System.out.println("[恶人格AI] 移动: " + evilClone.getName().getString() + " 距离目标: " + distance);
        
        if (distance <= 0.1) {
            return;
        }
        
        double dirX = dx / distance;
        double dirZ = dz / distance;
        
        double baseSpeed = 0.3;
        double moveSpeed = baseSpeed;
        
        if (distance > 12) {
            moveSpeed = baseSpeed * 1.5;
        } else if (distance > 6) {
            moveSpeed = baseSpeed * 1.2;
        } else if (distance > 3) {
            moveSpeed = baseSpeed * 1.0;
        } else {
            moveSpeed = baseSpeed * -0.3;
        }
        
        System.out.println("[恶人格AI] 实际移动速度: " + moveSpeed);
        
        double moveX = dirX * moveSpeed;
        double moveZ = dirZ * moveSpeed;
        double moveY = evilClone.getDeltaMovement().y;
        
        if (evilClone.getDeltaMovement().y < 0) {
            moveY = evilClone.getDeltaMovement().y * 0.98;
        }
        
        evilClone.moveRelative(1.0F, new Vec3(moveX, 0.0, moveZ));
        
        evilClone.setDeltaMovement(evilClone.getDeltaMovement().x, moveY, evilClone.getDeltaMovement().z);
        evilClone.hasImpulse = true;

        faceTarget(evilClone, target);
    }

    /**
     * 面向目标
     */
    private static void faceTarget(ServerPlayer evilClone, LivingEntity target) {
        Vec3 targetPos = target.position();
        Vec3 currentPos = evilClone.position();
        Vec3 direction = targetPos.subtract(currentPos);

        double yaw = Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0;
        double pitch = -Math.toDegrees(Math.atan2(direction.y, Math.sqrt(direction.x * direction.x + direction.z * direction.z)));

        evilClone.setYRot((float) yaw);
        evilClone.setXRot((float) pitch);
        evilClone.yHeadRot = (float) yaw;
    }

    /**
     * 游荡
     */
    private static void wander(EvilCloneData data) {
        ServerPlayer evilClone = data.evilClone;
        data.wanderTimer--;

        if (data.wanderTimer <= 0) {
            data.wanderTimer = 100 + evilClone.getRandom().nextInt(100);

            if (evilClone.getRandom().nextFloat() < 0.3) {
                double angle = evilClone.getRandom().nextDouble() * Math.PI * 2;
                double distance = 2.0 + evilClone.getRandom().nextDouble() * 3.0;

                Vec3 velocity = new Vec3(
                    Math.sin(angle) * distance * 0.1,
                    0,
                    Math.cos(angle) * distance * 0.1
                );

                evilClone.setDeltaMovement(velocity.x, evilClone.getDeltaMovement().y, velocity.z);
            }
        }
    }

    /**
     * 更新攻击
     */
    private static void updateAttack(EvilCloneData data) {
        ServerPlayer evilClone = data.evilClone;
        LivingEntity target = data.target;

        if (data.attackCooldown > 0) {
            data.attackCooldown--;
            return;
        }

        if (target == null || evilClone.distanceTo(target) > ATTACK_RANGE) {
            return;
        }

        attackTarget(evilClone, target);
        data.attackCooldown = ATTACK_COOLDOWN;
    }

    /**
     * 攻击目标
     */
    private static void attackTarget(ServerPlayer evilClone, LivingEntity target) {
        ItemStack mainHandItem = evilClone.getMainHandItem();

        if (!mainHandItem.isEmpty()) {
            evilClone.attack(target);
            System.out.println("[恶人格AI] " + evilClone.getName().getString() + " 攻击了 " + target.getName().getString());
        } else {
            double damage = evilClone.getAttributeValue(Attributes.ATTACK_DAMAGE);
            target.hurt(evilClone.level().damageSources().playerAttack(evilClone), (float) damage);
            System.out.println("[恶人格AI] " + evilClone.getName().getString() + " 拳击了 " + target.getName().getString());
        }

        swingArm(evilClone);
    }
    
    /**
     * 结束伏击状态
     */
    private static void endAmbush(EvilCloneData data) {
        if (data.isAmbushing) {
            System.out.println("[恶人格AI] " + data.evilClone.getName().getString() + " 结束伏击");
            data.isAmbushing = false;
            data.ambushPos = null;
            data.ambushCooldown = AMBUSH_COOLDOWN;
        }
    }

    /**
     * 挥动手臂动画
     */
    private static void swingArm(ServerPlayer evilClone) {
        evilClone.swing(evilClone.getUsedItemHand());
        
        ClientboundAnimatePacket packet = new ClientboundAnimatePacket(
            evilClone, 
            ClientboundAnimatePacket.SWING_MAIN_HAND
        );
        evilClone.serverLevel().getServer().getPlayerList().broadcastAll(packet, evilClone.serverLevel().dimension());
    }
}
