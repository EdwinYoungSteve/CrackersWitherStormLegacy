package com.wdcftgg.witherstormmod.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelTaintedSlimePort extends ModelBase {

    private final ModelRenderer body;
    private final ModelRenderer innerCube;
    private float animationX;
    private float animationY;
    private float animationZ;
    private float animationScale;

    public ModelTaintedSlimePort() {
        textureWidth = 48;
        textureHeight = 48;

        body = new ModelRenderer(this, 0, 0);
        body.setRotationPoint(0.0F, 24.0F, 0.0F);
        body.addBox(-5.0F, -10.0F, -5.0F, 10, 10, 10, 0.0F);

        innerCube = new ModelRenderer(this, 0, 20);
        innerCube.setRotationPoint(0.0F, 0.0F, 0.0F);
        innerCube.addBox(-3.0F, -3.0F, -3.0F, 6, 6, 6, -0.1F);
        innerCube.rotateAngleX = -0.7854F;
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch, float scale) {
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        body.render(scale);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, 19.0F * scale, 0.0F);
        GlStateManager.rotate(animationZ * (180.0F / (float) Math.PI), 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(animationY * (180.0F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(animationX * (180.0F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(animationScale, animationScale, animationScale);
        innerCube.render(scale);
        GlStateManager.popMatrix();
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        float seconds = (ageInTicks / 20.0F) % 8.125F;
        float[] rotation = sampleRotation(seconds);
        animationX = rotation[0] * 0.017453292F;
        animationY = rotation[1] * 0.017453292F;
        animationZ = rotation[2] * 0.017453292F;
        animationScale = sampleScale(seconds);
    }

    private static float[] sampleRotation(float time) {
        float[][] values = {
                {0.0F, 0.0F, 0.0F, 0.0F},
                {2.0F, 15.0F, 90.0F, 0.0F},
                {4.0F, 30.0F, 180.0F, 30.0F},
                {6.0F, 0.0F, 270.0F, 15.0F},
                {8.0F, 0.0F, 360.0F, 0.0F}
        };
        for (int index = 0; index < values.length - 1; index++) {
            float[] from = values[index];
            float[] to = values[index + 1];
            if (time <= to[0]) {
                float progress = MathHelper.clamp((time - from[0]) / (to[0] - from[0]), 0.0F, 1.0F);
                return new float[] {
                        lerp(from[1], to[1], progress),
                        lerp(from[2], to[2], progress),
                        lerp(from[3], to[3], progress)
                };
            }
        }
        return new float[] {0.0F, 360.0F, 0.0F};
    }

    private static float sampleScale(float time) {
        if (time <= 2.0F || time >= 6.0F) return 0.95F;
        if (time <= 4.0F) return lerp(0.95F, 0.85F, (time - 2.0F) / 2.0F);
        return lerp(0.85F, 0.95F, (time - 4.0F) / 2.0F);
    }

    private static float lerp(float from, float to, float progress) {
        return from + (to - from) * progress;
    }
}
