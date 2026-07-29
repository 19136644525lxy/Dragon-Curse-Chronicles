package com.qituo.dcc.events;

import com.qituo.dcc.DragonCurseChronicles;
import com.qituo.dcc.TalismanItems;
import com.qituo.dcc.config.MeteorShowerConfig;
import net.minecraft.util.math.BlockPos;
import net.minecraft.text.Text;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        final Vec3d startPos;
        final Vec3d targetPos;
        final int lifespan;
        int ticksLived = 0;
        final float size;
        final double speed;
        
        Meteor(Vec3d start, Vec3d target, int lifespan, float size, double speed) {
            this.startPos = start;
            this.targetPos = target;
            this.lifespan = lifespan;
            this.size = size;
            this.speed = speed;
        }
    }
    
    /**
     * 服务器 tick 回调（由 DragonCurseChronicles 通过 ServerTickEvents.END_SERVER_TICK 注册）
     */
    public static void onServerTick(ServerWorld world) {
        if (world.getRegistryKey() == World.OVERWORLD) {
            checkTimeAndTriggerMeteorShower(world);
            updateMeteorShower(world);
        }
    }
    
    private static void checkTimeAndTriggerMeteorShower(ServerWorld world) {
        long timeOfDay = world.getTime() % 24000;
        boolean isMidnight = timeOfDay >= 18000 && timeOfDay <= 18010;
        
        if (isMidnight) {
            if (!hasCheckedForMeteorShower) {
                hasCheckedForMeteorShower = true;
                
                if (MeteorShowerConfig.isMeteorShowerEnabled() && !isMeteorShowerActive && RANDOM.nextDouble() < EVENT_TRIGGER_CHANCE) {
                    if (areAllPlayersInOverworld(world)) {
                        startMeteorShower(world);
                    }
                }
            }
        } else {
            hasCheckedForMeteorShower = false;
        }
    }
    
    private static boolean areAllPlayersInOverworld(ServerWorld world) {
        List<ServerPlayerEntity> players = world.getPlayers(p -> true);
        if (players.isEmpty()) {
            return false;
        }
        
        for (ServerPlayerEntity player : players) {
            if (player.getWorld().getRegistryKey() != World.OVERWORLD) {
                return false;
            }
        }
        return true;
    }
    
    private static void startMeteorShower(ServerWorld world) {
        isMeteorShowerActive = true;
        meteorShowerTick = 0;
        activeMeteors.clear();
        treasureChestPos = null;
        
        List<ServerPlayerEntity> players = world.getPlayers(p -> true);
        if (!players.isEmpty()) {
            ServerPlayerEntity randomPlayer = players.get(RANDOM.nextInt(players.size()));
            Vec3d pos = randomPlayer.getPos();
            showerCenterPos = new BlockPos((int) pos.x, (int) pos.y, (int) pos.z);
        } else {
            showerCenterPos = new BlockPos(0, 64, 0);
        }
        
        sendGlobalMessage(world, Text.translatable("dcc.message.meteor_shower.start"));
        
        DragonCurseChronicles.LOGGER.info("流星雨事件开始！中心位置: " + showerCenterPos);
    }
    
    private static void updateMeteorShower(ServerWorld world) {
        if (!isMeteorShowerActive) {
            return;
        }
        
        meteorShowerTick++;
        
        if (meteorShowerTick % 2 == 0 && meteorShowerTick < METEOR_SHOWER_DURATION - 100) {
            spawnRandomMeteor(world);
            if (RANDOM.nextBoolean()) {
                spawnRandomMeteor(world);
            }
        }
        
        List<Meteor> meteorsToRemove = new ArrayList<>();
        for (Meteor meteor : activeMeteors) {
            meteor.ticksLived++;
            
            sendMeteorParticles(world, meteor);
            
            if (meteor.ticksLived >= meteor.lifespan) {
                meteorsToRemove.add(meteor);
                createMeteorCrater(world, meteor.targetPos, meteor.size);
            }
        }
        
        activeMeteors.removeAll(meteorsToRemove);
        
        if (meteorShowerTick >= METEOR_SHOWER_DURATION - 50 && treasureChestPos == null) {
            placeTreasureChestAtCenter(world);
        }
        
        if (meteorShowerTick >= METEOR_SHOWER_DURATION) {
            endMeteorShower(world);
        }
    }
    
    private static void spawnRandomMeteor(ServerWorld world) {
        List<ServerPlayerEntity> players = world.getPlayers(p -> true);
        if (players.isEmpty()) {
            return;
        }
        
        ServerPlayerEntity targetPlayer = players.get(RANDOM.nextInt(players.size()));
        BlockPos playerPos = targetPlayer.getBlockPos();
        
        int centerX = playerPos.getX();
        int centerZ = playerPos.getZ();
        int radius = 250;
        
        // 在玩家周围250格范围内随机生成
        double offsetX = (RANDOM.nextDouble() - 0.5) * radius * 2;
        double offsetZ = (RANDOM.nextDouble() - 0.5) * radius * 2;
        
        BlockPos groundPos = findGroundPosition(world, centerX + offsetX, centerZ + offsetZ);
        
        Vec3d targetPos = new Vec3d(groundPos.getX() + 0.5, groundPos.getY() + 1, groundPos.getZ() + 0.5);
        
        Vec3d startPos = new Vec3d(
            targetPos.x + (RANDOM.nextDouble() - 0.5) * 150,
            280 + RANDOM.nextInt(80),
            targetPos.z + (RANDOM.nextDouble() - 0.5) * 150
        );
        
        int lifespan = 40 + RANDOM.nextInt(30);
        float size = 1.0f + RANDOM.nextFloat() * 2.5f;
        double speed = 0.5 + RANDOM.nextDouble() * 0.5;
        
        activeMeteors.add(new Meteor(startPos, targetPos, lifespan, size, speed));
    }
    
    private static BlockPos findGroundPosition(ServerWorld world, double x, double z) {
        BlockPos.Mutable mutablePos = new BlockPos.Mutable((int) x, 300, (int) z);
        
        while (mutablePos.getY() > 0) {
            BlockState state = world.getBlockState(mutablePos);
            if (!state.isAir() && !state.isOf(Blocks.WATER)) {
                return mutablePos.toImmutable();
            }
            mutablePos.move(0, -1, 0);
        }
        
        return new BlockPos((int) x, 64, (int) z);
    }
    
    private static void sendMeteorParticles(ServerWorld world, Meteor meteor) {
        double progress = (double) meteor.ticksLived / meteor.lifespan;
        Vec3d currentPos = new Vec3d(
            meteor.startPos.x + (meteor.targetPos.x - meteor.startPos.x) * progress,
            meteor.startPos.y + (meteor.targetPos.y - meteor.startPos.y) * progress,
            meteor.startPos.z + (meteor.targetPos.z - meteor.startPos.z) * progress
        );
        
        for (ServerPlayerEntity player : world.getPlayers(p -> true)) {
            player.networkHandler.sendPacket(new ExplosionS2CPacket(
                currentPos.x, currentPos.y, currentPos.z, 0.8f, new ArrayList<>(), new Vec3d(0, 0, 0)
            ));
            
            player.networkHandler.sendPacket(new ParticleS2CPacket(
                ParticleTypes.FLAME,
                true,
                currentPos.x, currentPos.y, currentPos.z,
                meteor.size * 1.5f, meteor.size * 1.5f, meteor.size * 1.5f,
                0.15f,
                15
            ));
            
            player.networkHandler.sendPacket(new ParticleS2CPacket(
                ParticleTypes.LARGE_SMOKE,
                true,
                currentPos.x, currentPos.y, currentPos.z,
                meteor.size, meteor.size, meteor.size,
                0.08f,
                10
            ));
            
            player.networkHandler.sendPacket(new ParticleS2CPacket(
                ParticleTypes.END_ROD,
                true,
                currentPos.x, currentPos.y, currentPos.z,
                meteor.size * 2, meteor.size * 2, meteor.size * 2,
                0.2f,
                8
            ));
        }
    }
    
    private static void createMeteorCrater(ServerWorld world, Vec3d targetPos, float size) {
        BlockPos center = new BlockPos((int) targetPos.x, (int) targetPos.y, (int) targetPos.z);
        int radius = Math.max(2, (int) (size * 3));
        
        float explosionPower = size * 3;
        world.createExplosion(
            null,
            targetPos.x, targetPos.y, targetPos.z,
            explosionPower,
            true,
            World.ExplosionSourceType.BLOCK
        );
        
        damageEntitiesNearby(world, targetPos, explosionPower);
        
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -radius; dy <= 2; dy++) {
                    BlockPos pos = center.add(dx, dy, dz);
                    double dist = Math.sqrt(dx * dx + dz * dz + dy * dy);
                    
                    if (dist <= radius + 0.5) {
                        if (dy <= 0) {
                            if (RANDOM.nextFloat() < 0.3) {
                                world.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState(), 3);
                            } else if (RANDOM.nextFloat() < 0.5) {
                                world.setBlockState(pos, Blocks.BASALT.getDefaultState(), 3);
                            } else {
                                world.setBlockState(pos, Blocks.STONE.getDefaultState(), 3);
                            }
                        } else if (dy == 1 && dist <= radius * 0.6) {
                            world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState(), 3);
                        }
                    }
                }
            }
        }
        
        for (int i = 0; i < 3; i++) {
            int px = center.getX() + RANDOM.nextInt(radius) - radius / 2;
            int py = center.getY() + 1;
            int pz = center.getZ() + RANDOM.nextInt(radius) - radius / 2;
            world.setBlockState(new BlockPos(px, py, pz), Blocks.FIRE.getDefaultState(), 3);
        }
        
        if (treasureChestPos == null && RANDOM.nextDouble() < 0.15) {
            placeTreasureChestInCrater(world, center);
        }
    }
    
    private static void damageEntitiesNearby(ServerWorld world, Vec3d pos, float explosionPower) {
        Box aabb = new Box(
            pos.x - explosionPower * 2, pos.y - explosionPower * 2, pos.z - explosionPower * 2,
            pos.x + explosionPower * 2, pos.y + explosionPower * 2, pos.z + explosionPower * 2
        );
        
        List<Entity> entities = world.getOtherEntities(null, aabb);
        for (Entity entity : entities) {
            double distance = entity.squaredDistanceTo(pos.x, pos.y, pos.z);
            if (distance < explosionPower * explosionPower * 4) {
                double damage = (1 - Math.sqrt(distance) / (explosionPower * 2)) * 20;
                entity.damage(world.getDamageSources().explosion(null), (float) damage);
                
                Vec3d push = new Vec3d(
                    entity.getX() - pos.x,
                    entity.getY() - pos.y,
                    entity.getZ() - pos.z
                ).normalize().multiply(explosionPower * 0.5);
                entity.addVelocity(push.x, push.y + 0.5, push.z);
            }
        }
    }
    
    private static void placeTreasureChestInCrater(ServerWorld world, BlockPos craterCenter) {
        BlockPos chestPos = craterCenter.down(3);
        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos clearPos = chestPos.add(dx, dy, dz);
                    if (dy == 0 && dx == 0 && dz == 0) {
                        continue;
                    }
                    world.setBlockState(clearPos, Blocks.AIR.getDefaultState(), 3);
                }
            }
        }
        
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = -2; dy <= 2; dy++) {
                    BlockPos pos = chestPos.add(dx, dy, dz);
                    if (Math.abs(dx) == 2 || Math.abs(dz) == 2 || dy == -2) {
                        world.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState(), 3);
                    } else if (dy == 2 && Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
                        world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState(), 3);
                    }
                }
            }
        }
        
        world.setBlockState(chestPos.down(), Blocks.OBSIDIAN.getDefaultState(), 3);
        world.setBlockState(chestPos, Blocks.CHEST.getDefaultState(), 3);

        BlockEntity blockEntity = world.getBlockEntity(chestPos);
        if (blockEntity instanceof ChestBlockEntity chest) {
            int slotIndex = 0;

            if (RANDOM.nextDouble() < 0.35) {
                chest.setStack(slotIndex++, new ItemStack(TalismanItems.CUBE_OF_TANG_SHAN));
            }

            if (RANDOM.nextBoolean() && slotIndex < chest.size()) {
                int diamondCount = 5 + RANDOM.nextInt(8);
                chest.setStack(slotIndex++, new ItemStack(net.minecraft.item.Items.DIAMOND, diamondCount));
            }

            if (RANDOM.nextBoolean() && slotIndex < chest.size()) {
                int scrapCount = 3 + RANDOM.nextInt(5);
                chest.setStack(slotIndex++, new ItemStack(net.minecraft.item.Items.NETHERITE_SCRAP, scrapCount));
            }

            if (RANDOM.nextBoolean() && slotIndex < chest.size()) {
                int goldBlockCount = 4 + RANDOM.nextInt(5);
                chest.setStack(slotIndex++, new ItemStack(net.minecraft.item.Items.GOLD_BLOCK, goldBlockCount));
            }

            if (RANDOM.nextBoolean() && slotIndex < chest.size()) {
                int appleCount = 3 + RANDOM.nextInt(4);
                chest.setStack(slotIndex++, new ItemStack(net.minecraft.item.Items.GOLDEN_APPLE, appleCount));
            }

            if (RANDOM.nextBoolean() && slotIndex < chest.size()) {
                chest.setStack(slotIndex++, new ItemStack(net.minecraft.item.Items.EMERALD, 8 + RANDOM.nextInt(10)));
            }

            treasureChestPos = chestPos;
            DragonCurseChronicles.LOGGER.info("流星宝箱生成于陨石坑内: " + chestPos);
        }
    }

    private static void placeTreasureChestAtCenter(ServerWorld world) {
        if (showerCenterPos == null || treasureChestPos != null) {
            return;
        }

        BlockPos centerGroundPos = findGroundPosition(world, showerCenterPos.getX(), showerCenterPos.getZ());
        BlockPos chestPos = new BlockPos(centerGroundPos.getX(), centerGroundPos.getY() + 1, centerGroundPos.getZ());

        while (world.getBlockState(chestPos).isSolid()) {
            chestPos = chestPos.up();
        }

        world.setBlockState(chestPos.down(), Blocks.OBSIDIAN.getDefaultState(), 3);
        world.setBlockState(chestPos, Blocks.CHEST.getDefaultState(), 3);
        
        BlockEntity blockEntity = world.getBlockEntity(chestPos);
        if (blockEntity instanceof ChestBlockEntity chest) {
            int slotIndex = 0;
            
            if (RANDOM.nextDouble() < 0.35) {
                chest.setStack(slotIndex++, new ItemStack(TalismanItems.CUBE_OF_TANG_SHAN));
            }
            
            if (RANDOM.nextBoolean() && slotIndex < chest.size()) {
                int diamondCount = 5 + RANDOM.nextInt(8);
                chest.setStack(slotIndex++, new ItemStack(net.minecraft.item.Items.DIAMOND, diamondCount));
            }
            
            if (RANDOM.nextBoolean() && slotIndex < chest.size()) {
                int scrapCount = 3 + RANDOM.nextInt(5);
                chest.setStack(slotIndex++, new ItemStack(net.minecraft.item.Items.NETHERITE_SCRAP, scrapCount));
            }
            
            if (RANDOM.nextBoolean() && slotIndex < chest.size()) {
                int goldBlockCount = 4 + RANDOM.nextInt(5);
                chest.setStack(slotIndex++, new ItemStack(net.minecraft.item.Items.GOLD_BLOCK, goldBlockCount));
            }
            
            if (RANDOM.nextBoolean() && slotIndex < chest.size()) {
                int appleCount = 3 + RANDOM.nextInt(4);
                chest.setStack(slotIndex++, new ItemStack(net.minecraft.item.Items.GOLDEN_APPLE, appleCount));
            }
            
            if (RANDOM.nextBoolean() && slotIndex < chest.size()) {
                chest.setStack(slotIndex++, new ItemStack(net.minecraft.item.Items.EMERALD, 8 + RANDOM.nextInt(10)));
            }
            
            treasureChestPos = chestPos;
            DragonCurseChronicles.LOGGER.info("流星宝箱生成于中心位置(备用): " + chestPos);
        }
    }
    
    private static void endMeteorShower(ServerWorld world) {
        isMeteorShowerActive = false;
        activeMeteors.clear();
        
        Text message;
        if (treasureChestPos != null) {
            message = Text.translatable(
                "dcc.message.meteor_shower.end_with_chest",
                treasureChestPos.getX(),
                treasureChestPos.getY(),
                treasureChestPos.getZ()
            );
        } else {
            message = Text.translatable("dcc.message.meteor_shower.end_no_chest");
        }
        
        sendGlobalMessage(world, message);
        
        DragonCurseChronicles.LOGGER.info("流星雨事件结束！");
    }
    
    private static void sendGlobalMessage(ServerWorld world, Text message) {
        for (ServerPlayerEntity player : world.getPlayers(p -> true)) {
            player.sendMessage(message);
        }
    }
    
    public static boolean canStartMeteorShower() {
        return !isMeteorShowerActive;
    }
    
    public static void forceStartMeteorShower(ServerWorld world) {
        if (!isMeteorShowerActive) {
            startMeteorShower(world);
        }
    }
    
    public static boolean isMeteorShowerActive() {
        return isMeteorShowerActive;
    }
}