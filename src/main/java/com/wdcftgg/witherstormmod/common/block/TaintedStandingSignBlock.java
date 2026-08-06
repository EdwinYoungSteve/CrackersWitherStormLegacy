package com.wdcftgg.witherstormmod.common.block;

import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import com.wdcftgg.witherstormmod.common.tile.TaintedSignTileEntity;
import net.minecraft.block.BlockStandingSign;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TaintedStandingSignBlock extends BlockStandingSign {
    public TaintedStandingSignBlock(String name) {
        setRegistryName(name);
        setTranslationKey(name);
        setCreativeTab(ModCreativeTabs.MAIN);
        setHardness(1.0F);
    }

    @Override public TileEntity createNewTileEntity(World world, int metadata) { return new TaintedSignTileEntity(); }
}
