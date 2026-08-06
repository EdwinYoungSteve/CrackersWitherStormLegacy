package com.wdcftgg.witherstormmod.common.block;

import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DirectionalBlock extends BlockHorizontal {

    public DirectionalBlock(String name, Material material) {
        super(material);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        setRegistryName(name);
        setTranslationKey(name);
        setCreativeTab(ModCreativeTabs.MAIN);
        setHardness(1.0F);
        setResistance(5.0F);
        setSoundType(material == Material.WOOD ? SoundType.WOOD : SoundType.STONE);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos position, EnumFacing facing, float hitX, float hitY,
                                            float hitZ, int metadata, EntityLivingBase placer) {
        return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public IBlockState getStateFromMeta(int metadata) {
        EnumFacing facing = EnumFacing.byHorizontalIndex(metadata);
        return getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        String name = getRegistryName() == null ? "" : getRegistryName().getPath();
        return name.startsWith("tainted_zombie_") || "tainted_skeleton_wall".equals(name)
                || "tainted_skull_ceiling".equals(name) || "tainted_bone_pile".equals(name)
                ? BlockRenderLayer.CUTOUT : super.getRenderLayer();
    }
}
