package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.client.WitherStormClientConfig;
import com.wdcftgg.witherstormmod.client.model.WitherStormPhaseModel;
import com.wdcftgg.witherstormmod.client.resources.WitherStormResourceConfigManager;
import com.wdcftgg.witherstormmod.common.entity.WitherStormEntity;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import org.lwjgl.opengl.GL11;

public final class WitherStormEmissiveLayer implements LayerRenderer<WitherStormEntity> {
    private final WitherStormRenderer renderer;

    public WitherStormEmissiveLayer(WitherStormRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(WitherStormEntity entity, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float netHeadYaw,
                              float headPitch, float scale) {
        if (entity.shouldFlicker() || entity.onGround && entity.isDeadOrPlayingDead()) return;
        renderer.bindTexture(WitherStormResourceConfigManager.INSTANCE
                .getTextureSetByPhase(entity.getPhase()).getEmissiveDecal());
        WitherStormPhaseModel model = (WitherStormPhaseModel) renderer.getMainModel();
        if (entity.getDeathTime() > 0) GlStateManager.depthFunc(GL11.GL_EQUAL);
        if (WitherStormClientConfig.renderEmissiveDecalForHeads) beginEmissive();
        model.renderHeads(entity, scale, head -> !entity.isHeadInjured(head));
        if (WitherStormClientConfig.renderEmissiveDecalForHeads) endEmissive(entity);

        if (LegacyRenderBufferer.INSTANCE.shouldUse()) {
            beginEmissive();
            model.renderMass(entity, scale);
            endEmissive(entity);
        }
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
    }

    private static void beginEmissive() {
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE);
        GlStateManager.depthMask(true);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 61680.0F, 0.0F);
    }

    private void endEmissive(WitherStormEntity entity) {
        renderer.setLightmap(entity);
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
