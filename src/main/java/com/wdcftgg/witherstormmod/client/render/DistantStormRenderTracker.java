package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.client.WitherStormClientConfig;
import com.wdcftgg.witherstormmod.common.entity.DistantStormPart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/** 补绘被 1.12 客户端区块收集阶段漏掉的远距风暴实体。 */
public final class DistantStormRenderTracker {
    private static final Set<Entity> RENDERED_THIS_FRAME =
            Collections.newSetFromMap(new IdentityHashMap<Entity, Boolean>());

    private DistantStormRenderTracker() {
    }

    public static void markRendered(Entity entity) {
        if (entity instanceof DistantStormPart) {
            RENDERED_THIS_FRAME.add(entity);
        }
    }

    public static void renderMissing(float partialTicks) {
        Minecraft minecraft = Minecraft.getMinecraft();
        try {
            if (!WitherStormClientConfig.distantRenderer
                    || minecraft.world == null
                    || minecraft.getRenderViewEntity() == null) {
                return;
            }

            RenderManager renderManager = minecraft.getRenderManager();
            RenderHelper.enableStandardItemLighting();
            try {
                for (Entity entity : minecraft.world.loadedEntityList) {
                    if (!(entity instanceof DistantStormPart)
                            || entity.isDead
                            || RENDERED_THIS_FRAME.contains(entity)
                            || !DistantProjection.isWithinFarPlane(entity.posX, entity.posY, entity.posZ,
                            renderManager.viewerPosX, renderManager.viewerPosY, renderManager.viewerPosZ)) {
                        continue;
                    }
                    renderManager.renderEntityStatic(entity, partialTicks, false);
                }
            } finally {
                RenderHelper.disableStandardItemLighting();
            }
        } finally {
            RENDERED_THIS_FRAME.clear();
        }
    }
}
