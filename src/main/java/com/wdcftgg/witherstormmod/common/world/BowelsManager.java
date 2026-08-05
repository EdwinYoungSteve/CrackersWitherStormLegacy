package com.wdcftgg.witherstormmod.common.world;

import com.wdcftgg.witherstormmod.common.entity.EntityWitherStormLegacy;
import com.wdcftgg.witherstormmod.common.entity.SickenedEntities;
import com.wdcftgg.witherstormmod.common.entity.SupplementalEntities;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.potion.PotionEffect;
import net.minecraft.init.MobEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.WorldServer;
import net.minecraft.util.Rotation;
import net.minecraft.world.gen.structure.template.Template;

public final class BowelsManager {
    private BowelsManager() {
    }

    public static BowelsInstanceData.Instance enter(EntityWitherStormLegacy storm, EntityPlayerMP player) {
        MinecraftServer server = player.getServer();
        if (server == null || storm.isDead) return null;
        WorldServer bowels = server.getWorld(BowelsDimensions.DIMENSION_ID);
        BowelsInstanceData data = BowelsInstanceData.get(bowels);
        BowelsInstanceData.Instance instance = data.getOrCreate(storm.getUniqueID(), storm.dimension, storm.getPosition());
        prepareArena(bowels, data, instance);
        BlockPos entrance = findEntrance(bowels, instance);
        server.getPlayerList().transferPlayerToDimension(player, BowelsDimensions.DIMENSION_ID,
                new BowelsTeleporter(entrance));
        return instance;
    }

    public static void leave(EntityPlayerMP player) {
        MinecraftServer server = player.getServer();
        if (server == null || player.dimension != BowelsDimensions.DIMENSION_ID) return;
        WorldServer bowels = server.getWorld(BowelsDimensions.DIMENSION_ID);
        BowelsInstanceData.Instance instance = BowelsInstanceData.get(bowels).findContaining(player.getPosition());
        int destinationDimension = instance == null ? 0 : instance.originDimension;
        BlockPos destination = instance == null ? server.getWorld(0).getSpawnPoint() : instance.origin.up(5);
        server.getPlayerList().transferPlayerToDimension(player, destinationDimension, new BowelsTeleporter(destination));
        player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 2400, 4, false, false));
    }

    public static void prepareArena(WorldServer world, BowelsInstanceData data, BowelsInstanceData.Instance instance) {
        if (instance.prepared) return;
        BlockPos center = instance.center;
        LegacyStructureTemplates.placeBowelsNetwork(world, center, world.rand);
        BlockPos arena = instance.getArenaPosition();
        placeCenteredPodium(world, arena);
        spawnArenaTentacles(world, arena, 6 + world.rand.nextInt(6));
        spawnArenaHeads(world, center);
        SupplementalEntities.CommandBlockCore core = new SupplementalEntities.CommandBlockCore(world);
        core.setIndependentBowelsPart();
        core.setPosition(arena.getX() + 0.5D, arena.getY(), arena.getZ() + 0.5D);
        core.rotationYaw = core.rotationYawHead = 90.0F;
        if (world.spawnEntity(core)) instance.commandBlockUuid = core.getUniqueID();
        instance.prepared = true;
        data.markDirty();
    }

    private static void placeCenteredPodium(WorldServer world, BlockPos center) {
        Template template = LegacyStructureTemplates.get("bowels_podium");
        if (template == null) return;
        Rotation rotation = LegacyStructureTemplates.getFeatureRotation(center);
        BlockPos origin = LegacyStructureTemplates.getTopAnchoredFeatureOrigin(template, center, rotation);
        LegacyStructureTemplates.place(world, "bowels_podium", origin, rotation, true);
    }

    private static void spawnArenaTentacles(WorldServer world, BlockPos center, int amount) {
        for (int index = 0; index < amount; index++) {
            BlockPos spawn = null;
            for (int attempt = 0; attempt < 10; attempt++) {
                int x = center.getX() + world.rand.nextInt(50) - 25;
                int z = center.getZ() + world.rand.nextInt(50) - 25;
                BlockPos candidate = findFloor(world, new BlockPos(x, center.getY() + 8, z), 30);
                if (candidate == null || Math.sqrt(candidate.distanceSq(center)) <= 10.0D) continue;
                if (!world.getEntitiesWithinAABB(SickenedEntities.Tentacle.class,
                        new AxisAlignedBB(candidate).grow(10.0D)).isEmpty()) continue;
                if (!hasVerticalSpace(world, candidate, 8)) continue;
                spawn = candidate;
                break;
            }
            if (spawn == null) continue;
            SickenedEntities.Tentacle tentacle = new SickenedEntities.Tentacle(world);
            tentacle.setPosition(spawn.getX() + 0.5D, spawn.getY() + 1.0D, spawn.getZ() + 0.5D);
            tentacle.rotationYaw = world.rand.nextFloat() * 360.0F;
            tentacle.setDormant(true);
            world.spawnEntity(tentacle);
        }
    }

    private static void spawnArenaHeads(WorldServer world, BlockPos structureCenter) {
        BlockPos[] positions = {structureCenter.add(-2, 128, 27), structureCenter.add(-3, 128, -23)};
        for (int index = 0; index < positions.length; index++) {
            SupplementalEntities.WitherStormHead head = new SupplementalEntities.WitherStormHead(world);
            head.setIndependentBowelsPart();
            head.setPosition(positions[index].getX() + 0.5D, positions[index].getY(), positions[index].getZ() + 0.5D);
            head.rotationYaw = head.rotationYawHead = index == 0 ? 180.0F : 0.0F;
            head.rotationPitch = 60.0F;
            head.setActive(false);
            world.spawnEntity(head);
        }
    }

    private static BlockPos findEntrance(WorldServer world, BowelsInstanceData.Instance instance) {
        BlockPos center = instance.center;
        for (int radius = 96; radius > 35; radius -= 10) {
            int startAngle = world.rand.nextInt(360);
            for (int offset = 0; offset < 360; offset += 20) {
                double angle = Math.toRadians(startAngle + offset);
                BlockPos start = new BlockPos(center.getX() + Math.cos(angle) * radius,
                        center.getY() + 24, center.getZ() + Math.sin(angle) * radius);
                BlockPos floor = findFloor(world, start, 48);
                if (floor != null && hasVerticalSpace(world, floor, 3)) return floor.up();
            }
        }
        BlockPos fallback = findFloor(world, center.up(24), 64);
        return fallback == null ? center.up(8) : fallback.up();
    }

    private static BlockPos findFloor(WorldServer world, BlockPos start, int verticalSearch) {
        BlockPos cursor = start;
        for (int step = 0; step < verticalSearch && cursor.getY() > 1; step++, cursor = cursor.down()) {
            if (!world.isAirBlock(cursor) || world.isAirBlock(cursor.down())) continue;
            return cursor.down();
        }
        return null;
    }

    private static boolean hasVerticalSpace(WorldServer world, BlockPos floor, int height) {
        if (!world.isSideSolid(floor, net.minecraft.util.EnumFacing.UP)) return false;
        for (int y = 1; y <= height; y++) {
            BlockPos pos = floor.up(y);
            if (!world.isAirBlock(pos) && !world.getBlockState(pos).getBlock().isReplaceable(world, pos)) return false;
        }
        return true;
    }
}
