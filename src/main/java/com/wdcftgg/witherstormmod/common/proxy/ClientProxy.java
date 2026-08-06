package com.wdcftgg.witherstormmod.common.proxy;

import com.wdcftgg.witherstormmod.client.ClientEffects;
import com.wdcftgg.witherstormmod.client.render.WitherStormRenderer;
import com.wdcftgg.witherstormmod.client.render.StormPartRenderer;
import com.wdcftgg.witherstormmod.client.render.WitherStormHeadRenderer;
import com.wdcftgg.witherstormmod.client.model.CommandBlockCoreModel;
import com.wdcftgg.witherstormmod.client.model.WitherStormSegmentModel;
import com.wdcftgg.witherstormmod.client.render.TaintedSignRenderer;
import com.wdcftgg.witherstormmod.client.render.WitheredPhlegmRenderer;
import com.wdcftgg.witherstormmod.client.particle.PhlegmBlockParticle;
import com.wdcftgg.witherstormmod.client.particle.CommandBlockParticle;
import com.wdcftgg.witherstormmod.client.gui.WitheredPhlegmScreen;
import com.wdcftgg.witherstormmod.client.gui.SuperBeaconScreen;
import com.wdcftgg.witherstormmod.common.inventory.WitheredPhlegmContainer;
import com.wdcftgg.witherstormmod.common.inventory.SuperBeaconContainer;
import com.wdcftgg.witherstormmod.client.render.SickenedRendererRegistry;
import com.wdcftgg.witherstormmod.common.entity.WitherStormEntity;
import com.wdcftgg.witherstormmod.common.entity.PowerfulExplosiveEntity;
import com.wdcftgg.witherstormmod.common.entity.SupplementalEntities;
import com.wdcftgg.witherstormmod.common.init.ModBlocks;
import com.wdcftgg.witherstormmod.common.item.SpawnEggItem;
import com.wdcftgg.witherstormmod.common.resource.UpstreamResourcePackInstaller;
import com.wdcftgg.witherstormmod.common.tile.TaintedSignTileEntity;
import com.wdcftgg.witherstormmod.common.tile.WitheredPhlegmTileEntity;
import com.wdcftgg.witherstormmod.common.tile.AbstractSuperBeaconTileEntity;
import com.wdcftgg.witherstormmod.common.tile.SuperBeaconTileEntity;
import com.wdcftgg.witherstormmod.common.tile.SuperSupportBeaconTileEntity;
import com.wdcftgg.witherstormmod.client.render.SuperBeaconRenderer;
import com.wdcftgg.witherstormmod.client.DistantSuperBeaconRenderer;
import com.wdcftgg.witherstormmod.common.network.ModNetwork;
import com.wdcftgg.witherstormmod.common.block.WitheredPhlegmBlock;
import com.wdcftgg.witherstormmod.common.block.TaintedDustBlock;
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
import com.wdcftgg.witherstormmod.client.render.SickenedMobRenderer;
import com.wdcftgg.witherstormmod.client.render.PowerfulExplosiveRenderer;
import com.wdcftgg.witherstormmod.client.render.FlamingWitherSkullRenderer;
import com.wdcftgg.witherstormmod.client.render.TentacleSpikeRenderer;
import com.wdcftgg.witherstormmod.client.render.BlockClusterRenderer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        UpstreamResourcePackInstaller.install();
        RenderingRegistry.registerEntityRenderingHandler(WitherStormEntity.class, WitherStormRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(PowerfulExplosiveEntity.SuperTntEntity.class,
                manager -> new PowerfulExplosiveRenderer<PowerfulExplosiveEntity.SuperTntEntity>(manager, "super_tnt"));
        RenderingRegistry.registerEntityRenderingHandler(PowerfulExplosiveEntity.FormidibombEntity.class,
                manager -> new PowerfulExplosiveRenderer<PowerfulExplosiveEntity.FormidibombEntity>(manager, "formidibomb"));
        RenderingRegistry.registerEntityRenderingHandler(SupplementalEntities.FlamingWitherSkullEntity.class,
                manager -> new FlamingWitherSkullRenderer<SupplementalEntities.FlamingWitherSkullEntity>(manager, "flaming_wither_skull"));
        RenderingRegistry.registerEntityRenderingHandler(SupplementalEntities.BlueFlamingWitherSkullEntity.class,
                manager -> new FlamingWitherSkullRenderer<SupplementalEntities.BlueFlamingWitherSkullEntity>(manager, "blue_flaming_wither_skull"));
        RenderingRegistry.registerEntityRenderingHandler(SupplementalEntities.TentacleSpikeEntity.class, TentacleSpikeRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(SupplementalEntities.BlockClusterEntity.class, BlockClusterRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(SupplementalEntities.CommandBlockEntity.class,
                manager -> new StormPartRenderer<SupplementalEntities.CommandBlockEntity>(manager, new CommandBlockCoreModel(), 1.0F,
                        "textures/entity/command_block/ribcage.png", 3.0F));
        RenderingRegistry.registerEntityRenderingHandler(SupplementalEntities.WitherStormHeadEntity.class,
                WitherStormHeadRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(SupplementalEntities.WitherStormSegmentEntity.class,
                manager -> new StormPartRenderer<SupplementalEntities.WitherStormSegmentEntity>(manager, new WitherStormSegmentModel(), 1.5F,
                        "textures/entity/wither_storm/wither_storm.png", 4.0F));
        SickenedRendererRegistry.register();
        ClientRegistry.bindTileEntitySpecialRenderer(TaintedSignTileEntity.class, new TaintedSignRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(WitheredPhlegmTileEntity.class, new WitheredPhlegmRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(SuperBeaconTileEntity.class, new SuperBeaconRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(SuperSupportBeaconTileEntity.class, new SuperBeaconRenderer());
        ModelLoader.setCustomStateMapper(ModBlocks.get("tainted_door"), new StateMap.Builder().ignore(BlockDoor.POWERED).build());
        ModelLoader.setCustomStateMapper(ModBlocks.get("tainted_fence_gate"), new StateMap.Builder().ignore(BlockFenceGate.POWERED).build());
        ModelLoader.setCustomStateMapper(ModBlocks.get("tainted_sign"),
                new StateMap.Builder().ignore(BlockStandingSign.ROTATION).build());
        ModelLoader.setCustomStateMapper(ModBlocks.get("tainted_wall_sign"),
                new StateMap.Builder().ignore(BlockWallSign.FACING).build());
        ModelLoader.setCustomStateMapper(ModBlocks.get("withered_phlegm_block"),
                new StateMap.Builder().ignore(WitheredPhlegmBlock.POWERED).build());
    }

    @Override
    public void init(FMLInitializationEvent event) {
        TaintedDustBlock taintedDust = (TaintedDustBlock) ModBlocks.get("tainted_dust");
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(
                (state, world, position, tintIndex) -> TaintedDustBlock.getColor(), taintedDust);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(
                (stack, tintIndex) -> TaintedDustBlock.getColor(), Item.getItemFromBlock(taintedDust));
        for (Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
            if (item instanceof SpawnEggItem) {
                SpawnEggItem egg = (SpawnEggItem) item;
                Minecraft.getMinecraft().getItemColors().registerItemColorHandler(
                        (stack, tintIndex) -> egg.getColor(tintIndex), egg);
            }
        }
    }

    @Override
    public void handleShakeScreen(float duration, float power) {
        Minecraft.getMinecraft().addScheduledTask(() -> ClientEffects.shake(duration, power));
    }

    @Override
    public void handleBlindScreen(int duration, int fadeInDuration, int fadeOutDuration) {
        Minecraft.getMinecraft().addScheduledTask(
                () -> ClientEffects.blind(duration, fadeInDuration, fadeOutDuration));
    }

    @Override
    public void handleGlobalSound(ResourceLocation sound, float volume, float pitch) {
        Minecraft.getMinecraft().addScheduledTask(
                () -> ClientEffects.playGlobalSound(sound, volume, pitch));
    }

    @Override
    public void handleFormidibombExplosion(int sourceEntityId, double x, double y, double z,
                                           int radius, int squish) {
        Minecraft.getMinecraft().addScheduledTask(
                () -> ClientEffects.spawnFormidibombExplosion(x, y, z));
    }

    @Override
    public void spawnWitheredPhlegmParticles(World world, BlockPos pos, boolean powered,
                                             java.util.Random random) {
        PhlegmBlockParticle.spawnForBlock(world, pos, powered, random);
    }

    @Override
    public void spawnSuperBeaconResummonParticle(World world, BlockPos pos,
                                                  java.util.Random random) {
        CommandBlockParticle.spawnForSuperBeacon(world, pos, random);
    }

    @Override
    public void handleSuperBeaconParticles(BlockPos pos, int type) {
        Minecraft.getMinecraft().addScheduledTask(
                () -> CommandBlockParticle.spawnSuperBeaconBurst(pos, type));
    }

    @Override
    public Object createWitheredPhlegmGui(EntityPlayer player, WitheredPhlegmTileEntity tile) {
        return new WitheredPhlegmScreen(new WitheredPhlegmContainer(player.inventory, tile), tile);
    }

    @Override
    public Object createSuperBeaconGui(EntityPlayer player, AbstractSuperBeaconTileEntity tile) {
        return new SuperBeaconScreen(new SuperBeaconContainer(tile), tile);
    }

    @Override
    public void handleDistantSuperBeacon(ModNetwork.DistantSuperBeaconMessage message) {
        Minecraft.getMinecraft().addScheduledTask(() -> DistantSuperBeaconRenderer.update(message));
    }
}
