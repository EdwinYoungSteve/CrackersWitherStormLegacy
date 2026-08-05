package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.common.entity.SickenedEntities;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;

public class LayerSickenedIronGolemCracks implements LayerRenderer<SickenedEntities.SickenedIronGolem> {
    private static final ResourceLocation LOW = texture("low");
    private static final ResourceLocation MEDIUM = texture("medium");
    private static final ResourceLocation HIGH = texture("high");
    private final RenderSickenedIronGolem renderer;

    public LayerSickenedIronGolemCracks(RenderSickenedIronGolem renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(SickenedEntities.SickenedIronGolem entity, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (entity.isInvisible()) return;
        float health = entity.getHealth() / entity.getMaxHealth();
        ResourceLocation texture = health < 0.25F ? HIGH : health < 0.5F ? MEDIUM : health < 0.75F ? LOW : null;
        if (texture == null) return;
        renderer.bindTexture(texture);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        renderer.getMainModel().render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        GlStateManager.disableBlend();
    }

    private static ResourceLocation texture(String level) {
        return new ResourceLocation(Tags.MOD_ID, "textures/entity/sickened/sickened_iron_golem_crackiness_" + level + ".png");
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
