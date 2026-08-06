package com.wdcftgg.witherstormmod.common.item;

import com.wdcftgg.witherstormmod.common.block.TaintedSlabBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.advancements.CriteriaTriggers;

public class SlabItem extends ItemBlock {

    private final TaintedSlabBlock slab;

    public SlabItem(TaintedSlabBlock slab) {
        super(slab);
        this.slab = slab;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                      EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.isEmpty()) return EnumActionResult.FAIL;

        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == slab) {
            TaintedSlabBlock.Half half = state.getValue(TaintedSlabBlock.HALF);
            boolean clickedJoinFace = half == TaintedSlabBlock.Half.BOTTOM && facing == EnumFacing.UP
                    || half == TaintedSlabBlock.Half.TOP && facing == EnumFacing.DOWN;
            if (clickedJoinFace && merge(player, world, pos, stack)) return EnumActionResult.SUCCESS;
        }

        BlockPos adjacent = pos.offset(facing);
        if (merge(player, world, adjacent, stack)) return EnumActionResult.SUCCESS;
        return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == slab && state.getValue(TaintedSlabBlock.HALF) != TaintedSlabBlock.Half.DOUBLE) {
            TaintedSlabBlock.Half half = state.getValue(TaintedSlabBlock.HALF);
            if (half == TaintedSlabBlock.Half.BOTTOM && side == EnumFacing.UP
                    || half == TaintedSlabBlock.Half.TOP && side == EnumFacing.DOWN) return true;
        }
        IBlockState adjacent = world.getBlockState(pos.offset(side));
        return adjacent.getBlock() == slab && adjacent.getValue(TaintedSlabBlock.HALF) != TaintedSlabBlock.Half.DOUBLE
                || super.canPlaceBlockOnSide(world, pos, side, player, stack);
    }

    private boolean merge(EntityPlayer player, World world, BlockPos pos, ItemStack stack) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() != slab || state.getValue(TaintedSlabBlock.HALF) == TaintedSlabBlock.Half.DOUBLE
                || !player.canPlayerEdit(pos, EnumFacing.UP, stack)) return false;

        IBlockState merged = slab.getDefaultState().withProperty(TaintedSlabBlock.HALF, TaintedSlabBlock.Half.DOUBLE);
        AxisAlignedBB collision = merged.getCollisionBoundingBox(world, pos);
        if (collision == Block.NULL_AABB || !world.checkNoEntityCollision(collision.offset(pos))) return false;
        if (!world.setBlockState(pos, merged, 11)) return false;

        SoundType sound = slab.getSoundType(merged, world, pos, player);
        world.playSound(player, pos, sound.getPlaceSound(), SoundCategory.BLOCKS,
                (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
        stack.shrink(1);
        if (player instanceof EntityPlayerMP) CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, pos, stack);
        return true;
    }
}
