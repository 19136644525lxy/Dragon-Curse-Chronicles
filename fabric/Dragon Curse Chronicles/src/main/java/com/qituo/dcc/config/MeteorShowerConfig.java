package com.qituo.dcc.config;

import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.util.Properties;

public class MeteorShowerConfig {
    private static final String CONFIG_FILE = "dcc/meteor_shower.properties";
    private static Properties properties = new Properties();

    private static boolean enableMeteorShower = false;

    public static void loadConfig() {
        File configFile = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE).toFile();

        if (!configFile.exists()) {
            createDefaultConfig(configFile);
        }

        try (FileInputStream input = new FileInputStream(configFile)) {
            properties.load(input);
            enableMeteorShower = Boolean.parseBoolean(properties.getProperty("enableMeteorShower", "false"));
        } catch (IOException e) {
            com.qituo.dcc.DragonCurseChronicles.LOGGER.error("Failed to load meteor shower config", e);
        }
    }

    private static void createDefaultConfig(File configFile) {
        try {
            configFile.getParentFile().mkdirs();
            try (FileOutputStream output = new FileOutputStream(configFile)) {
                properties.setProperty("enableMeteorShower", "false");
                properties.store(output, "Meteor Shower Configuration\n# Set to true to enable meteor shower random events at midnight");
            }
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
        properties.setProperty("enableMeteorShower", String.valueOf(enabled));
        saveConfig();
    }

    private static void saveConfig() {
        File configFile = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE).toFile();
        try (FileOutputStream output = new FileOutputStream(configFile)) {
            properties.store(output, "Meteor Shower Configuration\n# Set to true to enable meteor shower random events at midnight");
        } catch (IOException e) {
            com.qituo.dcc.DragonCurseChronicles.LOGGER.error("Failed to save meteor shower config", e);
        }
    }
}