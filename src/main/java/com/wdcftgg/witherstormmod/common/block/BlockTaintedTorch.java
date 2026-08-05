package com.wdcftgg.witherstormmod.common.block;

import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BlockTaintedTorch extends BlockTorch {

    private final boolean wall;

    public BlockTaintedTorch(String name, boolean wall) {
        this.wall = wall;
        if (wall) setDefaultState(getDefaultState().withProperty(FACING, EnumFacing.NORTH));
        setRegistryName(name);
        setTranslationKey(name);
        if (!wall) setCreativeTab(ModCreativeTabs.MAIN);
        setLightLevel(8.0F / 15.0F);
        setSoundType(SoundType.WOOD);
    }

    public boolean isWallTorch() {
        return wall;
    }

    @Override
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
        EnumFacing facing = state.getValue(FACING);
        double x = pos.getX() + 0.5D + random.nextGaussian() * 0.1D;
        double y = pos.getY() + 0.7D + random.nextGaussian() * 0.1D;
        double z = pos.getZ() + 0.5D + random.nextGaussian() * 0.1D;
        if (facing.getAxis().isHorizontal()) {
            EnumFacing opposite = facing.getOpposite();
            x += 0.27D * opposite.getXOffset();
            y += 0.22D;
            z += 0.27D * opposite.getZOffset();
        }
        world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, 0.0D, 0.0D, 0.0D);
        world.spawnParticle(EnumParticleTypes.SPELL_WITCH, x, y, z, 0.0D, 0.01D, 0.0D);
    }
}
