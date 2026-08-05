package com.wdcftgg.witherstormmod.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelWitherStormHeadPort extends ModelBase {
    private final ModelRenderer head;
    private final ModelRenderer upperJaw;
    private final ModelRenderer lowerJaw;

    public ModelWitherStormHeadPort() {
        textureWidth = 160;
        textureHeight = 160;
        head = new ModelRenderer(this);
        head.setRotationPoint(0.0F, 0.0F, 0.0F);

        upperJaw = new ModelRenderer(this, 0, 65);
        upperJaw.setRotationPoint(0.0F, 2.5F, 0.0F);
        upperJaw.addBox(-4.0F, -6.5F, 12.0F, 8, 6, 2, 0.0F);
        upperJaw.setTextureOffset(0, 47).addBox(-2.0F, -8.5F, 10.0F, 4, 2, 2);
        upperJaw.setTextureOffset(0, 35).addBox(-4.0F, -8.5F, 0.0F, 8, 2, 10);
        upperJaw.setTextureOffset(0, 47).addBox(-6.0F, -6.5F, 0.0F, 12, 6, 12);
        upperJaw.setTextureOffset(4, 13).addBox(-1.0F, -4.5F, 13.0F, 2, 2, 1, 0.2F);
        addUpperTeeth();

        lowerJaw = new ModelRenderer(this, 0, 73);
        lowerJaw.setRotationPoint(0.0F, 2.5F, 0.0F);
        lowerJaw.addBox(-4.0F, 0.5F, 12.0F, 8, 2, 2);
        lowerJaw.setTextureOffset(48, 0).addBox(-6.0F, 0.5F, 0.0F, 12, 2, 12);
        addLowerTeeth();
        head.addChild(upperJaw);
        head.addChild(lowerJaw);
    }

    private void addUpperTeeth() {
        int[][] teeth = {{-1, 13}, {-3, 12}, {-5, 11}, {-6, 9}, {-6, 7}, {-6, 5}, {-6, 3}, {-6, 1},
                {1, 13}, {3, 12}, {4, 10}, {5, 8}, {5, 6}, {5, 4}, {5, 2}, {5, 0}};
        for (int[] tooth : teeth) upperJaw.setTextureOffset(0, 54).addBox(tooth[0], -1.0F, tooth[1], 1, 1, 1);
    }

    private void addLowerTeeth() {
        int[][] teeth = {{0, 5}, {2, 4}, {4, 3}, {5, 1}, {5, -1}, {5, -3}, {5, -5}, {5, -7},
                {-2, 5}, {-4, 4}, {-5, 2}, {-6, 0}, {-6, -2}, {-6, -4}, {-6, -6}, {-6, -8}};
        for (int[] tooth : teeth) lowerJaw.setTextureOffset(0, 54).addBox(tooth[0], -3.0F, tooth[1], 1, 1, 1);
    }

    @Override
    public void render(Entity entity, float limbSwing, float amount, float age, float yaw, float pitch, float scale) {
        head.rotateAngleY = (180.0F + yaw) * 0.017453292F;
        head.rotateAngleX = -pitch * 0.017453292F;
        head.rotateAngleZ = 0.0F;
        float partialTicks = MathHelper.clamp(age - entity.ticksExisted, 0.0F, 1.0F);
        if (entity instanceof com.wdcftgg.witherstormmod.common.entity.SupplementalEntities.WitherStormHead) {
            com.wdcftgg.witherstormmod.common.entity.SupplementalEntities.WitherStormHead stormHead =
                    (com.wdcftgg.witherstormmod.common.entity.SupplementalEntities.WitherStormHead) entity;
            float hinge = stormHead.getMouthAnimation(partialTicks) * 0.3F;
            float ticks = stormHead.isDeadOrPlayingDead() ? 0.0F : age;
            float wave = MathHelper.sin(ticks * 0.1F);
            lowerJaw.rotateAngleX = MathHelper.sin(hinge) * 10.0F - 10.0F
                    + (0.065F + 0.02F * wave) * (float) Math.PI - 0.5F;
            lowerJaw.rotateAngleZ = 0.0F;
            head.rotateAngleZ = stormHead.getHeadShakeAnimation(partialTicks);
        } else {
            lowerJaw.rotateAngleX = 0.2F + (MathHelper.sin(age * 0.12F) + 1.0F) * 0.18F;
        }
        head.render(scale);
    }
}
