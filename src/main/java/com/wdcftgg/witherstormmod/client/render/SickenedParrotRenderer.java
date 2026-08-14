package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.client.model.SickenedParrotModel;
import com.wdcftgg.witherstormmod.common.entity.SickenedEntities;
import net.minecraft.client.renderer.entity.RenderManager;

public final class SickenedParrotRenderer
        extends SickenedMobRenderer<SickenedEntities.SickenedParrotEntity> {

    public SickenedParrotRenderer(RenderManager manager) {
        super(manager, new SickenedParrotModel(), 0.3F,
                "textures/entity/sickened/sickened_parrot.png");
    }

    @Override
    protected float handleRotationFloat(SickenedEntities.SickenedParrotEntity entity,
                                        float partialTicks) {
        return entity.getFlapBob(partialTicks);
    }
}
