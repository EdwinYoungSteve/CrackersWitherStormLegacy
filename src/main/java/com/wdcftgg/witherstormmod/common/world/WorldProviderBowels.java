package com.wdcftgg.witherstormmod.common.world;

import net.minecraft.init.Biomes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.IChunkGenerator;

public class WorldProviderBowels extends WorldProvider {
    @Override
    protected void init() {
        hasSkyLight = false;
        doesWaterVaporize = false;
        biomeProvider = new BiomeProviderSingle(Biomes.VOID);
    }

    @Override
    public IChunkGenerator createChunkGenerator() {
        return new BowelsChunkGenerator(world);
    }

    @Override
    public DimensionType getDimensionType() {
        return BowelsDimensions.BOWELS;
    }

    @Override
    public boolean isSurfaceWorld() {
        return false;
    }

    @Override
    public boolean canRespawnHere() {
        return true;
    }

    @Override
    public float calculateCelestialAngle(long worldTime, float partialTicks) {
        return 0.5F;
    }

    @Override
    public Vec3d getFogColor(float celestialAngle, float partialTicks) {
        return new Vec3d(1.0D, 0.41960785D, 0.972549D);
    }

    @Override
    public double getVoidFogYFactor() {
        return 1.0D;
    }

    @Override
    protected void generateLightBrightnessTable() {
        for (int index = 0; index <= 15; index++) {
            float darkness = 1.0F - index / 15.0F;
            lightBrightnessTable[index] = (1.0F - darkness) / (darkness * 3.0F + 1.0F) * 0.99F + 0.01F;
        }
    }
}
