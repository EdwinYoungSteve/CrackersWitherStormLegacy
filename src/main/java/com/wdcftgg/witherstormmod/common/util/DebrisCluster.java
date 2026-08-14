package com.wdcftgg.witherstormmod.common.util;

import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class DebrisCluster {
    private final float orbitalAngleOffset;
    private final float verticalOffset;
    private final float radiusFromCenter;
    private final float speed;
    private final float sizeModifier;
    private final List<Piece> pieces = new ArrayList<Piece>();
    private float pitchVelocity;
    private float yawVelocity;
    private float pitch;
    private float previousPitch;
    private float yaw;
    private float previousYaw;
    private float orbitalAngle;
    private float previousOrbitalAngle;
    private int renderPhase;
    private boolean disabled;
    private boolean glowing;
    private boolean forcedGlowing;

    public DebrisCluster(float orbitalAngleOffset, float verticalOffset, float radiusFromCenter,
                         float speed, float sizeModifier) {
        this.orbitalAngleOffset = orbitalAngleOffset;
        this.verticalOffset = verticalOffset;
        this.radiusFromCenter = radiusFromCenter;
        this.speed = speed;
        this.sizeModifier = sizeModifier;
    }

    public void randomize(Random random, int pieceCount, float spread) {
        pitch = random.nextFloat() * 360.0F;
        previousPitch = pitch;
        yaw = random.nextFloat() * 360.0F;
        previousYaw = yaw;
        pitchVelocity = random.nextFloat() * 10.0F - 5.0F;
        yawVelocity = random.nextFloat() * 10.0F - 5.0F;
        pieces.clear();
        for (int index = 0; index < pieceCount; index++) {
            float x = random.nextFloat() * spread * 2.0F - spread;
            float y = random.nextFloat() * spread * 2.0F - spread;
            float z = random.nextFloat() * spread * 2.0F - spread;
            float size = (0.3F + random.nextFloat() * 0.3F) * sizeModifier;
            pieces.add(new Piece(x, y, z, size));
        }
        glowing = random.nextInt(20) == 0;
    }

    public void determineRenderPhase() {
        if (radiusFromCenter > 80.0F) {
            renderPhase = 6;
        } else if (verticalOffset < 60.0F) {
            renderPhase = 4;
        } else if (verticalOffset < 80.0F) {
            renderPhase = 5;
        } else {
            renderPhase = 6;
        }
    }

    public void tick() {
        previousPitch = pitch;
        previousYaw = yaw;
        previousOrbitalAngle = orbitalAngle;
        orbitalAngle += speed;
        pitch += pitchVelocity;
        yaw += yawVelocity;
    }

    public float getOrbitalAngle(float partialTicks) {
        return previousOrbitalAngle + (orbitalAngle - previousOrbitalAngle) * partialTicks
                + orbitalAngleOffset;
    }

    public float getPitch(float partialTicks) {
        return previousPitch + (pitch - previousPitch) * partialTicks;
    }

    public float getYaw(float partialTicks) {
        return previousYaw + (yaw - previousYaw) * partialTicks;
    }

    public float getVerticalOffset() {
        return verticalOffset;
    }

    public float getRadiusFromCenter() {
        return radiusFromCenter;
    }

    public int getRenderPhase() {
        return renderPhase;
    }

    public void setRenderPhase(int renderPhase) {
        this.renderPhase = MathHelper.clamp(renderPhase, 0, 7);
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isGlowing() {
        return glowing;
    }

    public boolean isForcedGlowing() {
        return forcedGlowing;
    }

    public void setGlowing(boolean glowing) {
        forcedGlowing = glowing;
    }

    public List<Piece> getPieces() {
        return Collections.unmodifiableList(pieces);
    }

    public static final class Piece {
        private final float x;
        private final float y;
        private final float z;
        private final float size;

        private Piece(float x, float y, float z, float size) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.size = size;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getZ() {
            return z;
        }

        public float getSize() {
            return size;
        }
    }
}
