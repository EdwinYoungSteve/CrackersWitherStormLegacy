package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.client.model.SickenedVillagerModel;
import com.wdcftgg.witherstormmod.common.entity.SickenedEntities;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerVillagerArmor;

public final class SickenedVillagerRenderer
        extends SickenedMobRenderer<SickenedEntities.SickenedVillagerEntity> {

    public SickenedVillagerRenderer(RenderManager manager) {
        super(manager, new SickenedVillagerModel(), 0.5F,
                "textures/entity/sickened/sickened_villager.png");
        addLayer(new LayerVillagerArmor(this));
        addLayer(new SickenedVillagerProfessionLayer(this));
    }
}
