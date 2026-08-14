package com.wdcftgg.witherstormmod.common.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntitySign;

public class TaintedSignTileEntity extends TileEntitySign {
    private boolean hangingAttachmentSet;
    private boolean hangingAttached;

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (hangingAttachmentSet) compound.setBoolean("HangingAttached", hangingAttached);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        hangingAttachmentSet = compound.hasKey("HangingAttached", 1);
        hangingAttached = hangingAttachmentSet && compound.getBoolean("HangingAttached");
    }

    @Override
    public void onDataPacket(NetworkManager network, SPacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
    }

    public boolean hasHangingAttachmentOverride() {
        return hangingAttachmentSet;
    }

    public boolean isHangingAttached() {
        return hangingAttached;
    }

    public void setHangingAttached(boolean attached) {
        hangingAttachmentSet = true;
        hangingAttached = attached;
        markDirty();
        if (world != null) {
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
    }
}
