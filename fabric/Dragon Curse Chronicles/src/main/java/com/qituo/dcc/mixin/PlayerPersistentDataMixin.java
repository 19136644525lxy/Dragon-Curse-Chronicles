package com.qituo.dcc.mixin;

import com.qituo.dcc.util.PersistentDataAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 为 PlayerEntity 添加持久化数据支持
 *
 * 原理：
 * Forge 的 player.getPersistentData() 返回自动保存的 CompoundTag。
 * Fabric 没有此 API，需要通过 Mixin 实现：
 * 1. 在 PlayerEntity 中注入 NbtCompound 字段
 * 2. 通过 Inject 在 writeCustomDataToNbt / readCustomDataFromNbt 中保存/加载
 * 3. 实现 PersistentDataAccess 接口，提供 getDccPersistentData() 方法
 * 4. 业务代码通过 (PersistentDataAccess) player 访问注入方法
 *
 * 注意：Fabric 1.20.1 中 PlayerEntity 的序列化方法是
 *      writeCustomDataToNbt(NbtCompound) 和 readCustomDataFromNbt(NbtCompound)
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerPersistentDataMixin implements PersistentDataAccess {

    @Unique
    private NbtCompound dcc$persistentData = new NbtCompound();

    /**
     * 从 NBT 读取自定义数据
     */
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void dcc$readPersistentData(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("dcc_persistent_data")) {
            dcc$persistentData = nbt.getCompound("dcc_persistent_data");
        }
    }

    /**
     * 写入自定义数据到 NBT
     */
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void dcc$writePersistentData(NbtCompound nbt, CallbackInfo ci) {
        nbt.put("dcc_persistent_data", dcc$persistentData);
    }

    /**
     * 获取 DCC 持久化数据（实现 PersistentDataAccess 接口）
     * 业务代码应使用此方法替代 Forge 的 player.getPersistentData()
     */
    @Override
    public NbtCompound getDccPersistentData() {
        return dcc$persistentData;
    }
}
