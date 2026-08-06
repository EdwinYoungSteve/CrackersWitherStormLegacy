package com.wdcftgg.witherstormmod.client.model;

import com.wdcftgg.witherstormmod.common.entity.SickenedEntities;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class TentacleModel extends ModelBase {

    private final ModelRenderer[] segments = new ModelRenderer[5];

    public TentacleModel() {
        textureWidth = 128;
        textureHeight = 128;

        segments[0] = part(0, 0, -6.0F, -12.0F, -6.0F, 12, 12, 12, 0.0F, 0.0F, 0.0F);
        segments[1] = part(0, 24, -5.0F, -16.0F, -5.0F, 10, 16, 10, 0.0F, -12.0F, 0.0F);
        segments[2] = part(40, 16, -4.0F, -20.0F, -4.0F, 8, 20, 8, 0.0F, -16.0F, 0.0F);
        segments[3] = part(34, 44, -3.0F, -24.0F, -3.0F, 6, 24, 6, 0.0F, -20.0F, 0.0F);
        segments[4] = part(0, 50, -2.0F, -24.0F, -2.0F, 4, 24, 4, 0.0F, -24.0F, 0.0F);

        for (int index = 1; index < segments.length; index++) {
            segments[index - 1].addChild(segments[index]);
        }
    }

    private ModelRenderer part(int textureX, int textureY, float x, float y, float z,
                               int width, int height, int depth, float pivotX, float pivotY, float pivotZ) {
        ModelRenderer part = new ModelRenderer(this, textureX, textureY);
        part.addBox(x, y, z, width, height, depth, 0.0F);
        part.setRotationPoint(pivotX, pivotY, pivotZ);
        return part;
    }

    @Override
    public void render(Entity entity, float partialTicks, float amount, float ageInTicks,
                       float yaw, float pitch, float scale) {
        if (entity instanceof SickenedEntities.TentacleEntity) {
            SickenedEntities.TentacleEntity tentacle = (SickenedEntities.TentacleEntity) entity;
            for (int index = 0; index < segments.length; index++) {
                segments[index].rotateAngleX = tentacle.getSegmentPitch(index, partialTicks);
                segments[index].rotateAngleY = tentacle.getSegmentYaw(index, partialTicks);
                segments[index].rotateAngleZ = 0.0F;
            }
        }
        segments[0].render(scale);
    }
}
