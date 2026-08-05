package com.wdcftgg.witherstormmod.common.init;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.common.tile.TileEntitySuperBeacon;
import com.wdcftgg.witherstormmod.common.tile.TileEntitySuperSupportBeacon;
import com.wdcftgg.witherstormmod.common.tile.TileEntityFireworkBundle;
import com.wdcftgg.witherstormmod.common.tile.TileEntityTaintedSign;
import com.wdcftgg.witherstormmod.common.tile.TileEntityFormidibomb;
import com.wdcftgg.witherstormmod.common.tile.TileEntityWitheredPhlegm;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class ModTileEntities {

    private ModTileEntities() {
    }

    public static void register() {
        GameRegistry.registerTileEntity(TileEntitySuperBeacon.class, new ResourceLocation(Tags.MOD_ID, "super_beacon"));
        GameRegistry.registerTileEntity(TileEntitySuperSupportBeacon.class, new ResourceLocation(Tags.MOD_ID, "super_support_beacon"));
        GameRegistry.registerTileEntity(TileEntityFireworkBundle.class, new ResourceLocation(Tags.MOD_ID, "firework_bundle"));
        GameRegistry.registerTileEntity(TileEntityTaintedSign.class, new ResourceLocation(Tags.MOD_ID, "tainted_sign"));
        GameRegistry.registerTileEntity(TileEntityFormidibomb.class, new ResourceLocation(Tags.MOD_ID, "formidibomb"));
        GameRegistry.registerTileEntity(TileEntityWitheredPhlegm.class,
                new ResourceLocation(Tags.MOD_ID, "withered_phlegm"));
    }
}
