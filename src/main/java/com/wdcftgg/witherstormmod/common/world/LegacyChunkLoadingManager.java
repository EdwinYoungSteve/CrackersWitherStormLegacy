package com.wdcftgg.witherstormmod.common.world;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.WitherStormMod;
import com.wdcftgg.witherstormmod.common.entity.EntityWitherStormLegacy;
import com.wdcftgg.witherstormmod.common.entity.SupplementalEntities;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** 将上游区域票据语义映射到 1.12 Forge 的持久化区块票据。 */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class LegacyChunkLoadingManager implements ForgeChunkManager.LoadingCallback {
    private static final String MANAGED = "WitherStormLegacyManaged";
    private static final String LOADER_KEY = "LoaderKey";
    private static final String LOADER_KIND = "LoaderKind";
    private static final String CENTER_X = "CenterX";
    private static final String CENTER_Z = "CenterZ";
    private static final String RADIUS = "Radius";
    private static final String BATCH_INDEX = "BatchIndex";
    private static final String CHUNKS = "Chunks";
    private static final String CHUNK_X = "X";
    private static final String CHUNK_Z = "Z";
    private static final int STORM_RADIUS = 12;
    private static final int SEGMENT_RADIUS = 6;
    private static final int BOWELS_RADIUS = 3;
    private static final int BEACON_RADIUS = 0;
    private static final long RESTORE_GRACE_TICKS = 600L;

    public static final LegacyChunkLoadingManager INSTANCE = new LegacyChunkLoadingManager();

    private final Map<WorldServer, Map<String, TicketGroup>> groupsByWorld =
            new IdentityHashMap<WorldServer, Map<String, TicketGroup>>();
    private boolean registered;

    private LegacyChunkLoadingManager() {
    }

    public synchronized void register(WitherStormMod mod) {
        if (registered) return;
        ForgeChunkManager.setForcedChunkLoadingCallback(mod, this);
        registered = true;
    }

    @Override
    public synchronized void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world) {
        if (!(world instanceof WorldServer)) {
            for (ForgeChunkManager.Ticket ticket : tickets) ForgeChunkManager.releaseTicket(ticket);
            return;
        }
        WorldServer serverWorld = (WorldServer) world;
        Map<String, List<ForgeChunkManager.Ticket>> restored = new LinkedHashMap<String, List<ForgeChunkManager.Ticket>>();
        for (ForgeChunkManager.Ticket ticket : tickets) {
            NBTTagCompound data = ticket.getModData();
            if (!data.getBoolean(MANAGED) || !data.hasKey(LOADER_KEY, 8)) continue;
            String key = data.getString(LOADER_KEY);
            List<ForgeChunkManager.Ticket> loaderTickets = restored.get(key);
            if (loaderTickets == null) {
                loaderTickets = new ArrayList<ForgeChunkManager.Ticket>();
                restored.put(key, loaderTickets);
            }
            loaderTickets.add(ticket);
        }

        Map<String, TicketGroup> worldGroups = groups(serverWorld);
        for (Map.Entry<String, List<ForgeChunkManager.Ticket>> entry : restored.entrySet()) {
            List<ForgeChunkManager.Ticket> loaderTickets = entry.getValue();
            Collections.sort(loaderTickets, Comparator.comparingInt(ticket -> ticket.getModData().getInteger(BATCH_INDEX)));
            NBTTagCompound first = loaderTickets.get(0).getModData();
            TicketGroup existing = worldGroups.remove(entry.getKey());
            if (existing != null) existing.release();
            TicketGroup group = new TicketGroup(serverWorld, entry.getKey(), first.getString(LOADER_KIND),
                    first.getInteger(CENTER_X), first.getInteger(CENTER_Z),
                    Math.max(0, first.getInteger(RADIUS)), loaderTickets, serverWorld.getTotalWorldTime());
            worldGroups.put(entry.getKey(), group);
            group.restoreChunks();
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.START || event.world.isRemote || !(event.world instanceof WorldServer)) return;
        INSTANCE.tickWorld((WorldServer) event.world);
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (!event.getWorld().isRemote && event.getWorld() instanceof WorldServer) {
            INSTANCE.forgetWorld((WorldServer) event.getWorld());
        }
    }

    private synchronized void tickWorld(WorldServer world) {
        long now = world.getTotalWorldTime();
        List<Entity> entities = new ArrayList<Entity>(world.loadedEntityList);
        for (Entity entity : entities) {
            if (entity.isDead) continue;
            if (entity instanceof EntityWitherStormLegacy) {
                touch(world, entityKey("storm", entity.getUniqueID()), "storm", entity.chunkCoordX, entity.chunkCoordZ,
                        STORM_RADIUS, now);
            } else if (entity instanceof SupplementalEntities.WitherStormSegment
                    && !((SupplementalEntities.WitherStormSegment) entity).isIndependentBowelsPart()) {
                touch(world, entityKey("segment", entity.getUniqueID()), "segment", entity.chunkCoordX,
                        entity.chunkCoordZ, SEGMENT_RADIUS, now);
            }
        }

        if (world.provider.getDimension() == BowelsDimensions.DIMENSION_ID) {
            for (BowelsInstanceData.Instance instance : BowelsInstanceData.get(world).getInstances()) {
                if (instance.completed) continue;
                ChunkPos center = new ChunkPos(instance.getArenaPosition());
                touch(world, bowelsKey(instance.stormUuid), "bowels", center.x, center.z, BOWELS_RADIUS, now);
            }
        }

        for (net.minecraft.tileentity.TileEntity tile : new ArrayList<net.minecraft.tileentity.TileEntity>(
                world.loadedTileEntityList)) {
            if (tile instanceof com.wdcftgg.witherstormmod.common.tile.TileEntityAbstractSuperBeacon
                    && !tile.isInvalid()) {
                ChunkPos center = new ChunkPos(tile.getPos());
                touch(world, beaconKey(tile.getPos()), "super_beacon", center.x, center.z,
                        BEACON_RADIUS, now);
            }
        }

        Map<String, TicketGroup> worldGroups = groupsByWorld.get(world);
        if (worldGroups == null || worldGroups.isEmpty()) return;
        List<String> stale = new ArrayList<String>();
        for (TicketGroup group : worldGroups.values()) {
            if (now - group.lastSeenTick > RESTORE_GRACE_TICKS) stale.add(group.key);
        }
        for (String key : stale) release(world, key);
    }

    private void touch(WorldServer world, String key, String kind, int centerX, int centerZ, int radius, long now) {
        Map<String, TicketGroup> worldGroups = groups(world);
        TicketGroup group = worldGroups.get(key);
        if (group == null) {
            group = new TicketGroup(world, key, kind, centerX, centerZ, radius,
                    new ArrayList<ForgeChunkManager.Ticket>(), now);
            worldGroups.put(key, group);
        }
        group.lastSeenTick = now;
        if (group.centerX != centerX || group.centerZ != centerZ || group.radius != radius || !group.isComplete()) {
            group.reconfigure(centerX, centerZ, radius);
        }
    }

    public synchronized void releaseEntity(World world, String kind, UUID uuid) {
        if (world instanceof WorldServer) release((WorldServer) world, entityKey(kind, uuid));
    }

    public synchronized void releaseBowelsInstance(World world, UUID stormUuid) {
        if (world instanceof WorldServer) release((WorldServer) world, bowelsKey(stormUuid));
    }

    public synchronized void releaseSuperBeacon(World world, BlockPos position) {
        if (world instanceof WorldServer && position != null) {
            release((WorldServer) world, beaconKey(position));
        }
    }

    private void release(WorldServer world, String key) {
        Map<String, TicketGroup> worldGroups = groupsByWorld.get(world);
        if (worldGroups == null) return;
        TicketGroup group = worldGroups.remove(key);
        if (group != null) group.release();
        if (worldGroups.isEmpty()) groupsByWorld.remove(world);
    }

    private void forgetWorld(WorldServer world) {
        groupsByWorld.remove(world);
    }

    private Map<String, TicketGroup> groups(WorldServer world) {
        Map<String, TicketGroup> groups = groupsByWorld.get(world);
        if (groups == null) {
            groups = new LinkedHashMap<String, TicketGroup>();
            groupsByWorld.put(world, groups);
        }
        return groups;
    }

    static String entityKey(String kind, UUID uuid) {
        return kind + ":" + uuid;
    }

    static String bowelsKey(UUID stormUuid) {
        return "bowels:" + stormUuid;
    }

    static String beaconKey(BlockPos position) {
        return "super_beacon:" + position.toLong();
    }

    static List<ChunkPos> createChunkPlan(int centerX, int centerZ, int radius) {
        List<ChunkPos> chunks = new ArrayList<ChunkPos>();
        for (int offsetX = -radius; offsetX <= radius; offsetX++) {
            for (int offsetZ = -radius; offsetZ <= radius; offsetZ++) {
                chunks.add(new ChunkPos(centerX + offsetX, centerZ + offsetZ));
            }
        }
        Collections.sort(chunks, (first, second) -> {
            int firstX = first.x - centerX;
            int firstZ = first.z - centerZ;
            int secondX = second.x - centerX;
            int secondZ = second.z - centerZ;
            int comparison = Integer.compare(Math.max(Math.abs(firstX), Math.abs(firstZ)),
                    Math.max(Math.abs(secondX), Math.abs(secondZ)));
            if (comparison != 0) return comparison;
            comparison = Integer.compare(firstX * firstX + firstZ * firstZ, secondX * secondX + secondZ * secondZ);
            if (comparison != 0) return comparison;
            comparison = Integer.compare(first.x, second.x);
            return comparison != 0 ? comparison : Integer.compare(first.z, second.z);
        });
        return chunks;
    }

    static int requiredTicketCount(int chunkCount, int ticketCapacity) {
        if (chunkCount <= 0) return 0;
        if (ticketCapacity <= 0) return 1;
        return (chunkCount + ticketCapacity - 1) / ticketCapacity;
    }

    private static final class TicketGroup {
        private final WorldServer world;
        private final String key;
        private final String kind;
        private final List<ForgeChunkManager.Ticket> tickets;
        private int centerX;
        private int centerZ;
        private int radius;
        private int configuredChunks;
        private long lastSeenTick;

        private TicketGroup(WorldServer world, String key, String kind, int centerX, int centerZ, int radius,
                            List<ForgeChunkManager.Ticket> tickets, long lastSeenTick) {
            this.world = world;
            this.key = key;
            this.kind = kind;
            this.centerX = centerX;
            this.centerZ = centerZ;
            this.radius = radius;
            this.tickets = tickets;
            this.lastSeenTick = lastSeenTick;
        }

        private void restoreChunks() {
            configuredChunks = 0;
            for (ForgeChunkManager.Ticket ticket : tickets) {
                NBTTagList chunks = ticket.getModData().getTagList(CHUNKS, 10);
                for (int index = 0; index < chunks.tagCount(); index++) {
                    NBTTagCompound chunk = chunks.getCompoundTagAt(index);
                    ForgeChunkManager.forceChunk(ticket, new ChunkPos(chunk.getInteger(CHUNK_X), chunk.getInteger(CHUNK_Z)));
                    configuredChunks++;
                }
            }
        }

        private boolean isComplete() {
            int diameter = radius * 2 + 1;
            return configuredChunks == diameter * diameter;
        }

        private void reconfigure(int newCenterX, int newCenterZ, int newRadius) {
            centerX = newCenterX;
            centerZ = newCenterZ;
            radius = Math.max(0, newRadius);
            List<ChunkPos> desired = createChunkPlan(centerX, centerZ, radius);
            int capacity = ForgeChunkManager.getMaxChunkDepthFor(Tags.MOD_ID);
            int ticketCount = requiredTicketCount(desired.size(), capacity);

            while (tickets.size() < ticketCount) {
                ForgeChunkManager.Ticket ticket = ForgeChunkManager.requestTicket(
                        WitherStormMod.INSTANCE, world, ForgeChunkManager.Type.NORMAL);
                if (ticket == null) break;
                tickets.add(ticket);
            }
            while (tickets.size() > ticketCount) {
                ForgeChunkManager.Ticket ticket = tickets.remove(tickets.size() - 1);
                ForgeChunkManager.releaseTicket(ticket);
            }

            configuredChunks = 0;
            int chunkIndex = 0;
            int batchCapacity = capacity <= 0 ? desired.size() : capacity;
            for (int ticketIndex = 0; ticketIndex < tickets.size(); ticketIndex++) {
                ForgeChunkManager.Ticket ticket = tickets.get(ticketIndex);
                for (ChunkPos previous : new ArrayList<ChunkPos>(ticket.getChunkList())) {
                    ForgeChunkManager.unforceChunk(ticket, previous);
                }
                NBTTagCompound data = ticket.getModData();
                data.setBoolean(MANAGED, true);
                data.setString(LOADER_KEY, key);
                data.setString(LOADER_KIND, kind);
                data.setInteger(CENTER_X, centerX);
                data.setInteger(CENTER_Z, centerZ);
                data.setInteger(RADIUS, radius);
                data.setInteger(BATCH_INDEX, ticketIndex);
                NBTTagList chunks = new NBTTagList();
                int end = Math.min(desired.size(), chunkIndex + batchCapacity);
                while (chunkIndex < end) {
                    ChunkPos position = desired.get(chunkIndex++);
                    NBTTagCompound chunk = new NBTTagCompound();
                    chunk.setInteger(CHUNK_X, position.x);
                    chunk.setInteger(CHUNK_Z, position.z);
                    chunks.appendTag(chunk);
                    ForgeChunkManager.forceChunk(ticket, position);
                    configuredChunks++;
                }
                data.setTag(CHUNKS, chunks);
            }
            if (configuredChunks < desired.size()) {
                WitherStormMod.LOGGER.error("Unable to allocate enough Forge chunk tickets for {}: loaded {}/{} chunks",
                        key, configuredChunks, desired.size());
            }
        }

        private void release() {
            for (ForgeChunkManager.Ticket ticket : tickets) ForgeChunkManager.releaseTicket(ticket);
            tickets.clear();
            configuredChunks = 0;
        }
    }
}
