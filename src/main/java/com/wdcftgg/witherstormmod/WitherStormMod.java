package com.wdcftgg.witherstormmod;

import com.wdcftgg.witherstormmod.common.advancement.LegacyCriteriaTriggers;
import com.wdcftgg.witherstormmod.common.advancement.LegacyExternalAdvancements;
import com.wdcftgg.witherstormmod.common.init.ModBlocks;
import com.wdcftgg.witherstormmod.common.init.ModEntities;
import com.wdcftgg.witherstormmod.common.init.ModEffects;
import com.wdcftgg.witherstormmod.common.init.ModItems;
import com.wdcftgg.witherstormmod.common.init.ModPotionTypes;
import com.wdcftgg.witherstormmod.common.init.ModRecipes;
import com.wdcftgg.witherstormmod.common.init.ModTileEntities;
import com.wdcftgg.witherstormmod.common.init.ModSounds;
import com.wdcftgg.witherstormmod.common.loot.LegacyLootConditions;
import com.wdcftgg.witherstormmod.common.loot.LegacyExternalLootTables;
import com.wdcftgg.witherstormmod.common.network.LegacyNetwork;
import com.wdcftgg.witherstormmod.common.proxy.CommonProxy;
import com.wdcftgg.witherstormmod.common.gui.LegacyGuiHandler;
import com.wdcftgg.witherstormmod.common.resource.UpstreamResourceArchive;
import com.wdcftgg.witherstormmod.common.resource.LegacyUpstreamBlockTags;
import com.wdcftgg.witherstormmod.common.world.BowelsDimensions;
import com.wdcftgg.witherstormmod.common.world.LegacyChunkLoadingManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION,
        dependencies = "required-after:futuremc")
public class WitherStormMod {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);
    public static final String UPSTREAM_RESOURCEPACK_NAME = "witherstormmod-1.20.1-4.2.1-all.jar";

    @Mod.Instance(Tags.MOD_ID)
    public static WitherStormMod INSTANCE;

    @SidedProxy(
            clientSide = "com.wdcftgg.witherstormmod.common.proxy.ClientProxy",
            serverSide = "com.wdcftgg.witherstormmod.common.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        try {
            UpstreamResourceArchive.initialize(event.getModConfigurationDirectory().getParentFile());
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Unable to initialize the external Wither Storm resource pack", exception);
        }
        LegacyUpstreamBlockTags.initialize();
        LegacyLootConditions.register();
        LegacyExternalLootTables.initialize();
        LegacyNetwork.register();
        BowelsDimensions.register();
        LegacyChunkLoadingManager.INSTANCE.register(this);
        ModBlocks.bootstrap();
        ModItems.bootstrap();
        ModEffects.bootstrap();
        ModPotionTypes.bootstrap();
        ModSounds.bootstrap();
        ModEntities.register();
        LegacyCriteriaTriggers.register();
        ModTileEntities.register();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new LegacyGuiHandler());
        proxy.preInit(event);
        LOGGER.info("Loaded {} as a 1.12.2 port using upstream resources from {}", Tags.MOD_NAME, UPSTREAM_RESOURCEPACK_NAME);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ModRecipes.registerSmelting();
        ModRecipes.registerAnvil();
        ModRecipes.registerStonecutting();
        ModRecipes.registerBrewing();
        proxy.init(event);
    }

    @Mod.EventHandler
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
        try {
            LegacyExternalAdvancements.install(event.getServer());
        } catch (java.io.IOException exception) {
            throw new IllegalStateException(
                    "Unable to prepare advancements from the external Wither Storm resource pack", exception);
        }
    }
}
