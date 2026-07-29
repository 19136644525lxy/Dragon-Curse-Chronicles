package com.qituo.dcc.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

/**
 * 持久化数据辅助类
 *
 * 原理：
 * Forge 的 player.getPersistentData() 返回自动保存的 CompoundTag。
 * Fabric 通过 PlayerPersistentDataMixin 注入 getDccPersistentData() 方法。
 * 此类提供统一的静态方法，避免业务代码直接依赖 Mixin。
 */
public class PersistentDataHelper {

    /**
     * 获取玩家 DCC 持久化数据
     * 通过 PersistentDataAccess 接口访问（Mixin Implements Interface 模式）
     */
    public static NbtCompound getPersistentData(PlayerEntity player) {
        return ((PersistentDataAccess) player).getDccPersistentData();
    }

    /**
     * 获取布尔值
     */
    public static boolean getBoolean(PlayerEntity player, String key) {
        return getPersistentData(player).getBoolean(key);
    }

    /**
     * 设置布尔值
     */
    public static void putBoolean(PlayerEntity player, String key, boolean value) {
        getPersistentData(player).putBoolean(key, value);
    }

    /**
     * 获取整数值
     */
    public static int getInt(PlayerEntity player, String key) {
        return getPersistentData(player).getInt(key);
    }

    /**
     * 设置整数值
     */
    public static void putInt(PlayerEntity player, String key, int value) {
        getPersistentData(player).putInt(key, value);
    }

    /**
     * 获取浮点值
     */
    public static float getFloat(PlayerEntity player, String key) {
        return getPersistentData(player).getFloat(key);
    }

    /**
     * 设置浮点值
     */
    public static void putFloat(PlayerEntity player, String key, float value) {
        getPersistentData(player).putFloat(key, value);
    }

    /**
     * 获取双精度浮点值
     */
    public static double getDouble(PlayerEntity player, String key) {
        return getPersistentData(player).getDouble(key);
    }

    /**
     * 设置双精度浮点值
     */
    public static void putDouble(PlayerEntity player, String key, double value) {
        getPersistentData(player).putDouble(key, value);
    }

    /**
     * 获取字符串值
     */
    public static String getString(PlayerEntity player, String key) {
        return getPersistentData(player).getString(key);
    }

    /**
     * 设置字符串值
     */
    public static void putString(PlayerEntity player, String key, String value) {
        getPersistentData(player).putString(key, value);
    }

    /**
     * 移除键
     */
    public static void remove(PlayerEntity player, String key) {
        getPersistentData(player).remove(key);
    }

    /**
     * 是否包含键
     */
    public static boolean contains(PlayerEntity player, String key) {
        return getPersistentData(player).contains(key);
    }
}
