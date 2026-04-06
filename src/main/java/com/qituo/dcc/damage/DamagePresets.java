package com.qituo.dcc.damage;

public class DamagePresets {
    // 预设伤害值
    public static final float LOW = 666.0F;
    public static final float MEDIUM = 999.0F;
    public static final float OMEGA = 1314520.0F;
    
    // 伤害类型名称
    public static final String DAMAGE_TYPE_NAME = "origin_end";
    
    /**
     * 获取预设伤害值
     * @param preset 预设名称 ("low", "medium", "omega")
     * @return 对应的伤害值
     */
    public static float getDamage(String preset) {
        switch (preset.toLowerCase()) {
            case "low":
                return LOW;
            case "medium":
                return MEDIUM;
            case "omega":
                return OMEGA;
            default:
                return LOW;
        }
    }
    
    /**
     * 获取预设伤害值
     * @param index 预设索引 (0: low, 1: medium, 2: omega)
     * @return 对应的伤害值
     */
    public static float getDamage(int index) {
        switch (index) {
            case 0:
                return LOW;
            case 1:
                return MEDIUM;
            case 2:
                return OMEGA;
            default:
                return LOW;
        }
    }
}