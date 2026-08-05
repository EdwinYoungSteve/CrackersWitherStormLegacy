package com.wdcftgg.witherstormmod.common.world;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;

public class BowelsTeleporter implements ITeleporter {
    private final BlockPos destination;

    public BowelsTeleporter(BlockPos destination) {
        this.destination = destination;
    }

    @Override
    public void placeEntity(World world, Entity entity, float yaw) {
        entity.setLocationAndAngles(destination.getX() + 0.5D, destination.getY(), destination.getZ() + 0.5D, yaw, 0.0F);
        entity.motionX = 0.0D;
        entity.motionY = 0.0D;
        entity.motionZ = 0.0D;
    }
}
