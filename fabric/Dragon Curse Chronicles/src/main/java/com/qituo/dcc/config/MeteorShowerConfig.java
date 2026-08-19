package com.qituo.dcc.config;

import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.util.Properties;

/**
 * 流星雨事件配置
 * 管理事件开关、触发概率、唐扇魔方刷新概率、确认超时等配置项
 * 配置文件位于 config/dcc/meteor_shower.properties
 *
 * 升级兼容：读取配置时会检测缺失的配置项（旧版本配置文件），
 * 自动以默认值补全并写回磁盘，确保用户能看到完整配置。
 */
public class MeteorShowerConfig {
    private static final String CONFIG_FILE = "dcc/meteor_shower.properties";
    private static final Properties properties = new Properties();

    private static boolean enableMeteorShower = false;
    private static double triggerProbability = 0.05;
    private static double cubeSpawnProbability = 0.10;
    private static int askTimeoutSeconds = 60;

    public static void loadConfig() {
        File configFile = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE).toFile();

        if (!configFile.exists()) {
            createDefaultConfig(configFile);
            return;
        }

        boolean needResave = false;

        try (FileInputStream input = new FileInputStream(configFile)) {
            properties.load(input);
        } catch (IOException e) {
            com.qituo.dcc.DragonCurseChronicles.LOGGER.error("Failed to load meteor shower config", e);
            return;
        }

        // 逐项读取并校验，缺失/非法时回退默认值并标记需要重写
        String raw = properties.getProperty("enableMeteorShower");
        if (raw == null) {
            needResave = true;
        } else {
            enableMeteorShower = Boolean.parseBoolean(raw);
        }

        raw = properties.getProperty("triggerProbability");
        Double parsed = tryParseDouble(raw, 0.0, 1.0);
        if (parsed == null) {
            needResave = true;
        } else {
            triggerProbability = parsed;
        }

        raw = properties.getProperty("cubeSpawnProbability");
        parsed = tryParseDouble(raw, 0.0, 1.0);
        if (parsed == null) {
            needResave = true;
        } else {
            cubeSpawnProbability = parsed;
        }

        raw = properties.getProperty("askTimeoutSeconds");
        Integer parsedInt = tryParseInt(raw, 1, 3600);
        if (parsedInt == null) {
            needResave = true;
        } else {
            askTimeoutSeconds = parsedInt;
        }

        // 覆盖检测：有缺失/非法项 → 把默认值写回 Properties 并重新保存
        if (needResave) {
            syncToProperties();
            try {
                saveConfigRaw(configFile);
                com.qituo.dcc.DragonCurseChronicles.LOGGER.info(
                    "流星雨配置文件已补全缺失项并保存：{}", configFile.getAbsolutePath());
            } catch (IOException e) {
                com.qituo.dcc.DragonCurseChronicles.LOGGER.error("Failed to resave meteor shower config after upgrade", e);
            }
        }
    }

    private static Double tryParseDouble(String raw, double min, double max) {
        if (raw == null || raw.isBlank()) return null;
        try {
            double v = Double.parseDouble(raw);
            if (v < min || v > max) return null;
            return v;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer tryParseInt(String raw, int min, int max) {
        if (raw == null || raw.isBlank()) return null;
        try {
            int v = Integer.parseInt(raw);
            if (v < min || v > max) return null;
            return v;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static void syncToProperties() {
        properties.setProperty("enableMeteorShower", String.valueOf(enableMeteorShower));
        properties.setProperty("triggerProbability", String.valueOf(triggerProbability));
        properties.setProperty("cubeSpawnProbability", String.valueOf(cubeSpawnProbability));
        properties.setProperty("askTimeoutSeconds", String.valueOf(askTimeoutSeconds));
    }

    private static void createDefaultConfig(File configFile) {
        try {
            configFile.getParentFile().mkdirs();
            syncToProperties();
            saveConfigRaw(configFile);
        } catch (IOException e) {
            com.qituo.dcc.DragonCurseChronicles.LOGGER.error("Failed to create meteor shower config", e);
        }
    }

    public static void reloadConfig() {
        loadConfig();
    }

    public static boolean isMeteorShowerEnabled() {
        return enableMeteorShower;
    }

    public static void setMeteorShowerEnabled(boolean enabled) {
        enableMeteorShower = enabled;
        syncToProperties();
        saveConfig();
    }

    public static double getTriggerProbability() {
        return triggerProbability;
    }

    public static void setTriggerProbability(double probability) {
        triggerProbability = Math.max(0.0, Math.min(1.0, probability));
        syncToProperties();
        saveConfig();
    }

    public static double getCubeSpawnProbability() {
        return cubeSpawnProbability;
    }

    public static void setCubeSpawnProbability(double probability) {
        cubeSpawnProbability = Math.max(0.0, Math.min(1.0, probability));
        syncToProperties();
        saveConfig();
    }

    public static int getAskTimeoutSeconds() {
        return askTimeoutSeconds;
    }

    private static void saveConfig() {
        File configFile = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE).toFile();
        try {
            saveConfigRaw(configFile);
        } catch (IOException e) {
            com.qituo.dcc.DragonCurseChronicles.LOGGER.error("Failed to save meteor shower config", e);
        }
    }

    private static void saveConfigRaw(File configFile) throws IOException {
        try (FileOutputStream output = new FileOutputStream(configFile)) {
            properties.store(output,
                    "Meteor Shower Configuration\n" +
                    "# Set to true to enable meteor shower random events at midnight\n" +
                    "# triggerProbability: chance to trigger at midnight (0.0-1.0, default 0.05 = 5%)\n" +
                    "# cubeSpawnProbability: chance for Cube of Tang Shan in treasure chest (0.0-1.0, default 0.10 = 10%)\n" +
                    "# askTimeoutSeconds: player confirmation timeout seconds (1-3600, default 60)");
        }
    }
}
