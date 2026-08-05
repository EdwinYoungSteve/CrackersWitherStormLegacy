package com.wdcftgg.witherstormmod.common.item;

import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;

public class WitheredNetherStarItem extends LegacyItem {
    public WitheredNetherStarItem(String name) {
        super(name, EnumRarity.EPIC);
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }

}
