package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.common.entity.SickenedMobEntity;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class SickenedMobRenderer<T extends SickenedMobEntity> extends RenderLiving<T> {

    private final ResourceLocation texture;
    private final float scaleX;
    private final float scaleY;
    private final float scaleZ;

    public SickenedMobRenderer(RenderManager renderManager, ModelBase model, float shadowSize, String texturePath) {
        this(renderManager, model, shadowSize, texturePath, 1.0F, 1.0F, 1.0F);
    }

    public SickenedMobRenderer(RenderManager renderManager, ModelBase model, float shadowSize, String texturePath,
                             float scaleX, float scaleY, float scaleZ) {
        super(renderManager, model, shadowSize);
        texture = new ResourceLocation(Tags.MOD_ID, texturePath);
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
        addLayer(new AbsorptionLayer(model));
        if (texturePath.startsWith("textures/entity/sickened/") && texturePath.endsWith(".png")) {
            String emissivePath = texturePath.substring(0, texturePath.length() - 4) + "_emissive.png";
            addLayer(new SickenedEmissiveLayer<T>(this, new ResourceLocation(Tags.MOD_ID, emissivePath)));
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

    @Override
    protected void applyRotations(T entity, float ageInTicks, float rotationYaw,
                                  float partialTicks) {
        if (entity.isConverting() && shakesWhileConverting(entity.getSickenedType())) {
            rotationYaw += (float) (Math.cos((entity.ticksExisted + partialTicks) * 3.25D)
                    * Math.PI * 0.4D);
        }
        super.applyRotations(entity, ageInTicks, rotationYaw, partialTicks);
    }

    private static boolean shakesWhileConverting(String type) {
        return "sickened_bee".equals(type)
                || "sickened_cat".equals(type)
                || "sickened_parrot".equals(type)
                || "sickened_creeper".equals(type)
                || "sickened_skeleton".equals(type)
                || "sickened_spider".equals(type)
                || "sickened_villager".equals(type)
                || "sickened_vindicator".equals(type)
                || "sickened_wolf".equals(type)
                || "sickened_zombie".equals(type);
    }
}
