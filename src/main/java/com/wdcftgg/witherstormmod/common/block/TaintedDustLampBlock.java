package com.wdcftgg.witherstormmod.common.block;

import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** 红石控制的病化尘土灯，保留上游按状态发光的语义。 */
public class TaintedDustLampBlock extends Block {

    public static final PropertyBool POWERED = PropertyBool.create("powered");
    private static final int LIGHT_LEVEL = 12;

    public TaintedDustLampBlock(String name) {
        super(Material.ROCK);
        setRegistryName(name);
        setTranslationKey(name);
        setCreativeTab(ModCreativeTabs.MAIN);
        setHardness(1.2F);
        // Cleanroom 1.12.2 将 setResistance 参数按 0.6 倍暴露给爆炸计算，传入 2.0 才对应上游 1.2。
        setResistance(2.0F);
        setHarvestLevel("pickaxe", 0);
        setSoundType(SoundType.STONE);
        setDefaultState(blockState.getBaseState().withProperty(POWERED, Boolean.FALSE));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, POWERED);
    }

    @Override
    public IBlockState getStateFromMeta(int metadata) {
        return getDefaultState().withProperty(POWERED, (metadata & 1) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(POWERED) ? 1 : 0;
    }

    @Override
    public int getLightValue(IBlockState state) {
        return state.getValue(POWERED) ? LIGHT_LEVEL : 0;
    }

    @Override
    public void onBlockAdded(World world, BlockPos position, IBlockState state) {
        super.onBlockAdded(world, position, state);
        updatePoweredState(world, position, state);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos position,
                                Block neighborBlock, BlockPos neighborPosition) {
        updatePoweredState(world, position, state);
    }

    private void updatePoweredState(World world, BlockPos position, IBlockState state) {
        if (world.isRemote) return;
        boolean powered = world.isBlockPowered(position);
        if (powered != state.getValue(POWERED)) {
            world.setBlockState(position, state.withProperty(POWERED, powered), 3);
        }
    }
}
