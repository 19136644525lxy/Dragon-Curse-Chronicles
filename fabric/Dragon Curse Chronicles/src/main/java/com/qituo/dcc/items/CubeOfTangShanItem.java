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

public class CubeOfTangShanItem extends Item {
    private static final Map<Identifier, Identifier> CONFIG_BIOME_TO_TALISMAN = new HashMap<>();

    private static final Map<String, Double> TALISMAN_EXTRACT_PROBABILITY = new HashMap<>();

    private static final Map<String, Integer> TALISMAN_EXTRACT_COUNT = new HashMap<>();
    private static final int DEFAULT_EXTRACT_COUNT = 10;
    private static final double DEFAULT_EXTRACTION_PROBABILITY = 0.10;

    static {
        TALISMAN_EXTRACT_COUNT.put("sheep_talisman", 100);

        TALISMAN_EXTRACT_PROBABILITY.put("sheep_talisman", 0.10);

        loadConfigMapping();
    }

    public CubeOfTangShanItem(Item.Settings properties) {
        super(properties);
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

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity target, Hand hand) {
        World world = user.getWorld();

        if (user.isSneaking()) {
            return showExtractionProgress(user, target);
        }

        if (!world.getGameRules().getBoolean(DragonCurseChronicles.RULE_ALLOW_TALISMAN_EXTRACTION)) {
            if (!world.isClient()) {
                user.sendMessage(Text.translatable("dcc.message.cube_of_tang_shan.extraction_disabled"));
            }
            return ActionResult.FAIL;
        }

        ItemStack offhandStack = user.getOffHandStack();
        if (!offhandStack.isOf(TalismanItems.TALISMAN_BASE)) {
            if (!world.isClient()) {
                user.sendMessage(Text.translatable("dcc.message.cube_of_tang_shan.need_talisman_base"));
            }
            return ActionResult.FAIL;
        }

        if (hasCooldown(user)) {
            if (!world.isClient()) {
                user.sendMessage(Text.translatable("dcc.message.cube_of_tang_shan.on_cooldown"));
            }
            return ActionResult.FAIL;
        }

        Identifier entityType = Registries.ENTITY_TYPE.getId(target.getType());

        Identifier talismanRL = CONFIG_BIOME_TO_TALISMAN.get(entityType);

        if (talismanRL == null || !talismanRL.getPath().equals("sheep_talisman")) {
            if (!world.isClient()) {
                user.sendMessage(Text.translatable("dcc.message.cube_of_tang_shan.no_mapping"));
            }
            return ActionResult.FAIL;
        }

        String talismanId = talismanRL.getPath();

        int requiredCount = TALISMAN_EXTRACT_COUNT.getOrDefault(talismanId, DEFAULT_EXTRACT_COUNT);

        double probability = TALISMAN_EXTRACT_PROBABILITY.getOrDefault(talismanId, DEFAULT_EXTRACTION_PROBABILITY);

        int currentCount = incrementExtractionCount(user, talismanId);

        if (currentCount < requiredCount) {
            if (!world.isClient()) {
                user.sendMessage(Text.translatable("dcc.message.cube_of_tang_shan.extraction_progress", currentCount, requiredCount));
            }
            return ActionResult.SUCCESS;
        }

        if (Math.random() > probability) {
            resetExtractionCount(user, talismanId);
            if (!world.isClient()) {
                user.sendMessage(Text.translatable("dcc.message.cube_of_tang_shan.extraction_failed", (int)((1 - probability) * 100)));
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
            user.sendMessage(Text.translatable("dcc.message.cube_of_tang_shan.extraction_success", Text.translatable("item.dcc." + talismanId).getString()));
        }
        return ActionResult.SUCCESS;
    }

    private void showCooldownTime(PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return;
        }

        NbtCompound playerData = getOrCreatePlayerData(player);
        if (!playerData.contains("cube_of_tang_shan_cooldown_start") || !playerData.contains("cube_of_tang_shan_cooldown_duration")) {
            player.sendMessage(Text.translatable("dcc.message.cube_of_tang_shan.no_cooldown"));
            return;
        }

        long cooldownStart = playerData.getLong("cube_of_tang_shan_cooldown_start");
        long cooldownDuration = playerData.getLong("cube_of_tang_shan_cooldown_duration");
        long elapsedTime = System.currentTimeMillis() - cooldownStart;

        if (elapsedTime >= cooldownDuration) {
            player.sendMessage(Text.translatable("dcc.message.cube_of_tang_shan.no_cooldown"));
            return;
        }

        long remainingTime = cooldownDuration - elapsedTime;
        int minutes = (int) (remainingTime / 60000);
        int seconds = (int) ((remainingTime % 60000) / 1000);

        player.sendMessage(Text.translatable("dcc.message.cube_of_tang_shan.cooldown_remaining", minutes, seconds));
    }

    private ActionResult showExtractionProgress(PlayerEntity player, LivingEntity target) {
        World world = player.getWorld();

        if (!world.getGameRules().getBoolean(DragonCurseChronicles.RULE_ALLOW_TALISMAN_EXTRACTION)) {
            if (!world.isClient()) {
                player.sendMessage(Text.translatable("dcc.message.cube_of_tang_shan.extraction_disabled"));
            }
            return ActionResult.FAIL;
        }

        Identifier entityType = Registries.ENTITY_TYPE.getId(target.getType());

        Identifier talismanRL = CONFIG_BIOME_TO_TALISMAN.get(entityType);

        if (talismanRL == null || !talismanRL.getPath().equals("sheep_talisman")) {
            if (!player.getWorld().isClient()) {
                player.sendMessage(Text.translatable("dcc.message.cube_of_tang_shan.no_mapping"));
            }
            return ActionResult.FAIL;
        }

        String talismanId = talismanRL.getPath();

        int currentCount = getExtractionCount(player, talismanId);
        int requiredCount = TALISMAN_EXTRACT_COUNT.getOrDefault(talismanId, DEFAULT_EXTRACT_COUNT);
        double probability = TALISMAN_EXTRACT_PROBABILITY.getOrDefault(talismanId, DEFAULT_EXTRACTION_PROBABILITY);

        if (!player.getWorld().isClient()) {
            player.sendMessage(Text.translatable("dcc.message.cube_of_tang_shan.extraction_status",
                Text.translatable("item.dcc." + talismanId).getString(),
                currentCount,
                requiredCount,
                (int)(probability * 100)));
        }

        return ActionResult.SUCCESS;
    }

    private int incrementExtractionCount(PlayerEntity player, String talismanId) {
        NbtCompound playerData = getOrCreatePlayerData(player);
        String key = "cube_of_tang_shan_extraction_" + talismanId;
        int currentCount = playerData.getInt(key);
        currentCount++;
        playerData.putInt(key, currentCount);
        savePlayerData(player, playerData);
        return currentCount;
    }

    private int getExtractionCount(PlayerEntity player, String talismanId) {
        NbtCompound playerData = getOrCreatePlayerData(player);
        String key = "cube_of_tang_shan_extraction_" + talismanId;
        return playerData.getInt(key);
    }

    private void resetExtractionCount(PlayerEntity player, String talismanId) {
        NbtCompound playerData = getOrCreatePlayerData(player);
        String key = "cube_of_tang_shan_extraction_" + talismanId;
        playerData.remove(key);
        savePlayerData(player, playerData);
    }

    private boolean hasCooldown(PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity)) return false;

        NbtCompound playerData = getOrCreatePlayerData(player);
        if (!playerData.contains("cube_of_tang_shan_cooldown_start") || !playerData.contains("cube_of_tang_shan_cooldown_duration")) {
            return false;
        }

        long cooldownStart = playerData.getLong("cube_of_tang_shan_cooldown_start");
        long cooldownDuration = playerData.getLong("cube_of_tang_shan_cooldown_duration");
        return (System.currentTimeMillis() - cooldownStart) < cooldownDuration;
    }

    private void setCooldown(PlayerEntity player) {
        NbtCompound playerData = getOrCreatePlayerData(player);
        long cooldownStart = System.currentTimeMillis();
        long cooldownDuration = 3600000;
        playerData.putLong("cube_of_tang_shan_cooldown_start", cooldownStart);
        playerData.putLong("cube_of_tang_shan_cooldown_duration", cooldownDuration);
        savePlayerData(player, playerData);
    }

    private NbtCompound getOrCreatePlayerData(PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return new NbtCompound();
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

        return new NbtCompound();
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
        String fileName = "cube_of_tang_shan_cache_" + player.getUuid().toString() + ".dat";
        return dccPath.resolve(fileName);
    }

    private static void loadConfigMapping() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("dcc").resolve("biological_mapping.json");

        if (!configPath.toFile().exists()) {
            try {
                configPath.getParent().toFile().mkdirs();
                copyDefaultConfig(configPath, "biological_mapping.json");
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        try (FileReader reader = new FileReader(configPath.toFile())) {
            JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);

            for (String talismanId : jsonObject.keySet()) {
                if (!talismanId.equals("sheep_talisman")) {
                    continue;
                }

                JsonArray entityArray = jsonObject.getAsJsonArray(talismanId);
                for (int i = 0; i < entityArray.size(); i++) {
                    String entityId = entityArray.get(i).getAsString();
                    try {
                        Identifier entityRL = new Identifier(entityId);
                        Identifier talismanRL = new Identifier("dcc:" + talismanId);
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