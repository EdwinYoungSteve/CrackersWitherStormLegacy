package com.wdcftgg.witherstormmod.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class FlamingWitherSkullModel extends ModelBase {

    private final ModelRenderer head;
    private final ModelRenderer flame;
    private final ModelRenderer flameSide;

    public FlamingWitherSkullModel() {
        textureWidth = 32;
        textureHeight = 32;
        head = new ModelRenderer(this, 0, 0);
        head.addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8);
        head.setRotationPoint(0.0F, -3.5F, 0.0F);

        flame = new ModelRenderer(this, 0, 24);
        flame.addBox(-4.0F, -8.0F, 4.0F, 8, 0, 8);
        flame.addBox(-4.0F, 0.0F, 4.0F, 8, 0, 8);
        flame.setRotationPoint(0.0F, 4.0F, 0.0F);
        head.addChild(flame);

        flameSide = new ModelRenderer(this, 0, 16);
        flameSide.addBox(0.0F, -4.0F, -4.0F, 0, 8, 8);
        flameSide.addBox(8.0F, -4.0F, -4.0F, 0, 8, 8);
        flameSide.setRotationPoint(-4.0F, -4.0F, 8.0F);
        flameSide.rotateAngleX = -1.5708F;
        flame.addChild(flameSide);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch, float scale) {
        head.rotateAngleY = netHeadYaw * 0.017453292F;
        head.rotateAngleX = headPitch * 0.017453292F;
        head.render(scale);
    }
}
