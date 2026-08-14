package com.wdcftgg.witherstormmod.common.block;

import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.IPlantable;

public class TaintedSandBlock extends BlockFalling {
    private static final int DUST_COLOR = 10708917;

    public TaintedSandBlock(String name, Material material) {
        super(material);
        setRegistryName(name);
        setTranslationKey(name);
        setCreativeTab(ModCreativeTabs.MAIN);
        setHardness(0.5F);
        setResistance(SimpleBlock.toLegacyResistance(0.5F));
        setSoundType(SoundType.SAND);
    }

    @Override
    public int getDustColor(IBlockState state) {
        return DUST_COLOR;
    }

    @Override
    public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos,
                                   EnumFacing direction, IPlantable plantable) {
        IBlockState plant = plantable.getPlant(world, pos.offset(direction));
        if (plant.getBlock() == Blocks.CACTUS) return true;
        if (plant.getBlock() == Blocks.REEDS) {
            for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
                IBlockState adjacent = world.getBlockState(pos.offset(facing));
                if (adjacent.getMaterial() == Material.WATER || adjacent.getBlock() == Blocks.FROSTED_ICE) {
                    return true;
                }
            }
            return false;
        }
        return super.canSustainPlant(state, world, pos, direction, plantable);
    }
}
