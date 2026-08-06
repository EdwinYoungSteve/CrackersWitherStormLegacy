package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.common.entity.SickenedMobEntity;
import com.wdcftgg.witherstormmod.common.entity.SupplementalEntities;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
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
    protected ResourceLocation getEntityTexture(T entity) {
        if (entity instanceof SupplementalEntities.WitherStormHeadEntity
                && ((SupplementalEntities.WitherStormHeadEntity) entity).isHurt()) {
            return new ResourceLocation(Tags.MOD_ID,
                    "textures/entity/wither_storm_head/wither_storm_head_hurt.png");
        }
        return texture;
    }

    @Override
    protected void preRenderCallback(T entity, float partialTickTime) {
        GlStateManager.scale(renderScale, renderScale, renderScale);
    }
}
