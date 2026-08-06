package com.wdcftgg.witherstormmod.common.item;

import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class CommandBlockShovelItem extends ItemSpade {

    public CommandBlockShovelItem(String name, ToolMaterial material) {
        this(name, material, 1.5F, -3.0F);
    }

    public CommandBlockShovelItem(String name, ToolMaterial material, float attackDamage, float attackSpeed) {
        super(material);
        this.attackDamage = attackDamage + material.getAttackDamage();
        this.attackSpeed = attackSpeed;
        setRegistryName(name);
        setTranslationKey(name);
        setCreativeTab(ModCreativeTabs.MAIN);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.EPIC;
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return FireResistantItemEntity.isFireResistant(stack);
    }

    @Override
    public Entity createEntity(World world, Entity location, ItemStack stack) {
        return hasCustomEntity(stack) ? FireResistantItemEntity.create(world, location, stack) : null;
    }
}
