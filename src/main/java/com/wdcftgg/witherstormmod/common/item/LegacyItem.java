package com.wdcftgg.witherstormmod.common.item;

import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class LegacyItem extends Item {

    private final EnumRarity rarity;

    public LegacyItem(String name) {
        this(name, EnumRarity.COMMON);
    }

    public LegacyItem(String name, EnumRarity rarity) {
        setRegistryName(name);
        setTranslationKey(name);
        setCreativeTab(ModCreativeTabs.MAIN);
        this.rarity = rarity;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return rarity;
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return LegacyFireResistantItemEntity.isFireResistant(stack);
    }

    @Override
    public Entity createEntity(World world, Entity location, ItemStack stack) {
        return hasCustomEntity(stack) ? LegacyFireResistantItemEntity.create(world, location, stack) : null;
    }
}
