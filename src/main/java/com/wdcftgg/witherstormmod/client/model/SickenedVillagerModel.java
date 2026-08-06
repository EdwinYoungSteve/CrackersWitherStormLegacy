package com.wdcftgg.witherstormmod.client.model;

import com.wdcftgg.witherstormmod.common.entity.SickenedMobEntity;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class SickenedVillagerModel extends ModelBiped {

    public SickenedVillagerModel() {
        super(0.0F, 0.0F, 64, 64);
        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.addBox(-4.0F, -10.0F, -4.0F, 8, 10, 8);
        bipedHead.setTextureOffset(24, 0).addBox(-1.0F, -3.0F, -6.0F, 2, 4, 2);
        bipedHeadwear = new ModelRenderer(this, 32, 0);
        bipedHeadwear.addBox(-4.0F, -10.0F, -4.0F, 8, 10, 8, 0.5F);
        bipedBody = new ModelRenderer(this, 16, 20);
        bipedBody.addBox(-4.0F, 0.0F, -3.0F, 8, 12, 6);
        bipedBody.setTextureOffset(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8, 18, 6, 0.05F);
        bipedRightArm = new ModelRenderer(this, 44, 22);
        bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedLeftArm = new ModelRenderer(this, 44, 22);
        bipedLeftArm.mirror = true;
        bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedRightLeg = new ModelRenderer(this, 0, 22);
        bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedLeftLeg = new ModelRenderer(this, 0, 22);
        bipedLeftLeg.mirror = true;
        bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4);
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);
        boolean aggressive = entity instanceof SickenedMobEntity
                && ((SickenedMobEntity) entity).getAttackTarget() != null;
        float swing = MathHelper.sin(swingProgress * (float) Math.PI);
        float eased = MathHelper.sin((1.0F - (1.0F - swingProgress) * (1.0F - swingProgress)) * (float) Math.PI);
        float raised = -(float) Math.PI / (aggressive ? 1.5F : 2.25F);
        bipedRightArm.rotateAngleY = -(0.1F - swing * 0.6F);
        bipedLeftArm.rotateAngleY = 0.1F - swing * 0.6F;
        bipedRightArm.rotateAngleX = raised + swing * 1.2F - eased * 0.4F;
        bipedLeftArm.rotateAngleX = raised + swing * 1.2F - eased * 0.4F;
        bipedRightArm.rotateAngleZ = MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        bipedLeftArm.rotateAngleZ = -MathHelper.cos(ageInTicks * 0.09F) * 0.05F - 0.05F;
    }
}
