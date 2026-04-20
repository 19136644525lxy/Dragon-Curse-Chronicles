package com.qituo.dcc.items;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.qituo.dcc.DragonCurseChronicles;
import com.qituo.dcc.TalismanItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.fml.loading.FMLPaths;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class CubeOfTangShanItem extends Item {
    // 配置文件生物到符咒的映射
    private static final Map<ResourceLocation, ResourceLocation> CONFIG_BIOME_TO_TALISMAN = new HashMap<>();
    
    // 提取概率配置
    private static final Map<String, Double> TALISMAN_EXTRACT_PROBABILITY = new HashMap<>();
    
    // 提取次数需求（硬编码）
    private static final Map<String, Integer> TALISMAN_EXTRACT_COUNT = new HashMap<>();
    private static final int DEFAULT_EXTRACT_COUNT = 10;
    private static final double DEFAULT_EXTRACTION_PROBABILITY = 0.10; // 10%
    
    static {
        // 硬编码提取次数需求
        TALISMAN_EXTRACT_COUNT.put("sheep_talisman", 100); // 羊符咒需要100次
        
        // 硬编码提取概率（羊符咒为10%）
        TALISMAN_EXTRACT_PROBABILITY.put("sheep_talisman", 0.10);
        
        // 加载配置文件
        loadConfigMapping();
    }
    
    public CubeOfTangShanItem(Item.Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        // 检查游戏规则
        if (!level.getGameRules().getBoolean(DragonCurseChronicles.RULE_ALLOW_TALISMAN_EXTRACTION)) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.translatable("dcc.message.extraction_disabled"));
            }
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }
        
        // Shift+右键对着空气使用，显示冷却剩余时间
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                showCooldownTime(player);
            }
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }
        return super.use(level, player, hand);
    }
    
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        Level level = player.level();
        
        // Shift+右键查看提取进度
        if (player.isShiftKeyDown()) {
            return showExtractionProgress(player, target);
        }
        
        // 检查游戏规则
        if (!level.getGameRules().getBoolean(DragonCurseChronicles.RULE_ALLOW_TALISMAN_EXTRACTION)) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.translatable("dcc.message.cube_of_tang_shan.extraction_disabled"));
            }
            return InteractionResult.FAIL;
        }
        
        // 检查副手是否持有符咒基
        ItemStack offhandStack = player.getOffhandItem();
        if (!offhandStack.is(TalismanItems.TALISMAN_BASE.get())) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.translatable("dcc.message.cube_of_tang_shan.need_talisman_base"));
            }
            return InteractionResult.FAIL;
        }
        
        // 检查冷却时间
        if (hasCooldown(player)) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.translatable("dcc.message.cube_of_tang_shan.on_cooldown"));
            }
            return InteractionResult.FAIL;
        }
        
        // 获取生物类型
        ResourceLocation entityType = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
        
        // 查找对应的符咒（只使用配置文件的映射，且只处理羊符咒）
        ResourceLocation talismanRL = CONFIG_BIOME_TO_TALISMAN.get(entityType);
        
        if (talismanRL == null || !talismanRL.getPath().equals("sheep_talisman")) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.translatable("dcc.message.cube_of_tang_shan.no_mapping"));
            }
            return InteractionResult.FAIL;
        }
        
        String talismanId = talismanRL.getPath();
        
        // 获取提取次数需求
        int requiredCount = TALISMAN_EXTRACT_COUNT.getOrDefault(talismanId, DEFAULT_EXTRACT_COUNT);
        
        // 获取提取概率
        double probability = TALISMAN_EXTRACT_PROBABILITY.getOrDefault(talismanId, DEFAULT_EXTRACTION_PROBABILITY);
        
        // 更新提取次数
        int currentCount = incrementExtractionCount(player, talismanId);
        
        // 检查是否达到提取次数
        if (currentCount < requiredCount) {
            // 显示当前进度
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.translatable("dcc.message.cube_of_tang_shan.extraction_progress", currentCount, requiredCount));
            }
            return InteractionResult.SUCCESS;
        }
        
        // 达到提取次数，进行概率判定
        if (Math.random() > probability) {
            // 概率判定失败，重置提取次数
            resetExtractionCount(player, talismanId);
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.translatable("dcc.message.cube_of_tang_shan.extraction_failed", (int)((1 - probability) * 100)));
            }
            return InteractionResult.FAIL;
        }
        
        // 提取成功
        // 消耗符咒基
        offhandStack.shrink(1);
        
        // 生成符咒
        ItemStack talismanStack = new ItemStack(ForgeRegistries.ITEMS.getValue(talismanRL));
        if (!player.getInventory().add(talismanStack)) {
            player.drop(talismanStack, false);
        }
        
        // 播放音效
        level.playSound(null, player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
        
        // 重置提取次数
        resetExtractionCount(player, talismanId);
        
        // 设置冷却时间（现实时间1小时 = 3600000毫秒）
        setCooldown(player);
        
        if (!level.isClientSide()) {
            player.sendSystemMessage(Component.translatable("dcc.message.cube_of_tang_shan.extraction_success", Component.translatable("item.dcc." + talismanId).getString()));
        }
        return InteractionResult.SUCCESS;
    }
    
    /**
     * 显示冷却剩余时间
     */
    private void showCooldownTime(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        
        CompoundTag playerData = getOrCreatePlayerData(player);
        if (!playerData.contains("cube_of_tang_shan_cooldown_start") || !playerData.contains("cube_of_tang_shan_cooldown_duration")) {
            player.sendSystemMessage(Component.translatable("dcc.message.cube_of_tang_shan.no_cooldown"));
            return;
        }
        
        long cooldownStart = playerData.getLong("cube_of_tang_shan_cooldown_start");
        long cooldownDuration = playerData.getLong("cube_of_tang_shan_cooldown_duration");
        long elapsedTime = System.currentTimeMillis() - cooldownStart;
        
        if (elapsedTime >= cooldownDuration) {
            player.sendSystemMessage(Component.translatable("dcc.message.cube_of_tang_shan.no_cooldown"));
            return;
        }
        
        long remainingTime = cooldownDuration - elapsedTime;
        int minutes = (int) (remainingTime / 60000);
        int seconds = (int) ((remainingTime % 60000) / 1000);
        
        player.sendSystemMessage(Component.translatable("dcc.message.cube_of_tang_shan.cooldown_remaining", minutes, seconds));
    }
    
    /**
     * 显示提取进度
     */
    private InteractionResult showExtractionProgress(Player player, LivingEntity target) {
        Level level = player.level();
        
        // 检查游戏规则
        if (!level.getGameRules().getBoolean(DragonCurseChronicles.RULE_ALLOW_TALISMAN_EXTRACTION)) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.translatable("dcc.message.cube_of_tang_shan.extraction_disabled"));
            }
            return InteractionResult.FAIL;
        }
        
        // 获取生物类型
        ResourceLocation entityType = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
        
        // 查找对应的符咒（只使用配置文件的映射，且只处理羊符咒）
        ResourceLocation talismanRL = CONFIG_BIOME_TO_TALISMAN.get(entityType);
        
        if (talismanRL == null || !talismanRL.getPath().equals("sheep_talisman")) {
            if (!player.level().isClientSide()) {
                player.sendSystemMessage(Component.translatable("dcc.message.cube_of_tang_shan.no_mapping"));
            }
            return InteractionResult.FAIL;
        }
        
        String talismanId = talismanRL.getPath();
        
        // 获取当前提取次数
        int currentCount = getExtractionCount(player, talismanId);
        int requiredCount = TALISMAN_EXTRACT_COUNT.getOrDefault(talismanId, DEFAULT_EXTRACT_COUNT);
        double probability = TALISMAN_EXTRACT_PROBABILITY.getOrDefault(talismanId, DEFAULT_EXTRACTION_PROBABILITY);
        
        if (!player.level().isClientSide()) {
            player.sendSystemMessage(Component.translatable("dcc.message.cube_of_tang_shan.extraction_status",
                Component.translatable("item.dcc." + talismanId).getString(),
                currentCount,
                requiredCount,
                (int)(probability * 100)));
        }
        
        return InteractionResult.SUCCESS;
    }
    
    private int incrementExtractionCount(Player player, String talismanId) {
        CompoundTag playerData = getOrCreatePlayerData(player);
        String key = "cube_of_tang_shan_extraction_" + talismanId;
        int currentCount = playerData.getInt(key);
        currentCount++;
        playerData.putInt(key, currentCount);
        savePlayerData(player, playerData);
        return currentCount;
    }
    
    private int getExtractionCount(Player player, String talismanId) {
        CompoundTag playerData = getOrCreatePlayerData(player);
        String key = "cube_of_tang_shan_extraction_" + talismanId;
        return playerData.getInt(key);
    }
    
    private void resetExtractionCount(Player player, String talismanId) {
        CompoundTag playerData = getOrCreatePlayerData(player);
        String key = "cube_of_tang_shan_extraction_" + talismanId;
        playerData.remove(key);
        savePlayerData(player, playerData);
    }
    
    private boolean hasCooldown(Player player) {
        if (!(player instanceof ServerPlayer)) return false;

        CompoundTag playerData = getOrCreatePlayerData(player);
        if (!playerData.contains("cube_of_tang_shan_cooldown_start") || !playerData.contains("cube_of_tang_shan_cooldown_duration")) {
            return false;
        }

        long cooldownStart = playerData.getLong("cube_of_tang_shan_cooldown_start");
        long cooldownDuration = playerData.getLong("cube_of_tang_shan_cooldown_duration");
        return (System.currentTimeMillis() - cooldownStart) < cooldownDuration;
    }

    private void setCooldown(Player player) {
        CompoundTag playerData = getOrCreatePlayerData(player);
        long cooldownStart = System.currentTimeMillis();
        long cooldownDuration = 3600000; // 1小时
        playerData.putLong("cube_of_tang_shan_cooldown_start", cooldownStart);
        playerData.putLong("cube_of_tang_shan_cooldown_duration", cooldownDuration);
        savePlayerData(player, playerData);
    }
    
    private CompoundTag getOrCreatePlayerData(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return new CompoundTag();
        }
        
        Path dataPath = getPlayerDataPath(serverPlayer);
        
        if (dataPath.toFile().exists()) {
            try {
                try (FileInputStream fis = new FileInputStream(dataPath.toFile())) {
                    return NbtIo.readCompressed(fis);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        return new CompoundTag();
    }
    
    private void savePlayerData(Player player, CompoundTag data) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        
        Path dataPath = getPlayerDataPath(serverPlayer);
        
        // 确保目录存在
        dataPath.getParent().toFile().mkdirs();
        
        try {
            try (FileOutputStream fos = new FileOutputStream(dataPath.toFile())) {
                NbtIo.writeCompressed(data, fos);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private Path getPlayerDataPath(ServerPlayer player) {
        Path worldPath = player.server.getWorldPath(LevelResource.PLAYER_DATA_DIR);
        Path dccPath = worldPath.resolve("dcc");
        String fileName = "cube_of_tang_shan_cache_" + player.getUUID().toString() + ".dat";
        return dccPath.resolve(fileName);
    }
    
    private static void loadConfigMapping() {
        Path configPath = FMLPaths.CONFIGDIR.get().resolve("dcc").resolve("biological_mapping.json");
        
        // 如果配置文件不存在，创建默认配置
        if (!configPath.toFile().exists()) {
            try {
                configPath.getParent().toFile().mkdirs();
                copyDefaultConfig(configPath, "biological_mapping.json");
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        
        // 读取配置
        try (FileReader reader = new FileReader(configPath.toFile())) {
            JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);
            
            for (String talismanId : jsonObject.keySet()) {
                // 只处理羊符咒的映射
                if (!talismanId.equals("sheep_talisman")) {
                    continue;
                }
                
                JsonArray entityArray = jsonObject.getAsJsonArray(talismanId);
                for (int i = 0; i < entityArray.size(); i++) {
                    String entityId = entityArray.get(i).getAsString();
                    try {
                        ResourceLocation entityRL = ResourceLocation.parse(entityId);
                        ResourceLocation talismanRL = ResourceLocation.parse("dcc:" + talismanId);
                        CONFIG_BIOME_TO_TALISMAN.put(entityRL, talismanRL);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void copyDefaultConfig(Path configPath, String resourceName) throws IOException {
        try (InputStream inputStream = CubeOfTangShanItem.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream != null) {
                try (FileOutputStream fos = new FileOutputStream(configPath.toFile())) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                }
            }
        }
    }
    
    public static void reloadConfigs() {
        CONFIG_BIOME_TO_TALISMAN.clear();
        loadConfigMapping();
    }
}