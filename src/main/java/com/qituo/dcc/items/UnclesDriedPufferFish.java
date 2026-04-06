package com.qituo.dcc.items;

import com.qituo.dcc.DragonCurseChronicles;
import com.qituo.dcc.damage.DamagePresets;
import com.qituo.dcc.damage.OriginEndDamageSource;
import com.qituo.dcc.sounds.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import java.util.List;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnclesDriedPufferFish extends Item {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnclesDriedPufferFish.class);
    private static final int CASTING_TIME = 100; // 5秒，20刻/秒
    
    public UnclesDriedPufferFish(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        LOGGER.info("[Uncle's Puffer Fish] Use method called, level.isClientSide: {}", level.isClientSide);
        
        // 开始施法
        player.startUsingItem(hand);
        LOGGER.info("[Uncle's Puffer Fish] Started using item, casting time: {}", CASTING_TIME);
        
        // 检查声音事件是否注册成功
        LOGGER.info("[Uncle's Puffer Fish] Checking sound event registration status");
        LOGGER.info("[Uncle's Puffer Fish] MADGAQ sound event: {}", ModSounds.MADGAQ);
        LOGGER.info("[Uncle's Puffer Fish] MADGAQ sound event get(): {}", ModSounds.MADGAQ.get());
        LOGGER.info("[Uncle's Puffer Fish] MADGAQ sound event location: {}", ModSounds.MADGAQ.getId());
        
        // 播放施法音频 - 客户端和服务器端都播放
        LOGGER.info("[Uncle's Puffer Fish] Playing spell sound");
        
        // 尝试不同的音量和音高
        float volume = 2.0F; // 增加音量
        float pitch = 1.0F;
        
        if (level.isClientSide) {
            // 客户端播放声音
            LOGGER.info("[Uncle's Puffer Fish] Client side: playing sound with volume={}, pitch={}", volume, pitch);
            player.playSound(ModSounds.MADGAQ.get(), volume, pitch);
        } else {
            // 服务器端播放声音（会同步到客户端）
            LOGGER.info("[Uncle's Puffer Fish] Server side: playing sound with volume={}, pitch={}", volume, pitch);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.MADGAQ.get(), SoundSource.PLAYERS, volume, pitch);
        }
        
        LOGGER.info("[Uncle's Puffer Fish] Sound played successfully");
        LOGGER.info("[Uncle's Puffer Fish] Player position: x={}, y={}, z={}", player.getX(), player.getY(), player.getZ());
        LOGGER.info("[Uncle's Puffer Fish] Level: {}, dimension: {}", level, level.dimension());

        
        return InteractionResultHolder.consume(stack); // 使用consume而不是success，确保物品被正确使用
    }
    
    @Override
    public int getUseDuration(ItemStack stack) {
        LOGGER.info("[Uncle's Puffer Fish] getUseDuration called, returning: {}", CASTING_TIME);
        return CASTING_TIME;
    }
    
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        LOGGER.info("[Uncle's Puffer Fish] getUseAnimation called");
        return UseAnim.BLOCK; // 使用BLOCK动画，更符合施法的动作
    }
    
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        LOGGER.info("[Uncle's Puffer Fish] releaseUsing method called");
        if (!(entity instanceof Player player)) {
            LOGGER.info("[Uncle's Puffer Fish] Entity is not a player, returning");
            return;
        }
        
        int timeUsed = getUseDuration(stack) - timeLeft;
        LOGGER.info("[Uncle's Puffer Fish] Time used: {}, time left: {}, casting time: {}", timeUsed, timeLeft, CASTING_TIME);
        
        // 施法完成，释放绿色激光
        LOGGER.info("[Uncle's Puffer Fish] Releasing laser");
        Vec3 lookVec = player.getLookAngle();
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(lookVec.scale(20));
        
        LOGGER.info("[Uncle's Puffer Fish] Laser details - lookVec: {}, start: {}, end: {}", lookVec, start, end);
        
        // 服务器端处理伤害
        if (!level.isClientSide) {
            LOGGER.info("[Uncle's Puffer Fish] Server side: processing laser damage");
            
            // 检测并伤害路径上的实体 - 增大AABB范围，确保能检测到更多实体
            AABB laserAABB = new AABB(start, end).inflate(5.0); // 增大到5.0，确保能检测到更多实体
            List<Entity> entities = level.getEntities(player, laserAABB);
            LOGGER.info("[Uncle's Puffer Fish] Found {} entities in laser path", entities.size());
            
            // 使用预设的抹杀伤害值
            float damage = DamagePresets.OMEGA; // 使用1314520伤害值
            
            for (Entity target : entities) {
                if (target != player) {
                    LOGGER.info("[Uncle's Puffer Fish] Damaging entity: {} at position {} with high damage", target.getName().getString(), target.position());
                    
                    // 使用正确的伤害方法，确保实体正常死亡并掉落物品
                    if (target instanceof LivingEntity livingEntity) {
                        LOGGER.info("[Uncle's Puffer Fish] Entity health before: {}", livingEntity.getHealth());
                        // 创建伤害源并造成伤害
                        OriginEndDamageSource damageSource = new OriginEndDamageSource(player);
                        
                        // 针对Draconic Guardian的特殊处理
                        if (target.getClass().getName().equals("com.brandon3055.draconicevolution.entity.guardian.DraconicGuardianEntity")) {
                            LOGGER.info("[Uncle's Puffer Fish] Special handling for Draconic Guardian");
                            try {
                                // 先设置护盾为0
                                LOGGER.info("[Uncle's Puffer Fish] Setting Draconic Guardian shield to 0");
                                java.lang.reflect.Method setShieldPowerMethod = target.getClass().getMethod("setShieldPower", float.class);
                                setShieldPowerMethod.invoke(target, 0.0F);
                                
                                // 获取头部部分
                                LOGGER.info("[Uncle's Puffer Fish] Getting Draconic Guardian head part");
                                java.lang.reflect.Method getDragonPartsMethod = target.getClass().getMethod("getDragonParts");
                                Object[] parts = (Object[]) getDragonPartsMethod.invoke(target);
                                
                                if (parts != null && parts.length > 0) {
                                    Object headPart = parts[0]; // 头部是第一个部分
                                    LOGGER.info("[Uncle's Puffer Fish] Attacking Draconic Guardian head");
                                    
                                    // 多次攻击头部，确保击杀
                                    for (int i = 0; i < 5; i++) {
                                        // 调用attackEntityPartFrom方法攻击头部
                                        java.lang.reflect.Method attackEntityPartFromMethod = target.getClass().getMethod("attackEntityPartFrom", Class.forName("com.brandon3055.draconicevolution.entity.guardian.DraconicGuardianPartEntity"), Class.forName("net.minecraft.world.damagesource.DamageSource"), float.class);
                                        attackEntityPartFromMethod.invoke(target, headPart, player.damageSources().playerAttack(player), 1000.0F);
                                        LOGGER.info("[Uncle's Puffer Fish] Attack {} completed", i+1);
                                        
                                        // 检查是否已死亡
                                        if (livingEntity.isDeadOrDying()) {
                                            LOGGER.info("[Uncle's Puffer Fish] Draconic Guardian killed");
                                            break;
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                LOGGER.error("[Uncle's Puffer Fish] Failed to handle Draconic Guardian: {}", e.getMessage());
                                // 失败时使用正常伤害方法
                                livingEntity.hurt(damageSource, damage);
                            }
                        } else {
                            // 对其他实体使用正常伤害方法
                            livingEntity.hurt(damageSource, damage);
                        }
                        
                        LOGGER.info("[Uncle's Puffer Fish] Entity health after: {}", livingEntity.getHealth());
                    }
                }
            }
        }
        
        // 生成粒子效果 - 客户端和服务器端都生成
        LOGGER.info("[Uncle's Puffer Fish] Generating laser particles, isClientSide: {}", level.isClientSide);
        
        // 生成更多粒子，使激光更明显
        for (int i = 0; i < 2000; i++) { // 增加粒子数量到2000
            double distance = i * 0.02; // 更密集的粒子
            Vec3 pos = start.add(lookVec.scale(distance));
            
            // 添加火焰粒子
            level.addParticle(ParticleTypes.FLAME, pos.x, pos.y, pos.z, 0, 0, 0);
            
            // 添加绿色粒子效果
            level.addParticle(ParticleTypes.HAPPY_VILLAGER, pos.x, pos.y, pos.z, 0, 0, 0);
            
            // 添加更多绿色粒子
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, pos.x, pos.y, pos.z, 0, 0, 0);
            
            // 添加白色粒子，使激光更明亮
            level.addParticle(ParticleTypes.WHITE_ASH, pos.x, pos.y, pos.z, 0, 0, 0);
            
            // 添加火花粒子
            level.addParticle(ParticleTypes.SPIT, pos.x, pos.y, pos.z, 0, 0, 0);
        }
        
        // 播放额外的音效，使激光更有冲击力
        level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.0F, 1.0F);
        
        // 播放火焰音效
        level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 1.0F, 1.0F);
        
        // 添加使用冷却，5秒（100刻）
        player.getCooldowns().addCooldown(this, 100);
    }
    
    private boolean isPointInLine(Vec3 start, Vec3 end, Vec3 point, double radius) {
        // 计算点到线段的距离
        Vec3 lineVec = end.subtract(start);
        Vec3 pointVec = point.subtract(start);
        double lineLengthSqr = lineVec.lengthSqr();
        
        if (lineLengthSqr == 0) {
            return point.distanceToSqr(start) <= radius * radius;
        }
        
        double t = Math.max(0, Math.min(1, pointVec.dot(lineVec) / lineLengthSqr));
        Vec3 closestPoint = start.add(lineVec.scale(t));
        return point.distanceToSqr(closestPoint) <= radius * radius;
    }
}
