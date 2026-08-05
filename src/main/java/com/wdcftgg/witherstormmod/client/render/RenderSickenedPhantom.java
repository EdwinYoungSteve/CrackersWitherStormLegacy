package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.client.model.ModelSickenedPhantomPort;
import com.wdcftgg.witherstormmod.common.entity.SickenedEntities;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;

public class RenderSickenedPhantom extends RenderSickenedMob<SickenedEntities.SickenedPhantom> {

    public RenderSickenedPhantom(RenderManager renderManager) {
        super(renderManager, new ModelSickenedPhantomPort(), 0.75F,
                "textures/entity/sickened/sickened_phantom.png");
    }

    @Override
    protected void preRenderCallback(SickenedEntities.SickenedPhantom entity, float partialTickTime) {
        GlStateManager.translate(0.0F, 1.3125F, 0.1875F);
    }

    @Override
    protected void applyRotations(SickenedEntities.SickenedPhantom entity, float ageInTicks,
                                  float rotationYaw, float partialTicks) {
        super.applyRotations(entity, ageInTicks, rotationYaw, partialTicks);
        GlStateManager.rotate(entity.rotationPitch, 1.0F, 0.0F, 0.0F);
    }
}
