package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.client.WitherStormClientConfig;
import com.wdcftgg.witherstormmod.client.util.SpecialDay;
import com.wdcftgg.witherstormmod.common.entity.SickenedMobEntity;
import com.wdcftgg.witherstormmod.common.entity.SupplementalEntities;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class StormPartRenderer<T extends SickenedMobEntity> extends RenderLiving<T> {
    private final ResourceLocation texture;
    private final float renderScale;

    public StormPartRenderer(RenderManager manager, ModelBase model, float shadow, String texturePath, float renderScale) {
        super(manager, model, shadow);
        this.texture = new ResourceLocation(Tags.MOD_ID, texturePath);
        this.renderScale = renderScale;
    }

    @Override
    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
        DistantStormRenderTracker.markRendered(entity);
        boolean extendedProjection = DistantProjection.shouldUse(entity);
        if (extendedProjection) DistantProjection.push();
        DistantProjection.FogState previousFog = extendedProjection
                ? DistantProjection.pushDistantFog(entity, WitherStormClientConfig.distantFog) : null;
        try {
            super.doRender(entity, x, y, z, entityYaw, partialTicks);
            renderAfterLiving(entity, x, y, z, entityYaw, partialTicks);
        } finally {
            if (extendedProjection) DistantProjection.restoreFog(previousFog);
            if (extendedProjection) DistantProjection.pop();
        }
    }

    protected void renderAfterLiving(T entity, double x, double y, double z,
                                     float entityYaw, float partialTicks) {
    }

    @Override
    public boolean shouldRender(T entity, ICamera camera,
                                double cameraX, double cameraY, double cameraZ) {
        if (WitherStormClientConfig.distantRenderer) {
            return DistantProjection.isWithinFarPlane(entity.posX, entity.posY, entity.posZ,
                    cameraX, cameraY, cameraZ);
        }
        return super.shouldRender(entity, camera, cameraX, cameraY, cameraZ);
    }

    @Override
    protected ResourceLocation getEntityTexture(T entity) {
        if (entity instanceof SupplementalEntities.WitherStormHeadEntity
                && ((SupplementalEntities.WitherStormHeadEntity) entity).isHurt()) {
            return new ResourceLocation(Tags.MOD_ID,
                    "textures/entity/wither_storm_head/wither_storm_head_hurt.png");
        }
        if (entity instanceof SupplementalEntities.WitherStormSegmentEntity
                && SpecialDay.isAprilFoolsDate() && WitherStormClientConfig.aprilFools) {
            return new ResourceLocation(Tags.MOD_ID, "textures/misc/pink_wither_storm.png");
        }
        return texture;
    }

    @Override
    protected void preRenderCallback(T entity, float partialTickTime) {
        GlStateManager.scale(renderScale, renderScale, renderScale);
    }
}
