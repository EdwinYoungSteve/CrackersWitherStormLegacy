package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.client.model.ModelWitherStormHeadPort;
import com.wdcftgg.witherstormmod.common.entity.SupplementalEntities;
import net.minecraft.client.renderer.entity.RenderManager;

public final class RenderWitherStormHeadPort extends RenderStormPart<SupplementalEntities.WitherStormHead> {
    public RenderWitherStormHeadPort(RenderManager manager) {
        super(manager, new ModelWitherStormHeadPort(), 1.5F,
                "textures/entity/wither_storm_head/wither_storm_head.png", 6.0F);
        addLayer(new LayerWitherStormHeadEyes(this));
    }
}
