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
    @Inject(method = "getMinBuildHeight", at = @At("HEAD"), cancellable = true)
    public void getMinBuildHeight(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(0);
    }

    @Inject(method = "getMaxBuildHeight", at = @At("HEAD"), cancellable = true)
    public void getMaxBuildHeight(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(512);
    }
}
