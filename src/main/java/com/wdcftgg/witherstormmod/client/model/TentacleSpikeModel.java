package com.wdcftgg.witherstormmod.client.model;

import com.wdcftgg.witherstormmod.common.entity.SupplementalEntities;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import java.util.Random;

public class TentacleSpikeModel extends ModelBase {

    private final ModelRenderer base;
    private final ModelRenderer middle;
    private final ModelRenderer end;

    public TentacleSpikeModel() {
        textureWidth = 32;
        textureHeight = 32;
        base = new ModelRenderer(this, 0, 0);
        base.addBox(-3.0F, -7.0F, -3.0F, 6, 7, 6);
        middle = new ModelRenderer(this, 0, 13);
        middle.addBox(-2.0F, -9.0F, -2.0F, 4, 9, 4);
        middle.setRotationPoint(0.0F, -7.0F, 0.0F);
        base.addChild(middle);
        end = new ModelRenderer(this, 16, 13);
        end.addBox(-1.0F, -11.0F, -1.0F, 2, 11, 2);
        end.setRotationPoint(0.0F, -9.0F, 0.0F);
        middle.addChild(end);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch, float scale) {
        float anim = limbSwing;
        float sway = anim * (3.0F - anim) + new Random(entity.getEntityId()).nextFloat() * 1000.0F;
        float zSway = MathHelper.sin(sway) * 0.1F;
        float xSway = MathHelper.cos(sway) * 0.1F;
        base.rotateAngleZ = middle.rotateAngleZ = end.rotateAngleZ = zSway;
        base.rotateAngleX = middle.rotateAngleX = end.rotateAngleX = xSway;
        base.render(scale);
    }
}
