package com.wdcftgg.witherstormmod.common.block;

import com.wdcftgg.witherstormmod.common.tile.TileEntityTaintedSign;
import net.minecraft.block.BlockWallSign;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockTaintedWallSign extends BlockWallSign {
    public BlockTaintedWallSign(String name) {
        setRegistryName(name);
        setTranslationKey(name);
        setHardness(1.0F);
    }

    @Override public TileEntity createNewTileEntity(World world, int metadata) { return new TileEntityTaintedSign(); }
}
