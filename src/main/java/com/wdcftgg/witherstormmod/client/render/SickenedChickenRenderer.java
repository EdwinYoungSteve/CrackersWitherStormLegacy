package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.common.entity.SickenedEntities;
import net.minecraft.client.model.ModelChicken;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.math.MathHelper;

public class SickenedChickenRenderer extends SickenedMobRenderer<SickenedEntities.SickenedChickenEntity> {
    public SickenedChickenRenderer(RenderManager renderManager) {
        super(renderManager, new ModelChicken(), 0.3F,
                "textures/entity/sickened/sickened_chicken.png");
    }

    @Override
    protected float handleRotationFloat(SickenedEntities.SickenedChickenEntity chicken, float partialTicks) {
        float flap = chicken.previousFlap
                + (chicken.wingRotation - chicken.previousFlap) * partialTicks;
        float flapSpeed = chicken.previousFlapSpeed
                + (chicken.destPos - chicken.previousFlapSpeed) * partialTicks;
        return (MathHelper.sin(flap) + 1.0F) * flapSpeed;
    }
}
