package com.wdcftgg.witherstormmod.common.tile;

import com.wdcftgg.witherstormmod.common.beacon.SuperBeaconLogic;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

public class SuperSupportBeaconTileEntity extends AbstractSuperBeaconTileEntity implements ITickable {

    private SuperBeaconTileEntity.SupportColor color;
    private BlockPos connectedBeacon;

    @Override
    public void update() {
        if (world == null) return;
        if (!world.isRemote) {
            SuperBeaconTileEntity.SupportColor detected = detectColor();
            if (detected != color) {
                color = detected;
                if (effect != null && !getValidEffects().contains(effect)) effect = null;
                markAndNotify();
            }

            SuperBeaconTileEntity main = findNearbyValidBeacon();
            BlockPos newConnection = main == null ? null : main.getPos().toImmutable();
            if (newConnection == null ? connectedBeacon != null : !newConnection.equals(connectedBeacon)) {
                connectedBeacon = newConnection;
                markAndNotify();
            }
            boolean shouldBeActive = color != null && main != null;
            if (active != shouldBeActive) setActive(shouldBeActive);
            int previousLevel = beaconLevel;
            boolean previousShowWorkingArea = showWorkingArea;
            if (main != null) {
                beaconLevel = main.getBeaconLevel();
                showWorkingArea = main.showWorkingArea();
                if (color != null && main.getResummonTicks() == getResummonThreshold()) {
                    playSound("withered_beacon_activate", 1.0F, 1.0F);
                    playSound("tremble", 10.0F, 1.0F);
                    BlockPos mainPos = main.getPos();
                    com.wdcftgg.witherstormmod.common.network.ModNetwork.shakeNear(world,
                            mainPos.getX() + 0.5D, mainPos.getY() + 0.5D, mainPos.getZ() + 0.5D,
                            20.0D, 80.0F, 10.0F);
                }
            } else {
                beaconLevel = 0;
            }
            if (beaconLevel != previousLevel || showWorkingArea != previousShowWorkingArea) markAndNotify();
        }
        tickBeaconBase();
    }

    private SuperBeaconTileEntity.SupportColor detectColor() {
        Block expected = world.getBlockState(pos.down()).getBlock();
        SuperBeaconLogic.SupportColor logicColor = SuperBeaconLogic.SupportColor.forBase(expected);
        if (logicColor == null) return null;
        for (BlockPos check : BlockPos.getAllInBox(pos.add(-1, -1, -1), pos.add(1, -1, 1))) {
            if (world.getBlockState(check).getBlock() != expected) return null;
        }
        return SuperBeaconTileEntity.SupportColor.valueOf(logicColor.name());
    }

    private SuperBeaconTileEntity findNearbyValidBeacon() {
        SuperBeaconTileEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        int distance = SuperBeaconLogic.SUPPORT_SCAN_DISTANCE;
        for (BlockPos check : BlockPos.getAllInBox(pos.add(-distance, -distance, -distance),
                pos.add(distance, distance, distance))) {
            if (check.distanceSq(pos) > distance * distance) continue;
            TileEntity tile = world.getTileEntity(check);
            if (!(tile instanceof SuperBeaconTileEntity)) continue;
            SuperBeaconTileEntity beacon = (SuperBeaconTileEntity) tile;
            if (!beacon.isConnected(pos)) continue;
            double squaredDistance = check.distanceSq(pos);
            if (squaredDistance < nearestDistance) {
                nearest = beacon;
                nearestDistance = squaredDistance;
            }
        }
        return nearest;
    }

    @Override
    protected void applyEffect() {
        SuperBeaconTileEntity main = getConnectedBeaconEntity();
        if (main == null || color == null || effect == null) return;
        int amplifier = Math.max(0, beaconLevel - 1);
        for (EntityPlayer player : world.playerEntities) {
            if (SuperBeaconLogic.isInsideSupportArc(
                    main.getPos().getX() + 0.5D, main.getPos().getZ() + 0.5D,
                    pos.getX() + 0.5D, pos.getZ() + 0.5D,
                    player.posX, player.posZ)) {
                player.addPotionEffect(new PotionEffect(effect,
                        SuperBeaconLogic.SUPPORT_EFFECT_DURATION, amplifier, true, true));
            }
        }
    }

    @Override
    public Set<Potion> getValidEffects() {
        return color == null ? java.util.Collections.<Potion>emptySet()
                : color.getLogic().getValidEffects();
    }

    @Override
    public void doPowerUp(net.minecraft.entity.player.EntityPlayerMP player) {
        super.doPowerUp(player);
    }

    @Override
    public void setShowWorkingArea(boolean show) {
        SuperBeaconTileEntity main = getConnectedBeaconEntity();
        if (main != null) main.setShowWorkingArea(show);
        super.setShowWorkingArea(show);
    }

    public SuperBeaconTileEntity.SupportColor getColor() {
        return color;
    }

    public BlockPos getConnectedBeacon() {
        return connectedBeacon;
    }

    public BlockPos getBeamTarget() {
        SuperBeaconTileEntity main = getConnectedBeaconEntity();
        if (main != null && color != null
                && main.getResummonTicks() > getResummonThreshold()) {
            return connectedBeacon.up(3);
        }
        return connectedBeacon;
    }

    @Override
    protected boolean shouldDoActivatedAnimation() {
        SuperBeaconTileEntity main = getConnectedBeaconEntity();
        return super.shouldDoActivatedAnimation() || main != null && color != null
                && main.getResummonTicks() > getResummonThreshold();
    }

    public int getResummonThreshold() {
        return color == null ? Integer.MAX_VALUE
                : SuperBeaconLogic.getSupportResummonThreshold(color.ordinal());
    }

    public SuperBeaconTileEntity getConnectedBeaconEntity() {
        if (world == null || connectedBeacon == null) return null;
        TileEntity tile = world.getTileEntity(connectedBeacon);
        return tile instanceof SuperBeaconTileEntity ? (SuperBeaconTileEntity) tile : null;
    }

    @Override
    public String getNameForGui() {
        return "container.witherstormmod.withered_support_beacon";
    }

    @Override
    public int[] getBeamColor() {
        if (color == null) return super.getBeamColor();
        float[] values = color.getLogic().getBeamColor();
        return new int[] {(int) (values[0] * 255.0F), (int) (values[1] * 255.0F),
                (int) (values[2] * 255.0F)};
    }

    @Override
    public float getBeamThickness() {
        return 0.15F;
    }

    @Override
    public float getOuterBeamThickness() {
        return 0.2F;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("Color", color == null ? -1 : color.ordinal());
        if (connectedBeacon != null) compound.setLong("Connected", connectedBeacon.toLong());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        int value = compound.getInteger("Color");
        color = value >= 0 && value < SuperBeaconTileEntity.SupportColor.values().length
                ? SuperBeaconTileEntity.SupportColor.values()[value] : null;
        connectedBeacon = compound.hasKey("Connected") ? BlockPos.fromLong(compound.getLong("Connected")) : null;
    }

}
