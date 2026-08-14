package com.wdcftgg.witherstormmod.common.world;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nullable;

public class WitherStormSpawnData extends WorldSavedData {
    public static final String DATA_NAME = "witherstormmod_spawn";

    private int tickCount;
    private boolean hasSpawnedWitherStorm;
    private boolean platformGenerated;
    private BlockPos spawnPosition;

    public WitherStormSpawnData() {
        super(DATA_NAME);
    }

    public WitherStormSpawnData(String name) {
        super(name);
    }

    public static WitherStormSpawnData get(World world) {
        WitherStormSpawnData data = (WitherStormSpawnData) world.getPerWorldStorage()
                .getOrLoadData(WitherStormSpawnData.class, DATA_NAME);
        if (data == null) {
            data = new WitherStormSpawnData();
            world.getPerWorldStorage().setData(DATA_NAME, data);
        }
        return data;
    }

    public int advanceTickCount() {
        tickCount++;
        markDirty();
        return tickCount;
    }

    public boolean hasSpawnedWitherStorm() {
        return hasSpawnedWitherStorm;
    }

    public void setHasSpawnedWitherStorm(boolean spawned) {
        if (hasSpawnedWitherStorm == spawned) return;
        hasSpawnedWitherStorm = spawned;
        markDirty();
    }

    public boolean isPlatformGenerated() {
        return platformGenerated;
    }

    @Nullable
    public BlockPos getSpawnPosition() {
        return spawnPosition;
    }

    public void recordPlatform(BlockPos position) {
        platformGenerated = true;
        spawnPosition = position;
        markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        tickCount = compound.getInteger("TickCount");
        hasSpawnedWitherStorm = compound.getBoolean("HasSpawnedWitherStorm");
        platformGenerated = compound.getBoolean("PlatformGenerated");
        spawnPosition = compound.hasKey("SpawnPosition", 4)
                ? BlockPos.fromLong(compound.getLong("SpawnPosition")) : null;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("TickCount", tickCount);
        compound.setBoolean("HasSpawnedWitherStorm", hasSpawnedWitherStorm);
        compound.setBoolean("PlatformGenerated", platformGenerated);
        if (spawnPosition != null) compound.setLong("SpawnPosition", spawnPosition.toLong());
        return compound;
    }
}
