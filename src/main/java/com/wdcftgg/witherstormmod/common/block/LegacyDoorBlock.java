package com.wdcftgg.witherstormmod.common.block;

import net.minecraft.block.BlockDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class LegacyDoorBlock extends BlockDoor {

    public LegacyDoorBlock(String name) {
        super(Material.WOOD);
        setRegistryName(name);
        setTranslationKey(name);
        setHardness(2.0F);
        setResistance(LegacyBlock.toLegacyResistance(3.0F));
        setSoundType(SoundType.WOOD);
    }
}
