package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.common.entity.SickenedEntities;
import net.minecraft.client.model.ModelCreeper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.math.MathHelper;

public class SickenedCreeperRenderer extends SickenedMobRenderer<SickenedEntities.SickenedCreeperEntity> {
    public SickenedCreeperRenderer(RenderManager manager) {
        super(manager, new ModelCreeper(), 0.5F, "textures/entity/sickened/sickened_creeper.png");
        addLayer(new SickenedCreeperChargeLayer(this));
    }

    @Override
    protected void preRenderCallback(SickenedEntities.SickenedCreeperEntity entity, float partialTicks) {
        float swelling = entity.getCreeperFlashIntensity(partialTicks);
        float pulse = 1.0F + MathHelper.sin(swelling * 100.0F) * swelling * 0.01F;
        swelling = MathHelper.clamp(swelling, 0.0F, 1.0F);
        swelling *= swelling;
        swelling *= swelling;
        float horizontal = (1.0F + swelling * 0.4F) * pulse;
        float vertical = (1.0F + swelling * 0.1F) / pulse;
        GlStateManager.scale(horizontal, vertical, horizontal);
    }

    @Override
    protected int getColorMultiplier(SickenedEntities.SickenedCreeperEntity entity, float brightness, float partialTicks) {
        float swelling = entity.getCreeperFlashIntensity(partialTicks);
        if ((int) (swelling * 10.0F) % 2 == 0) {
            return 0;
        }
        int alpha = MathHelper.clamp((int) (swelling * 0.2F * 255.0F), 0, 255);
        return alpha << 24 | 0x30FFFFFF;
    }
}
