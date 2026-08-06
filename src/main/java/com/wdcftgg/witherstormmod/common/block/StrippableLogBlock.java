package com.wdcftgg.witherstormmod.common.block;

import com.wdcftgg.witherstormmod.common.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class StrippableLogBlock extends AxisBlock {

    private final String strippedBlockName;

    public StrippableLogBlock(String name, String strippedBlockName) {
        super(name);
        this.strippedBlockName = strippedBlockName;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos position, IBlockState state, EntityPlayer player,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack tool = player.getHeldItem(hand);
        if (!isAxe(tool)) {
            return false;
        }
        if (!world.isRemote) {
            world.setBlockState(position, getStrippedState(state), 11);
            world.playSound(null, position, SoundEvents.BLOCK_WOOD_HIT,
                    SoundCategory.BLOCKS, 1.0F, 1.0F);
            if (!player.capabilities.isCreativeMode) {
                tool.damageItem(1, player);
            }
        }
        return true;
    }

    IBlockState getStrippedState(IBlockState originalState) {
        Block strippedBlock = ModBlocks.get(strippedBlockName);
        if (!(strippedBlock instanceof AxisBlock)) {
            throw new IllegalStateException("Missing stripped axis block " + strippedBlockName);
        }
        return strippedBlock.getDefaultState().withProperty(AXIS, originalState.getValue(AXIS));
    }

    static boolean isAxe(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem().getToolClasses(stack).contains("axe");
    }
}
