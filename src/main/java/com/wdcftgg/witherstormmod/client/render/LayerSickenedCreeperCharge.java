package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.common.entity.SickenedEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelCreeper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;

public class LayerSickenedCreeperCharge implements LayerRenderer<SickenedEntities.SickenedCreeper> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/creeper/creeper_armor.png");
    private final RenderSickenedCreeper renderer;
    private final ModelCreeper model = new ModelCreeper(2.0F);

    public LayerSickenedCreeperCharge(RenderSickenedCreeper renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(SickenedEntities.SickenedCreeper entity, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (!entity.isPowered()) return;
        boolean invisible = entity.isInvisible();
        GlStateManager.depthMask(!invisible);
        renderer.bindTexture(TEXTURE);
        GlStateManager.matrixMode(5890);
        GlStateManager.loadIdentity();
        float offset = entity.ticksExisted + partialTicks;
        GlStateManager.translate(offset * 0.01F, offset * 0.01F, 0.0F);
        GlStateManager.matrixMode(5888);
        GlStateManager.enableBlend();
        GlStateManager.color(0.5F, 0.5F, 0.5F, 1.0F);
        GlStateManager.disableLighting();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        model.setModelAttributes(renderer.getMainModel());
        Minecraft.getMinecraft().entityRenderer.setupFogColor(true);
        model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        Minecraft.getMinecraft().entityRenderer.setupFogColor(false);
        GlStateManager.matrixMode(5890);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(5888);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(invisible);
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
