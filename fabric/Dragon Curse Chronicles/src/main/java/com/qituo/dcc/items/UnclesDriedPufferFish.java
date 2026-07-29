package com.qituo.dcc.items;

import com.qituo.dcc.DragonCurseChronicles;
import com.qituo.dcc.damage.DamagePresets;
import com.qituo.dcc.damage.ModDamageSources;
import com.qituo.dcc.enchantments.ModEnchantments;
import com.qituo.dcc.sounds.ModSounds;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.UseAction;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.world.World;
import net.minecraft.util.math.Box;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnclesDriedPufferFish extends Item {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnclesDriedPufferFish.class);
    private static final int CASTING_TIME = 100;

    public UnclesDriedPufferFish(Item.Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack stack = super.getDefaultStack();
        // 在1.20.1中EnchantmentHelper.setEnchantments已被移除，使用循环添加附魔
        java.util.Map<net.minecraft.enchantment.Enchantment, Integer> enchantments = java.util.Map.of(ModEnchantments.ORIGIN_POWER, 10);
        net.minecraft.enchantment.EnchantmentHelper.set(enchantments, stack);
        return stack;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        LOGGER.info("[Uncle's Puffer Fish] Use method called, world.isClient: {}", world.isClient);

        user.setCurrentHand(hand);
        LOGGER.info("[Uncle's Puffer Fish] Started using item, casting time: {}", CASTING_TIME);

        LOGGER.info("[Uncle's Puffer Fish] Checking sound event registration status");
        LOGGER.info("[Uncle's Puffer Fish] MADGAQ sound event: {}", ModSounds.MADGAQ);

        float volume = 2.0F;
        float pitch = 1.0F;

        if (world.isClient) {
            LOGGER.info("[Uncle's Puffer Fish] Client side: playing sound with volume={}, pitch={}", volume, pitch);
            user.playSound(ModSounds.MADGAQ, volume, pitch);
        } else {
            LOGGER.info("[Uncle's Puffer Fish] Server side: playing sound with volume={}, pitch={}", volume, pitch);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), ModSounds.MADGAQ, SoundCategory.PLAYERS, volume, pitch);
        }

        LOGGER.info("[Uncle's Puffer Fish] Sound played successfully");

        return TypedActionResult.consume(stack);
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        LOGGER.info("[Uncle's Puffer Fish] getMaxUseTime called, returning: {}", CASTING_TIME);
        return CASTING_TIME;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        LOGGER.info("[Uncle's Puffer Fish] getUseAction called");
        return UseAction.BOW;
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseDuration) {
        LOGGER.info("[Uncle's Puffer Fish] onStoppedUsing method called");
        if (!(user instanceof PlayerEntity player)) {
            LOGGER.info("[Uncle's Puffer Fish] Entity is not a player, returning");
            return;
        }

        int timeUsed = getMaxUseTime(stack) - remainingUseDuration;
        LOGGER.info("[Uncle's Puffer Fish] Time used: {}, time left: {}, casting time: {}", timeUsed, remainingUseDuration, CASTING_TIME);

        LOGGER.info("[Uncle's Puffer Fish] Releasing laser");
        Vec3d lookVec = player.getRotationVec(1.0F);
        Vec3d start = player.getEyePos();
        Vec3d end = start.add(lookVec.multiply(20.0));

        LOGGER.info("[Uncle's Puffer Fish] Laser details - lookVec: {}, start: {}, end: {}", lookVec, start, end);

        if (!world.isClient) {
            LOGGER.info("[Uncle's Puffer Fish] Server side: processing laser damage");

            Box laserBox = new Box(start, end).expand(5.0);
            List<Entity> entities = world.getOtherEntities(player, laserBox);
            LOGGER.info("[Uncle's Puffer Fish] Found {} entities in laser path", entities.size());

            float damage = DamagePresets.getDamage(10);

            for (Entity target : entities) {
                if (target != player) {
                    LOGGER.info("[Uncle's Puffer Fish] Damaging entity: {} at position {} with high damage", target.getName().getString(), target.getPos());

                    if (target instanceof LivingEntity livingEntity) {
                        LOGGER.info("[Uncle's Puffer Fish] Entity health before: {}", livingEntity.getHealth());
                        var damageSource = ModDamageSources.causeOriginEndDamage(player, 10);

                        if (target.getClass().getName().equals("com.brandon3055.draconicevolution.entity.guardian.DraconicGuardianEntity")) {
                            LOGGER.info("[Uncle's Puffer Fish] Special handling for Draconic Guardian");
                            try {
                                LOGGER.info("[Uncle's Puffer Fish] Setting Draconic Guardian shield to 0");
                                java.lang.reflect.Method setShieldPowerMethod = target.getClass().getMethod("setShieldPower", float.class);
                                setShieldPowerMethod.invoke(target, 0.0F);

                                LOGGER.info("[Uncle's Puffer Fish] Getting Draconic Guardian head part");
                                java.lang.reflect.Method getDragonPartsMethod = target.getClass().getMethod("getDragonParts");
                                Object[] parts = (Object[]) getDragonPartsMethod.invoke(target);

                                if (parts != null && parts.length > 0) {
                                    Object headPart = parts[0];
                                    LOGGER.info("[Uncle's Puffer Fish] Attacking Draconic Guardian head");

                                    for (int i = 0; i < 5; i++) {
                                        java.lang.reflect.Method attackEntityPartFromMethod = target.getClass().getMethod("attackEntityPartFrom", Class.forName("com.brandon3055.draconicevolution.entity.guardian.DraconicGuardianPartEntity"), Class.forName("net.minecraft.world.damagesource.DamageSource"), float.class);
                                        attackEntityPartFromMethod.invoke(target, headPart, player.getDamageSources().playerAttack(player), 1000.0F);
                                        LOGGER.info("[Uncle's Puffer Fish] Attack {} completed", i+1);

                                        if (livingEntity.isDead()) {
                                            LOGGER.info("[Uncle's Puffer Fish] Draconic Guardian killed");
                                            break;
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                LOGGER.error("[Uncle's Puffer Fish] Failed to handle Draconic Guardian: {}", e.getMessage());
                                livingEntity.damage(damageSource, damage);
                            }
                        } else {
                            livingEntity.damage(damageSource, damage);
                        }

                        LOGGER.info("[Uncle's Puffer Fish] Entity health after: {}", livingEntity.getHealth());
                    }
                }
            }
        }

        LOGGER.info("[Uncle's Puffer Fish] Generating laser particles, isClient: {}", world.isClient);

        for (int i = 0; i < 2000; i++) {
            double distance = i * 0.02;
            Vec3d pos = start.add(lookVec.multiply(distance));

            world.addParticle(ParticleTypes.FLAME, pos.x, pos.y, pos.z, 0, 0, 0);
            world.addParticle(ParticleTypes.HAPPY_VILLAGER, pos.x, pos.y, pos.z, 0, 0, 0);
            world.addParticle(ParticleTypes.SOUL_FIRE_FLAME, pos.x, pos.y, pos.z, 0, 0, 0);
            world.addParticle(ParticleTypes.WHITE_ASH, pos.x, pos.y, pos.z, 0, 0, 0);
            world.addParticle(ParticleTypes.SPIT, pos.x, pos.y, pos.z, 0, 0, 0);
        }

        world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.PLAYERS, 1.0F, 1.0F);
        world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.PLAYERS, 1.0F, 1.0F);

        player.getItemCooldownManager().set(this, 100);
    }

    private boolean isPointInLine(Vec3d start, Vec3d end, Vec3d point, double radius) {
        Vec3d lineVec = end.subtract(start);
        Vec3d pointVec = point.subtract(start);
        double lineLengthSqr = lineVec.lengthSquared();

        if (lineLengthSqr == 0) {
            return point.distanceTo(start) <= radius * radius;
        }

        double t = Math.max(0, Math.min(1, pointVec.dotProduct(lineVec) / lineLengthSqr));
        Vec3d closestPoint = start.add(lineVec.multiply(t));
        return point.distanceTo(closestPoint) <= radius * radius;
    }
}