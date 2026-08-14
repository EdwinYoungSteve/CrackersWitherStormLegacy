package com.wdcftgg.witherstormmod.common.block;

import com.wdcftgg.witherstormmod.common.tile.TaintedSignTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockSign;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class TaintedWallHangingSignBlock extends BlockSign {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    private static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.0D, 0.25D, 0.875D, 1.0D, 1.0D, 1.0D);
    private static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.0D, 0.25D, 0.0D, 1.0D, 1.0D, 0.125D);
    private static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0.875D, 0.25D, 0.0D, 1.0D, 1.0D, 1.0D);
    private static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.0D, 0.25D, 0.0D, 0.125D, 1.0D, 1.0D);

    public TaintedWallHangingSignBlock(String name) {
        setRegistryName(name);
        setTranslationKey(name);
        setHardness(1.0F);
        setResistance(1.0F);
        setSoundType(SoundType.WOOD);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        switch (state.getValue(FACING)) {
            case SOUTH: return SOUTH_AABB;
            case WEST: return WEST_AABB;
            case EAST: return EAST_AABB;
            case NORTH:
            default: return NORTH_AABB;
        }
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
            if (hasSupport(world, pos, facing)) return super.canPlaceBlockAt(world, pos);
        }
        return false;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                             float hitX, float hitY, float hitZ, int meta,
                                             EntityLivingBase placer, EnumHand hand) {
        EnumFacing horizontal = facing.getAxis().isHorizontal() ? facing : placer.getHorizontalFacing().getOpposite();
        if (!hasSupport(world, pos, horizontal)) {
            for (EnumFacing candidate : EnumFacing.Plane.HORIZONTAL) {
                if (hasSupport(world, pos, candidate)) {
                    horizontal = candidate;
                    break;
                }
            }
        }
        return getDefaultState().withProperty(FACING, horizontal);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block changedBlock,
                                BlockPos changedPos) {
        if (!hasSupport(world, pos, state.getValue(FACING))) {
            dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
        }
        super.neighborChanged(state, world, pos, changedBlock, changedPos);
    }

    private boolean hasSupport(IBlockAccess world, BlockPos pos, EnumFacing facing) {
        BlockPos supportPos = pos.offset(facing.getOpposite());
        return world.getBlockState(supportPos).isSideSolid(world, supportPos, facing);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.byIndex(meta);
        if (!facing.getAxis().isHorizontal()) facing = EnumFacing.NORTH;
        return getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rotation) {
        return state.withProperty(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        return state.withRotation(mirror.toRotation(state.getValue(FACING)));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TaintedSignTileEntity();
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Items.AIR;
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return ItemStack.EMPTY;
    }
}
