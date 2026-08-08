package com.qituo.dcc.mixin;

import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldGenerationContext.class)
public abstract class WorldGenerationContextMixin {
    // 1.21 重命名：getMinBuildHeight → getMinGenY, getMaxBuildHeight → getGenDepth
    @Inject(method = "getMinGenY", at = @At("HEAD"), cancellable = true)
    public void getMinGenY(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(0);
    }

    @Inject(method = "getGenDepth", at = @At("HEAD"), cancellable = true)
    public void getGenDepth(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(512);
    }
}
