package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.common.entity.SickenedEntities;
import com.wdcftgg.witherstormmod.common.init.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelSnowMan;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.item.ItemStack;

public class SickenedSnowGolemHeadLayer implements LayerRenderer<SickenedEntities.SickenedSnowGolemEntity> {
    private final SickenedSnowGolemRenderer renderer;

    public SickenedSnowGolemHeadLayer(SickenedSnowGolemRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(SickenedEntities.SickenedSnowGolemEntity entity, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (entity.isInvisible()) return;
        GlStateManager.pushMatrix();
        ((ModelSnowMan) renderer.getMainModel()).head.postRender(0.0625F);
        GlStateManager.translate(0.0F, -0.34375F, 0.0F);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(0.625F, -0.625F, -0.625F);
        Minecraft.getMinecraft().getItemRenderer().renderItem(entity,
                new ItemStack(ModBlocks.get("tainted_carved_pumpkin")), ItemCameraTransforms.TransformType.HEAD);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures() { return true; }
}
