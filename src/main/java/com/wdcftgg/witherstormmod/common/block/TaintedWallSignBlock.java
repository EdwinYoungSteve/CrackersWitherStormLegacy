package com.wdcftgg.witherstormmod.common.block;

import com.wdcftgg.witherstormmod.common.tile.TaintedSignTileEntity;
import net.minecraft.block.BlockWallSign;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TaintedWallSignBlock extends BlockWallSign {
    public TaintedWallSignBlock(String name) {
        setRegistryName(name);
        setTranslationKey(name);
        setHardness(1.0F);
    }

    @Override public TileEntity createNewTileEntity(World world, int metadata) { return new TaintedSignTileEntity(); }
}
