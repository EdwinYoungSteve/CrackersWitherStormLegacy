package com.wdcftgg.witherstormmod.common.item;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class TaintedCarvedPumpkinItem extends ItemBlock {
    public TaintedCarvedPumpkinItem(Block block) {
        super(block);
        setMaxStackSize(64);
    }

    @Override
    public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot slot, Entity entity) {
        return slot == EntityEquipmentSlot.HEAD;
    }

    @Override
    public EntityEquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EntityEquipmentSlot.HEAD;
    }
}
