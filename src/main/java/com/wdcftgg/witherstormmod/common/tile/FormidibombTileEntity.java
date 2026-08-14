package com.wdcftgg.witherstormmod.common.tile;

import com.wdcftgg.witherstormmod.common.config.WitherStormConfig;
import com.wdcftgg.witherstormmod.common.entity.PowerfulExplosiveEntity;
import com.wdcftgg.witherstormmod.common.entity.FormidibombSource;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public class FormidibombTileEntity extends TileEntity implements ITickable, FormidibombSource {
    private int fuse = 1200;
    private int startFuse = 1200;
    private EntityLivingBase owner;

    @Override
    public void update() {
        if (world == null || startFuse <= 0) return;
        if (WitherStormConfig.formidibombFuseEnabled) --fuse;
        if (world.isRemote) return;
        if (fuse <= 0) {
            world.setBlockToAir(pos);
        } else if (shouldBecomeEntity(fuse, startFuse)) {
            prime();
        }
        if (WitherStormConfig.formidibombFuseEnabled) markDirty();
    }

    private void prime() {
        PowerfulExplosiveEntity.FormidibombEntity bomb = new PowerfulExplosiveEntity.FormidibombEntity(world,
                pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, owner, this,
                world.getBlockState(pos));
        if (world.spawnEntity(bomb)) {
            world.setBlockToAir(pos);
            world.playSound(null, pos, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
    }

    static boolean shouldBecomeEntity(int fuse, int startFuse) {
        return startFuse > 0 && fuse > 0 && fuse <= startFuse / 4;
    }

    public void setFuse(int fuse, int startFuse, EntityLivingBase owner) {
        this.fuse = fuse;
        this.startFuse = startFuse;
        this.owner = owner;
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
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        fuse = compound.hasKey("Fuse") ? compound.getInteger("Fuse") : 1200;
        startFuse = compound.hasKey("StartFuse") ? compound.getInteger("StartFuse") : fuse;
        owner = null;
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
