package com.wdcftgg.witherstormmod.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

public class ModelSickenedZombiePort extends ModelBiped {
    public ModelSickenedZombiePort() {
        super(0.0F, 0.0F, 64, 64);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                                  float headPitch, float scaleFactor, Entity entity) {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);
        boolean aggressive = entity instanceof EntityLiving && ((EntityLiving) entity).getAttackTarget() != null;
        float swing = MathHelper.sin(swingProgress * (float) Math.PI);
        float easedSwing = MathHelper.sin((1.0F - (1.0F - swingProgress) * (1.0F - swingProgress)) * (float) Math.PI);
        bipedRightArm.rotateAngleZ = 0.0F;
        bipedLeftArm.rotateAngleZ = 0.0F;
        bipedRightArm.rotateAngleY = -(0.1F - swing * 0.6F);
        bipedLeftArm.rotateAngleY = 0.1F - swing * 0.6F;
        float base = -(float) Math.PI / (aggressive ? 1.5F : 2.25F);
        bipedRightArm.rotateAngleX = base + swing * 1.2F - easedSwing * 0.4F;
        bipedLeftArm.rotateAngleX = base + swing * 1.2F - easedSwing * 0.4F;
        bipedRightArm.rotateAngleZ += MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        bipedLeftArm.rotateAngleZ -= MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        bipedRightArm.rotateAngleX += MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
        bipedLeftArm.rotateAngleX -= MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
    }
}
