package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.common.entity.SickenedEntities;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;

public final class SickenedWolfCollarLayer
        implements LayerRenderer<SickenedEntities.SickenedWolfEntity> {
    private static final ResourceLocation COLLAR_TEXTURE =
            new ResourceLocation("textures/entity/wolf/wolf_collar.png");
    private final SickenedWolfRenderer renderer;

    public SickenedWolfCollarLayer(SickenedWolfRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(SickenedEntities.SickenedWolfEntity entity,
                              float limbSwing, float limbSwingAmount, float partialTicks,
                              float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (!entity.isSickenedTamed() || entity.isInvisible()) return;
        renderer.bindTexture(COLLAR_TEXTURE);
        float[] color = entity.getCollarColor().getColorComponentValues();
        GlStateManager.color(color[0], color[1], color[2]);
        renderer.getMainModel().render(entity, limbSwing, limbSwingAmount, ageInTicks,
                netHeadYaw, headPitch, scale);
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
