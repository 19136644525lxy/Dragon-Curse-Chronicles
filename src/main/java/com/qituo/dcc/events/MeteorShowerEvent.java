package com.qituo.dcc.events;

import com.qituo.dcc.DragonCurseChronicles;
import com.qituo.dcc.TalismanItems;
import com.qituo.dcc.config.MeteorShowerConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
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
    private static boolean hasCheckedForMeteorShower = false;
    private static BlockPos treasureChestPos = null;
    private static BlockPos showerCenterPos = null;
    private static final List<Meteor> activeMeteors = new ArrayList<>();

    // 待确认状态：概率触发成功后，需玩家点击确认才真正启动
    private static boolean pendingConfirmation = false;
    private static ServerPlayer pendingPlayer = null;
    private static long pendingTimeoutTick = 0;

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
                checkPendingTimeout(level);
                updateMeteorShower(level);
            }
        });
    }

    /**
     * 午夜0点检测触发概率
     * 成功 → 询问随机玩家确认
     * 失败 → 聊天栏通知
     */
    private static void checkTimeAndTriggerMeteorShower(ServerLevel level) {
        long timeOfDay = level.getDayTime() % 24000;
        // 午夜窗口扩大到100 ticks（5秒），防止服务器卡顿跳过
        boolean isMidnight = timeOfDay >= 18000 && timeOfDay <= 18099;

        if (isMidnight) {
            if (!hasCheckedForMeteorShower) {
                hasCheckedForMeteorShower = true;

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
                    sendGlobalMessage(level, Component.translatable("dcc.message.meteor_shower.trigger_failed"));
                    DragonCurseChronicles.LOGGER.info("流星雨触发失败（概率{}）", chance);
                    return;
                }

                // 概率成功，检查所有在线玩家是否都在主世界
                if (!areAllPlayersInOverworld(level)) {
                    sendGlobalMessage(level, Component.translatable("dcc.message.meteor_shower.player_not_in_overworld"));
                    return;
                }

                // 询问随机玩家确认
                askPlayerForConfirmation(level);
            }
        } else {
            hasCheckedForMeteorShower = false;
        }
    }

    /**
     * 检查所有在线玩家是否都在主世界
     * 必须使用服务器的全局玩家列表，而非单个维度的玩家列表
     * 因为 ServerLevel.players 只包含当前在该维度的玩家，
     * 下界/末地的玩家对主世界 level.getPlayers() 不可见
     */
    private static boolean areAllPlayersInOverworld(ServerLevel level) {
        List<ServerPlayer> allPlayers = level.getServer().getPlayerList().getPlayers();
        if (allPlayers.isEmpty()) {
            return false;
        }

        for (ServerPlayer player : allPlayers) {
            ResourceKey<Level> playerDim = player.level().dimension();
            if (!playerDim.equals(Level.OVERWORLD)) {
                DragonCurseChronicles.LOGGER.debug("玩家 {} 处于非主世界维度 {}，流星雨事件不触发",
                        player.getName().getString(), playerDim.location());
                return false;
            }
        }
        return true;
    }

    /**
     * 随机询问一名主世界玩家是否启动流星雨
     * 发送带 ClickEvent 的交互式聊天消息
     */
    private static void askPlayerForConfirmation(ServerLevel level) {
        List<ServerPlayer> players = level.getPlayers(p -> true);
        if (players.isEmpty()) {
            return;
        }

        ServerPlayer target = players.get(RANDOM.nextInt(players.size()));
        pendingConfirmation = true;
        pendingPlayer = target;
        pendingTimeoutTick = level.getGameTime() + MeteorShowerConfig.getAskTimeoutSeconds() * 20L;

        // 构建确认按钮（绿色加粗，点击执行 /meteorshower confirm）
        MutableComponent confirmBtn = Component.literal("[确认启动流星雨]")
                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)
                .withStyle(style -> style.withClickEvent(
                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/meteorshower confirm")));

        // 构建取消按钮（红色，点击执行 /meteorshower cancel）
        MutableComponent cancelBtn = Component.literal("  [取消]")
                .withStyle(ChatFormatting.RED)
                .withStyle(style -> style.withClickEvent(
                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/meteorshower cancel")));

        // 发送给被选中的玩家
        Component askMsg = Component.translatable("dcc.message.meteor_shower.ask_confirm")
                .append("\n").append(confirmBtn).append(cancelBtn);
        target.sendSystemMessage(askMsg);

        // 通知其他玩家
        Component notifyMsg = Component.translatable(
                "dcc.message.meteor_shower.notify_others", target.getName());
        for (ServerPlayer p : players) {
            if (p != target) {
                p.sendSystemMessage(notifyMsg);
            }
        }

        DragonCurseChronicles.LOGGER.info("流星雨概率触发成功，已询问玩家 {}", target.getName().getString());
    }

    /**
     * 检查待确认状态是否超时或玩家已下线
     */
    private static void checkPendingTimeout(ServerLevel level) {
        if (!pendingConfirmation) {
            return;
        }

        // 玩家下线 → 自动取消
        if (pendingPlayer == null || !pendingPlayer.connection.isAcceptingMessages()) {
            cancelPending(level, true);
            return;
        }

        // 超时 → 自动取消
        if (level.getGameTime() >= pendingTimeoutTick) {
            cancelPending(level, false);
        }
    }

    /**
     * 取消待确认状态
     * @param playerOffline 玩家是否已下线
     */
    private static void cancelPending(ServerLevel level, boolean playerOffline) {
        String playerName = pendingPlayer != null ? pendingPlayer.getName().getString() : "Unknown";

        pendingConfirmation = false;
        pendingPlayer = null;
        pendingTimeoutTick = 0;

        Component msg = playerOffline
                ? Component.translatable("dcc.message.meteor_shower.cancel_offline", playerName)
                : Component.translatable("dcc.message.meteor_shower.cancel_timeout", playerName);
        sendGlobalMessage(level, msg);

        DragonCurseChronicles.LOGGER.info("流星雨待确认已取消（{}），玩家：{}",
                playerOffline ? "玩家下线" : "超时", playerName);
    }

    /**
     * 玩家确认启动流星雨（由指令调用）
     */
    public static boolean confirmMeteorShower(ServerPlayer player) {
        if (!pendingConfirmation || pendingPlayer == null) {
            return false;
        }
        // 验证确认者是待确认玩家本人
        if (!player.getUUID().equals(pendingPlayer.getUUID())) {
            return false;
        }

        ServerLevel level = player.serverLevel();
        if (level == null || level.dimension() != Level.OVERWORLD) {
            return false;
        }

        pendingConfirmation = false;
        pendingPlayer = null;
        pendingTimeoutTick = 0;

        startMeteorShower(level);
        return true;
    }

    /**
     * 玩家取消启动流星雨（由指令调用）
     */
    public static boolean cancelMeteorShower(ServerPlayer player) {
        if (!pendingConfirmation || pendingPlayer == null) {
            return false;
        }
        if (!player.getUUID().equals(pendingPlayer.getUUID())) {
            return false;
        }

        ServerLevel level = player.serverLevel();
        pendingConfirmation = false;
        pendingPlayer = null;
        pendingTimeoutTick = 0;

        if (level != null) {
            sendGlobalMessage(level, Component.translatable(
                    "dcc.message.meteor_shower.cancel_by_player", player.getName()));
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

    /**
     * 创建陨石坑
     * 安全检测：如果爆炸点离已有宝箱太近（<15格），跳过方块破坏，
     * 只保留粒子效果和实体伤害，防止后续陨石摧毁已生成的宝箱
     */
    private static void createMeteorCrater(ServerLevel level, Vec3 targetPos, float size) {
        BlockPos center = new BlockPos((int) targetPos.x, (int) targetPos.y, (int) targetPos.z);
        int radius = Math.max(2, (int) (size * 3));
        float explosionPower = size * 3;

        // 宝箱安全区检测：离已有宝箱太近则跳过爆炸和方块破坏
        boolean safeFromChest = false;
        if (treasureChestPos != null) {
            int distToChest = center.distManhattan(treasureChestPos);
            if (distToChest < 15) {
                safeFromChest = true;
                DragonCurseChronicles.LOGGER.debug("陨石爆炸点离宝箱太近({}格)，跳过方块破坏", distToChest);
            }
        }

        if (!safeFromChest) {
            level.explode(
                null,
                targetPos.x, targetPos.y, targetPos.z,
                explosionPower,
                true,
                Level.ExplosionInteraction.BLOCK
            );
        }

        damageEntitiesNearby(level, targetPos, explosionPower);

        // 方块替换也受安全区保护
        if (!safeFromChest) {
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
        }

        // 唐扇魔方在陨石坑宝箱中的刷新概率（从配置读取，默认10%）
        if (treasureChestPos == null && RANDOM.nextDouble() < MeteorShowerConfig.getCubeSpawnProbability()) {
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

    /**
     * 填充流星宝箱内容（提取公共逻辑，消除重复代码）
     * 唐扇魔方刷新概率从配置读取，默认10%
     */
    private static void fillTreasureChest(ChestBlockEntity chest, BlockPos chestPos) {
        int slotIndex = 0;

        // 唐扇魔方（概率从配置读取）
        if (RANDOM.nextDouble() < MeteorShowerConfig.getCubeSpawnProbability()) {
            chest.setItem(slotIndex++, new ItemStack(TalismanItems.CUBE_OF_TANG_SHAN.get()));
        }

        if (RANDOM.nextBoolean() && slotIndex < chest.getContainerSize()) {
            chest.setItem(slotIndex++, new ItemStack(net.minecraft.world.item.Items.DIAMOND, 5 + RANDOM.nextInt(8)));
        }
        if (RANDOM.nextBoolean() && slotIndex < chest.getContainerSize()) {
            chest.setItem(slotIndex++, new ItemStack(net.minecraft.world.item.Items.NETHERITE_SCRAP, 3 + RANDOM.nextInt(5)));
        }
        if (RANDOM.nextBoolean() && slotIndex < chest.getContainerSize()) {
            chest.setItem(slotIndex++, new ItemStack(net.minecraft.world.item.Items.GOLD_BLOCK, 4 + RANDOM.nextInt(5)));
        }
        if (RANDOM.nextBoolean() && slotIndex < chest.getContainerSize()) {
            chest.setItem(slotIndex++, new ItemStack(net.minecraft.world.item.Items.GOLDEN_APPLE, 3 + RANDOM.nextInt(4)));
        }
        if (RANDOM.nextBoolean() && slotIndex < chest.getContainerSize()) {
            chest.setItem(slotIndex++, new ItemStack(net.minecraft.world.item.Items.EMERALD, 8 + RANDOM.nextInt(10)));
        }

        treasureChestPos = chestPos;
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
            fillTreasureChest(chest, chestPos);
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
            fillTreasureChest(chest, chestPos);
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

    public static boolean isPendingConfirmation() {
        return pendingConfirmation;
    }

    public static ServerPlayer getPendingPlayer() {
        return pendingPlayer;
    }
}
