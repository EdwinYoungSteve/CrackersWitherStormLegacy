package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.common.entity.SickenedEntities;
import net.minecraft.client.model.ModelSnowMan;
import net.minecraft.client.renderer.entity.RenderManager;

public class RenderSickenedSnowGolem extends RenderSickenedMob<SickenedEntities.SickenedSnowGolem> {
    public RenderSickenedSnowGolem(RenderManager manager) {
        super(manager, new ModelSnowMan(), 0.5F, "textures/entity/sickened/sickened_snow_golem.png");
        addLayer(new LayerSickenedSnowGolemHead(this));
    }
}
