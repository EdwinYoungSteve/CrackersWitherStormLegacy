package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.common.init.ModPaintings;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPainting;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.util.ResourceLocation;

/** 用子类覆盖替代 mixin：AMULET 画作绑定运行时生成的图集，其余画作走原版。 */
public class WitherStormPaintingRenderer extends RenderPainting {

    public WitherStormPaintingRenderer(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityPainting entity) {
        if (entity.art == ModPaintings.AMULET) {
            AmuletPaintingAtlas.ensureLoaded();
            return AmuletPaintingAtlas.ATLAS;
        }
        return super.getEntityTexture(entity);
    }
}
