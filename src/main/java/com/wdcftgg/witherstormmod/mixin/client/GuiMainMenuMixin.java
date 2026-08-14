package com.wdcftgg.witherstormmod.mixin.client;

import com.wdcftgg.witherstormmod.client.PanoramaCustomizer;
import com.wdcftgg.witherstormmod.client.util.UpstreamSplashes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 将上游 splash 常量加入 1.12 主菜单的随机选择池。 */
@Mixin(GuiMainMenu.class)
public abstract class GuiMainMenuMixin {
    @Shadow
    private String splashText;

    @Inject(method = "<init>()V", at = @At("TAIL"))
    private void witherstormmod$chooseSplash(CallbackInfo callbackInfo) {
        String splash = UpstreamSplashes.choose(Minecraft.getMinecraft().getResourceManager());
        if (splash != null) splashText = splash;
    }

    @Inject(method = "drawScreen", at = @At("HEAD"))
    private void witherstormmod$syncPanorama(int mouseX, int mouseY, float partialTicks,
                                             CallbackInfo callbackInfo) {
        PanoramaCustomizer.sync();
    }
}
