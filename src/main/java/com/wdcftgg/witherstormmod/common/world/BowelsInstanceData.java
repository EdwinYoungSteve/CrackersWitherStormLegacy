package com.wdcftgg.witherstormmod.common.world;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BowelsInstanceData extends WorldSavedData {
    public static final String DATA_NAME = "witherstormmod_bowels_instances";
    private final List<Instance> instances = new ArrayList<>();

    public BowelsInstanceData() {
        super(DATA_NAME);
    }

    public BowelsInstanceData(String name) {
        super(name);
    }

    public static BowelsInstanceData get(World world) {
        BowelsInstanceData data = (BowelsInstanceData) world.getPerWorldStorage().getOrLoadData(BowelsInstanceData.class, DATA_NAME);
        if (data == null) {
            data = new BowelsInstanceData();
            world.getPerWorldStorage().setData(DATA_NAME, data);
        }
        return data;
    }

    public Instance getOrCreate(UUID stormUuid, int originDimension, BlockPos origin) {
        Instance existing = get(stormUuid);
        if (existing != null) return existing;
        int index = instances.size();
        int gridX = index % 16;
        int gridZ = index / 16;
        BlockPos center = new BlockPos(gridX * 1024, 96, gridZ * 1024);
        Instance instance = new Instance(stormUuid, center, originDimension, origin);
        instances.add(instance);
        markDirty();
        return instance;
    }

    @Nullable
    public Instance get(UUID stormUuid) {
        for (Instance instance : instances) if (instance.stormUuid.equals(stormUuid)) return instance;
        return null;
    }

    @Nullable
    public Instance findContaining(BlockPos pos) {
        for (Instance instance : instances) {
            if (Math.abs(pos.getX() - instance.center.getX()) <= 192 && Math.abs(pos.getZ() - instance.center.getZ()) <= 192) return instance;
        }
        return null;
    }

    public List<Instance> getInstances() {
        return java.util.Collections.unmodifiableList(instances);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        instances.clear();
        NBTTagList list = compound.getTagList("Instances", 10);
        for (int i = 0; i < list.tagCount(); i++) instances.add(Instance.read(list.getCompoundTagAt(i)));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (Instance instance : instances) list.appendTag(instance.write());
        compound.setTag("Instances", list);
        return compound;
    }

    public static class Instance {
        public final UUID stormUuid;
        public final BlockPos center;
        public final int originDimension;
        public final BlockPos origin;
        public UUID commandBlockUuid;
        public boolean prepared;
        public boolean completed;
        public int bossPhase;
        public int bossPhaseTicks;
        public BlockPos arenaPosition;

        private Instance(UUID stormUuid, BlockPos center, int originDimension, BlockPos origin) {
            this.stormUuid = stormUuid;
            this.center = center;
            this.originDimension = originDimension;
            this.origin = origin;
        }

        private NBTTagCompound write() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setUniqueId("Storm", stormUuid);
            tag.setLong("Center", center.toLong());
            tag.setInteger("OriginDimension", originDimension);
            tag.setLong("Origin", origin.toLong());
            if (commandBlockUuid != null) tag.setUniqueId("CommandBlock", commandBlockUuid);
            tag.setBoolean("Prepared", prepared);
            tag.setBoolean("Completed", completed);
            tag.setInteger("BossPhase", bossPhase);
            tag.setInteger("BossPhaseTicks", bossPhaseTicks);
            if (arenaPosition != null) tag.setLong("ArenaPosition", arenaPosition.toLong());
            return tag;
        }

        private static Instance read(NBTTagCompound tag) {
            Instance instance = new Instance(tag.getUniqueId("Storm"), BlockPos.fromLong(tag.getLong("Center")),
                    tag.getInteger("OriginDimension"), BlockPos.fromLong(tag.getLong("Origin")));
            if (tag.hasUniqueId("CommandBlock")) instance.commandBlockUuid = tag.getUniqueId("CommandBlock");
            instance.prepared = tag.getBoolean("Prepared");
            instance.completed = tag.getBoolean("Completed");
            instance.bossPhase = tag.getInteger("BossPhase");
            instance.bossPhaseTicks = tag.getInteger("BossPhaseTicks");
            instance.arenaPosition = tag.hasKey("ArenaPosition", 4)
                    ? BlockPos.fromLong(tag.getLong("ArenaPosition")) : null;
            return instance;
        }

        public BlockPos getArenaPosition() {
            if (arenaPosition == null) arenaPosition = center.add(-3, 110, 0);
            return arenaPosition;
        }
    }
}
