package com.wdcftgg.witherstormmod.client.model;

import com.wdcftgg.witherstormmod.common.entity.WitherStormEntity;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class WitherStormModel extends ModelBase {
    private final ModelRenderer core;
    private final ModelRenderer mass;
    private final ModelRenderer[] heads = new ModelRenderer[3];
    private final ModelRenderer[] lowerJaws = new ModelRenderer[3];
    private final ModelRenderer[][] tentacles = new ModelRenderer[9][4];

    public WitherStormModel() {
        textureWidth = 160;
        textureHeight = 160;
        core = new ModelRenderer(this, 0, 0);
        core.addBox(-4, -4, -4, 8, 8, 8);
        core.setRotationPoint(0, 0, 0);
        mass = new ModelRenderer(this, 32, 0);
        mass.addBox(-12, -10, -9, 24, 20, 18);
        mass.setRotationPoint(0, -2, 2);
        core.addChild(mass);
        for (int i = 0; i < 3; i++) {
            ModelRenderer head = new ModelRenderer(this, 0, 35);
            head.addBox(-6, -6, -1, 12, 7, 13);
            head.setRotationPoint((i - 1) * 11, -8, -8);
            ModelRenderer jaw = new ModelRenderer(this, 48, 0);
            jaw.addBox(-6, 0, -1, 12, 3, 13);
            jaw.setRotationPoint(0, 1, 0);
            head.addChild(jaw);
            mass.addChild(head);
            heads[i] = head;
            lowerJaws[i] = jaw;
        }
        for (int i = 0; i < tentacles.length; i++) {
            ModelRenderer parent = mass;
            for (int segment = 0; segment < 4; segment++) {
                ModelRenderer part = new ModelRenderer(this, 58, 0);
                part.addBox(-1.5F, -1.5F, 0, 3, 3, 12);
                part.setRotationPoint(segment == 0 ? ((i % 3) - 1) * 8 : 0,
                        segment == 0 ? 2 + (i / 3) * 4 : 0, segment == 0 ? 7 : 11);
                parent.addChild(part);
                tentacles[i][segment] = part;
                parent = part;
            }
        }
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch, float scale) {
        WitherStormEntity storm = (WitherStormEntity) entity;
        int phase = storm.getPhase();
        mass.showModel = phase > 0;
        heads[1].showModel = phase >= 2 && !storm.areOtherHeadsDisabled();
        heads[2].showModel = phase >= 2 && !storm.areOtherHeadsDisabled();
        for (int i = 0; i < tentacles.length; i++) {
            boolean visible = i < Math.max(0, (phase - 1) * 2 + 1);
            tentacles[i][0].showModel = visible;
            float wave = MathHelper.sin(age * (0.035F + i * 0.003F) + i * 0.9F);
            for (int segment = 0; segment < 4; segment++) {
                tentacles[i][segment].rotateAngleX = 0.12F + wave * (0.16F + segment * 0.035F);
                tentacles[i][segment].rotateAngleY = MathHelper.sin(age * 0.025F + i + segment) * 0.11F;
            }
        }
        float jaw = 0.18F + (MathHelper.sin(age * 0.09F) + 1.0F) * 0.16F;
        for (int i = 0; i < heads.length; i++) {
            lowerJaws[i].rotateAngleX = jaw;
            heads[i].rotateAngleY = (i - 1) * 0.14F + yaw * 0.004F;
            heads[i].rotateAngleX = pitch * 0.004F;
        }
        core.render(scale);
    }
}
