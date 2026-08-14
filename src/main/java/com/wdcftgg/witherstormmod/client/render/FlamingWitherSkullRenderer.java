package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.client.model.FlamingWitherSkullModel;
import com.wdcftgg.witherstormmod.common.entity.SupplementalEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.util.ResourceLocation;

public class FlamingWitherSkullRenderer<T extends EntityWitherSkull> extends Render<T> {

    private final FlamingWitherSkullModel model = new FlamingWitherSkullModel();
    private final ResourceLocation texture;
    private final ResourceLocation emissiveTexture;

    public FlamingWitherSkullRenderer(RenderManager renderManager, String textureName) {
        super(renderManager);
        String base = "textures/entity/flaming_wither_skull/" + textureName;
        texture = new ResourceLocation(Tags.MOD_ID, base + ".png");
        emissiveTexture = new ResourceLocation(Tags.MOD_ID, base + "_emissive.png");
    }

    @Override
    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
        float previousBrightnessX = OpenGlHelper.lastBrightnessX;
        float previousBrightnessY = OpenGlHelper.lastBrightnessY;
        GlStateManager.pushMatrix();
        try {
            float yaw = interpolateRotation(entity.prevRotationYaw, entity.rotationYaw, partialTicks);
            float pitch = entity.prevRotationPitch
                    + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
            GlStateManager.translate((float) x, (float) y, (float) z);
            GlStateManager.enableRescaleNormal();
            GlStateManager.scale(-1.8F, -1.8F, 1.8F);
            GlStateManager.enableAlpha();
            GlStateManager.enableCull();
            int packedLight = entity.getBrightnessForRender();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,
                    240.0F, packedLight / 65536);
            bindTexture(texture);
            model.render(entity, 0.0F, 0.0F, entity.ticksExisted + partialTicks,
                    yaw, pitch, 0.0625F);

            bindTexture(emissiveTexture);
            GlStateManager.enableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.disableCull();
            GlStateManager.depthMask(false);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ONE);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 61680.0F, 0.0F);
            Minecraft.getMinecraft().entityRenderer.setupFogColor(true);
            try {
                model.render(entity, 0.0F, 0.0F, entity.ticksExisted + partialTicks,
                        yaw, pitch, 0.0625F);
            } finally {
                Minecraft.getMinecraft().entityRenderer.setupFogColor(false);
            }
        } finally {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,
                    previousBrightnessX, previousBrightnessY);
            GlStateManager.depthMask(true);
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.disableRescaleNormal();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    private float interpolateRotation(float previous, float current, float partialTicks) {
        float difference;
        for (difference = current - previous; difference < -180.0F; difference += 360.0F) { }
        while (difference >= 180.0F) difference -= 360.0F;
        return previous + partialTicks * difference;
    }

    @Override
    protected ResourceLocation getEntityTexture(T entity) {
        return texture;
    }
}
