package com.wdcftgg.witherstormmod.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

final class WitherStormHeadAnimation {
    private WitherStormHeadAnimation() {
    }

    static float jawPitch(float mouthAnimation, float ticks, float animationOffset) {
        float hinge = mouthAnimation * 0.3F;
        float wave = MathHelper.cos((ticks + animationOffset) * 0.1F);
        return MathHelper.cos(hinge) * 10.0F - 10.0F
                + (0.065F + 0.02F * wave) * (float) Math.PI - 0.5F;
    }

    static float brokenJawRoll(Entity entity, int head, float animation) {
        Random random = new Random(entity.getEntityId());
        boolean mirror = false;
        for (int index = 0; index < head; index++) {
            mirror = random.nextBoolean();
        }
        float roll = MathHelper.cos(animation * 0.3F) * 10.0F - 10.0F;
        return roll * (mirror ? -1.0F : 1.0F);
    }
}
