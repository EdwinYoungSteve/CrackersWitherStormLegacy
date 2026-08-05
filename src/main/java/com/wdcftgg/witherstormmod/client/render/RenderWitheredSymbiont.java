package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.client.model.ModelWitheredSymbiontPort;
import com.wdcftgg.witherstormmod.common.entity.SickenedEntities;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.util.ResourceLocation;

public final class RenderWitheredSymbiont extends RenderLiving<SickenedEntities.WitheredSymbiont> {
    public RenderWitheredSymbiont(RenderManager renderManager) {
        super(renderManager, new ModelWitheredSymbiontPort(), WitheredSymbiontRenderProfile.SHADOW_SIZE);
        addLayer(new LayerWitheredSymbiontEyes(this));
        addLayer(new LayerWitheredSymbiontTear(this));
        addLayer(new LayerHeldItem(this));
        addLayer(new LayerWitheredSymbiontArmor(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(SickenedEntities.WitheredSymbiont entity) {
        return entity.hasCustomName()
                && WitheredSymbiontRenderProfile.usesEasterEggTexture(entity.getCustomNameTag())
                ? WitheredSymbiontRenderProfile.EASTER_EGG_TEXTURE
                : WitheredSymbiontRenderProfile.BASE_TEXTURE;
    }

    @Override
    protected void preRenderCallback(SickenedEntities.WitheredSymbiont entity, float partialTickTime) {
        float scale = WitheredSymbiontRenderProfile.MODEL_SCALE;
        GlStateManager.scale(scale, scale, scale);
    }

    @Override
    protected void applyRotations(SickenedEntities.WitheredSymbiont entity, float ageInTicks,
                                  float rotationYaw, float partialTicks) {
        super.applyRotations(entity, ageInTicks, rotationYaw, partialTicks);
        float walkSpeed = entity.prevLimbSwingAmount
                + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTicks;
        float walkPosition = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks);
        GlStateManager.rotate(WitheredSymbiontRenderProfile.getWalkWobbleDegrees(
                walkPosition, walkSpeed), 0.0F, 0.0F, 1.0F);
    }
}
