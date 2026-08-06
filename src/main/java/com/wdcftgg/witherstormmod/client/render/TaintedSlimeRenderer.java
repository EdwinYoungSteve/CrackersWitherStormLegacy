package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.client.model.TaintedSlimeModel;
import com.wdcftgg.witherstormmod.common.entity.SickenedEntities;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class TaintedSlimeRenderer extends RenderLiving<SickenedEntities.TaintedSlimeEntity> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            Tags.MOD_ID, "textures/entity/tainted_slime/tainted_slime.png");

    public TaintedSlimeRenderer(RenderManager manager) {
        super(manager, new TaintedSlimeModel(), 0.25F);
    }

    @Override
    protected ResourceLocation getEntityTexture(SickenedEntities.TaintedSlimeEntity entity) {
        return TEXTURE;
    }
}
