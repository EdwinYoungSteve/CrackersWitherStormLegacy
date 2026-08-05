package com.wdcftgg.witherstormmod.common.item;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class LegacyRarityBlockItem extends ItemBlock {

    private final EnumRarity rarity;

    public LegacyRarityBlockItem(Block block, EnumRarity rarity) {
        super(block);
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
