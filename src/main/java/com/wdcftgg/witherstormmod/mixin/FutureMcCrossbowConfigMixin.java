package com.wdcftgg.witherstormmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 启用 Future MC 0.2.6 内置但固定关闭的完整弩注册链。 */
@Mixin(targets = "thedarkcolour.futuremc.config.FConfig$VillageAndPillage", remap = false)
public abstract class FutureMcCrossbowConfigMixin {

    @Inject(method = "getCrossbow()Z", at = @At("RETURN"), cancellable = true)
    private void witherstormmod$enableCrossbow(CallbackInfoReturnable<Boolean> callback) {
        callback.setReturnValue(true);
    }
}
