package com.wdcftgg.witherstormmod.common.item;

import com.wdcftgg.witherstormmod.common.init.ModBlocks;
import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class TaintedSignItem extends Item {

    public TaintedSignItem(String name) {
        setCreativeTab(ModCreativeTabs.MAIN);
        setMaxStackSize(16);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing,
                                      float hitX, float hitY, float hitZ) {
        IBlockState clicked = world.getBlockState(pos);
        boolean replaceable = clicked.getBlock().isReplaceable(world, pos);
        if (facing == EnumFacing.DOWN || (!clicked.getMaterial().isSolid() && !replaceable) || replaceable && facing != EnumFacing.UP) {
            return EnumActionResult.FAIL;
        }
        BlockPos placePos = replaceable ? pos : pos.offset(facing);
        ItemStack stack = player.getHeldItem(hand);
        if (!player.canPlayerEdit(placePos, facing, stack)
                || !ModBlocks.get("tainted_sign").canPlaceBlockAt(world, placePos)) {
            return EnumActionResult.FAIL;
        }
        if (world.isRemote) return EnumActionResult.SUCCESS;

        if (facing == EnumFacing.UP) {
            int rotation = MathHelper.floor((player.rotationYaw + 180.0F) * 16.0F / 360.0F + 0.5D) & 15;
            world.setBlockState(placePos, ModBlocks.get("tainted_sign").getDefaultState()
                    .withProperty(BlockStandingSign.ROTATION, rotation), 11);
        } else {
            world.setBlockState(placePos, ModBlocks.get("tainted_wall_sign").getDefaultState()
                    .withProperty(BlockWallSign.FACING, facing), 11);
        }
        TileEntity tile = world.getTileEntity(placePos);
        if (tile instanceof TileEntitySign && !ItemBlock.setTileEntityNBT(world, player, placePos, stack)) {
            player.openEditSign((TileEntitySign) tile);
        }
        if (player instanceof EntityPlayerMP) CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, placePos, stack);
        if (!player.capabilities.isCreativeMode) stack.shrink(1);
        return EnumActionResult.SUCCESS;
    }
}
