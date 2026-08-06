package com.wdcftgg.witherstormmod.common.tile;

import com.wdcftgg.witherstormmod.common.entity.PowerfulExplosiveEntity;
import com.wdcftgg.witherstormmod.common.entity.FormidibombSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.init.SoundEvents;
import net.minecraft.world.WorldServer;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

import java.util.UUID;

public class FormidibombTileEntity extends TileEntity implements ITickable, FormidibombSource {
    private int fuse = 1200;
    private int startFuse = 1200;
    private UUID ownerUuid;

    @Override
    public void update() {
        if (world == null || fuse <= 0) return;
        --fuse;
        if (world.isRemote) return;
        if (shouldBecomeEntity(fuse, startFuse)) prime();
        markDirty();
    }

    private void prime() {
        EntityLivingBase owner = null;
        if (ownerUuid != null && world instanceof WorldServer) {
            Entity entity = ((WorldServer) world).getEntityFromUuid(ownerUuid);
            if (entity instanceof EntityLivingBase) owner = (EntityLivingBase) entity;
        }
        PowerfulExplosiveEntity.FormidibombEntity bomb = new PowerfulExplosiveEntity.FormidibombEntity(world,
                pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, owner);
        bomb.setFuse(Math.max(1, fuse));
        bomb.setStartFuse(startFuse);
        if (world.spawnEntity(bomb)) {
            world.setBlockToAir(pos);
            world.playSound(null, pos, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
    }

    static boolean shouldBecomeEntity(int fuse, int startFuse) {
        return startFuse > 0 && fuse <= Math.max(1, startFuse / 4);
    }

    public void setFuse(int fuse, int startFuse, EntityLivingBase owner) {
        this.fuse = Math.max(0, fuse);
        this.startFuse = Math.max(1, startFuse);
        this.ownerUuid = owner == null ? null : owner.getUniqueID();
        markDirty();
        if (world != null && !world.isRemote) {
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
    }

    @Override public int getFuseLife() { return fuse; }
    @Override public int getStartFuse() { return startFuse; }
    @Override public Vec3d getFormidibombPosition() { return new Vec3d(pos); }
    @Override public boolean isFormidibombAlive() {
        return !isInvalid() && world != null && world.getTileEntity(pos) == this;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("Fuse", fuse);
        compound.setInteger("StartFuse", startFuse);
        if (ownerUuid != null) compound.setUniqueId("Owner", ownerUuid);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        fuse = compound.hasKey("Fuse") ? compound.getInteger("Fuse") : 1200;
        startFuse = compound.hasKey("StartFuse") ? compound.getInteger("StartFuse") : fuse;
        ownerUuid = compound.hasUniqueId("Owner") ? compound.getUniqueId("Owner") : null;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager networkManager, SPacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
    }
}
