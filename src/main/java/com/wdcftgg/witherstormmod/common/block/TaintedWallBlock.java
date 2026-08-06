package com.wdcftgg.witherstormmod.common.block;

import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWall;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class TaintedWallBlock extends BlockWall {

    public TaintedWallBlock(String name, Block modelBlock) {
        super(modelBlock);
        setRegistryName(name);
        setTranslationKey(name);
        setCreativeTab(ModCreativeTabs.MAIN);
        boolean cobblestone = name.contains("cobblestone");
        setHardness(cobblestone ? 3.0F : 0.8F);
        setResistance(SimpleBlock.toLegacyResistance(cobblestone ? 6.0F : 0.8F));
        setSoundType(SoundType.STONE);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        items.add(new ItemStack(this, 1, 0));
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
    }

    @Override
    public IBlockState getStateFromMeta(int metadata) {
        return getDefaultState().withProperty(VARIANT, EnumType.NORMAL);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }
}
