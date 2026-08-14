package com.wdcftgg.witherstormmod.common.block;

import com.wdcftgg.witherstormmod.common.tile.TaintedSignTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockSign;
import net.minecraft.block.BlockWall;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyInteger;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class TaintedCeilingHangingSignBlock extends BlockSign {
    public static final PropertyInteger ROTATION = PropertyInteger.create("rotation", 0, 15);

    public TaintedCeilingHangingSignBlock(String name) {
        setRegistryName(name);
        setTranslationKey(name);
        setHardness(1.0F);
        setResistance(1.0F);
        setSoundType(SoundType.WOOD);
        setDefaultState(blockState.getBaseState().withProperty(ROTATION, 0));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        double angle = state.getValue(ROTATION) * Math.PI / 8.0D;
        double halfX = Math.abs(Math.cos(angle)) * 0.5D + Math.abs(Math.sin(angle)) * 0.0625D;
        double halfZ = Math.abs(Math.sin(angle)) * 0.5D + Math.abs(Math.cos(angle)) * 0.0625D;
        return new AxisAlignedBB(0.5D - halfX, 0.25D, 0.5D - halfZ,
                0.5D + halfX, 1.0D, 0.5D + halfZ);
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return canHangFrom(world, pos.up()) && super.canPlaceBlockAt(world, pos);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                             float hitX, float hitY, float hitZ, int meta,
                                             EntityLivingBase placer, EnumHand hand) {
        int rotation = MathHelper.floor((placer.rotationYaw + 180.0F) * 16.0F / 360.0F + 0.5D) & 15;
        return getDefaultState().withProperty(ROTATION, rotation);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block changedBlock,
                                BlockPos changedPos) {
        if (!canHangFrom(world, pos.up())) {
            dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
        }
        super.neighborChanged(state, world, pos, changedBlock, changedPos);
    }

    public boolean hasVerticalChains(IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TaintedSignTileEntity
                && ((TaintedSignTileEntity) tile).hasHangingAttachmentOverride()) {
            return ((TaintedSignTileEntity) tile).isHangingAttached();
        }
        BlockPos supportPos = pos.up();
        IBlockState support = world.getBlockState(supportPos);
        return !support.isSideSolid(world, supportPos, EnumFacing.DOWN);
    }

    private boolean canHangFrom(IBlockAccess world, BlockPos supportPos) {
        IBlockState support = world.getBlockState(supportPos);
        Block block = support.getBlock();
        return support.isSideSolid(world, supportPos, EnumFacing.DOWN)
                || block instanceof BlockFence
                || block instanceof BlockWall
                || block instanceof TaintedCeilingHangingSignBlock
                || block instanceof TaintedWallHangingSignBlock;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(ROTATION, meta & 15);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(ROTATION);
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rotation) {
        return state.withProperty(ROTATION, rotation.rotate(state.getValue(ROTATION), 16));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        return state.withProperty(ROTATION, mirror.mirrorRotation(state.getValue(ROTATION), 16));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ROTATION);
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
