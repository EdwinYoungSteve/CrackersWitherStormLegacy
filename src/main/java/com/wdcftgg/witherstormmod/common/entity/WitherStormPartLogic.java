package com.wdcftgg.witherstormmod.common.entity;

import java.util.Random;

/** Pure timing and animation rules shared by the 1.12 entities. */
public final class WitherStormPartLogic {
    private WitherStormPartLogic() {
    }

    public static int initialRoarDelay(Random random) {
        return 400 + random.nextInt(600);
    }

    public static int nextRoarDelay(Random random) {
        return 400 + random.nextInt(600);
    }

    public static int nextShotDelay(Random random) {
        return 60 + random.nextInt(40);
    }

    public static float advanceMouth(float current, boolean roaring, boolean biting) {
        if (!biting && roaring) {
            return Math.clamp(current + (1.0F - current) * 0.15F + 0.04F, 0.0F, 2.0F);
        }
        if (biting) {
            return Math.clamp(current + (1.0F - current) * 0.16F + 0.1F, 0.0F, 1.4F);
        }
        return Math.clamp(current - current * 0.16F - 0.02F, 0.0F, 2.0F);
    }

    public static float advanceFade(float current, boolean playingDead, Random random) {
        float next = playingDead ? current + 1.0F + random.nextFloat() * 2.0F
                : current - 1.0F - random.nextFloat() * 2.0F;
        return Math.clamp(next, 0.0F, 300.0F);
    }

    public static float advanceShake(float current, boolean shaking, Random random) {
        return shaking ? current + 0.02F + random.nextFloat() * 0.05F : current;
    }

    public static float shakeRoll(float previous, float current, float partialTicks) {
        float lerp = Math.clamp(previous + (current - previous) * partialTicks, 0.0F, 1.0F);
        return (float) Math.sin(lerp * Math.PI) * (float) Math.sin(lerp * Math.PI * 12.0F)
                * 0.05F * (float) Math.PI;
    }

    public static int segmentDropDuration(Random random) {
        return 10 + random.nextInt(5);
    }

    public static int segmentDropCooldown(Random random, float healthRatio) {
        return (int) ((360 + random.nextInt(160)) * Math.max(0.2F, healthRatio));
    }

    public static int segmentFreeFallDelay(Random random) {
        return Math.max(220, random.nextInt(260));
    }
}
