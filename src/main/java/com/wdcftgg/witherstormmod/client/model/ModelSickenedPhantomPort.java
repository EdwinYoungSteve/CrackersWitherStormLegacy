package com.wdcftgg.witherstormmod.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelSickenedPhantomPort extends ModelBase {
    private final ModelRenderer body;
    private final ModelRenderer tailBase;
    private final ModelRenderer tailTip;
    private final ModelRenderer leftWingBase;
    private final ModelRenderer leftWingTip;
    private final ModelRenderer rightWingBase;
    private final ModelRenderer rightWingTip;

    public ModelSickenedPhantomPort() {
        textureWidth = 64;
        textureHeight = 64;
        body = new ModelRenderer(this, 0, 8);
        body.addBox(-3.0F, -2.0F, -8.0F, 5, 3, 9);
        body.rotateAngleX = -0.1F;
        tailBase = new ModelRenderer(this, 3, 20);
        tailBase.addBox(-2.0F, 0.0F, 0.0F, 3, 2, 6);
        tailBase.setRotationPoint(0.0F, -2.0F, 1.0F);
        body.addChild(tailBase);
        tailTip = new ModelRenderer(this, 4, 29);
        tailTip.addBox(-1.0F, 0.0F, 0.0F, 1, 1, 6);
        tailTip.setRotationPoint(0.0F, 0.5F, 6.0F);
        tailBase.addChild(tailTip);
        leftWingBase = new ModelRenderer(this, 23, 12);
        leftWingBase.addBox(0.0F, 0.0F, 0.0F, 6, 2, 9);
        leftWingBase.setRotationPoint(2.0F, -2.0F, -8.0F);
        leftWingBase.rotateAngleZ = 0.1F;
        body.addChild(leftWingBase);
        leftWingTip = new ModelRenderer(this, 16, 24);
        leftWingTip.addBox(0.0F, 0.0F, 0.0F, 13, 1, 9);
        leftWingTip.setRotationPoint(6.0F, 0.0F, 0.0F);
        leftWingTip.rotateAngleZ = 0.1F;
        leftWingBase.addChild(leftWingTip);
        rightWingBase = new ModelRenderer(this, 23, 12);
        rightWingBase.mirror = true;
        rightWingBase.addBox(-6.0F, 0.0F, 0.0F, 6, 2, 9);
        rightWingBase.setRotationPoint(-3.0F, -2.0F, -8.0F);
        rightWingBase.rotateAngleZ = -0.1F;
        body.addChild(rightWingBase);
        rightWingTip = new ModelRenderer(this, 16, 24);
        rightWingTip.mirror = true;
        rightWingTip.addBox(-13.0F, 0.0F, 0.0F, 13, 1, 9);
        rightWingTip.setRotationPoint(-6.0F, 0.0F, 0.0F);
        rightWingTip.rotateAngleZ = -0.1F;
        rightWingBase.addChild(rightWingTip);
        ModelRenderer head = new ModelRenderer(this, 0, 0);
        head.addBox(-4.0F, -2.0F, -5.0F, 7, 3, 5);
        head.setRotationPoint(0.0F, 1.0F, -7.0F);
        head.rotateAngleX = 0.2F;
        body.addChild(head);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch, float scale) {
        float phase = ageInTicks * 7.448451F * 0.017453292F;
        leftWingBase.rotateAngleZ = MathHelper.cos(phase) * 16.0F * 0.017453292F;
        leftWingTip.rotateAngleZ = leftWingBase.rotateAngleZ;
        rightWingBase.rotateAngleZ = -leftWingBase.rotateAngleZ;
        rightWingTip.rotateAngleZ = -leftWingTip.rotateAngleZ;
        tailBase.rotateAngleX = -(5.0F + MathHelper.cos(phase * 2.0F) * 5.0F) * 0.017453292F;
        tailTip.rotateAngleX = tailBase.rotateAngleX;
        body.render(scale);
    }
}
