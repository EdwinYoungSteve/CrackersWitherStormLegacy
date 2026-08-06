package com.wdcftgg.witherstormmod.common.block;

import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AxisBlock extends Block {

    public static final PropertyEnum<EnumFacing.Axis> AXIS = PropertyEnum.create("axis", EnumFacing.Axis.class);

    public AxisBlock(String name) {
        super(Material.WOOD);
        setDefaultState(blockState.getBaseState().withProperty(AXIS, EnumFacing.Axis.Y));
        setRegistryName(name);
        setTranslationKey(name);
        setCreativeTab(ModCreativeTabs.MAIN);
        setHardness(2.0F);
        setResistance(SimpleBlock.toLegacyResistance(3.0F));
        setSoundType(SoundType.WOOD);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos position, EnumFacing facing, float hitX, float hitY,
                                            float hitZ, int metadata, EntityLivingBase placer) {
        return getDefaultState().withProperty(AXIS, facing.getAxis());
    }

    @Override
    public IBlockState getStateFromMeta(int metadata) {
        EnumFacing.Axis axis = metadata == 0 ? EnumFacing.Axis.X : metadata == 2 ? EnumFacing.Axis.Z : EnumFacing.Axis.Y;
        return getDefaultState().withProperty(AXIS, axis);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        EnumFacing.Axis axis = state.getValue(AXIS);
        return axis == EnumFacing.Axis.X ? 0 : axis == EnumFacing.Axis.Z ? 2 : 1;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AXIS);
    }
}
