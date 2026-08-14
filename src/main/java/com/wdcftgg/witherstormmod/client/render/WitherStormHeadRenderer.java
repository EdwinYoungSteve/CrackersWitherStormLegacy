package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.client.model.WitherStormHeadModel;
import com.wdcftgg.witherstormmod.common.entity.SupplementalEntities;
import net.minecraft.client.renderer.entity.RenderManager;

public final class WitherStormHeadRenderer extends StormPartRenderer<SupplementalEntities.WitherStormHeadEntity> {
    public WitherStormHeadRenderer(RenderManager manager) {
        // 上游 = 渲染器 scale(2) + 模型内 HeadModel.scale(3.0)。渲染器级保持 2.0，
        // 模型级 3.0 由 WitherStormHeadModel 在旋转前应用；两者之间的 -1.501 平移
        // 由 1.12 RenderLivingBase.prepareScale 内置。此前把 3.0 合并为渲染器 6.0
        // 会错误放大平移，使模型抬高约 6 格并穿入墙内。
        super(manager, new WitherStormHeadModel(), 3.5F,
                "textures/entity/wither_storm_head/wither_storm_head.png", 2.0F);
        addLayer(new WitherStormHeadEyesLayer(this));
    }
}
