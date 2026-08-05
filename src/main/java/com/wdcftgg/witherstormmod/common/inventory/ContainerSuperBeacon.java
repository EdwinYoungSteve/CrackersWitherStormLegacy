package com.wdcftgg.witherstormmod.common.inventory;

import com.wdcftgg.witherstormmod.common.beacon.LegacySuperBeaconLogic;
import com.wdcftgg.witherstormmod.common.tile.TileEntityAbstractSuperBeacon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;

import java.util.Set;

public class ContainerSuperBeacon extends Container {
    public static final int DATA_SIZE = 4;

    private final TileEntityAbstractSuperBeacon beacon;
    private final int[] cached = new int[DATA_SIZE];

    public ContainerSuperBeacon(TileEntityAbstractSuperBeacon beacon) {
        this.beacon = beacon;
        for (int i = 0; i < DATA_SIZE; i++) cached[i] = beacon.getField(i);
    }

    public TileEntityAbstractSuperBeacon getBeacon() {
        return beacon;
    }

    public int getLevel() {
        return cached[0];
    }

    public Potion getPrimaryEffect() {
        return cached[1] < 0 ? null : Potion.getPotionById(cached[1]);
    }

    public boolean shouldShowArea() {
        return cached[2] == 1;
    }

    public int getCooldown() {
        return cached[3];
    }

    public Set<Potion> getValidEffects() {
        return beacon.getValidEffects();
    }

    public boolean requestEffect(EntityPlayerMP player, int effectId) {
        Potion requested = effectId < 0 ? null : Potion.getPotionById(effectId);
        if (effectId >= 0 && requested == null) return false;
        if (requested != null && !beacon.getValidEffects().contains(requested)) return false;
        if (!LegacySuperBeaconLogic.canChangeEffect(beacon.getCooldown(), requested == null)) return false;

        Potion previous = beacon.getEffect();
        if (requested != null && requested != previous) {
            beacon.doPowerUp(player);
            beacon.setCooldown(LegacySuperBeaconLogic.EFFECT_CHANGE_COOLDOWN);
        }
        boolean changed = beacon.setEffect(requested);
        if (changed) detectAndSendChanges();
        return changed;
    }

    public void setShowArea(boolean show) {
        beacon.setShowWorkingArea(show);
        detectAndSendChanges();
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        for (int i = 0; i < DATA_SIZE; i++) listener.sendWindowProperty(this, i, beacon.getField(i));
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (int i = 0; i < DATA_SIZE; i++) {
            int current = beacon.getField(i);
            if (current == cached[i]) continue;
            for (IContainerListener listener : listeners) listener.sendWindowProperty(this, i, current);
            cached[i] = current;
        }
    }

    @Override
    public void updateProgressBar(int id, int data) {
        if (id >= 0 && id < DATA_SIZE) {
            cached[id] = data;
            beacon.setField(id, data);
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return beacon.getWorld() != null && beacon.getWorld().getTileEntity(beacon.getPos()) == beacon
                && player.getDistanceSq(beacon.getPos()) <= 64.0D;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return ItemStack.EMPTY;
    }
}
