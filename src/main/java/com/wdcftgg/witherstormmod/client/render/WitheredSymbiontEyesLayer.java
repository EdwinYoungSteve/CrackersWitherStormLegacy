package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.common.entity.SickenedEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;

public final class WitheredSymbiontEyesLayer implements LayerRenderer<SickenedEntities.WitheredSymbiontEntity> {
    private final WitheredSymbiontRenderer renderer;

    public WitheredSymbiontEyesLayer(WitheredSymbiontRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(SickenedEntities.WitheredSymbiontEntity entity,
                              float limbSwing, float limbSwingAmount, float partialTicks,
                              float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        renderer.bindTexture(WitheredSymbiontRenderProfile.EMISSIVE_TEXTURE);
        float previousBrightnessX = OpenGlHelper.lastBrightnessX;
        float previousBrightnessY = OpenGlHelper.lastBrightnessY;
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        GlStateManager.depthMask(false);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 61680.0F, 0.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().entityRenderer.setupFogColor(true);
        try {
            renderer.getMainModel().render(entity, limbSwing, limbSwingAmount,
                    ageInTicks, netHeadYaw, headPitch, scale);
        } finally {
            Minecraft.getMinecraft().entityRenderer.setupFogColor(false);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,
                    previousBrightnessX, previousBrightnessY);
            GlStateManager.depthMask(true);
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.enableCull();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
