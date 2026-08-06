package com.wdcftgg.witherstormmod.common.block;

import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class TaintedSandBlock extends BlockFalling {

    public TaintedSandBlock(String name, Material material) {
        super(material);
        setRegistryName(name);
        setTranslationKey(name);
        setCreativeTab(ModCreativeTabs.MAIN);
        setHardness(0.5F);
        setResistance(SimpleBlock.toLegacyResistance(0.5F));
        setSoundType(SoundType.SAND);
    }
}
