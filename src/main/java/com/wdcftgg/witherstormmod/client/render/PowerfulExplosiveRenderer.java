package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.common.entity.PowerfulExplosiveEntity;
import com.wdcftgg.witherstormmod.common.init.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class PowerfulExplosiveRenderer<T extends PowerfulExplosiveEntity> extends Render<T> {

    private final IBlockState renderedState;

    public PowerfulExplosiveRenderer(RenderManager renderManager, String blockName) {
        super(renderManager);
        shadowSize = 0.5F;
        renderedState = ModBlocks.get(blockName).getDefaultState();
    }

    @Override
    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
        BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y + 0.5F, (float) z);
        float remaining = entity.getFuse() - partialTicks + 1.0F;
        if (remaining < 10.0F) {
            float swelling = 1.0F - remaining / 10.0F;
            swelling = MathHelper.clamp(swelling, 0.0F, 1.0F);
            swelling *= swelling;
            swelling *= swelling;
            float scale = 1.0F + swelling * 5.0F;
            GlStateManager.scale(scale, scale, scale);
        }

        float flashAlpha = (1.0F - remaining / 100.0F) * 0.8F;
        bindEntityTexture(entity);
        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(-0.5F, -0.5F, 0.5F);
        dispatcher.renderBlockBrightness(renderedState, entity.getBrightness());
        GlStateManager.translate(0.0F, 0.0F, 1.0F);

        if (renderOutlines) {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(getTeamColor(entity));
            dispatcher.renderBlockBrightness(renderedState, 1.0F);
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        } else if (entity.getFuse() / 5 % 2 == 0) {
            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.DST_ALPHA);
            GlStateManager.color(1.0F, 1.0F, 1.0F, flashAlpha);
            GlStateManager.doPolygonOffset(-3.0F, -3.0F);
            GlStateManager.enablePolygonOffset();
            dispatcher.renderBlockBrightness(renderedState, 1.0F);
            GlStateManager.doPolygonOffset(0.0F, 0.0F);
            GlStateManager.disablePolygonOffset();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.enableTexture2D();
        }
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(T entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}
