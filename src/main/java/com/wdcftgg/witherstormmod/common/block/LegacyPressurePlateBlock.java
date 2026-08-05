package com.wdcftgg.witherstormmod.common.block;

import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import net.minecraft.block.BlockPressurePlate;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class LegacyPressurePlateBlock extends BlockPressurePlate {

    public LegacyPressurePlateBlock(String name, Material material, Sensitivity sensitivity) {
        super(material, sensitivity);
        setRegistryName(name);
        setTranslationKey(name);
        setCreativeTab(ModCreativeTabs.MAIN);
        setHardness(0.5F);
        setResistance(LegacyBlock.toLegacyResistance(0.5F));
        setSoundType(material == Material.WOOD ? SoundType.WOOD : SoundType.STONE);
    }
}
