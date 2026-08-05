package com.wdcftgg.witherstormmod.common.item;

import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;

public class LegacyFoiledItem extends LegacyItem {

    public LegacyFoiledItem(String name, EnumRarity rarity) {
        super(name, rarity);
        setMaxStackSize(1);
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }
}
