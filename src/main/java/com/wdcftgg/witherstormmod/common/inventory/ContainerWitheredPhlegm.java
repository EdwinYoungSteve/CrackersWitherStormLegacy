package com.wdcftgg.witherstormmod.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerWitheredPhlegm extends Container {
    public static final int PHLEGM_SLOTS = 25;

    private final IInventory inventory;
    private int xp;

    public ContainerWitheredPhlegm(InventoryPlayer playerInventory, IInventory inventory) {
        this.inventory = inventory;
        inventory.openInventory(playerInventory.player);

        int slot = 0;
        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 5; column++) {
                addSlotToContainer(new Slot(inventory, slot++, 44 + column * 18, 18 + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlotToContainer(new Slot(playerInventory, column + row * 9 + 9,
                        8 + column * 18, 125 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlotToContainer(new Slot(playerInventory, column, 8 + column * 18, 183));
        }
        xp = inventory.getField(0);
    }

    public IInventory getPhlegmInventory() {
        return inventory;
    }

    public int getXp() {
        return xp;
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendWindowProperty(this, 0, inventory.getField(0));
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        int currentXp = inventory.getField(0);
        if (currentXp != xp) {
            for (IContainerListener listener : listeners) listener.sendWindowProperty(this, 0, currentXp);
            xp = currentXp;
        }
    }

    @Override
    public void updateProgressBar(int id, int data) {
        if (id == 0) xp = data;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return inventory.isUsableByPlayer(player);
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        inventory.closeInventory(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) return result;

        ItemStack stack = slot.getStack();
        result = stack.copy();
        if (index < PHLEGM_SLOTS) {
            if (!mergeItemStack(stack, PHLEGM_SLOTS, inventorySlots.size(), true)) return ItemStack.EMPTY;
        } else if (!mergeItemStack(stack, 0, PHLEGM_SLOTS, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.putStack(ItemStack.EMPTY);
        else slot.onSlotChanged();
        return result;
    }
}
