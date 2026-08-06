package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.common.entity.SupplementalEntities;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import java.util.Map;

public class BlockClusterRenderer extends Render<SupplementalEntities.BlockClusterEntity> {
    public BlockClusterRenderer(RenderManager renderManager) {
        super(renderManager);
        shadowSize = 0.5F;
    }

    @Override
    public void doRender(SupplementalEntities.BlockClusterEntity entity, double x, double y, double z,
                         float entityYaw, float partialTicks) {
        if (entity.getBlocks().isEmpty()) return;
        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.translate(0.0F, 0.5F, 0.0F);
        float shakeX = entity.getShakeX(partialTicks);
        float shakeZ = entity.getShakeZ(partialTicks);
        GlStateManager.rotate(-entity.getClusterYaw(partialTicks) - shakeX * 50.0F,
                0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(entity.getClusterPitch(partialTicks) - shakeZ * 30.0F,
                1.0F, 0.0F, 0.0F);
        GlStateManager.translate(0.0F, -0.5F, 0.0F);
        GlStateManager.translate(getParityOffset(entity.getClusterSizeX()),
                getParityOffset(entity.getClusterSizeY()), getParityOffset(entity.getClusterSizeZ()));

        float fade = entity.getFadeAmount(partialTicks);
        float scale = getFadeScale(fade);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(shakeX, 0.0F, shakeZ);
        GlStateManager.color(Math.min(1.0F, fade + 0.4F), fade,
                Math.min(1.0F, fade + 0.2F), 1.0F);
        for (Map.Entry<BlockPos, IBlockState> entry : entity.getBlocks().entrySet()) {
            BlockPos offset = entry.getKey();
            GlStateManager.pushMatrix();
            GlStateManager.translate(offset.getX(), offset.getY() + entity.getClusterSizeY() / 2.0F,
                    offset.getZ());
            dispatcher.renderBlockBrightness(entry.getValue(), entity.getBrightness());
            GlStateManager.popMatrix();
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    static float getParityOffset(float size) {
        return -0.5F - (Math.round(size) % 2 == 0 ? 0.5F : 0.0F);
    }

    static float getFadeScale(float fade) {
        return Math.max(0.8F, fade * 0.5F + 0.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(SupplementalEntities.BlockClusterEntity entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}
