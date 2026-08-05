package com.wdcftgg.witherstormmod.common.proxy;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import com.wdcftgg.witherstormmod.common.tile.TileEntityWitheredPhlegm;
import com.wdcftgg.witherstormmod.common.tile.TileEntityAbstractSuperBeacon;
import com.wdcftgg.witherstormmod.common.network.LegacyNetwork;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
    }

    public void init(FMLInitializationEvent event) {
    }

    public void handleShakeScreen(float duration, float power) {
    }

    public void handleBlindScreen(int duration, int fadeInDuration, int fadeOutDuration) {
    }

    public void handleGlobalSound(ResourceLocation sound, float volume, float pitch) {
    }

    public void handleFormidibombExplosion(int sourceEntityId, double x, double y, double z,
                                           int radius, int squish) {
    }

    public void spawnWitheredPhlegmParticles(World world, BlockPos pos, boolean powered,
                                             java.util.Random random) {
    }

    public void spawnSuperBeaconResummonParticle(World world, BlockPos pos,
                                                  java.util.Random random) {
    }

    public void handleSuperBeaconParticles(BlockPos pos, int type) {
    }

    public Object createWitheredPhlegmGui(EntityPlayer player, TileEntityWitheredPhlegm tile) {
        return null;
    }

    public Object createSuperBeaconGui(EntityPlayer player, TileEntityAbstractSuperBeacon tile) {
        return null;
    }

    public void handleDistantSuperBeacon(LegacyNetwork.DistantSuperBeaconMessage message) {
    }
}
