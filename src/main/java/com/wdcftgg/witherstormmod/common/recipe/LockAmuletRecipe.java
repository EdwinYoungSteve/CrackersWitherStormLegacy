package com.wdcftgg.witherstormmod.common.recipe;

import com.wdcftgg.witherstormmod.common.item.AmuletItem;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

public final class LockAmuletRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    @Override
    public boolean matches(InventoryCrafting inventory, World world) {
        boolean matches = true;
        int amulets = 0;
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            boolean amulet = stack.getItem() instanceof AmuletItem;
            if (amulet) amulets++;
            if ((!stack.isEmpty() && !amulet) || AmuletItem.isLocked(stack) || amulets > 1) {
                matches = false;
            }
        }
        return matches;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventory) {
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.getItem() instanceof AmuletItem) {
                ItemStack result = stack.copy();
                NBTTagCompound tag = result.getTagCompound();
                if (tag == null) {
                    tag = new NBTTagCompound();
                    result.setTagCompound(tag);
                }
                tag.setBoolean(AmuletItem.LOCKED_TAG, true);
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }
}
