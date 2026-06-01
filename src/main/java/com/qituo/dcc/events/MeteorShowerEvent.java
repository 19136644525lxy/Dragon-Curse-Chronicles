package com.qituo.dcc.events;

import com.qituo.dcc.DragonCurseChronicles;
import com.qituo.dcc.TalismanItems;
import com.qituo.dcc.config.MeteorShowerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = DragonCurseChronicles.MODID)
public class MeteorShowerEvent {
    
    private static final Random RANDOM = new Random();
    private static boolean isMeteorShowerActive = false;
    private static int meteorShowerTick = 0;
    private static final int METEOR_SHOWER_DURATION = 600;
    private static final double EVENT_TRIGGER_CHANCE = 0.1;
    private static boolean hasCheckedForMeteorShower = false;
    private static BlockPos treasureChestPos = null;
    private static BlockPos showerCenterPos = null;
    private static final List<Meteor> activeMeteors = new ArrayList<>();
    
    private static class Meteor {
        final Vec3 startPos;
        final Vec3 targetPos;
        final int lifespan;
        int ticksLived = 0;
        final float size;
        final double speed;
        
        Meteor(Vec3 start, Vec3 target, int lifespan, float size, double speed) {
            this.startPos = start;
            this.targetPos = target;
            this.lifespan = lifespan;
            this.size = size;
            this.speed = speed;
        }
    }
    
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        
        event.getServer().getAllLevels().forEach(level -> {
            if (level.dimension() == Level.OVERWORLD) {
                checkTimeAndTriggerMeteorShower(level);
                updateMeteorShower(level);
            }
        });
    }
    
    private static void checkTimeAndTriggerMeteorShower(ServerLevel level) {
        long timeOfDay = level.getDayTime() % 24000;
        boolean isMidnight = timeOfDay >= 18000 && timeOfDay <= 18010;
        
        if (isMidnight) {
            if (!hasCheckedForMeteorShower) {
                hasCheckedForMeteorShower = true;
                
                if (MeteorShowerConfig.isMeteorShowerEnabled() && !isMeteorShowerActive && RANDOM.nextDouble() < EVENT_TRIGGER_CHANCE) {
                    if (areAllPlayersInOverworld(level)) {
                        startMeteorShower(level);
                    }
                }
            }
        } else {
            hasCheckedForMeteorShower = false;
        }
    }
    
    private static boolean areAllPlayersInOverworld(ServerLevel level) {
        List<ServerPlayer> players = level.getPlayers(p -> true);
        if (players.isEmpty()) {
            return false;
        }
        
        for (ServerPlayer player : players) {
            if (!player.level().dimension().equals(Level.OVERWORLD)) {
                return false;
            }
        }
        return true;
    }
    
    private static void startMeteorShower(ServerLevel level) {
        isMeteorShowerActive = true;
        meteorShowerTick = 0;
        activeMeteors.clear();
        treasureChestPos = null;
        
        List<ServerPlayer> players = level.getPlayers(p -> true);
        if (!players.isEmpty()) {
            ServerPlayer randomPlayer = players.get(RANDOM.nextInt(players.size()));
            Vec3 pos = randomPlayer.position();
            showerCenterPos = new BlockPos((int) pos.x, (int) pos.y, (int) pos.z);
        } else {
            showerCenterPos = new BlockPos(0, 64, 0);
        }
        
        sendGlobalMessage(level, Component.translatable("dcc.message.meteor_shower.start"));
        
        DragonCurseChronicles.LOGGER.info("流星雨事件开始！中心位置: " + showerCenterPos);
    }
    
    private static void updateMeteorShower(ServerLevel level) {
        if (!isMeteorShowerActive) {
            return;
        }
        
        meteorShowerTick++;
        
        if (meteorShowerTick % 2 == 0 && meteorShowerTick < METEOR_SHOWER_DURATION - 100) {
            spawnRandomMeteor(level);
            if (RANDOM.nextBoolean()) {
                spawnRandomMeteor(level);
            }
        }
        
        List<Meteor> meteorsToRemove = new ArrayList<>();
        for (Meteor meteor : activeMeteors) {
            meteor.ticksLived++;
            
            sendMeteorParticles(level, meteor);
            
            if (meteor.ticksLived >= meteor.lifespan) {
                meteorsToRemove.add(meteor);
                createMeteorCrater(level, meteor.targetPos, meteor.size);
            }
        }
        
        activeMeteors.removeAll(meteorsToRemove);
        
        if (meteorShowerTick >= METEOR_SHOWER_DURATION - 50 && treasureChestPos == null) {
            placeTreasureChestAtCenter(level);
        }
        
        if (meteorShowerTick >= METEOR_SHOWER_DURATION) {
            endMeteorShower(level);
        }
    }
    
    private static void spawnRandomMeteor(ServerLevel level) {
        List<ServerPlayer> players = level.getPlayers(p -> true);
        if (players.isEmpty()) {
            return;
        }
        
        ServerPlayer targetPlayer = players.get(RANDOM.nextInt(players.size()));
        BlockPos playerPos = targetPlayer.blockPosition();
        
        int centerX = playerPos.getX();
        int centerZ = playerPos.getZ();
        int radius = 250;
        
        // 在玩家周围250格范围内随机生成
        double offsetX = (RANDOM.nextDouble() - 0.5) * radius * 2;
        double offsetZ = (RANDOM.nextDouble() - 0.5) * radius * 2;
        
        BlockPos groundPos = findGroundPosition(level, centerX + offsetX, centerZ + offsetZ);
        
        Vec3 targetPos = new Vec3(groundPos.getX() + 0.5, groundPos.getY() + 1, groundPos.getZ() + 0.5);
        
        Vec3 startPos = new Vec3(
            targetPos.x + (RANDOM.nextDouble() - 0.5) * 150,
            280 + RANDOM.nextInt(80),
            targetPos.z + (RANDOM.nextDouble() - 0.5) * 150
        );
        
        int lifespan = 40 + RANDOM.nextInt(30);
        float size = 1.0f + RANDOM.nextFloat() * 2.5f;
        double speed = 0.5 + RANDOM.nextDouble() * 0.5;
        
        activeMeteors.add(new Meteor(startPos, targetPos, lifespan, size, speed));
    }
    
    private static BlockPos findGroundPosition(ServerLevel level, double x, double z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos((int) x, 300, (int) z);
        
        while (pos.getY() > 0) {
            if (!level.getBlockState(pos).isAir() && !level.getBlockState(pos).is(Blocks.WATER)) {
                return pos.immutable();
            }
            pos.move(0, -1, 0);
        }
        
        return new BlockPos((int) x, 64, (int) z);
    }
    
    private static void sendMeteorParticles(ServerLevel level, Meteor meteor) {
        double progress = (double) meteor.ticksLived / meteor.lifespan;
        Vec3 currentPos = new Vec3(
            meteor.startPos.x + (meteor.targetPos.x - meteor.startPos.x) * progress,
            meteor.startPos.y + (meteor.targetPos.y - meteor.startPos.y) * progress,
            meteor.startPos.z + (meteor.targetPos.z - meteor.startPos.z) * progress
        );
        
        for (ServerPlayer player : level.getPlayers(p -> true)) {
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundExplodePacket(
                currentPos.x, currentPos.y, currentPos.z, 0.8f, new ArrayList<>(), new Vec3(0, 0, 0)
            ));
            
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket(
                net.minecraft.core.particles.ParticleTypes.FLAME,
                true,
                currentPos.x, currentPos.y, currentPos.z,
                meteor.size * 1.5f, meteor.size * 1.5f, meteor.size * 1.5f,
                0.15f,
                15
            ));
            
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket(
                net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,
                true,
                currentPos.x, currentPos.y, currentPos.z,
                meteor.size, meteor.size, meteor.size,
                0.08f,
                10
            ));
            
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket(
                net.minecraft.core.particles.ParticleTypes.END_ROD,
                true,
                currentPos.x, currentPos.y, currentPos.z,
                meteor.size * 2, meteor.size * 2, meteor.size * 2,
                0.2f,
                8
            ));
        }
    }
    
    private static void createMeteorCrater(ServerLevel level, Vec3 targetPos, float size) {
        BlockPos center = new BlockPos((int) targetPos.x, (int) targetPos.y, (int) targetPos.z);
        int radius = Math.max(2, (int) (size * 3));
        
        float explosionPower = size * 3;
        level.explode(
            null,
            targetPos.x, targetPos.y, targetPos.z,
            explosionPower,
            true,
            Level.ExplosionInteraction.BLOCK
        );
        
        damageEntitiesNearby(level, targetPos, explosionPower);
        
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -radius; dy <= 2; dy++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    double dist = Math.sqrt(dx * dx + dz * dz + dy * dy);
                    
                    if (dist <= radius + 0.5) {
                        if (dy <= 0) {
                            if (RANDOM.nextFloat() < 0.3) {
                                level.setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), 3);
                            } else if (RANDOM.nextFloat() < 0.5) {
                                level.setBlock(pos, Blocks.BASALT.defaultBlockState(), 3);
                            } else {
                                level.setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
                            }
                        } else if (dy == 1 && dist <= radius * 0.6) {
                            level.setBlock(pos, Blocks.COBBLESTONE.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
        
        for (int i = 0; i < 3; i++) {
            int px = center.getX() + RANDOM.nextInt(radius) - radius / 2;
            int py = center.getY() + 1;
            int pz = center.getZ() + RANDOM.nextInt(radius) - radius / 2;
            level.setBlock(new BlockPos(px, py, pz), Blocks.FIRE.defaultBlockState(), 3);
        }
        
        if (treasureChestPos == null && RANDOM.nextDouble() < 0.15) {
            placeTreasureChestInCrater(level, center);
        }
    }
    
    private static void damageEntitiesNearby(ServerLevel level, Vec3 pos, float explosionPower) {
        AABB aabb = new AABB(
            pos.x - explosionPower * 2, pos.y - explosionPower * 2, pos.z - explosionPower * 2,
            pos.x + explosionPower * 2, pos.y + explosionPower * 2, pos.z + explosionPower * 2
        );
        
        List<Entity> entities = level.getEntities(null, aabb);
        for (Entity entity : entities) {
            double distance = entity.distanceToSqr(pos.x, pos.y, pos.z);
            if (distance < explosionPower * explosionPower * 4) {
                double damage = (1 - Math.sqrt(distance) / (explosionPower * 2)) * 20;
                entity.hurt(level.damageSources().explosion(null), (float) damage);
                
                Vec3 push = new Vec3(
                    entity.getX() - pos.x,
                    entity.getY() - pos.y,
                    entity.getZ() - pos.z
                ).normalize().scale(explosionPower * 0.5);
                entity.push(push.x, push.y + 0.5, push.z);
            }
        }
    }
    
    private static void placeTreasureChestInCrater(ServerLevel level, BlockPos craterCenter) {
        BlockPos chestPos = craterCenter.below(3);
        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos clearPos = chestPos.offset(dx, dy, dz);
                    if (dy == 0 && dx == 0 && dz == 0) {
                        continue;
                    }
                    level.setBlock(clearPos, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
        
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = -2; dy <= 2; dy++) {
                    BlockPos pos = chestPos.offset(dx, dy, dz);
                    if (Math.abs(dx) == 2 || Math.abs(dz) == 2 || dy == -2) {
                        level.setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), 3);
                    } else if (dy == 2 && Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
                        level.setBlock(pos, Blocks.COBBLESTONE.defaultBlockState(), 3);
                    }
                }
            }
        }
        
        level.setBlock(chestPos.below(), Blocks.OBSIDIAN.defaultBlockState(), 3);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);
        
        BlockEntity blockEntity = level.getBlockEntity(chestPos);
        if (blockEntity instanceof ChestBlockEntity chest) {
            int slotIndex = 0;
            
            if (RANDOM.nextDouble() < 0.35) {
                chest.setItem(slotIndex++, new ItemStack(TalismanItems.CUBE_OF_TANG_SHAN.get()));
            }
            
            if (RANDOM.nextBoolean() && slotIndex < chest.getContainerSize()) {
                int diamondCount = 5 + RANDOM.nextInt(8);
                chest.setItem(slotIndex++, new ItemStack(net.minecraft.world.item.Items.DIAMOND, diamondCount));
            }
            
            if (RANDOM.nextBoolean() && slotIndex < chest.getContainerSize()) {
                int scrapCount = 3 + RANDOM.nextInt(5);
                chest.setItem(slotIndex++, new ItemStack(net.minecraft.world.item.Items.NETHERITE_SCRAP, scrapCount));
            }
            
            if (RANDOM.nextBoolean() && slotIndex < chest.getContainerSize()) {
                int goldBlockCount = 4 + RANDOM.nextInt(5);
                chest.setItem(slotIndex++, new ItemStack(net.minecraft.world.item.Items.GOLD_BLOCK, goldBlockCount));
            }
            
            if (RANDOM.nextBoolean() && slotIndex < chest.getContainerSize()) {
                int appleCount = 3 + RANDOM.nextInt(4);
                chest.setItem(slotIndex++, new ItemStack(net.minecraft.world.item.Items.GOLDEN_APPLE, appleCount));
            }
            
            if (RANDOM.nextBoolean() && slotIndex < chest.getContainerSize()) {
                chest.setItem(slotIndex++, new ItemStack(net.minecraft.world.item.Items.EMERALD, 8 + RANDOM.nextInt(10)));
            }
            
            treasureChestPos = chestPos;
            DragonCurseChronicles.LOGGER.info("流星宝箱生成于陨石坑内: " + chestPos);
        }
    }
    
    private static void placeTreasureChestAtCenter(ServerLevel level) {
        if (showerCenterPos == null || treasureChestPos != null) {
            return;
        }
        
        BlockPos centerGroundPos = findGroundPosition(level, showerCenterPos.getX(), showerCenterPos.getZ());
        BlockPos chestPos = new BlockPos(centerGroundPos.getX(), centerGroundPos.getY() + 1, centerGroundPos.getZ());
        
        while (level.getBlockState(chestPos).isSolid()) {
            chestPos = chestPos.above();
        }
        
        level.setBlock(chestPos.below(), Blocks.OBSIDIAN.defaultBlockState(), 3);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);
        
        BlockEntity blockEntity = level.getBlockEntity(chestPos);
        if (blockEntity instanceof ChestBlockEntity chest) {
            int slotIndex = 0;
            
            if (RANDOM.nextDouble() < 0.35) {
                chest.setItem(slotIndex++, new ItemStack(TalismanItems.CUBE_OF_TANG_SHAN.get()));
            }
            
            if (RANDOM.nextBoolean() && slotIndex < chest.getContainerSize()) {
                int diamondCount = 5 + RANDOM.nextInt(8);
                chest.setItem(slotIndex++, new ItemStack(net.minecraft.world.item.Items.DIAMOND, diamondCount));
            }
            
            if (RANDOM.nextBoolean() && slotIndex < chest.getContainerSize()) {
                int scrapCount = 3 + RANDOM.nextInt(5);
                chest.setItem(slotIndex++, new ItemStack(net.minecraft.world.item.Items.NETHERITE_SCRAP, scrapCount));
            }
            
            if (RANDOM.nextBoolean() && slotIndex < chest.getContainerSize()) {
                int goldBlockCount = 4 + RANDOM.nextInt(5);
                chest.setItem(slotIndex++, new ItemStack(net.minecraft.world.item.Items.GOLD_BLOCK, goldBlockCount));
            }
            
            if (RANDOM.nextBoolean() && slotIndex < chest.getContainerSize()) {
                int appleCount = 3 + RANDOM.nextInt(4);
                chest.setItem(slotIndex++, new ItemStack(net.minecraft.world.item.Items.GOLDEN_APPLE, appleCount));
            }
            
            if (RANDOM.nextBoolean() && slotIndex < chest.getContainerSize()) {
                chest.setItem(slotIndex++, new ItemStack(net.minecraft.world.item.Items.EMERALD, 8 + RANDOM.nextInt(10)));
            }
            
            treasureChestPos = chestPos;
            DragonCurseChronicles.LOGGER.info("流星宝箱生成于中心位置(备用): " + chestPos);
        }
    }
    
    private static void endMeteorShower(ServerLevel level) {
        isMeteorShowerActive = false;
        activeMeteors.clear();
        
        Component message;
        if (treasureChestPos != null) {
            message = Component.translatable(
                "dcc.message.meteor_shower.end_with_chest",
                treasureChestPos.getX(),
                treasureChestPos.getY(),
                treasureChestPos.getZ()
            );
        } else {
            message = Component.translatable("dcc.message.meteor_shower.end_no_chest");
        }
        
        sendGlobalMessage(level, message);
        
        DragonCurseChronicles.LOGGER.info("流星雨事件结束！");
    }
    
    private static void sendGlobalMessage(ServerLevel level, Component message) {
        for (ServerPlayer player : level.getPlayers(p -> true)) {
            player.sendSystemMessage(message);
        }
    }
    
    public static boolean canStartMeteorShower() {
        return !isMeteorShowerActive;
    }
    
    public static void forceStartMeteorShower(ServerLevel level) {
        if (!isMeteorShowerActive) {
            startMeteorShower(level);
        }
    }
    
    public static boolean isMeteorShowerActive() {
        return isMeteorShowerActive;
    }
}
