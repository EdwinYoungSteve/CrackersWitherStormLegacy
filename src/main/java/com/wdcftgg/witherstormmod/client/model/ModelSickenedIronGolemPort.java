package com.wdcftgg.witherstormmod.client.model;

import com.wdcftgg.witherstormmod.common.entity.SickenedEntities;
import net.minecraft.client.model.ModelIronGolem;
import net.minecraft.entity.EntityLivingBase;

public class ModelSickenedIronGolemPort extends ModelIronGolem {
    @Override
    public void setLivingAnimations(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks) {
        int attackTick = entity instanceof SickenedEntities.SickenedIronGolem
                ? ((SickenedEntities.SickenedIronGolem) entity).getAttackAnimationTick() : 0;
        if (attackTick > 0) {
            float swing = triangleWave((float) attackTick - partialTicks, 10.0F);
            ironGolemRightArm.rotateAngleX = -2.0F + 1.5F * swing;
            ironGolemLeftArm.rotateAngleX = -2.0F + 1.5F * swing;
        } else {
            ironGolemRightArm.rotateAngleX = (-0.2F + 1.5F * triangleWave(limbSwing, 13.0F)) * limbSwingAmount;
            ironGolemLeftArm.rotateAngleX = (-0.2F - 1.5F * triangleWave(limbSwing, 13.0F)) * limbSwingAmount;
        }
    }

    private float triangleWave(float value, float period) {
        return (Math.abs(value % period - period * 0.5F) - period * 0.25F) / (period * 0.25F);
    }
}
