package com.wdcftgg.witherstormmod.common.block;

import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class TaintedTrapdoorBlock extends BlockTrapDoor {

    public TaintedTrapdoorBlock(String name) {
        super(Material.WOOD);
        setRegistryName(name);
        setTranslationKey(name);
        setCreativeTab(ModCreativeTabs.MAIN);
        setHardness(2.0F);
        setResistance(SimpleBlock.toLegacyResistance(3.0F));
        setSoundType(SoundType.WOOD);
    }
}
