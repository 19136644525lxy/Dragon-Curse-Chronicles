package com.qituo.dcc.events;

import com.qituo.dcc.DragonCurseChronicles;
import com.qituo.dcc.TalismanItems;
import com.qituo.dcc.config.MeteorShowerConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MeteorShowerEvent {

    private static final Random RANDOM = new Random();
    private static boolean isMeteorShowerActive = false;
    private static int meteorShowerTick = 0;
    private static final int METEOR_SHOWER_DURATION = 600;
    private static boolean hasCheckedForMeteorShower = false;
    private static BlockPos treasureChestPos = null;
    private static BlockPos showerCenterPos = null;
    private static final List<Meteor> activeMeteors = new ArrayList<>();

    // 待确认状态：概率触发成功后，需玩家点击确认才真正启动
    private static boolean pendingConfirmation = false;
    private static ServerPlayerEntity pendingPlayer = null;
    private static long pendingTimeoutTick = 0;

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
            checkPendingTimeout(world);
            updateMeteorShower(world);
        }
    }

    /**
     * 午夜0点检测触发概率
     * 成功 → 询问随机玩家确认
     * 失败 → 聊天栏通知
     */
    private static void checkTimeAndTriggerMeteorShower(ServerWorld world) {
        // 必须用 getTimeOfDay()（当天时间 0-23999），对应 Forge/NeoForge 的 level.getDayTime()。
        // 此前误用 world.getTime()：Yarn 文档明确 getTime() 是"每 tick 自增、不可修改"的
        // 调度计时（等价于 Forge 的 getGameTime()），只有 getTimeOfDay() 才是昼夜时间，
        // 也就是会被 /time set 与 doDaylightCycle 影响的那个值。
        // 后果：只要世界里执行过 /time set，两者就会脱节，(getTime() % 24000) 再也落不到
        // 18000-18099 的午夜窗口，于是自然触发与"询问玩家确认"整条链路永远不会发生。
        long timeOfDay = world.getTimeOfDay() % 24000;
        // 午夜窗口扩大到100 ticks（5秒），防止服务器卡顿跳过
        boolean isMidnight = timeOfDay >= 18000 && timeOfDay <= 18099;

        if (isMidnight) {
            if (!hasCheckedForMeteorShower) {
                hasCheckedForMeteorShower = true;
                DragonCurseChronicles.LOGGER.info(
                        "流星雨：进入午夜判定窗口（timeOfDay={}），事件启用={}",
                        timeOfDay, MeteorShowerConfig.isMeteorShowerEnabled());

                if (!MeteorShowerConfig.isMeteorShowerEnabled()) {
                    return;
                }
                if (isMeteorShowerActive || pendingConfirmation) {
                    return;
                }

                // 概率判定
                double chance = MeteorShowerConfig.getTriggerProbability();
                if (RANDOM.nextDouble() >= chance) {
                    // 触发失败通知
                    sendGlobalMessage(world, Text.translatable("dcc.message.meteor_shower.trigger_failed"));
                    DragonCurseChronicles.LOGGER.info("流星雨触发失败（概率{}）", chance);
                    return;
                }

                // 概率成功，检查所有在线玩家是否都在主世界
                if (!areAllPlayersInOverworld(world)) {
                    sendGlobalMessage(world, Text.translatable("dcc.message.meteor_shower.player_not_in_overworld"));
                    return;
                }

                // 询问随机玩家确认
                askPlayerForConfirmation(world);
            }
        } else {
            hasCheckedForMeteorShower = false;
        }
    }

    /**
     * 检查所有在线玩家是否都在主世界
     * 必须使用服务器的全局玩家列表，而非单个维度的玩家列表
     * 因为 ServerWorld.getPlayers 只包含当前在该维度的玩家，
     * 下界/末地的玩家对主世界 world.getPlayers() 不可见
     */
    private static boolean areAllPlayersInOverworld(ServerWorld world) {
        List<ServerPlayerEntity> allPlayers = world.getServer().getPlayerManager().getPlayerList();
        if (allPlayers.isEmpty()) {
            return false;
        }

        for (ServerPlayerEntity player : allPlayers) {
            if (player.getWorld().getRegistryKey() != World.OVERWORLD) {
                DragonCurseChronicles.LOGGER.debug("玩家 {} 处于非主世界维度 {}，流星雨事件不触发",
                        player.getName().getString(), player.getWorld().getRegistryKey().getValue());
                return false;
            }
        }
        return true;
    }

    /**
     * 随机询问一名主世界玩家是否启动流星雨
     * 发送带 ClickEvent 的交互式聊天消息
     */
    private static void askPlayerForConfirmation(ServerWorld world) {
        List<ServerPlayerEntity> players = world.getPlayers(p -> true);
        if (players.isEmpty()) {
            return;
        }

        ServerPlayerEntity target = players.get(RANDOM.nextInt(players.size()));
        pendingConfirmation = true;
        pendingPlayer = target;
        pendingTimeoutTick = world.getTime() + MeteorShowerConfig.getAskTimeoutSeconds() * 20L;

        // 构建确认按钮（绿色加粗，点击执行 /meteorshower confirm）
        MutableText confirmBtn = Text.literal("[确认启动流星雨]")
                .formatted(Formatting.GREEN, Formatting.BOLD)
                .styled(style -> style.withClickEvent(
                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/meteorshower confirm")));

        // 构建取消按钮（红色，点击执行 /meteorshower cancel）
        MutableText cancelBtn = Text.literal("  [取消]")
                .formatted(Formatting.RED)
                .styled(style -> style.withClickEvent(
                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/meteorshower cancel")));

        // 发送给被选中的玩家
        Text askMsg = Text.translatable("dcc.message.meteor_shower.ask_confirm")
                .append("\n").append(confirmBtn).append(cancelBtn);
        target.sendMessage(askMsg);

        // 通知其他玩家
        Text notifyMsg = Text.translatable(
                "dcc.message.meteor_shower.notify_others", target.getName());
        for (ServerPlayerEntity p : players) {
            if (p != target) {
                p.sendMessage(notifyMsg);
            }
        }

        DragonCurseChronicles.LOGGER.info("流星雨概率触发成功，已询问玩家 {}", target.getName().getString());
    }

    /**
     * 检查待确认状态是否超时或玩家已下线
     */
    private static void checkPendingTimeout(ServerWorld world) {
        if (!pendingConfirmation) {
            return;
        }

        // 玩家下线 → 自动取消
        if (pendingPlayer == null || !pendingPlayer.networkHandler.isConnectionOpen()) {
            cancelPending(world, true);
            return;
        }

        // 超时 → 自动取消
        if (world.getTime() >= pendingTimeoutTick) {
            cancelPending(world, false);
        }
    }

    /**
     * 取消待确认状态
     * @param playerOffline 玩家是否已下线
     */
    private static void cancelPending(ServerWorld world, boolean playerOffline) {
        String playerName = pendingPlayer != null ? pendingPlayer.getName().getString() : "Unknown";

        pendingConfirmation = false;
        pendingPlayer = null;
        pendingTimeoutTick = 0;

        Text msg = playerOffline
                ? Text.translatable("dcc.message.meteor_shower.cancel_offline", playerName)
                : Text.translatable("dcc.message.meteor_shower.cancel_timeout", playerName);
        sendGlobalMessage(world, msg);

        DragonCurseChronicles.LOGGER.info("流星雨待确认已取消（{}），玩家：{}",
                playerOffline ? "玩家下线" : "超时", playerName);
    }

    /**
     * 玩家确认启动流星雨（由指令调用）
     */
    public static boolean confirmMeteorShower(ServerPlayerEntity player) {
        if (!pendingConfirmation || pendingPlayer == null) {
            return false;
        }
        // 验证确认者是待确认玩家本人
        if (!player.getUuid().equals(pendingPlayer.getUuid())) {
            return false;
        }

        ServerWorld world = player.getServerWorld();
        if (world == null || world.getRegistryKey() != World.OVERWORLD) {
            return false;
        }

        pendingConfirmation = false;
        pendingPlayer = null;
        pendingTimeoutTick = 0;

        startMeteorShower(world);
        return true;
    }

    /**
     * 玩家取消启动流星雨（由指令调用）
     */
    public static boolean cancelMeteorShower(ServerPlayerEntity player) {
        if (!pendingConfirmation || pendingPlayer == null) {
            return false;
        }
        if (!player.getUuid().equals(pendingPlayer.getUuid())) {
            return false;
        }

        ServerWorld world = player.getServerWorld();
        pendingConfirmation = false;
        pendingPlayer = null;
        pendingTimeoutTick = 0;

        if (world != null) {
            sendGlobalMessage(world, Text.translatable(
                    "dcc.message.meteor_shower.cancel_by_player", player.getName()));
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

    /**
     * 创建陨石坑
     * 安全检测：如果爆炸点离已有宝箱太近（<15格），跳过方块破坏，
     * 只保留粒子效果和实体伤害，防止后续陨石摧毁已生成的宝箱
     */
    private static void createMeteorCrater(ServerWorld world, Vec3d targetPos, float size) {
        BlockPos center = new BlockPos((int) targetPos.x, (int) targetPos.y, (int) targetPos.z);
        int radius = Math.max(2, (int) (size * 3));
        float explosionPower = size * 3;

        // 宝箱安全区检测：离已有宝箱太近则跳过爆炸和方块破坏
        boolean safeFromChest = false;
        if (treasureChestPos != null) {
            int distToChest = Math.abs(center.getX() - treasureChestPos.getX())
                    + Math.abs(center.getY() - treasureChestPos.getY())
                    + Math.abs(center.getZ() - treasureChestPos.getZ());
            if (distToChest < 15) {
                safeFromChest = true;
                DragonCurseChronicles.LOGGER.debug("陨石爆炸点离宝箱太近({}格)，跳过方块破坏", distToChest);
            }
        }

        if (!safeFromChest) {
            world.createExplosion(
                null,
                targetPos.x, targetPos.y, targetPos.z,
                explosionPower,
                true,
                World.ExplosionSourceType.BLOCK
            );
        }

        damageEntitiesNearby(world, targetPos, explosionPower);

        // 方块替换也受安全区保护
        if (!safeFromChest) {
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
        }

        // 唐扇魔方在陨石坑宝箱中的刷新概率（从配置读取，默认10%）
        if (treasureChestPos == null && RANDOM.nextDouble() < MeteorShowerConfig.getCubeSpawnProbability()) {
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

    /**
     * 填充流星宝箱内容（提取公共逻辑，消除重复代码）
     * 唐扇魔方刷新概率从配置读取，默认10%
     */
    private static void fillTreasureChest(ChestBlockEntity chest, BlockPos chestPos) {
        int slotIndex = 0;

        // 唐扇魔方（概率从配置读取）
        if (RANDOM.nextDouble() < MeteorShowerConfig.getCubeSpawnProbability()) {
            chest.setStack(slotIndex++, new ItemStack(TalismanItems.CUBE_OF_TANG_SHAN));
        }

        if (RANDOM.nextBoolean() && slotIndex < chest.size()) {
            chest.setStack(slotIndex++, new ItemStack(net.minecraft.item.Items.DIAMOND, 5 + RANDOM.nextInt(8)));
        }
        if (RANDOM.nextBoolean() && slotIndex < chest.size()) {
            chest.setStack(slotIndex++, new ItemStack(net.minecraft.item.Items.NETHERITE_SCRAP, 3 + RANDOM.nextInt(5)));
        }
        if (RANDOM.nextBoolean() && slotIndex < chest.size()) {
            chest.setStack(slotIndex++, new ItemStack(net.minecraft.item.Items.GOLD_BLOCK, 4 + RANDOM.nextInt(5)));
        }
        if (RANDOM.nextBoolean() && slotIndex < chest.size()) {
            chest.setStack(slotIndex++, new ItemStack(net.minecraft.item.Items.GOLDEN_APPLE, 3 + RANDOM.nextInt(4)));
        }
        if (RANDOM.nextBoolean() && slotIndex < chest.size()) {
            chest.setStack(slotIndex++, new ItemStack(net.minecraft.item.Items.EMERALD, 8 + RANDOM.nextInt(10)));
        }

        treasureChestPos = chestPos;
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
            fillTreasureChest(chest, chestPos);
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
            fillTreasureChest(chest, chestPos);
            DragonCurseChronicles.LOGGER.info("流星宝箱生成于中心位置(备用): " + chestPos);
        }
    }

    private static void endMeteorShower(ServerWorld world) {
        isMeteorShowerActive = false;
        activeMeteors.clear();

        Text message;
        if (treasureChestPos != null) {
            // 坐标部分做成可点击：点击执行 /dcc tp 传送过去（权限不足时由指令给出提示）
            message = Text.translatable(
                    "dcc.message.meteor_shower.end_with_chest",
                    com.qituo.dcc.commands.DccCommand.buildClickableCoords(treasureChestPos)
            );
        } else {
            message = Text.translatable("dcc.message.meteor_shower.end_no_chest");
        }

        sendGlobalMessage(world, message);
        DragonCurseChronicles.LOGGER.info("流星雨事件结束！");
    }

    /** 获取最近一次流星雨宝箱坐标；没有则为 null */
    public static BlockPos getTreasureChestPos() {
        return treasureChestPos;
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

    public static boolean isPendingConfirmation() {
        return pendingConfirmation;
    }

    public static ServerPlayerEntity getPendingPlayer() {
        return pendingPlayer;
    }
}
