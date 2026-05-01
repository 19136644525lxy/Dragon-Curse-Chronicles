package com.qituo.dcc.config;

import net.minecraftforge.fml.loading.FMLPaths;
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
    private static final File CONFIG_DIR = new File(FMLPaths.CONFIGDIR.get().toFile(), "dcc");
    private static final File CONFIG_FILE = new File(CONFIG_DIR, CONFIG_FILE_NAME);
    
    // 配置值（默认速度提高到0.5，创造模式默认是0.05）
    private static double sheepTalismanFlySpeed = 0.5D;
    
    static {
        loadConfig();
    }
    
    /**
     * 加载配置文件
     */
    public static void loadConfig() {
        try {
            // 确保配置目录存在
            if (!CONFIG_DIR.exists()) {
                CONFIG_DIR.mkdirs();
            }
            
            Properties properties = new Properties();
            
            // 如果配置文件不存在，创建默认配置文件
            if (!CONFIG_FILE.exists()) {
                createDefaultConfig();
            }
            
            // 读取配置文件（使用UTF-8编码）
            try (InputStream input = new FileInputStream(CONFIG_FILE)) {
                properties.load(input);
                
                // 读取羊符咒飞行速度配置
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
    
    /**
     * 创建默认配置文件
     */
    private static void createDefaultConfig() throws IOException {
        // 使用PrintWriter和UTF-8编码写入配置文件
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(CONFIG_FILE), StandardCharsets.UTF_8))) {
            // 写入配置注释和内容
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
    
    /**
     * 获取羊符咒飞行速度
     */
    public static double getSheepTalismanFlySpeed() {
        return sheepTalismanFlySpeed;
    }
    
    /**
     * 重新加载配置文件
     */
    public static void reloadConfig() {
        loadConfig();
        com.qituo.dcc.DragonCurseChronicles.LOGGER.info("Reloaded talisman configuration");
    }
}
