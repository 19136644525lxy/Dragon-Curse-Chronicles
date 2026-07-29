package com.qituo.dcc.damage;

public class DamagePresets {
    public static final float LEVEL_1 = 66.0F;
    public static final float LEVEL_2 = 99.0F;
    public static final float LEVEL_3 = 128.0F;
    public static final float LEVEL_4 = 256.0F;
    public static final float LEVEL_5 = 512.0F;
    public static final float LEVEL_6 = 1024.0F;
    public static final float LEVEL_7 = 2048.0F;
    public static final float LEVEL_8 = 4096.0F;
    public static final float LEVEL_9 = 1314520.0F;
    public static final float LEVEL_10 = 5201314.0F;

    public static final String DAMAGE_TYPE_NAME = "origin_end";

    public static float getDamage(int level) {
        switch (level) {
            case 1:
                return LEVEL_1;
            case 2:
                return LEVEL_2;
            case 3:
                return LEVEL_3;
            case 4:
                return LEVEL_4;
            case 5:
                return LEVEL_5;
            case 6:
                return LEVEL_6;
            case 7:
                return LEVEL_7;
            case 8:
                return LEVEL_8;
            case 9:
                return LEVEL_9;
            case 10:
                return LEVEL_10;
            default:
                return LEVEL_1;
        }
    }

    public static float[] getDamageArray() {
        return new float[] {
            LEVEL_1,
            LEVEL_2,
            LEVEL_3,
            LEVEL_4,
            LEVEL_5,
            LEVEL_6,
            LEVEL_7,
            LEVEL_8,
            LEVEL_9,
            LEVEL_10
        };
    }
}