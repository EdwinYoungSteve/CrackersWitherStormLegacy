package com.wdcftgg.witherstormmod.common.event;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.common.entity.WitherStormEntity;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class WitherStormSummoningEvents {

    private WitherStormSummoningEvents() {
    }

    @SubscribeEvent
    public static void onSkullPlaced(BlockEvent.PlaceEvent event) {
        if (!(event.getWorld() instanceof World) || event.getWorld().isRemote || event.getPlacedBlock().getBlock() != Blocks.SKULL) {
            return;
        }
        World world = (World) event.getWorld();
        BlockPos placedPosition = event.getPos();
        for (EnumFacing.Axis axis : new EnumFacing.Axis[] {EnumFacing.Axis.X, EnumFacing.Axis.Z}) {
            for (int offset = -1; offset <= 1; offset++) {
                BlockPos centerSkull = axis == EnumFacing.Axis.X ? placedPosition.add(offset, 0, 0) : placedPosition.add(0, 0, offset);
                if (matchesStructure(world, centerSkull, axis)) {
                    spawnStorm(world, centerSkull, axis);
                    return;
                }
            }
        }
    }

    private static boolean matchesStructure(World world, BlockPos centerSkull, EnumFacing.Axis axis) {
        BlockPos sideOne = axis == EnumFacing.Axis.X ? centerSkull.west() : centerSkull.north();
        BlockPos sideTwo = axis == EnumFacing.Axis.X ? centerSkull.east() : centerSkull.south();
        return isWitherSkull(world, sideOne) && isWitherSkull(world, centerSkull) && isWitherSkull(world, sideTwo)
                && world.getBlockState(sideOne.down()).getBlock() == Blocks.SOUL_SAND
                && world.getBlockState(centerSkull.down()).getBlock() == Blocks.COMMAND_BLOCK
                && world.getBlockState(sideTwo.down()).getBlock() == Blocks.SOUL_SAND
                && world.getBlockState(centerSkull.down(2)).getBlock() == Blocks.SOUL_SAND;
    }

    private static boolean isWitherSkull(World world, BlockPos position) {
        if (world.getBlockState(position).getBlock() != Blocks.SKULL) {
            return false;
        }
        TileEntity tileEntity = world.getTileEntity(position);
        return tileEntity instanceof TileEntitySkull && ((TileEntitySkull) tileEntity).getSkullType() == 1;
    }

    private static void spawnStorm(World world, BlockPos centerSkull, EnumFacing.Axis axis) {
        BlockPos sideOne = axis == EnumFacing.Axis.X ? centerSkull.west() : centerSkull.north();
        BlockPos sideTwo = axis == EnumFacing.Axis.X ? centerSkull.east() : centerSkull.south();
        BlockPos[] structure = {sideOne, centerSkull, sideTwo, sideOne.down(), centerSkull.down(), sideTwo.down(), centerSkull.down(2)};
        for (BlockPos position : structure) {
            world.setBlockToAir(position);
        }
        WitherStormEntity storm = new WitherStormEntity(world);
        storm.setLocationAndAngles(centerSkull.getX() + 0.5D, centerSkull.getY() - 1.0D, centerSkull.getZ() + 0.5D,
                axis == EnumFacing.Axis.X ? 0.0F : 90.0F, 0.0F);
        storm.ignite();
        if (world.spawnEntity(storm)) {
            for (EntityPlayerMP player : world.getEntitiesWithinAABB(EntityPlayerMP.class,
                    storm.getEntityBoundingBox().grow(50.0D))) {
                CriteriaTriggers.SUMMONED_ENTITY.trigger(player, storm);
            }
            world.playEvent(1023, centerSkull, 0);
        }
    }
}
