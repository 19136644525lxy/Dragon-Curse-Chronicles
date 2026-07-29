package com.qituo.dcc.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

/**
 * 持久化数据接口
 *
 * 原理：
 * 通过 Mixin 的 "Implements Interface" 模式，
 * 让 PlayerPersistentDataMixin 实现此接口，
 * 从而可以将 PlayerEntity 安全地转换为此接口来访问注入的方法。
 */
public interface PersistentDataAccess {

    /**
     * 获取 DCC 持久化数据
     */
    NbtCompound getDccPersistentData();
}
