package com.qituo.dcc.config;

import net.fabricmc.loader.api.FabricLoader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class TalismanConfig {
    private static final String CONFIG_FILE_NAME = "talisman_config.properties";
    private static final File CONFIG_DIR = new File(FabricLoader.getInstance().getConfigDir().toFile(), "dcc");
    private static final File CONFIG_FILE = new File(CONFIG_DIR, CONFIG_FILE_NAME);

    private static double sheepTalismanFlySpeed = 0.5D;

    static {
        loadConfig();
    }

    public static void loadConfig() {
        try {
            if (!CONFIG_DIR.exists()) {
                CONFIG_DIR.mkdirs();
            }

            Properties properties = new Properties();

            if (!CONFIG_FILE.exists()) {
                createDefaultConfig();
            }

            try (InputStream input = new FileInputStream(CONFIG_FILE)) {
                properties.load(input);

                String flySpeedStr = properties.getProperty("sheep_talisman.fly_speed");
                if (flySpeedStr != null && !flySpeedStr.isEmpty()) {
                    try {
                        sheepTalismanFlySpeed = Double.parseDouble(flySpeedStr);
                        sheepTalismanFlySpeed = Math.max(0.01D, Math.min(10.0D, sheepTalismanFlySpeed));
                    } catch (NumberFormatException e) {
                        sheepTalismanFlySpeed = 0.5D;
                    }
                }
            }
        } catch (IOException e) {
            com.qituo.dcc.DragonCurseChronicles.LOGGER.error("Failed to load talisman config", e);
            sheepTalismanFlySpeed = 0.1D;
        }
    }

    private static void createDefaultConfig() throws IOException {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(CONFIG_FILE), StandardCharsets.UTF_8))) {
            writer.println("# ==========================================");
            writer.println("# Dragon Curse Chronicles - Talisman Config");
            writer.println("# ==========================================");
            writer.println("#");
            writer.println("# 羊符咒配置 (Sheep Talisman Configuration)");
            writer.println("#");
            writer.println("# sheep_talisman.fly_speed:");
            writer.println("#   - 羊符咒灵魂出窍模式下的飞行速度");
            writer.println("#   - 默认值: 0.1");
            writer.println("#   - 范围: 0.01 - 5.0");
            writer.println("#   - 创造模式默认飞行速度是 0.05");
            writer.println("#");
            writer.println("sheep_talisman.fly_speed=0.1");
        }
    }

    public static double getSheepTalismanFlySpeed() {
        return sheepTalismanFlySpeed;
    }

    public static void reloadConfig() {
        loadConfig();
        com.qituo.dcc.DragonCurseChronicles.LOGGER.info("Reloaded talisman configuration");
    }
}