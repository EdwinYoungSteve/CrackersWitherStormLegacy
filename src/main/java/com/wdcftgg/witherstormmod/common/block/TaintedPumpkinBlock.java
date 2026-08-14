package com.wdcftgg.witherstormmod.common.block;

import com.wdcftgg.witherstormmod.common.init.ModBlocks;
import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class TaintedPumpkinBlock extends Block {

    public TaintedPumpkinBlock(String name) {
        super(Material.GOURD, MapColor.ADOBE);
        setRegistryName(name);
        setTranslationKey(name);
        setCreativeTab(ModCreativeTabs.MAIN);
        setHardness(1.0F);
        setSoundType(SoundType.WOOD);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos position, IBlockState state, EntityPlayer player,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (!(stack.getItem() instanceof ItemShears)) {
            return false;
        }
        if (!world.isRemote) {
            Block carvedBlock = ModBlocks.get("tainted_carved_pumpkin");
            if (!(carvedBlock instanceof TaintedCarvedPumpkinBlock)) {
                throw new IllegalStateException("Missing tainted carved pumpkin block");
            }
            EnumFacing carvedFacing = getCarvedFacing(facing, player.getHorizontalFacing());
            world.playSound(null, position, SoundEvents.ENTITY_SHEEP_SHEAR,
                    SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.setBlockState(position,
                    carvedBlock.getDefaultState().withProperty(BlockHorizontal.FACING, carvedFacing), 11);
            stack.damageItem(1, player);
            player.addStat(StatList.getObjectUseStats(Items.SHEARS));
        }
        return true;
    }

    static EnumFacing getCarvedFacing(EnumFacing clickedFace, EnumFacing playerFacing) {
        return clickedFace.getAxis() == EnumFacing.Axis.Y ? playerFacing.getOpposite() : clickedFace;
    }

}
