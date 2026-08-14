package com.wdcftgg.witherstormmod.common.item;

import com.wdcftgg.witherstormmod.common.init.ModBlocks;
import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TaintedTorchItem extends Item {
    public TaintedTorchItem(String name) {
        setCreativeTab(ModCreativeTabs.MAIN);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing,
                                      float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.isEmpty() || facing == EnumFacing.DOWN) return EnumActionResult.FAIL;

        IBlockState clicked = world.getBlockState(pos);
        if (!clicked.getBlock().isReplaceable(world, pos)) pos = pos.offset(facing);
        if (!player.canPlayerEdit(pos, facing, stack)) return EnumActionResult.FAIL;

        Block placedBlock = facing == EnumFacing.UP
                ? ModBlocks.get("tainted_torch") : ModBlocks.get("tainted_wall_torch");
        if (!placedBlock.canPlaceBlockOnSide(world, pos, facing)
                || !world.mayPlace(placedBlock, pos, false, facing, player)) {
            return EnumActionResult.FAIL;
        }
        IBlockState placed = placedBlock.getDefaultState().withProperty(BlockTorch.FACING, facing);
        if (!world.setBlockState(pos, placed, 11)) return EnumActionResult.FAIL;

        IBlockState actual = world.getBlockState(pos);
        if (actual.getBlock() == placedBlock) {
            ItemBlock.setTileEntityNBT(world, player, pos, stack);
            placedBlock.onBlockPlacedBy(world, pos, actual, player, stack);
            if (player instanceof EntityPlayerMP) {
                CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, pos, stack);
            }
        }
        SoundType sound = placedBlock.getSoundType(actual, world, pos, player);
        world.playSound(player, pos, sound.getPlaceSound(), SoundCategory.BLOCKS,
                (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
        stack.shrink(1);
        return EnumActionResult.SUCCESS;
    }
}
