package com.wdcftgg.witherstormmod.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelSickenedBeePort extends ModelBase {
    private final ModelRenderer bone;
    private final ModelRenderer rightWing;
    private final ModelRenderer leftWing;
    private final ModelRenderer frontLegs;
    private final ModelRenderer middleLegs;
    private final ModelRenderer backLegs;
    private final ModelRenderer leftAntenna;
    private final ModelRenderer rightAntenna;

    public ModelSickenedBeePort() {
        textureWidth = 64;
        textureHeight = 64;
        bone = new ModelRenderer(this);
        bone.setRotationPoint(0.0F, 19.0F, 0.0F);
        ModelRenderer body = new ModelRenderer(this, 0, 0);
        body.addBox(-3.5F, -4.0F, -5.0F, 7, 7, 10);
        bone.addChild(body);
        ModelRenderer stinger = new ModelRenderer(this, 26, 7);
        stinger.addBox(0.0F, -1.0F, 5.0F, 0, 1, 2);
        body.addChild(stinger);
        leftAntenna = new ModelRenderer(this, 2, 0);
        leftAntenna.addBox(1.5F, -2.0F, -3.0F, 1, 2, 3);
        leftAntenna.setRotationPoint(0.0F, -2.0F, -5.0F);
        body.addChild(leftAntenna);
        rightAntenna = new ModelRenderer(this, 2, 3);
        rightAntenna.addBox(-2.5F, -2.0F, -3.0F, 1, 2, 3);
        rightAntenna.setRotationPoint(0.0F, -2.0F, -5.0F);
        body.addChild(rightAntenna);
        rightWing = new ModelRenderer(this, 0, 18);
        rightWing.addBox(-9.0F, 0.0F, 0.0F, 9, 0, 6, 0.001F);
        rightWing.setRotationPoint(-1.5F, -4.0F, -3.0F);
        rightWing.rotateAngleY = -0.2618F;
        bone.addChild(rightWing);
        leftWing = new ModelRenderer(this, 0, 18);
        leftWing.mirror = true;
        leftWing.addBox(0.0F, 0.0F, 0.0F, 9, 0, 6, 0.001F);
        leftWing.setRotationPoint(1.5F, -4.0F, -3.0F);
        leftWing.rotateAngleY = 0.2618F;
        bone.addChild(leftWing);
        frontLegs = flatLegs(26, 1, -2.0F);
        middleLegs = flatLegs(26, 3, 0.0F);
        backLegs = flatLegs(26, 5, 2.0F);
    }

    private ModelRenderer flatLegs(int u, int v, float z) {
        ModelRenderer legs = new ModelRenderer(this, u, v);
        legs.addBox(-5.0F, 0.0F, 0.0F, 7, 2, 0);
        legs.setRotationPoint(1.5F, 3.0F, z);
        bone.addChild(legs);
        return legs;
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch, float scale) {
        boolean resting = entity.onGround && entity.motionX * entity.motionX + entity.motionY * entity.motionY
                + entity.motionZ * entity.motionZ < 1.0E-7D;
        rightWing.rotateAngleX = 0.0F;
        leftAntenna.rotateAngleX = rightAntenna.rotateAngleX = 0.0F;
        bone.rotateAngleX = bone.rotateAngleY = bone.rotateAngleZ = 0.0F;
        bone.rotationPointY = 19.0F;
        if (resting) {
            rightWing.rotateAngleY = -0.2618F;
            rightWing.rotateAngleZ = 0.0F;
            leftWing.rotateAngleX = 0.0F;
            leftWing.rotateAngleY = 0.2618F;
            leftWing.rotateAngleZ = 0.0F;
            frontLegs.rotateAngleX = middleLegs.rotateAngleX = backLegs.rotateAngleX = 0.0F;
        } else {
            float flap = ageInTicks * 120.32113F * 0.017453292F;
            rightWing.rotateAngleY = 0.0F;
            rightWing.rotateAngleZ = MathHelper.cos(flap) * (float) Math.PI * 0.15F;
            leftWing.rotateAngleX = rightWing.rotateAngleX;
            leftWing.rotateAngleY = rightWing.rotateAngleY;
            leftWing.rotateAngleZ = -rightWing.rotateAngleZ;
            frontLegs.rotateAngleX = middleLegs.rotateAngleX = backLegs.rotateAngleX = (float) Math.PI / 4.0F;
            float hover = MathHelper.cos(ageInTicks * 0.18F);
            bone.rotateAngleX = 0.1F + hover * (float) Math.PI * 0.025F;
            leftAntenna.rotateAngleX = rightAntenna.rotateAngleX = hover * (float) Math.PI * 0.03F;
            frontLegs.rotateAngleX = -hover * (float) Math.PI * 0.1F + (float) Math.PI / 8.0F;
            backLegs.rotateAngleX = -hover * (float) Math.PI * 0.05F + (float) Math.PI / 4.0F;
            bone.rotationPointY = 19.0F - MathHelper.cos(ageInTicks * 0.18F) * 0.9F;
        }
        bone.render(scale);
    }
}
