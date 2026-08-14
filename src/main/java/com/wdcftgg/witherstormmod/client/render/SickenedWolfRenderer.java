package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.client.model.SickenedWolfModel;
import com.wdcftgg.witherstormmod.common.entity.SickenedEntities;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;

public final class SickenedWolfRenderer
        extends SickenedMobRenderer<SickenedEntities.SickenedWolfEntity> {

    public SickenedWolfRenderer(RenderManager manager) {
        super(manager, new SickenedWolfModel(), 0.45F,
                "textures/entity/sickened/sickened_wolf.png");
        layerRenderers.add(Math.max(0, layerRenderers.size() - 1),
                new SickenedWolfCollarLayer(this));
    }

    @Override
    protected float handleRotationFloat(SickenedEntities.SickenedWolfEntity entity,
                                        float partialTicks) {
        return entity.getTailRotation();
    }

    @Override
    public void doRender(SickenedEntities.SickenedWolfEntity entity,
                         double x, double y, double z, float entityYaw, float partialTicks) {
        if (entity.isWolfWet()) {
            float shade = entity.getBrightness() * entity.getShadingWhileWet(partialTicks);
            GlStateManager.color(shade, shade, shade);
        }
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }
}
