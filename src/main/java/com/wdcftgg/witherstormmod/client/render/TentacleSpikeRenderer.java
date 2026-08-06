package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.client.model.TentacleSpikeModel;
import com.wdcftgg.witherstormmod.common.entity.SupplementalEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class TentacleSpikeRenderer extends Render<SupplementalEntities.TentacleSpikeEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MOD_ID,
            "textures/entity/tentacle_spike/tentacle_spike.png");
    private static final ResourceLocation EMISSIVE = new ResourceLocation(Tags.MOD_ID,
            "textures/entity/tentacle_spike/tentacle_spike_emissive.png");
    private final TentacleSpikeModel model = new TentacleSpikeModel();

    public TentacleSpikeRenderer(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(SupplementalEntities.TentacleSpikeEntity entity, double x, double y, double z,
                         float entityYaw, float partialTicks) {
        float progress = entity.getAnimationProgress(partialTicks);
        if (progress == 0.0F) return;
        float verticalScale = 1.0F;
        float horizontalScale = 1.0F;
        if (progress > 0.9F) {
            float fade = (1.0F - progress) / 0.1F;
            verticalScale *= fade;
            horizontalScale *= fade;
        } else if (progress < 0.08F) {
            verticalScale *= progress / 0.08F;
        }
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.rotate(90.0F - entity.rotationYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(-horizontalScale, -verticalScale, horizontalScale);
        bindTexture(TEXTURE);
        model.render(entity, progress, 0.0F, 0.0F, entity.rotationYaw, entity.rotationPitch, 0.0625F);
        bindTexture(EMISSIVE);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 61680.0F, 0.0F);
        Minecraft.getMinecraft().entityRenderer.setupFogColor(true);
        model.render(entity, progress, 0.0F, 0.0F, entity.rotationYaw, entity.rotationPitch, 0.0625F);
        Minecraft.getMinecraft().entityRenderer.setupFogColor(false);
        int light = entity.getBrightnessForRender();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, light % 65536, light / 65536);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(SupplementalEntities.TentacleSpikeEntity entity) {
        return TEXTURE;
    }
}
