package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.client.model.WitheredSymbiontModel;
import com.wdcftgg.witherstormmod.common.entity.SickenedEntities;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.util.ResourceLocation;

public final class WitheredSymbiontRenderer extends RenderLiving<SickenedEntities.WitheredSymbiontEntity> {
    public WitheredSymbiontRenderer(RenderManager renderManager) {
        super(renderManager, new WitheredSymbiontModel(), WitheredSymbiontRenderProfile.SHADOW_SIZE);
        addLayer(new WitheredSymbiontEyesLayer(this));
        addLayer(new WitheredSymbiontTearLayer(this));
        addLayer(new LayerHeldItem(this));
        addLayer(new WitheredSymbiontArmorLayer(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(SickenedEntities.WitheredSymbiontEntity entity) {
        return entity.hasCustomName()
                && WitheredSymbiontRenderProfile.usesEasterEggTexture(entity.getCustomNameTag())
                ? WitheredSymbiontRenderProfile.EASTER_EGG_TEXTURE
                : WitheredSymbiontRenderProfile.BASE_TEXTURE;
    }

    @Override
    protected void preRenderCallback(SickenedEntities.WitheredSymbiontEntity entity, float partialTickTime) {
        float scale = WitheredSymbiontRenderProfile.MODEL_SCALE;
        GlStateManager.scale(scale, scale, scale);
    }

    @Override
    protected void applyRotations(SickenedEntities.WitheredSymbiontEntity entity, float ageInTicks,
                                  float rotationYaw, float partialTicks) {
        super.applyRotations(entity, ageInTicks, rotationYaw, partialTicks);
        float walkSpeed = entity.prevLimbSwingAmount
                + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTicks;
        float walkPosition = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks);
        GlStateManager.rotate(WitheredSymbiontRenderProfile.getWalkWobbleDegrees(
                walkPosition, walkSpeed), 0.0F, 0.0F, 1.0F);
    }
}
