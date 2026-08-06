package com.wdcftgg.witherstormmod.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class WitherStormSegmentModel extends ModelBase {
    private final ModelRenderer body;
    private final ModelRenderer[] masses = new ModelRenderer[5];
    private final ModelRenderer[][] tentacles = new ModelRenderer[6][4];

    public WitherStormSegmentModel() {
        textureWidth = 160;
        textureHeight = 160;
        body = new ModelRenderer(this, 32, 0);
        body.addBox(-12, -10, -10, 24, 20, 20);
        body.setRotationPoint(0, 12, 0);
        for (int i = 0; i < masses.length; i++) {
            ModelRenderer mass = new ModelRenderer(this, 0, 80);
            mass.addBox(-7, -6, -7, 14, 12, 14);
            double angle = Math.PI * 2.0D * i / masses.length;
            mass.setRotationPoint((float) Math.cos(angle) * 12, (i % 2) * 8 - 4, (float) Math.sin(angle) * 12);
            body.addChild(mass);
            masses[i] = mass;
        }
        for (int t = 0; t < tentacles.length; t++) {
            ModelRenderer parent = body;
            for (int s = 0; s < 4; s++) {
                ModelRenderer part = new ModelRenderer(this, 58, 0);
                part.addBox(-1.5F, -1.5F, 0, 3, 3, 13);
                float angle = (float) (Math.PI * 2.0D * t / tentacles.length);
                part.setRotationPoint(s == 0 ? MathHelper.cos(angle) * 9 : 0, s == 0 ? 0 : 0,
                        s == 0 ? MathHelper.sin(angle) * 9 : 12);
                if (s == 0) part.rotateAngleY = angle;
                parent.addChild(part);
                tentacles[t][s] = part;
                parent = part;
            }
        }
    }

    @Override
    public void render(Entity entity, float limbSwing, float amount, float age, float yaw, float pitch, float scale) {
        body.rotateAngleY = age * 0.003F;
        for (int t = 0; t < tentacles.length; t++) {
            for (int s = 0; s < 4; s++) {
                tentacles[t][s].rotateAngleX = MathHelper.sin(age * 0.035F + t + s * 0.7F) * (0.15F + s * 0.04F);
                if (s > 0) tentacles[t][s].rotateAngleY = MathHelper.cos(age * 0.03F + t + s) * 0.12F;
            }
        }
        body.render(scale);
    }
}
