package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.client.model.SickenedIronGolemModel;
import com.wdcftgg.witherstormmod.common.entity.SickenedEntities;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;

public class SickenedIronGolemRenderer extends SickenedMobRenderer<SickenedEntities.SickenedIronGolemEntity> {
    public SickenedIronGolemRenderer(RenderManager manager) {
        super(manager, new SickenedIronGolemModel(), 0.7F, "textures/entity/sickened/sickened_iron_golem.png");
        addLayer(new SickenedIronGolemCracksLayer(this));
    }

    @Override
    protected void applyRotations(SickenedEntities.SickenedIronGolemEntity entity, float ageInTicks, float yaw, float partialTicks) {
        super.applyRotations(entity, ageInTicks, yaw, partialTicks);
        if (entity.limbSwingAmount >= 0.01F) {
            float walk = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks) + 6.0F;
            float wave = (Math.abs(walk % 13.0F - 6.5F) - 3.25F) / 3.25F;
            GlStateManager.rotate(6.5F * wave, 0.0F, 0.0F, 1.0F);
        }
    }
}
