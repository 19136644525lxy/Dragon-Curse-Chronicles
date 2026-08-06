package com.qituo.dcc.enchantments;

import com.qituo.dcc.damage.EntityBypassHelper;
import com.qituo.dcc.damage.ModDamageSources;
import com.qituo.dcc.network.ModNetwork;
import com.qituo.dcc.util.PersistentDataAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 始源之力附魔 - 盔甲效果处理器（Fabric 版）
 * 负责所有盔甲相关的被动效果：反弹、减伤、护盾、再生、免疫击退、光环
 *
 * 效果阶梯（按总等级）：
 * 1-5:   反弹+减伤（基础）
 * 6-10:  解锁始源护盾
 * 11-20: 解锁始源再生
 * 21-30: 解锁免疫击退
 * 31-40: 解锁始源光环
 *
 * 注意：Fabric 版本中，减伤和免疫击退通过 Mixin 实现（见 OriginPowerArmorMixin），
 * 周期效果（护盾/再生/光环）通过 ServerTickEvents 在主类注册。
 */
public class OriginPowerArmorHandler {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(OriginPowerArmorHandler.class);

    private static final String TAG_ANTI_LOOP = "temp$OriginPowerEnchantTag";

    /** 玩家tick计数器（用于周期性效果的间隔控制，线程安全） */
    private static final Map<UUID, Integer> tickCounters = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * 计算玩家护甲上的始源之力总等级
     */
    public static int getTotalArmorLevel(PlayerEntity player) {
        int total = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                total += net.minecraft.enchantment.EnchantmentHelper.getLevel(
                        ModEnchantments.ORIGIN_POWER, player.getEquippedStack(slot));
            }
        }
        return total;
    }

    // ==================== 减伤 + 反弹 ====================

    /**
     * 减伤比例 = 总等级/80，封顶50%
     * 满级40 → 50%
     */
    public static float getDamageReduction(int totalLevel) {
        return Math.min(0.5F, totalLevel / 80.0F);
    }

    /**
     * 伤害反弹概率 = 总等级/100 * 1.5（非线性加成）
     * 满级40 → 60%
     */
    public static float getReflectionChance(int totalLevel) {
        return Math.min(0.6F, totalLevel / 100.0F * 1.5F);
    }

    // ==================== 周期事件：护盾 + 再生 + 光环 ====================

    /**
     * 服务器 tick 回调（由主类通过 ServerTickEvents 注册）
     * 遍历所有玩家，应用周期性盔甲效果
     */
    public static void onServerTick(World world) {
        if (world.isClient) return;

        for (PlayerEntity player : world.getPlayers()) {
            int totalLevel = getTotalArmorLevel(player);
            if (totalLevel <= 0) {
                tickCounters.remove(player.getUuid());
                continue;
            }

            int tick = tickCounters.getOrDefault(player.getUuid(), 0) + 1;
            tickCounters.put(player.getUuid(), tick);

            // 始源护盾（总等级≥6）
            if (totalLevel >= 6) {
                applyOriginShield(player, totalLevel, tick);
            }

            // 始源再生（总等级≥11）
            if (totalLevel >= 11) {
                applyOriginRegeneration(player, totalLevel, tick);
            }

            // 始源光环（总等级≥31）
            if (totalLevel >= 31) {
                applyOriginAura(player, totalLevel, tick);
            }

            // 清理过期计数器防止内存泄漏
            if (tick > 10000) {
                tickCounters.put(player.getUuid(), 1);
            }
        }
    }

    /**
     * 始源护盾 - 周期性给予吸收效果
     * 吸收量 = 总等级 × 系数，冷却随等级递减
     */
    private static void applyOriginShield(PlayerEntity player, int totalLevel, int tick) {
        int interval;
        float absorptionAmount;

        if (totalLevel <= 10) {
            interval = 200;     // 10秒
            absorptionAmount = totalLevel * 1.0F;
        } else if (totalLevel <= 20) {
            interval = 150;     // 7.5秒
            absorptionAmount = totalLevel * 1.5F;
        } else if (totalLevel <= 30) {
            interval = 120;     // 6秒
            absorptionAmount = totalLevel * 2.0F;
        } else {
            interval = 100;     // 5秒
            absorptionAmount = totalLevel * 3.0F;
        }

        if (tick % interval == 0) {
            float currentAbsorption = player.getAbsorptionAmount();
            float threshold = absorptionAmount * 0.5F;
            if (currentAbsorption < threshold) {
                player.setAbsorptionAmount(Math.min(absorptionAmount,
                        absorptionAmount - currentAbsorption + player.getAbsorptionAmount()));
            }
        }
    }

    /**
     * 始源再生 - 周期性恢复生命值
     * 基准间隔2秒（40tick），高等级递减
     */
    private static void applyOriginRegeneration(PlayerEntity player, int totalLevel, int tick) {
        int interval;
        int healAmount;

        if (totalLevel <= 15) {
            interval = 40;      // 2秒
            healAmount = 1;
        } else if (totalLevel <= 25) {
            interval = 30;      // 1.5秒
            healAmount = 1;
        } else if (totalLevel <= 35) {
            interval = 25;      // 1.25秒
            healAmount = 2;
        } else {
            interval = 20;      // 1秒
            healAmount = 2;
        }

        if (tick % interval == 0) {
            if (player.getHealth() < player.getMaxHealth()) {
                player.heal(healAmount);
            }
        }
    }

    /**
     * 始源光环 - 对周围实体造成周期性始源伤害
     *
     * 开关：玩家可通过按键绑定（默认无绑定）切换光环启用状态。
     * 状态存储在玩家持久化NBT的 dcc_aura_disabled 标签，默认启用。
     */
    private static void applyOriginAura(PlayerEntity player, int totalLevel, int tick) {
        // 检查光环开关：被禁用则跳过（默认启用，保持原行为）
        if (((PersistentDataAccess) player).getDccPersistentData()
                .getBoolean(ModNetwork.TAG_AURA_DISABLED)) {
            return;
        }

        if (tick % 40 != 0) {
            return;
        }

        // 半径统一为 10 格（用户要求）
        double radius = 10.0;
        float damage = (float) (totalLevel * (totalLevel >= 36 ? 0.5 : 0.3));

        Box searchArea = player.getBoundingBox().expand(radius);
        List<LivingEntity> entities = player.getWorld().getEntitiesByClass(LivingEntity.class, searchArea,
                e -> e != player && !e.isSpectator() && e.isAlive());

        if (entities.isEmpty()) {
            return;
        }

        var damageSource = ModDamageSources.causeOriginEndDamage(player);
        for (LivingEntity entity : entities) {
            EntityBypassHelper.killEntity(entity, damageSource, damage);
        }
    }
}
