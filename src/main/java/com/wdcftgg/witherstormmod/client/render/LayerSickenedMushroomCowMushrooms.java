package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.common.entity.SickenedEntities;
import com.wdcftgg.witherstormmod.common.init.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelCow;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.block.state.IBlockState;

public class LayerSickenedMushroomCowMushrooms implements LayerRenderer<SickenedEntities.SickenedMushroomCow> {
    private final RenderSickenedMushroomCow renderer;

    public LayerSickenedMushroomCowMushrooms(RenderSickenedMushroomCow renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(SickenedEntities.SickenedMushroomCow entity, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (entity.isChild() || entity.isInvisible()) return;
        IBlockState mushroom = ModBlocks.get("tainted_mushroom").getDefaultState();
        BlockRendererDispatcher blocks = Minecraft.getMinecraft().getBlockRendererDispatcher();
        renderer.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.enableCull();
        GlStateManager.cullFace(GlStateManager.CullFace.FRONT);
        GlStateManager.pushMatrix();
        GlStateManager.scale(1.0F, -1.0F, 1.0F);
        GlStateManager.translate(0.2F, 0.35F, 0.5F);
        GlStateManager.rotate(42.0F, 0.0F, 1.0F, 0.0F);
        renderBlock(blocks, mushroom, -0.5F, -0.5F, 0.5F);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.1F, 0.0F, -0.6F);
        GlStateManager.rotate(42.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(-0.5F, -0.5F, 0.5F);
        blocks.renderBlockBrightness(mushroom, 1.0F);
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        ((ModelCow) renderer.getMainModel()).head.postRender(0.0625F);
        GlStateManager.scale(1.0F, -1.0F, 1.0F);
        GlStateManager.translate(0.0F, 0.7F, -0.2F);
        GlStateManager.rotate(12.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(-0.5F, -0.5F, 0.5F);
        blocks.renderBlockBrightness(mushroom, 1.0F);
        GlStateManager.popMatrix();
        GlStateManager.cullFace(GlStateManager.CullFace.BACK);
        GlStateManager.disableCull();
    }

    private void renderBlock(BlockRendererDispatcher renderer, IBlockState state, float x, float y, float z) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        renderer.renderBlockBrightness(state, 1.0F);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures() { return true; }
}
