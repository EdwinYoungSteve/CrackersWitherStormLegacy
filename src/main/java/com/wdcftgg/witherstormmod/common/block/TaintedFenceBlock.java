package com.wdcftgg.witherstormmod.common.block;

import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import net.minecraft.block.BlockFence;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MapColor;

public class TaintedFenceBlock extends BlockFence {

    public TaintedFenceBlock(String name, Material material, MapColor mapColor) {
        super(material, mapColor);
        setRegistryName(name);
        setTranslationKey(name);
        setCreativeTab(ModCreativeTabs.MAIN);
        setHardness(2.0F);
        setResistance(5.0F);
        setSoundType(SoundType.WOOD);
    }
}
