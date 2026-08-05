package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.common.entity.EntitySickenedMob;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class RenderSickenedMob<T extends EntitySickenedMob> extends RenderLiving<T> {

    private final ResourceLocation texture;
    private final float scaleX;
    private final float scaleY;
    private final float scaleZ;

    public RenderSickenedMob(RenderManager renderManager, ModelBase model, float shadowSize, String texturePath) {
        this(renderManager, model, shadowSize, texturePath, 1.0F, 1.0F, 1.0F);
    }

    public RenderSickenedMob(RenderManager renderManager, ModelBase model, float shadowSize, String texturePath,
                             float scaleX, float scaleY, float scaleZ) {
        super(renderManager, model, shadowSize);
        texture = new ResourceLocation(Tags.MOD_ID, texturePath);
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
        if (texturePath.startsWith("textures/entity/sickened/") && texturePath.endsWith(".png")) {
            String emissivePath = texturePath.substring(0, texturePath.length() - 4) + "_emissive.png";
            addLayer(new LayerSickenedEmissive<T>(this, new ResourceLocation(Tags.MOD_ID, emissivePath)));
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(T entity) {
        return texture;
    }

    @Override
    protected void preRenderCallback(T entity, float partialTickTime) {
        GlStateManager.scale(scaleX, scaleY, scaleZ);
    }
}
