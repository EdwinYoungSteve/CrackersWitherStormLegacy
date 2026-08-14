package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.client.model.SickenedPhantomModel;
import com.wdcftgg.witherstormmod.common.entity.SickenedEntities;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;

public class SickenedPhantomRenderer extends SickenedMobRenderer<SickenedEntities.SickenedPhantomEntity> {

    public SickenedPhantomRenderer(RenderManager renderManager) {
        super(renderManager, new SickenedPhantomModel(), 0.75F,
                "textures/entity/sickened/sickened_phantom.png");
    }

    @Override
    protected void preRenderCallback(SickenedEntities.SickenedPhantomEntity entity, float partialTickTime) {
        float scale = 1.0F + 0.15F * entity.getPhantomSize();
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(0.0F, 1.3125F, 0.1875F);
    }

    @Override
    protected void applyRotations(SickenedEntities.SickenedPhantomEntity entity, float ageInTicks,
                                  float rotationYaw, float partialTicks) {
        super.applyRotations(entity, ageInTicks, rotationYaw, partialTicks);
        GlStateManager.rotate(entity.rotationPitch, 1.0F, 0.0F, 0.0F);
    }
}
