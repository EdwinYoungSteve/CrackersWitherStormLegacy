package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.common.entity.SickenedEntities;
import net.minecraft.client.model.ModelCow;
import net.minecraft.client.renderer.entity.RenderManager;

public class SickenedMushroomCowRenderer extends SickenedMobRenderer<SickenedEntities.SickenedMushroomCowEntity> {
    public SickenedMushroomCowRenderer(RenderManager manager) {
        super(manager, new ModelCow(), 0.7F, "textures/entity/sickened/sickened_mushroom_cow.png");
        addLayer(new SickenedMushroomCowMushroomsLayer(this));
    }
}
