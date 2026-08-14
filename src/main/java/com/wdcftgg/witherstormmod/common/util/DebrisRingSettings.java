package com.wdcftgg.witherstormmod.common.util;

import net.minecraft.util.math.MathHelper;

public final class DebrisRingSettings {
    private final int segments;
    private final float bottomRadius;
    private final float topRadius;
    private final float bottomY;
    private final float topY;
    private final float speedModifier;
    private final boolean clockwise;
    private final int phaseRequirement;
    private float alpha = 1.0F;

    public DebrisRingSettings(int segments, float bottomRadius, float topRadius, float bottomY,
                              float height, float speedModifier, boolean clockwise,
                              int phaseRequirement, boolean hidden) {
        this.segments = segments;
        this.bottomRadius = bottomRadius;
        this.topRadius = topRadius;
        this.bottomY = bottomY;
        this.topY = bottomY + height;
        this.speedModifier = speedModifier;
        this.clockwise = clockwise;
        this.phaseRequirement = phaseRequirement;
        if (hidden) alpha = 0.0F;
    }

    public int getSegments() {
        return segments;
    }

    public float getBottomRadius() {
        return bottomRadius;
    }

    public float getTopRadius() {
        return topRadius;
    }

    public float getBottomY() {
        return bottomY;
    }

    public float getTopY() {
        return topY;
    }

    public float getSpeedModifier() {
        return speedModifier;
    }

    public boolean isClockwise() {
        return clockwise;
    }

    public int getPhaseRequirement() {
        return phaseRequirement;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = MathHelper.clamp(alpha, 0.0F, 1.0F);
    }
}
