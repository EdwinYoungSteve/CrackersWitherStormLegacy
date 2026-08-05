package com.wdcftgg.witherstormmod.common.block;

import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;

public class LegacyStairsBlock extends BlockStairs {

    private final float modernExplosionResistance;

    public LegacyStairsBlock(String name, IBlockState modelState, float hardness, float resistance) {
        super(modelState);
        modernExplosionResistance = resistance;
        setRegistryName(name);
        setTranslationKey(name);
        setCreativeTab(ModCreativeTabs.MAIN);
        setHardness(hardness);
        setResistance(LegacyBlock.toLegacyResistance(resistance));
        setSoundType(modelState.getMaterial() == Material.WOOD ? SoundType.WOOD : SoundType.STONE);
        useNeighborBrightness = true;
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return modernExplosionResistance;
    }
}
