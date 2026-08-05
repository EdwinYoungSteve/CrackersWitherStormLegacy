package com.wdcftgg.witherstormmod.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import com.wdcftgg.witherstormmod.common.network.LegacyNetwork;

import java.util.List;
import net.minecraft.nbt.NBTTagCompound;

public abstract class EntityPowerfulExplosive extends EntityTNTPrimed {

    protected EntityPowerfulExplosive(World world) {
        super(world);
    }

    protected EntityPowerfulExplosive(World world, double positionX, double positionY, double positionZ, EntityLivingBase igniter) {
        super(world, positionX, positionY, positionZ, igniter);
    }

    protected abstract float getExplosionStrength();

    protected boolean causesFire() {
        return true;
    }

    protected void beforeExplosion() {
    }

    protected void explode() {
        world.newExplosion(this, posX, posY + height / 16.0F, posZ, getExplosionStrength(), causesFire(), true);
    }

    @Override
    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        if (!hasNoGravity()) {
            motionY -= 0.03999999910593033D;
        }
        move(MoverType.SELF, motionX, motionY, motionZ);
        motionX *= 0.9800000190734863D;
        motionY *= 0.9800000190734863D;
        motionZ *= 0.9800000190734863D;
        if (onGround) {
            motionX *= 0.699999988079071D;
            motionZ *= 0.699999988079071D;
            motionY *= -0.5D;
        }
        setFuse(getFuse() - 1);
        if (getFuse() <= 0) {
            setDead();
            if (!world.isRemote) {
                beforeExplosion();
                explode();
            }
        } else {
            handleWaterMovement();
            world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, posX, posY + 0.5D, posZ, 0.0D, 0.02D, 0.0D);
        }
    }

    public static class SuperTnt extends EntityPowerfulExplosive {
        public SuperTnt(World world) { super(world); }
        public SuperTnt(World world, double positionX, double positionY, double positionZ, EntityLivingBase igniter) { super(world, positionX, positionY, positionZ, igniter); }
        @Override protected float getExplosionStrength() { return 12.0F; }
    }

    public static class Formidibomb extends EntityPowerfulExplosive implements LegacyFormidibombSource {
        private static final DataParameter<Integer> START_FUSE = EntityDataManager.createKey(
                Formidibomb.class, DataSerializers.VARINT);

        public Formidibomb(World world) {
            super(world);
            setFuse(1200);
            setStartFuse(1200);
        }

        public Formidibomb(World world, double positionX, double positionY, double positionZ,
                           EntityLivingBase igniter) {
            super(world, positionX, positionY, positionZ, igniter);
            setFuse(1200);
            setStartFuse(1200);
        }

        @Override
        protected void entityInit() {
            super.entityInit();
            dataManager.register(START_FUSE, 1200);
        }

        @Override protected float getExplosionStrength() { return 32.0F; }

        public void setStartFuse(int startFuse) { dataManager.set(START_FUSE, Math.max(1, startFuse)); }
        @Override public int getStartFuse() { return dataManager.get(START_FUSE); }
        @Override public int getFuseLife() { return getFuse(); }
        @Override public Vec3d getFormidibombPosition() { return getPositionVector(); }
        @Override public boolean isFormidibombAlive() { return !isDead; }

        @Override
        protected void writeEntityToNBT(NBTTagCompound compound) {
            super.writeEntityToNBT(compound);
            compound.setInteger("StartFuse", getStartFuse());
        }

        @Override
        protected void readEntityFromNBT(NBTTagCompound compound) {
            super.readEntityFromNBT(compound);
            setStartFuse(compound.hasKey("StartFuse")
                    ? compound.getInteger("StartFuse") : Math.max(1, getFuse()));
        }

        @Override
        protected void explode() {
            LegacyFormidibombExplosion.explode(world, this, 48 + world.rand.nextInt(9), 3, posX, posY, posZ);
            LegacyNetwork.shakeNear(world, posX, posY, posZ, 100.0D, 480.0F, 24.0F);
        }
    }
}
