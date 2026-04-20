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

public class TalismanExtractorItem extends Item {
    // 配置文件生物到符咒的映射
    private static final Map<ResourceLocation, ResourceLocation> CONFIG_BIOME_TO_TALISMAN = new HashMap<>();
    // 符咒提取次数需求映射（硬编码）
    private static final Map<String, Integer> TALISMAN_EXTRACT_COUNT = new HashMap<>();
    // 符咒提取概率映射（可配置）
    private static final Map<String, Double> TALISMAN_EXTRACT_PROBABILITY = new HashMap<>();
    
    // 默认提取概率
    private static final double DEFAULT_EXTRACTION_PROBABILITY = 0.10;
    // 默认提取次数需求
    private static final int DEFAULT_EXTRACT_COUNT = 10;
    
    static {
        // 初始化符咒提取次数需求（硬编码）
        TALISMAN_EXTRACT_COUNT.put("chicken_talisman", 100);
        TALISMAN_EXTRACT_COUNT.put("dragon_talisman", 100);
        TALISMAN_EXTRACT_COUNT.put("pig_talisman", 100);
        TALISMAN_EXTRACT_COUNT.put("cow_talisman", 10);
        TALISMAN_EXTRACT_COUNT.put("rabbit_talisman", 10);
        TALISMAN_EXTRACT_COUNT.put("horse_talisman", 10);
        TALISMAN_EXTRACT_COUNT.put("dog_talisman", 10);
        TALISMAN_EXTRACT_COUNT.put("mouse_talisman", 10);
        TALISMAN_EXTRACT_COUNT.put("tiger_talisman", 10);
        TALISMAN_EXTRACT_COUNT.put("snake_talisman", 10);
        TALISMAN_EXTRACT_COUNT.put("monkey_talisman", 10);
        
        // 初始化符咒提取概率（默认10%，特殊符咒硬编码）
        TALISMAN_EXTRACT_PROBABILITY.put("chicken_talisman", DEFAULT_EXTRACTION_PROBABILITY);
        TALISMAN_EXTRACT_PROBABILITY.put("dragon_talisman", DEFAULT_EXTRACTION_PROBABILITY);
        TALISMAN_EXTRACT_PROBABILITY.put("pig_talisman", DEFAULT_EXTRACTION_PROBABILITY);
        
        // 加载配置文件
        loadConfigMapping();
        loadExtractionProbabilityConfig();
    }
    
    /**
     * 重新加载配置文件（用于热重载）
     */
    public static void reloadConfigs() {
        // 清空现有映射
        CONFIG_BIOME_TO_TALISMAN.clear();
        TALISMAN_EXTRACT_PROBABILITY.clear();
        
        // 重新加载配置
        loadConfigMapping();
        loadExtractionProbabilityConfig();
    }
    
    private static void loadConfigMapping() {
        // 读取配置文件
        Path configPath = FMLPaths.CONFIGDIR.get().resolve("dcc").resolve("biological_mapping.json");
        
        // 如果配置文件不存在，从资源目录复制默认配置
        if (!configPath.toFile().exists()) {
            try {
                configPath.getParent().toFile().mkdirs();
                copyDefaultConfigFromJar(configPath, "biological_mapping.json");
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        
        // 读取配置
        try (FileReader reader = new FileReader(configPath.toFile())) {
            Gson gson = new Gson();
            JsonObject config = gson.fromJson(reader, JsonObject.class);
            
            // 添加空值检查
            if (config == null) {
                System.err.println("Warning: biological_mapping.json is empty or invalid, using default config");
                return;
            }
            
            // 解析配置并添加到CONFIG映射
            for (String talismanId : config.keySet()) {
                // 跳过羊符咒，由唐扇魔方处理
                if (talismanId.equals("sheep_talisman")) {
                    continue;
                }
                
                JsonArray entities = config.getAsJsonArray(talismanId);
                if (entities != null) {
                    for (int i = 0; i < entities.size(); i++) {
                        String entityId = entities.get(i).getAsString();
                        ResourceLocation entityRL = ResourceLocation.parse(entityId);
                        ResourceLocation talismanRL = ResourceLocation.parse("dcc:" + talismanId);
                        CONFIG_BIOME_TO_TALISMAN.put(entityRL, talismanRL);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void loadExtractionProbabilityConfig() {
        // 读取提取概率配置文件
        Path configPath = FMLPaths.CONFIGDIR.get().resolve("dcc").resolve("extraction_probability.properties");

        // 如果配置文件不存在，创建默认配置
        if (!configPath.toFile().exists()) {
            try {
                configPath.getParent().toFile().mkdirs();
                configPath.toFile().createNewFile();

                // 创建默认配置
                createDefaultProbabilityConfig(configPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 读取配置
        java.util.Properties configProps = new java.util.Properties();
        try (FileInputStream fis = new FileInputStream(configPath.toFile())) {
            configProps.load(fis);

            // 解析配置并更新概率映射
            for (String key : configProps.stringPropertyNames()) {
                // 跳过羊符咒，由唐扇魔方处理
                if (key.equals("sheep_talisman")) {
                    continue;
                }
                
                String value = configProps.getProperty(key);
                try {
                    double probability = Double.parseDouble(value);
                    TALISMAN_EXTRACT_PROBABILITY.put(key, probability);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void createDefaultProbabilityConfig(Path configPath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(configPath.toFile())) {
            String content = "# Extraction Probability Configuration\n" +
                "# Format: talisman_id=probability (0.0 to 1.0)\n" +
                "# Example: cow_talisman=0.15 means 15% chance\n" +
                "# Unlisted talismans will use default 10% probability\n" +
                "\n" +
                "# Default probabilities (10%)\n" +
                "cow_talisman=0.10\n" +
                "rabbit_talisman=0.10\n" +
                "horse_talisman=0.10\n" +
                "sheep_talisman=0.10\n" +
                "dog_talisman=0.10\n" +
                "mouse_talisman=0.10\n" +
                "tiger_talisman=0.10\n" +
                "snake_talisman=0.10\n" +
                "monkey_talisman=0.10\n";
            fos.write(content.getBytes());
        }
    }
    
    private static void copyDefaultConfigFromJar(Path configPath, String resourceName) throws IOException {
        try (InputStream is = TalismanExtractorItem.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourceName);
            }
            try (FileOutputStream fos = new FileOutputStream(configPath.toFile())) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
        }
    }
    
    public TalismanExtractorItem(Item.Properties itemProperties) {
        super(itemProperties);
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
    
    /**
     * 显示冷却剩余时间
     */
    private void showCooldownTime(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        
        CompoundTag playerData = getOrCreatePlayerData(player);
        if (!playerData.contains("talisman_extractor_cooldown_start") || !playerData.contains("talisman_extractor_cooldown_duration")) {
            player.sendSystemMessage(Component.translatable("dcc.message.no_cooldown"));
            return;
        }
        
        long cooldownStart = playerData.getLong("talisman_extractor_cooldown_start");
        long cooldownDuration = playerData.getLong("talisman_extractor_cooldown_duration");
        long elapsedTime = System.currentTimeMillis() - cooldownStart;
        
        if (elapsedTime >= cooldownDuration) {
            player.sendSystemMessage(Component.translatable("dcc.message.no_cooldown"));
            return;
        }
        
        long remainingTime = cooldownDuration - elapsedTime;
        int minutes = (int) (remainingTime / 60000);
        int seconds = (int) ((remainingTime % 60000) / 1000);
        
        player.sendSystemMessage(Component.translatable("dcc.message.cooldown_remaining", minutes, seconds));
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
                player.sendSystemMessage(Component.translatable("dcc.message.extraction_disabled"));
            }
            return InteractionResult.FAIL;
        }
        
        // 检查副手是否持有符咒基
        ItemStack offhandStack = player.getOffhandItem();
        if (!offhandStack.is(TalismanItems.TALISMAN_BASE.get())) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.translatable("dcc.message.need_talisman_base"));
            }
            return InteractionResult.FAIL;
        }
        
        // 检查冷却时间
        if (hasCooldown(player)) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.translatable("dcc.message.on_cooldown"));
            }
            return InteractionResult.FAIL;
        }
        
        // 获取生物类型
        ResourceLocation entityType = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
        
        // 查找对应的符咒（只使用配置文件的映射）
        ResourceLocation talismanRL = CONFIG_BIOME_TO_TALISMAN.get(entityType);
        
        if (talismanRL == null) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.translatable("dcc.message.no_mapping"));
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
                player.sendSystemMessage(Component.translatable("dcc.message.extraction_progress", currentCount, requiredCount));
            }
            return InteractionResult.SUCCESS;
        }
        
        // 达到提取次数，进行概率判定
        if (Math.random() > probability) {
            // 概率判定失败，重置提取次数
            resetExtractionCount(player, talismanId);
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.translatable("dcc.message.extraction_failed", (int)((1 - probability) * 100)));
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
            player.sendSystemMessage(Component.translatable("dcc.message.extraction_success", Component.translatable("item.dcc." + talismanId).getString()));
        }
        return InteractionResult.SUCCESS;
    }
    
    private InteractionResult showExtractionProgress(Player player, LivingEntity target) {
        Level level = player.level();
        
        // 检查游戏规则
        if (!level.getGameRules().getBoolean(DragonCurseChronicles.RULE_ALLOW_TALISMAN_EXTRACTION)) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.translatable("dcc.message.extraction_disabled"));
            }
            return InteractionResult.FAIL;
        }
        
        // 获取生物类型
        ResourceLocation entityType = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
        
        // 查找对应的符咒（只使用配置文件的映射）
        ResourceLocation talismanRL = CONFIG_BIOME_TO_TALISMAN.get(entityType);
        
        if (talismanRL == null) {
            if (!player.level().isClientSide()) {
                player.sendSystemMessage(Component.translatable("dcc.message.no_mapping"));
            }
            return InteractionResult.FAIL;
        }
        
        String talismanId = talismanRL.getPath();
        
        // 获取当前提取次数
        int currentCount = getExtractionCount(player, talismanId);
        int requiredCount = TALISMAN_EXTRACT_COUNT.getOrDefault(talismanId, DEFAULT_EXTRACT_COUNT);
        double probability = TALISMAN_EXTRACT_PROBABILITY.getOrDefault(talismanId, DEFAULT_EXTRACTION_PROBABILITY);
        
        if (!player.level().isClientSide()) {
            player.sendSystemMessage(Component.translatable("dcc.message.extraction_status",
                Component.translatable("item.dcc." + talismanId).getString(),
                currentCount,
                requiredCount,
                (int)(probability * 100)));
        }
        
        return InteractionResult.SUCCESS;
    }
    
    private int incrementExtractionCount(Player player, String talismanId) {
        CompoundTag playerData = getOrCreatePlayerData(player);
        String key = "extraction_" + talismanId;
        int currentCount = playerData.getInt(key);
        currentCount++;
        playerData.putInt(key, currentCount);
        savePlayerData(player, playerData);
        return currentCount;
    }
    
    private int getExtractionCount(Player player, String talismanId) {
        CompoundTag playerData = getOrCreatePlayerData(player);
        String key = "extraction_" + talismanId;
        return playerData.getInt(key);
    }
    
    private void resetExtractionCount(Player player, String talismanId) {
        CompoundTag playerData = getOrCreatePlayerData(player);
        String key = "extraction_" + talismanId;
        playerData.putInt(key, 0);
        savePlayerData(player, playerData);
    }
    
    private CompoundTag getOrCreatePlayerData(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return new CompoundTag();
        }
        
        CompoundTag tag = new CompoundTag();
        Path dataPath = getPlayerDataPath(serverPlayer);
        
        // 确保目录存在
        dataPath.getParent().toFile().mkdirs();
        
        File dataFile = dataPath.toFile();
        
        if (dataFile.exists()) {
            try (FileInputStream fis = new FileInputStream(dataFile)) {
                tag = NbtIo.readCompressed(fis);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        return tag;
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
        String fileName = "extraction_cache_" + player.getUUID().toString() + ".dat";
        return dccPath.resolve(fileName);
    }
    
    private boolean hasCooldown(Player player) {
        if (!(player instanceof ServerPlayer)) return false;

        CompoundTag playerData = getOrCreatePlayerData(player);
        if (!playerData.contains("talisman_extractor_cooldown_start") || !playerData.contains("talisman_extractor_cooldown_duration")) {
            return false;
        }

        long cooldownStart = playerData.getLong("talisman_extractor_cooldown_start");
        long cooldownDuration = playerData.getLong("talisman_extractor_cooldown_duration");
        return (System.currentTimeMillis() - cooldownStart) < cooldownDuration;
    }

    private void setCooldown(Player player) {
        CompoundTag playerData = getOrCreatePlayerData(player);
        long cooldownStart = System.currentTimeMillis();
        long cooldownDuration = 3600000; // 1小时
        playerData.putLong("talisman_extractor_cooldown_start", cooldownStart);
        playerData.putLong("talisman_extractor_cooldown_duration", cooldownDuration);
        savePlayerData(player, playerData);
    }
}