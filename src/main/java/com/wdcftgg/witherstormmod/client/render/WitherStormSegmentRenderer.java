package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.client.WitherStormClientConfig;
import com.wdcftgg.witherstormmod.client.model.WitherStormSegmentModel;
import com.wdcftgg.witherstormmod.client.resources.WitherStormResourceConfigManager;
import com.wdcftgg.witherstormmod.client.util.SpecialDay;
import com.wdcftgg.witherstormmod.common.entity.SupplementalEntities;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public final class WitherStormSegmentRenderer
        extends StormPartRenderer<SupplementalEntities.WitherStormSegmentEntity> {
    private static final ResourceLocation EXPLODING_TEXTURE = new ResourceLocation(Tags.MOD_ID,
            "textures/entity/wither_storm/wither_storm_exploding.png");

    public WitherStormSegmentRenderer(RenderManager manager) {
        super(manager, new WitherStormSegmentModel(), 0.0F,
                "textures/entity/wither_storm/wither_storm.png", 2.0F);
        addLayer(new WitherStormSegmentHurtLayer(this));
        addLayer(new WitherStormSegmentEmissiveLayer(this));
        addLayer(new WitherStormSegmentSantaHatLayer(this));
        addLayer(new WitherStormSegmentPulseLayer(this));
    }

    @Override
    protected void renderAfterLiving(SupplementalEntities.WitherStormSegmentEntity entity,
                                     double x, double y, double z,
                                     float entityYaw, float partialTicks) {
        if (entity.getDeathTime() > 0) {
            WitherStormRenderer.renderDeathWireframe(entity.getDeathTime(),
                    entity.getUnmodifiedHeight(), x, y, z, partialTicks);
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(SupplementalEntities.WitherStormSegmentEntity entity) {
        if (entity.getDeathTime() > 0) return EXPLODING_TEXTURE;
        return getBaseTexture(entity);
    }

    private ResourceLocation getBaseTexture(SupplementalEntities.WitherStormSegmentEntity entity) {
        int invulnerableTicks = entity.getInvulnerableTicks();
        if (invulnerableTicks > 0
                && (invulnerableTicks > 80 || invulnerableTicks / 5 % 2 != 1)) {
            return WitherStormResourceConfigManager.INSTANCE
                    .getTextureSetByPhase(entity.getPhase()).getInvulnerable();
        }
        if (SpecialDay.isAprilFoolsDate() && WitherStormClientConfig.aprilFools) {
            return new ResourceLocation(Tags.MOD_ID, "textures/misc/pink_wither_storm.png");
        }
        return WitherStormResourceConfigManager.INSTANCE
                .getTextureSetByPhase(entity.getPhase()).getMain();
    }

    @Override
    protected void renderModel(SupplementalEntities.WitherStormSegmentEntity entity,
                               float limbSwing, float limbSwingAmount, float ageInTicks,
                               float netHeadYaw, float headPitch, float scaleFactor) {
        if (entity.getDeathTime() <= 0) {
            super.renderModel(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw,
                    headPitch, scaleFactor);
            return;
        }
        WitherStormRenderer.renderDissolvingModel(entity, entity.getDeathTime(), mainModel,
                getBaseTexture(entity), EXPLODING_TEXTURE, limbSwing, limbSwingAmount,
                ageInTicks, netHeadYaw, headPitch, scaleFactor);
    }

    @Override
    protected void preRenderCallback(SupplementalEntities.WitherStormSegmentEntity entity,
                                     float partialTickTime) {
        float scale = 2.0F;
        int shrinkingTicks = Math.max(0, entity.getInvulnerableTicks() - 750);
        if (shrinkingTicks > 0) {
            int duration = Math.max(1, entity.getStartingInvulnerableTicks() - 750);
            scale -= (shrinkingTicks - partialTickTime) / duration * 0.5F;
        }
        GlStateManager.scale(scale, scale, scale);
    }

    @Override
    protected void applyRotations(SupplementalEntities.WitherStormSegmentEntity entity,
                                  float ageInTicks, float rotationYaw, float partialTicks) {
        GlStateManager.rotate(180.0F - rotationYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(entity.getBodyXRotation(partialTicks), 1.0F, 0.0F, 0.0F);
    }
}
