package com.wdcftgg.witherstormmod.common.block;

import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class LegacyTrapDoorBlock extends BlockTrapDoor {

    public LegacyTrapDoorBlock(String name) {
        super(Material.WOOD);
        setRegistryName(name);
        setTranslationKey(name);
        setCreativeTab(ModCreativeTabs.MAIN);
        setHardness(2.0F);
        setResistance(LegacyBlock.toLegacyResistance(3.0F));
        setSoundType(SoundType.WOOD);
    }
}
