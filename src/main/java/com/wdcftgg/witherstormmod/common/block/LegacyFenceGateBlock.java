package com.wdcftgg.witherstormmod.common.block;

import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.SoundType;

public class LegacyFenceGateBlock extends BlockFenceGate {

    public LegacyFenceGateBlock(String name) {
        super(BlockPlanks.EnumType.OAK);
        setRegistryName(name);
        setTranslationKey(name);
        setCreativeTab(ModCreativeTabs.MAIN);
        setHardness(2.0F);
        setResistance(5.0F);
        setSoundType(SoundType.WOOD);
    }
}
