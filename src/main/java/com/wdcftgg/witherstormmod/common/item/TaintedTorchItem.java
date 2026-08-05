package com.wdcftgg.witherstormmod.common.item;

import com.wdcftgg.witherstormmod.common.init.ModBlocks;
import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TaintedTorchItem extends Item {
    public TaintedTorchItem(String name) {
        setCreativeTab(ModCreativeTabs.MAIN);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing,
                                      float hitX, float hitY, float hitZ) {
        IBlockState clicked = world.getBlockState(pos);
        if (!clicked.getBlock().isReplaceable(world, pos)) pos = pos.offset(facing);
        if (!player.canPlayerEdit(pos, facing, player.getHeldItem(hand)) || facing == EnumFacing.DOWN) return EnumActionResult.FAIL;
        IBlockState placed = facing == EnumFacing.UP
                ? ModBlocks.get("tainted_torch").getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.UP)
                : ModBlocks.get("tainted_wall_torch").getDefaultState().withProperty(BlockTorch.FACING, facing);
        if (!world.setBlockState(pos, placed, 11)) return EnumActionResult.FAIL;
        ItemStack stack = player.getHeldItem(hand);
        if (!player.capabilities.isCreativeMode) stack.shrink(1);
        return EnumActionResult.SUCCESS;
    }
}
