package com.wdcftgg.witherstormmod.client;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.WitherStormMod;
import com.wdcftgg.witherstormmod.common.init.ModBlocks;
import com.wdcftgg.witherstormmod.common.init.ModItems;
import com.wdcftgg.witherstormmod.client.sound.WitherStormLoopSound;
import com.wdcftgg.witherstormmod.client.sound.WitherStormTrembleSound;
import com.wdcftgg.witherstormmod.client.sound.BowelsLoopSound;
import com.wdcftgg.witherstormmod.client.sound.FormidibombFuseSound;
import com.wdcftgg.witherstormmod.client.particle.CommandBlockParticle;
import com.wdcftgg.witherstormmod.client.particle.PhlegmBlockParticle;
import com.wdcftgg.witherstormmod.client.render.SuperBeaconRenderer;
import com.wdcftgg.witherstormmod.common.entity.PowerfulExplosiveEntity;
import com.wdcftgg.witherstormmod.common.entity.WitherStormEntity;
import com.wdcftgg.witherstormmod.common.entity.FormidibombSource;
import com.wdcftgg.witherstormmod.common.init.ModSounds;
import com.wdcftgg.witherstormmod.common.network.ModNetwork;
import com.wdcftgg.witherstormmod.common.tile.FormidibombTileEntity;
import com.wdcftgg.witherstormmod.common.world.BowelsDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public final class WitherStormClientEvents {
    private static WitherStormLoopSound loop;
    private static String loopName;
    private static final Map<Integer, WitherStormTrembleSound> TREMBLE_LOOPS =
            new HashMap<Integer, WitherStormTrembleSound>();
    private static final Map<FormidibombSource, FormidibombFuseSound> FORMIDIBOMB_LOOPS =
            new IdentityHashMap<FormidibombSource, FormidibombFuseSound>();
    private static BowelsLoopSound bowelsLoop;
    private static int bowelsMoodDelay;
    private static boolean creativeStackModelsAudited;

    private WitherStormClientEvents() {
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModBlocks.registerModels();
        ModItems.registerModels();
        WitherStormMod.LOGGER.info("Registered all legacy block and item models during ModelRegistryEvent");
    }

    @SubscribeEvent
    public static void registerParticleSprites(TextureStitchEvent.Pre event) {
        CommandBlockParticle.registerSprites(event.getMap());
        PhlegmBlockParticle.registerSprite(event.getMap());
        SuperBeaconRenderer.registerSprites(event.getMap());
    }

    @SubscribeEvent
    public static void auditBakedModels(ModelBakeEvent event) {
        IBakedModel missing = event.getModelManager().getMissingModel();
        int checked = 0;
        int failed = 0;
        for (Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
            ResourceLocation registryName = item.getRegistryName();
            if (registryName == null || !Tags.MOD_ID.equals(registryName.getNamespace())) continue;
            checked++;
            ModelResourceLocation location = new ModelResourceLocation(registryName, "inventory");
            IBakedModel model = event.getModelRegistry().getObject(location);
            List<String> missingTextures = findMissingQuadTextures(model);
            if (model == null || model == missing || !missingTextures.isEmpty()) {
                failed++;
                WitherStormMod.LOGGER.error("Item model audit failed: item={}, model={}, baked={}, missingTextures={}",
                        registryName, location, model == null ? "null" : model.getClass().getName(), missingTextures);
            }
        }
        if (failed == 0) {
            WitherStormMod.LOGGER.info("Item model audit passed for all {} registered items", checked);
        } else {
            WitherStormMod.LOGGER.error("Item model audit found {} failures among {} registered items", failed, checked);
        }
    }

    private static List<String> findMissingQuadTextures(IBakedModel model) {
        List<String> missingTextures = new ArrayList<String>();
        if (model == null) return missingTextures;
        collectMissingQuadTextures(model.getQuads(null, null, 0L), missingTextures);
        for (EnumFacing facing : EnumFacing.values()) {
            collectMissingQuadTextures(model.getQuads(null, facing, 0L), missingTextures);
        }
        return missingTextures;
    }

    private static void collectMissingQuadTextures(List<BakedQuad> quads, List<String> missingTextures) {
        for (BakedQuad quad : quads) {
            String texture = quad.getSprite() == null ? "null" : quad.getSprite().getIconName();
            if (("missingno".equals(texture) || texture.endsWith(":missingno"))
                    && !missingTextures.contains(texture)) {
                missingTextures.add(texture);
            }
        }
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (event.phase == TickEvent.Phase.START) {
            ClientEffects.tick(minecraft);
            return;
        }
        if (event.phase != TickEvent.Phase.END) return;
        AmuletAnimationHelper.tick(minecraft);
        if (!creativeStackModelsAudited) {
            auditCreativeStackModels(minecraft);
            creativeStackModelsAudited = true;
        }
        if (minecraft.world == null || minecraft.player == null) {
            if (loop != null) loop.stop();
            if (bowelsLoop != null) bowelsLoop.stop();
            for (WitherStormTrembleSound trembleLoop : TREMBLE_LOOPS.values()) {
                trembleLoop.stopImmediately();
            }
            TREMBLE_LOOPS.clear();
            for (FormidibombFuseSound formidibombLoop : FORMIDIBOMB_LOOPS.values()) {
                formidibombLoop.stop();
            }
            FORMIDIBOMB_LOOPS.clear();
            loop = null;
            loopName = null;
            bowelsLoop = null;
            bowelsMoodDelay = 0;
            return;
        }
        updateBowelsAmbience(minecraft);
        updateFormidibombLoops(minecraft);
        spawnFormidibombParticles(minecraft);
        updateWitherStormTrembleLoops(minecraft);
        WitherStormEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Entity entity : minecraft.world.loadedEntityList) {
            if (entity instanceof WitherStormEntity && !entity.isDead) {
                double distance = minecraft.player.getDistanceSq(entity);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = (WitherStormEntity) entity;
                }
            }
        }
        if (nearest == null || nearest.isPlayDeadAiDisabled()) {
            if (loop != null) loop.stop();
            loop = null;
            loopName = null;
            return;
        }
        double distance = Math.sqrt(nearestDistance);
        String desired = distance < 160.0D ? "wither_storm_close_loop"
                : distance < 480.0D ? "wither_storm_far_loop" : "wither_storm_distant_loop";
        if (loop == null || loop.isDonePlaying() || !desired.equals(loopName)) {
            if (loop != null) loop.stop();
            SoundEvent sound = ModSounds.get(desired);
            if (sound != null) {
                loop = new WitherStormLoopSound(nearest, sound);
                loopName = desired;
                minecraft.getSoundHandler().playSound(loop);
            }
        }
    }

    @SubscribeEvent
    public static void leftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        checkForHeadHit(event.getEntityPlayer());
    }

    @SubscribeEvent
    public static void attackEntity(AttackEntityEvent event) {
        checkForHeadHit(event.getEntityPlayer());
    }

    private static void checkForHeadHit(EntityPlayer player) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (player == null || player != minecraft.player || player.world == null
                || !player.world.isRemote || player.isSpectator() || minecraft.playerController == null) return;
        double reach = minecraft.playerController.getBlockReachDistance();
        for (WitherStormEntity storm : player.world.getEntitiesWithinAABB(
                WitherStormEntity.class, player.getEntityBoundingBox().grow(50.0D))) {
            for (int head = 0; head < storm.getTotalHeads(); head++) {
                if (!storm.tractorBeamActive(head) || !storm.canPlayerReachHead(player, head, reach)) continue;
                if (!storm.isDeadOrPlayingDead() && !storm.isHeadInjured(head)) {
                    ModNetwork.injureWitherStormHead(storm, head);
                } else {
                    player.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, 1.0F, 1.0F);
                }
                return;
            }
        }
    }

    @SubscribeEvent
    public static void renderAmuletInHand(RenderSpecificHandEvent event) {
        AmuletAnimationHelper.render(event);
    }

    private static void spawnFormidibombParticles(Minecraft minecraft) {
        for (Entity entity : minecraft.world.loadedEntityList) {
            if (entity instanceof PowerfulExplosiveEntity.FormidibombEntity && !entity.isDead) {
                CommandBlockParticle.spawnForBomb((PowerfulExplosiveEntity.FormidibombEntity) entity);
            }
        }
    }

    private static void updateWitherStormTrembleLoops(Minecraft minecraft) {
        Set<Integer> loadedStormIds = new HashSet<Integer>();
        SoundEvent trembleSound = ModSounds.get("wither_storm_tremble");
        for (Entity entity : minecraft.world.loadedEntityList) {
            if (!(entity instanceof WitherStormEntity) || entity.isDead) continue;
            WitherStormEntity storm = (WitherStormEntity) entity;
            int entityId = storm.getEntityId();
            loadedStormIds.add(entityId);
            WitherStormTrembleSound trembleLoop = TREMBLE_LOOPS.get(entityId);
            if (storm.getPlayDeadState() == WitherStormEntity.PlayDeadState.FALLING) {
                if ((trembleLoop == null || trembleLoop.isDonePlaying()) && trembleSound != null) {
                    trembleLoop = new WitherStormTrembleSound(storm, trembleSound);
                    TREMBLE_LOOPS.put(entityId, trembleLoop);
                    minecraft.getSoundHandler().playSound(trembleLoop);
                }
            } else if (trembleLoop != null) {
                trembleLoop.requestStop();
            }
        }

        Iterator<Map.Entry<Integer, WitherStormTrembleSound>> iterator = TREMBLE_LOOPS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, WitherStormTrembleSound> entry = iterator.next();
            WitherStormTrembleSound trembleLoop = entry.getValue();
            if (!loadedStormIds.contains(entry.getKey())) {
                trembleLoop.stopImmediately();
                iterator.remove();
            } else if (trembleLoop.isDonePlaying()) {
                iterator.remove();
            }
        }
    }

    private static void updateFormidibombLoops(Minecraft minecraft) {
        Set<FormidibombSource> currentSources = java.util.Collections.newSetFromMap(
                new IdentityHashMap<FormidibombSource, Boolean>());
        for (Entity entity : minecraft.world.loadedEntityList) {
            if (entity instanceof PowerfulExplosiveEntity.FormidibombEntity && !entity.isDead) {
                currentSources.add((PowerfulExplosiveEntity.FormidibombEntity) entity);
            }
        }
        for (TileEntity tile : minecraft.world.loadedTileEntityList) {
            if (tile instanceof FormidibombTileEntity
                    && minecraft.player.getEntityBoundingBox().grow(50.0D).contains(
                    new net.minecraft.util.math.Vec3d(tile.getPos()).add(0.5D, 0.5D, 0.5D))) {
                currentSources.add((FormidibombTileEntity) tile);
            }
        }

        SoundEvent pulseSound = ModSounds.get("formidibomb_pulse_loop");
        for (FormidibombSource source : currentSources) {
            FormidibombFuseSound loop = FORMIDIBOMB_LOOPS.get(source);
            if ((loop == null || loop.isDonePlaying()) && pulseSound != null) {
                loop = new FormidibombFuseSound(source, pulseSound);
                FORMIDIBOMB_LOOPS.put(source, loop);
                minecraft.getSoundHandler().playSound(loop);
            }
        }

        Iterator<Map.Entry<FormidibombSource, FormidibombFuseSound>> iterator =
                FORMIDIBOMB_LOOPS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<FormidibombSource, FormidibombFuseSound> entry = iterator.next();
            if (!currentSources.contains(entry.getKey()) || !entry.getKey().isFormidibombAlive()
                    || entry.getValue().isDonePlaying()) {
                entry.getValue().stop();
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static void setupCamera(EntityViewRenderEvent.CameraSetup event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.player == null || event.getEntity() != minecraft.player) return;
        float partialTicks = (float) event.getRenderPartialTicks();
        GlStateManager.translate(ClientEffects.getShakeTranslationX(partialTicks),
                ClientEffects.getShakeTranslationY(partialTicks), 0.0F);
    }

    @SubscribeEvent
    public static void modifyPhasometerFov(EntityViewRenderEvent.FOVModifier event) {
        if (event.getEntity() instanceof net.minecraft.entity.player.EntityPlayer
                && PhasometerOverlay.isScoping(
                (net.minecraft.entity.player.EntityPlayer) event.getEntity())) {
            event.setFOV(PhasometerOverlay.applyScopeFov(event.getFOV()));
        }
    }

    @SubscribeEvent
    public static void renderBlindOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.HOTBAR) return;
        float fade = ClientEffects.getBlindFade(event.getPartialTicks());
        if (fade <= 0.0F) return;
        int alpha = MathHelper.clamp(Math.round(fade * 255.0F), 0, 255);
        Gui.drawRect(0, 0, event.getResolution().getScaledWidth(), event.getResolution().getScaledHeight(),
                alpha << 24 | 0xFFFFFF);
    }

    @SubscribeEvent
    public static void renderPhasometerOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.HELMET) {
            PhasometerOverlay.render(Minecraft.getMinecraft(), event.getResolution());
        }
    }

    private static void updateBowelsAmbience(Minecraft minecraft) {
        if (minecraft.player.dimension != BowelsDimensions.DIMENSION_ID) {
            if (bowelsLoop != null) bowelsLoop.stop();
            bowelsLoop = null;
            bowelsMoodDelay = 0;
            return;
        }
        if (bowelsLoop == null || bowelsLoop.isDonePlaying()) {
            SoundEvent loopSound = ModSounds.get("bowels_loop");
            if (loopSound != null) {
                bowelsLoop = new BowelsLoopSound(loopSound);
                minecraft.getSoundHandler().playSound(bowelsLoop);
            }
        }
        if (--bowelsMoodDelay > 0) return;
        bowelsMoodDelay = 240 + minecraft.world.rand.nextInt(240);
        SoundEvent moodSound = ModSounds.get("bowels_mood");
        if (moodSound == null) return;
        BlockPos position = minecraft.player.getPosition().add(
                minecraft.world.rand.nextInt(17) - 8,
                minecraft.world.rand.nextInt(9) - 4,
                minecraft.world.rand.nextInt(17) - 8);
        minecraft.getSoundHandler().playSound(new PositionedSoundRecord(
                moodSound, SoundCategory.AMBIENT, 1.0F, 1.0F, position));
    }

    private static void auditCreativeStackModels(Minecraft minecraft) {
        IBakedModel missing = minecraft.getRenderItem().getItemModelMesher().getModelManager().getMissingModel();
        int checked = 0;
        int failed = 0;
        for (Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
            ResourceLocation registryName = item.getRegistryName();
            if (registryName == null || !Tags.MOD_ID.equals(registryName.getNamespace())) continue;
            NonNullList<ItemStack> stacks = NonNullList.create();
            if (item.getCreativeTab() != null) {
                item.getSubItems(item.getCreativeTab(), stacks);
            }
            if (stacks.isEmpty()) stacks.add(new ItemStack(item));
            for (ItemStack stack : stacks) {
                checked++;
                IBakedModel model = minecraft.getRenderItem().getItemModelMesher().getItemModel(stack);
                List<String> missingTextures = findMissingQuadTextures(model);
                if (model == null || model == missing || !missingTextures.isEmpty()) {
                    failed++;
                    WitherStormMod.LOGGER.error(
                            "Creative stack model audit failed: item={}, metadata={}, baked={}, missingTextures={}",
                            registryName, stack.getMetadata(), model == null ? "null" : model.getClass().getName(),
                            missingTextures);
                }
            }
        }
        if (failed == 0) {
            WitherStormMod.LOGGER.info("Creative stack model audit passed for all {} item stacks", checked);
        } else {
            WitherStormMod.LOGGER.error("Creative stack model audit found {} failures among {} item stacks",
                    failed, checked);
        }
    }

    @SubscribeEvent
    public static void renderOverlay(RenderGameOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (event.getType() == RenderGameOverlayEvent.ElementType.HELMET && minecraft.player != null
                && minecraft.player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem()
                == Item.getItemFromBlock(ModBlocks.get("tainted_carved_pumpkin"))) {
            ScaledResolution scaled = event.getResolution();
            minecraft.getTextureManager().bindTexture(new ResourceLocation(Tags.MOD_ID, "textures/misc/tainted_pumpkin_blur.png"));
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            Gui.drawModalRectWithCustomSizedTexture(0, 0, 0.0F, 0.0F, scaled.getScaledWidth(), scaled.getScaledHeight(),
                    scaled.getScaledWidth(), scaled.getScaledHeight());
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
        }
    }
}
