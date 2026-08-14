package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.client.model.SickenedVillagerModel;
import com.wdcftgg.witherstormmod.common.entity.SickenedEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;

public final class SickenedVillagerProfessionLayer
        implements LayerRenderer<SickenedEntities.SickenedVillagerEntity> {
    private final SickenedMobRenderer<SickenedEntities.SickenedVillagerEntity> renderer;
    private final SickenedVillagerModel professionModel = new SickenedVillagerModel();

    public SickenedVillagerProfessionLayer(
            SickenedMobRenderer<SickenedEntities.SickenedVillagerEntity> renderer) {
        this.renderer = renderer;
        professionModel.bipedHead.showModel = false;
        professionModel.bipedRightArm.showModel = false;
        professionModel.bipedLeftArm.showModel = false;
        professionModel.bipedRightLeg.showModel = false;
        professionModel.bipedLeftLeg.showModel = false;
    }

    @Override
    public void doRenderLayer(SickenedEntities.SickenedVillagerEntity entity,
                              float limbSwing, float limbSwingAmount, float partialTicks,
                              float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(entity.getProfessionSkin());
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        professionModel.setModelAttributes(renderer.getMainModel());
        professionModel.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
        professionModel.render(entity, limbSwing, limbSwingAmount, ageInTicks,
                netHeadYaw, headPitch, scale);
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
