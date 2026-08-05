package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.client.model.ModelWitherStormHeadPort;
import com.wdcftgg.witherstormmod.common.entity.SupplementalEntities;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;

/** Emissive eyes layer matching the upstream normal, hurt and inactive textures. */
public final class LayerWitherStormHeadEyes implements LayerRenderer<SupplementalEntities.WitherStormHead> {
    private static final ResourceLocation EMISSIVE = new ResourceLocation(Tags.MOD_ID,
            "textures/entity/wither_storm_head/wither_storm_head_emissive.png");
    private static final ResourceLocation EMISSIVE_HURT = new ResourceLocation(Tags.MOD_ID,
            "textures/entity/wither_storm_head/wither_storm_head_emissive_hurt.png");
    private final RenderStormPart<SupplementalEntities.WitherStormHead> renderer;

    public LayerWitherStormHeadEyes(RenderStormPart<SupplementalEntities.WitherStormHead> renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(SupplementalEntities.WitherStormHead entity, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ResourceLocation texture = entity.isHurt() ? EMISSIVE_HURT : EMISSIVE;
        renderer.bindTexture(texture);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        GlStateManager.depthMask(true);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 61680.0F, 0.0F);
        ModelWitherStormHeadPort model = (ModelWitherStormHeadPort) renderer.getMainModel();
        model.setModelAttributes(renderer.getMainModel());
        model.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
        model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        renderer.setLightmap(entity);
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
