package com.qituo.dcc.items;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.qituo.dcc.DragonCurseChronicles;
import com.qituo.dcc.TalismanItems;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;
import net.fabricmc.loader.api.FabricLoader;
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
    private static final Map<Identifier, Identifier> CONFIG_BIOME_TO_TALISMAN = new HashMap<>();
    private static final Map<String, Integer> TALISMAN_EXTRACT_COUNT = new HashMap<>();
    private static final Map<String, Double> TALISMAN_EXTRACT_PROBABILITY = new HashMap<>();

    private static final double DEFAULT_EXTRACTION_PROBABILITY = 0.10;
    private static final int DEFAULT_EXTRACT_COUNT = 10;

    static {
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

        TALISMAN_EXTRACT_PROBABILITY.put("chicken_talisman", DEFAULT_EXTRACTION_PROBABILITY);
        TALISMAN_EXTRACT_PROBABILITY.put("dragon_talisman", DEFAULT_EXTRACTION_PROBABILITY);
        TALISMAN_EXTRACT_PROBABILITY.put("pig_talisman", DEFAULT_EXTRACTION_PROBABILITY);

        loadConfigMapping();
        loadExtractionProbabilityConfig();
    }

    public static void reloadConfigs() {
        CONFIG_BIOME_TO_TALISMAN.clear();
        TALISMAN_EXTRACT_PROBABILITY.clear();

        loadConfigMapping();
        loadExtractionProbabilityConfig();
    }

    private static void loadConfigMapping() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("dcc").resolve("biological_mapping.json");

        if (!configPath.toFile().exists()) {
            try {
                configPath.getParent().toFile().mkdirs();
                copyDefaultConfigFromJar(configPath, "biological_mapping.json");
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        try (FileReader reader = new FileReader(configPath.toFile())) {
            Gson gson = new Gson();
            JsonObject config = gson.fromJson(reader, JsonObject.class);

            if (config == null) {
                System.err.println("Warning: biological_mapping.json is empty or invalid, using default config");
                return;
            }

            for (String talismanId : config.keySet()) {
                if (talismanId.equals("sheep_talisman")) {
                    continue;
                }

                JsonArray entities = config.getAsJsonArray(talismanId);
                if (entities != null) {
                    for (int i = 0; i < entities.size(); i++) {
                        String entityId = entities.get(i).getAsString();
                        Identifier entityRL = new Identifier(entityId);
                        Identifier talismanRL = new Identifier("dcc:" + talismanId);
                        CONFIG_BIOME_TO_TALISMAN.put(entityRL, talismanRL);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadExtractionProbabilityConfig() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("dcc").resolve("extraction_probability.properties");

        if (!configPath.toFile().exists()) {
            try {
                configPath.getParent().toFile().mkdirs();
                configPath.toFile().createNewFile();

                createDefaultProbabilityConfig(configPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        java.util.Properties configProps = new java.util.Properties();
        try (FileInputStream fis = new FileInputStream(configPath.toFile())) {
            configProps.load(fis);

            for (String key : configProps.stringPropertyNames()) {
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

    public TalismanExtractorItem(Item.Settings itemProperties) {
        super(itemProperties);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.getGameRules().getBoolean(DragonCurseChronicles.RULE_ALLOW_TALISMAN_EXTRACTION)) {
            if (!world.isClient()) {
                user.sendMessage(Text.translatable("dcc.message.extraction_disabled"));
            }
            return TypedActionResult.fail(user.getStackInHand(hand));
        }

        if (user.isSneaking()) {
            if (!world.isClient()) {
                showCooldownTime(user);
            }
            return TypedActionResult.success(user.getStackInHand(hand));
        }
        return super.use(world, user, hand);
    }

    private void showCooldownTime(PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return;
        }

        NbtCompound playerData = getOrCreatePlayerData(player);
        if (!playerData.contains("talisman_extractor_cooldown_start") || !playerData.contains("talisman_extractor_cooldown_duration")) {
            player.sendMessage(Text.translatable("dcc.message.no_cooldown"));
            return;
        }

        long cooldownStart = playerData.getLong("talisman_extractor_cooldown_start");
        long cooldownDuration = playerData.getLong("talisman_extractor_cooldown_duration");
        long elapsedTime = System.currentTimeMillis() - cooldownStart;

        if (elapsedTime >= cooldownDuration) {
            player.sendMessage(Text.translatable("dcc.message.no_cooldown"));
            return;
        }

        long remainingTime = cooldownDuration - elapsedTime;
        int minutes = (int) (remainingTime / 60000);
        int seconds = (int) ((remainingTime % 60000) / 1000);

        player.sendMessage(Text.translatable("dcc.message.cooldown_remaining", minutes, seconds));
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity target, Hand hand) {
        World world = user.getWorld();

        if (user.isSneaking()) {
            return showExtractionProgress(user, target);
        }

        if (!world.getGameRules().getBoolean(DragonCurseChronicles.RULE_ALLOW_TALISMAN_EXTRACTION)) {
            if (!world.isClient()) {
                user.sendMessage(Text.translatable("dcc.message.extraction_disabled"));
            }
            return ActionResult.FAIL;
        }

        ItemStack offhandStack = user.getOffHandStack();
        if (!offhandStack.isOf(TalismanItems.TALISMAN_BASE)) {
            if (!world.isClient()) {
                user.sendMessage(Text.translatable("dcc.message.need_talisman_base"));
            }
            return ActionResult.FAIL;
        }

        if (hasCooldown(user)) {
            if (!world.isClient()) {
                user.sendMessage(Text.translatable("dcc.message.on_cooldown"));
            }
            return ActionResult.FAIL;
        }

        Identifier entityType = Registries.ENTITY_TYPE.getId(target.getType());

        Identifier talismanRL = CONFIG_BIOME_TO_TALISMAN.get(entityType);

        if (talismanRL == null) {
            if (!world.isClient()) {
                user.sendMessage(Text.translatable("dcc.message.no_mapping"));
            }
            return ActionResult.FAIL;
        }

        String talismanId = talismanRL.getPath();

        int requiredCount = TALISMAN_EXTRACT_COUNT.getOrDefault(talismanId, DEFAULT_EXTRACT_COUNT);

        double probability = TALISMAN_EXTRACT_PROBABILITY.getOrDefault(talismanId, DEFAULT_EXTRACTION_PROBABILITY);

        int currentCount = incrementExtractionCount(user, talismanId);

        if (currentCount < requiredCount) {
            if (!world.isClient()) {
                user.sendMessage(Text.translatable("dcc.message.extraction_progress", currentCount, requiredCount));
            }
            return ActionResult.SUCCESS;
        }

        if (Math.random() > probability) {
            resetExtractionCount(user, talismanId);
            if (!world.isClient()) {
                user.sendMessage(Text.translatable("dcc.message.extraction_failed", (int)((1 - probability) * 100)));
            }
            return ActionResult.FAIL;
        }

        offhandStack.decrement(1);

        ItemStack talismanStack = new ItemStack(Registries.ITEM.get(talismanRL));
        if (!user.getInventory().insertStack(talismanStack)) {
            user.dropItem(talismanStack, false);
        }

        world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1.0F, 1.0F);

        resetExtractionCount(user, talismanId);

        setCooldown(user);

        if (!world.isClient()) {
            user.sendMessage(Text.translatable("dcc.message.extraction_success", Text.translatable("item.dcc." + talismanId).getString()));
        }
        return ActionResult.SUCCESS;
    }

    private ActionResult showExtractionProgress(PlayerEntity player, LivingEntity target) {
        World world = player.getWorld();

        if (!world.getGameRules().getBoolean(DragonCurseChronicles.RULE_ALLOW_TALISMAN_EXTRACTION)) {
            if (!world.isClient()) {
                player.sendMessage(Text.translatable("dcc.message.extraction_disabled"));
            }
            return ActionResult.FAIL;
        }

        Identifier entityType = Registries.ENTITY_TYPE.getId(target.getType());

        Identifier talismanRL = CONFIG_BIOME_TO_TALISMAN.get(entityType);

        if (talismanRL == null) {
            if (!player.getWorld().isClient()) {
                player.sendMessage(Text.translatable("dcc.message.no_mapping"));
            }
            return ActionResult.FAIL;
        }

        String talismanId = talismanRL.getPath();

        int currentCount = getExtractionCount(player, talismanId);
        int requiredCount = TALISMAN_EXTRACT_COUNT.getOrDefault(talismanId, DEFAULT_EXTRACT_COUNT);
        double probability = TALISMAN_EXTRACT_PROBABILITY.getOrDefault(talismanId, DEFAULT_EXTRACTION_PROBABILITY);

        if (!player.getWorld().isClient()) {
            player.sendMessage(Text.translatable("dcc.message.extraction_status",
                Text.translatable("item.dcc." + talismanId).getString(),
                currentCount,
                requiredCount,
                (int)(probability * 100)));
        }

        return ActionResult.SUCCESS;
    }

    private int incrementExtractionCount(PlayerEntity player, String talismanId) {
        NbtCompound playerData = getOrCreatePlayerData(player);
        String key = "extraction_" + talismanId;
        int currentCount = playerData.getInt(key);
        currentCount++;
        playerData.putInt(key, currentCount);
        savePlayerData(player, playerData);
        return currentCount;
    }

    private int getExtractionCount(PlayerEntity player, String talismanId) {
        NbtCompound playerData = getOrCreatePlayerData(player);
        String key = "extraction_" + talismanId;
        return playerData.getInt(key);
    }

    private void resetExtractionCount(PlayerEntity player, String talismanId) {
        NbtCompound playerData = getOrCreatePlayerData(player);
        String key = "extraction_" + talismanId;
        playerData.putInt(key, 0);
        savePlayerData(player, playerData);
    }

    private NbtCompound getOrCreatePlayerData(PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return new NbtCompound();
        }

        NbtCompound tag = new NbtCompound();
        Path dataPath = getPlayerDataPath(serverPlayer);

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

    private void savePlayerData(PlayerEntity player, NbtCompound data) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return;
        }

        Path dataPath = getPlayerDataPath(serverPlayer);

        dataPath.getParent().toFile().mkdirs();

        try {
            try (FileOutputStream fos = new FileOutputStream(dataPath.toFile())) {
                NbtIo.writeCompressed(data, fos);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Path getPlayerDataPath(ServerPlayerEntity player) {
        Path worldPath = player.server.getSavePath(WorldSavePath.PLAYERDATA);
        Path dccPath = worldPath.resolve("dcc");
        String fileName = "extraction_cache_" + player.getUuid().toString() + ".dat";
        return dccPath.resolve(fileName);
    }

    private boolean hasCooldown(PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity)) return false;

        NbtCompound playerData = getOrCreatePlayerData(player);
        if (!playerData.contains("talisman_extractor_cooldown_start") || !playerData.contains("talisman_extractor_cooldown_duration")) {
            return false;
        }

        long cooldownStart = playerData.getLong("talisman_extractor_cooldown_start");
        long cooldownDuration = playerData.getLong("talisman_extractor_cooldown_duration");
        return (System.currentTimeMillis() - cooldownStart) < cooldownDuration;
    }

    private void setCooldown(PlayerEntity player) {
        NbtCompound playerData = getOrCreatePlayerData(player);
        long cooldownStart = System.currentTimeMillis();
        long cooldownDuration = 3600000;
        playerData.putLong("talisman_extractor_cooldown_start", cooldownStart);
        playerData.putLong("talisman_extractor_cooldown_duration", cooldownDuration);
        savePlayerData(player, playerData);
    }
}