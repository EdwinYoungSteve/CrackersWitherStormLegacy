package com.wdcftgg.witherstormmod.common.proxy;

import com.wdcftgg.witherstormmod.client.LegacyClientEffects;
import com.wdcftgg.witherstormmod.client.render.RenderWitherStormLegacy;
import com.wdcftgg.witherstormmod.client.render.RenderStormPart;
import com.wdcftgg.witherstormmod.client.render.RenderWitherStormHeadPort;
import com.wdcftgg.witherstormmod.client.model.ModelCommandBlockCorePort;
import com.wdcftgg.witherstormmod.client.model.ModelWitherStormSegmentPort;
import com.wdcftgg.witherstormmod.client.render.RenderTaintedSign;
import com.wdcftgg.witherstormmod.client.render.RenderWitheredPhlegm;
import com.wdcftgg.witherstormmod.client.particle.LegacyPhlegmParticle;
import com.wdcftgg.witherstormmod.client.particle.LegacyCommandBlockParticle;
import com.wdcftgg.witherstormmod.client.gui.GuiWitheredPhlegm;
import com.wdcftgg.witherstormmod.client.gui.GuiSuperBeacon;
import com.wdcftgg.witherstormmod.common.inventory.ContainerWitheredPhlegm;
import com.wdcftgg.witherstormmod.common.inventory.ContainerSuperBeacon;
import com.wdcftgg.witherstormmod.client.render.SickenedRenderRegistry;
import com.wdcftgg.witherstormmod.common.entity.EntityWitherStormLegacy;
import com.wdcftgg.witherstormmod.common.entity.EntityPowerfulExplosive;
import com.wdcftgg.witherstormmod.common.entity.SupplementalEntities;
import com.wdcftgg.witherstormmod.common.init.ModBlocks;
import com.wdcftgg.witherstormmod.common.item.LegacySpawnEggItem;
import com.wdcftgg.witherstormmod.common.resource.UpstreamResourcePackInstaller;
import com.wdcftgg.witherstormmod.common.tile.TileEntityTaintedSign;
import com.wdcftgg.witherstormmod.common.tile.TileEntityWitheredPhlegm;
import com.wdcftgg.witherstormmod.common.tile.TileEntityAbstractSuperBeacon;
import com.wdcftgg.witherstormmod.common.tile.TileEntitySuperBeacon;
import com.wdcftgg.witherstormmod.common.tile.TileEntitySuperSupportBeacon;
import com.wdcftgg.witherstormmod.client.render.RenderSuperBeacon;
import com.wdcftgg.witherstormmod.client.LegacyDistantSuperBeacons;
import com.wdcftgg.witherstormmod.common.network.LegacyNetwork;
import com.wdcftgg.witherstormmod.common.block.BlockWitheredPhlegm;
import com.wdcftgg.witherstormmod.common.block.LegacyTaintedDustBlock;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.BlockWallSign;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelWither;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import com.wdcftgg.witherstormmod.client.render.RenderSickenedMob;
import com.wdcftgg.witherstormmod.client.render.RenderPowerfulExplosive;
import com.wdcftgg.witherstormmod.client.render.RenderFlamingWitherSkull;
import com.wdcftgg.witherstormmod.client.render.RenderTentacleSpike;
import com.wdcftgg.witherstormmod.client.render.RenderBlockCluster;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        UpstreamResourcePackInstaller.install();
        RenderingRegistry.registerEntityRenderingHandler(EntityWitherStormLegacy.class, RenderWitherStormLegacy::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityPowerfulExplosive.SuperTnt.class,
                manager -> new RenderPowerfulExplosive<EntityPowerfulExplosive.SuperTnt>(manager, "super_tnt"));
        RenderingRegistry.registerEntityRenderingHandler(EntityPowerfulExplosive.Formidibomb.class,
                manager -> new RenderPowerfulExplosive<EntityPowerfulExplosive.Formidibomb>(manager, "formidibomb"));
        RenderingRegistry.registerEntityRenderingHandler(SupplementalEntities.FlamingWitherSkull.class,
                manager -> new RenderFlamingWitherSkull<SupplementalEntities.FlamingWitherSkull>(manager, "flaming_wither_skull"));
        RenderingRegistry.registerEntityRenderingHandler(SupplementalEntities.BlueFlamingWitherSkull.class,
                manager -> new RenderFlamingWitherSkull<SupplementalEntities.BlueFlamingWitherSkull>(manager, "blue_flaming_wither_skull"));
        RenderingRegistry.registerEntityRenderingHandler(SupplementalEntities.TentacleSpike.class, RenderTentacleSpike::new);
        RenderingRegistry.registerEntityRenderingHandler(SupplementalEntities.BlockCluster.class, RenderBlockCluster::new);
        RenderingRegistry.registerEntityRenderingHandler(SupplementalEntities.CommandBlockCore.class,
                manager -> new RenderStormPart<SupplementalEntities.CommandBlockCore>(manager, new ModelCommandBlockCorePort(), 1.0F,
                        "textures/entity/command_block/ribcage.png", 3.0F));
        RenderingRegistry.registerEntityRenderingHandler(SupplementalEntities.WitherStormHead.class,
                RenderWitherStormHeadPort::new);
        RenderingRegistry.registerEntityRenderingHandler(SupplementalEntities.WitherStormSegment.class,
                manager -> new RenderStormPart<SupplementalEntities.WitherStormSegment>(manager, new ModelWitherStormSegmentPort(), 1.5F,
                        "textures/entity/wither_storm/wither_storm.png", 4.0F));
        SickenedRenderRegistry.register();
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTaintedSign.class, new RenderTaintedSign());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWitheredPhlegm.class, new RenderWitheredPhlegm());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySuperBeacon.class, new RenderSuperBeacon());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySuperSupportBeacon.class, new RenderSuperBeacon());
        ModelLoader.setCustomStateMapper(ModBlocks.get("tainted_door"), new StateMap.Builder().ignore(BlockDoor.POWERED).build());
        ModelLoader.setCustomStateMapper(ModBlocks.get("tainted_fence_gate"), new StateMap.Builder().ignore(BlockFenceGate.POWERED).build());
        ModelLoader.setCustomStateMapper(ModBlocks.get("tainted_sign"),
                new StateMap.Builder().ignore(BlockStandingSign.ROTATION).build());
        ModelLoader.setCustomStateMapper(ModBlocks.get("tainted_wall_sign"),
                new StateMap.Builder().ignore(BlockWallSign.FACING).build());
        ModelLoader.setCustomStateMapper(ModBlocks.get("withered_phlegm_block"),
                new StateMap.Builder().ignore(BlockWitheredPhlegm.POWERED).build());
    }

    @Override
    public void init(FMLInitializationEvent event) {
        LegacyTaintedDustBlock taintedDust = (LegacyTaintedDustBlock) ModBlocks.get("tainted_dust");
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(
                (state, world, position, tintIndex) -> LegacyTaintedDustBlock.getColor(), taintedDust);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(
                (stack, tintIndex) -> LegacyTaintedDustBlock.getColor(), Item.getItemFromBlock(taintedDust));
        for (Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
            if (item instanceof LegacySpawnEggItem) {
                LegacySpawnEggItem egg = (LegacySpawnEggItem) item;
                Minecraft.getMinecraft().getItemColors().registerItemColorHandler(
                        (stack, tintIndex) -> egg.getColor(tintIndex), egg);
            }
        }
    }

    @Override
    public void handleShakeScreen(float duration, float power) {
        Minecraft.getMinecraft().addScheduledTask(() -> LegacyClientEffects.shake(duration, power));
    }

    @Override
    public void handleBlindScreen(int duration, int fadeInDuration, int fadeOutDuration) {
        Minecraft.getMinecraft().addScheduledTask(
                () -> LegacyClientEffects.blind(duration, fadeInDuration, fadeOutDuration));
    }

    @Override
    public void handleGlobalSound(ResourceLocation sound, float volume, float pitch) {
        Minecraft.getMinecraft().addScheduledTask(
                () -> LegacyClientEffects.playGlobalSound(sound, volume, pitch));
    }

    @Override
    public void handleFormidibombExplosion(int sourceEntityId, double x, double y, double z,
                                           int radius, int squish) {
        Minecraft.getMinecraft().addScheduledTask(
                () -> LegacyClientEffects.spawnFormidibombExplosion(x, y, z));
    }

    @Override
    public void spawnWitheredPhlegmParticles(World world, BlockPos pos, boolean powered,
                                             java.util.Random random) {
        LegacyPhlegmParticle.spawnForBlock(world, pos, powered, random);
    }

    @Override
    public void spawnSuperBeaconResummonParticle(World world, BlockPos pos,
                                                  java.util.Random random) {
        LegacyCommandBlockParticle.spawnForSuperBeacon(world, pos, random);
    }

    @Override
    public void handleSuperBeaconParticles(BlockPos pos, int type) {
        Minecraft.getMinecraft().addScheduledTask(
                () -> LegacyCommandBlockParticle.spawnSuperBeaconBurst(pos, type));
    }

    @Override
    public Object createWitheredPhlegmGui(EntityPlayer player, TileEntityWitheredPhlegm tile) {
        return new GuiWitheredPhlegm(new ContainerWitheredPhlegm(player.inventory, tile), tile);
    }

    @Override
    public Object createSuperBeaconGui(EntityPlayer player, TileEntityAbstractSuperBeacon tile) {
        return new GuiSuperBeacon(new ContainerSuperBeacon(tile), tile);
    }

    @Override
    public void handleDistantSuperBeacon(LegacyNetwork.DistantSuperBeaconMessage message) {
        Minecraft.getMinecraft().addScheduledTask(() -> LegacyDistantSuperBeacons.update(message));
    }
}
