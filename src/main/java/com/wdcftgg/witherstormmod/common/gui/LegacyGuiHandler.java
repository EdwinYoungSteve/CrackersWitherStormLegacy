package com.wdcftgg.witherstormmod.common.gui;

import com.wdcftgg.witherstormmod.WitherStormMod;
import com.wdcftgg.witherstormmod.common.inventory.ContainerWitheredPhlegm;
import com.wdcftgg.witherstormmod.common.inventory.ContainerSuperBeacon;
import com.wdcftgg.witherstormmod.common.tile.TileEntityAbstractSuperBeacon;
import com.wdcftgg.witherstormmod.common.tile.TileEntityWitheredPhlegm;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class LegacyGuiHandler implements IGuiHandler {
    public static final int WITHERED_PHLEGM = 0;
    public static final int SUPER_BEACON = 1;

    @Nullable
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        if (id == WITHERED_PHLEGM && tile instanceof TileEntityWitheredPhlegm) {
            return new ContainerWitheredPhlegm(player.inventory, (TileEntityWitheredPhlegm) tile);
        }
        return id == SUPER_BEACON && tile instanceof TileEntityAbstractSuperBeacon
                ? new ContainerSuperBeacon((TileEntityAbstractSuperBeacon) tile) : null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        if (id == WITHERED_PHLEGM && tile instanceof TileEntityWitheredPhlegm) {
            TileEntityWitheredPhlegm phlegm = (TileEntityWitheredPhlegm) tile;
            return WitherStormMod.proxy.createWitheredPhlegmGui(player, phlegm);
        }
        if (id == SUPER_BEACON && tile instanceof TileEntityAbstractSuperBeacon) {
            return WitherStormMod.proxy.createSuperBeaconGui(player, (TileEntityAbstractSuperBeacon) tile);
        }
        return null;
    }
}
