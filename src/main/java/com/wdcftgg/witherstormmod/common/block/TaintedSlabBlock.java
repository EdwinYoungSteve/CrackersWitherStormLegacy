package com.wdcftgg.witherstormmod.common.block;

import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class TaintedSlabBlock extends Block {

    public static final PropertyEnum<Half> HALF = PropertyEnum.create("half", Half.class);
    private static final AxisAlignedBB BOTTOM = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);
    private static final AxisAlignedBB TOP = new AxisAlignedBB(0.0D, 0.5D, 0.0D, 1.0D, 1.0D, 1.0D);

    public TaintedSlabBlock(String name, Material material) {
        super(material);
        setDefaultState(blockState.getBaseState().withProperty(HALF, Half.BOTTOM));
        setRegistryName(name);
        setTranslationKey(name);
        setCreativeTab(ModCreativeTabs.MAIN);
        boolean wooden = material == Material.WOOD;
        boolean heavyStone = name.contains("tainted_stone_slab") || name.contains("tainted_cobblestone_slab");
        float hardness = wooden ? 2.0F : heavyStone ? 3.0F : 0.8F;
        float resistance = wooden ? 3.0F : heavyStone ? 6.0F : 0.8F;
        setHardness(hardness);
        setResistance(SimpleBlock.toLegacyResistance(resistance));
        setSoundType(material == Material.WOOD ? SoundType.WOOD : SoundType.STONE);
        useNeighborBrightness = true;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos position) {
        Half half = state.getValue(HALF);
        return half == Half.DOUBLE ? FULL_BLOCK_AABB : half == Half.TOP ? TOP : BOTTOM;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return state.getValue(HALF) == Half.DOUBLE;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return state.getValue(HALF) == Half.DOUBLE;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos position, EnumFacing facing, float hitX, float hitY,
                                            float hitZ, int metadata, EntityLivingBase placer) {
        Half half = facing == EnumFacing.DOWN || facing != EnumFacing.UP && hitY > 0.5F ? Half.TOP : Half.BOTTOM;
        return getDefaultState().withProperty(HALF, half);
    }

    @Override
    public IBlockState getStateFromMeta(int metadata) {
        Half half = metadata == 2 ? Half.DOUBLE : (metadata & 1) == 0 ? Half.BOTTOM : Half.TOP;
        return getDefaultState().withProperty(HALF, half);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        Half half = state.getValue(HALF);
        return half == Half.DOUBLE ? 2 : half == Half.TOP ? 1 : 0;
    }

    @Override
    public int quantityDropped(IBlockState state, int fortune, java.util.Random random) {
        return state.getValue(HALF) == Half.DOUBLE ? 2 : 1;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, HALF);
    }

    public enum Half implements IStringSerializable {
        BOTTOM("bottom"), TOP("top"), DOUBLE("double");

        private final String name;

        Half(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
