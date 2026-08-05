package com.wdcftgg.witherstormmod.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelCommandBlockCorePort extends ModelBase {
    private final ModelRenderer core;
    private final ModelRenderer[] ribs = new ModelRenderer[6];

    public ModelCommandBlockCorePort() {
        textureWidth = 128;
        textureHeight = 128;
        core = new ModelRenderer(this, 0, 0);
        core.addBox(-4, -4, -4, 8, 8, 8);
        core.setRotationPoint(0, 14, 0);
        for (int i = 0; i < ribs.length; i++) {
            ModelRenderer rib = new ModelRenderer(this, 32, 0);
            rib.addBox(-1, -1, 0, 2, 2, 18);
            float angle = (float) (Math.PI * 2.0D * i / ribs.length);
            rib.setRotationPoint(MathHelper.cos(angle) * 7, 0, MathHelper.sin(angle) * 7);
            rib.rotateAngleY = angle;
            core.addChild(rib);
            ribs[i] = rib;
        }
    }

    @Override
    public void render(Entity entity, float limbSwing, float amount, float age, float yaw, float pitch, float scale) {
        core.rotateAngleY = age * 0.025F;
        for (int i = 0; i < ribs.length; i++) ribs[i].rotateAngleX = MathHelper.sin(age * 0.04F + i) * 0.12F;
        core.render(scale);
    }
}
