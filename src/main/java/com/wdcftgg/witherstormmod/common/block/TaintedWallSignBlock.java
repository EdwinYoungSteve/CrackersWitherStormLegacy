package com.wdcftgg.witherstormmod.common.block;

import com.wdcftgg.witherstormmod.common.init.ModBlocks;
import com.wdcftgg.witherstormmod.common.tile.TaintedSignTileEntity;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class TaintedWallSignBlock extends BlockWallSign {
    public TaintedWallSignBlock(String name) {
        setRegistryName(name);
        setTranslationKey(name);
        setHardness(1.0F);
        setResistance(1.0F);
        setSoundType(SoundType.WOOD);
    }

    @Override public TileEntity createNewTileEntity(World world, int metadata) { return new TaintedSignTileEntity(); }

    @Override public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Item.getItemFromBlock(ModBlocks.get("tainted_sign"));
    }

    @Override public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return new ItemStack(Item.getItemFromBlock(ModBlocks.get("tainted_sign")));
    }
}
