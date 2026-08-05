package com.wdcftgg.witherstormmod.common.util;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.common.config.LegacyWitherStormConfig;
import com.wdcftgg.witherstormmod.common.entity.EntityWitherStormLegacy;
import com.wdcftgg.witherstormmod.common.entity.SupplementalEntities;
import com.wdcftgg.witherstormmod.common.init.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class PhlegmGravestoneHelper {
    private static final int TILE_SIZE = 25;
    private static final int CLUSTER_RADIUS = 1;

    private PhlegmGravestoneHelper() {
    }

    @Nullable
    public static Vec3d findPotentialClusterPosition(EntityLivingBase victim, DamageSource source) {
        if (victim == null || source == null) return null;
        if (!LegacyWitherStormConfig.preserveDropsForAllMobs && !(victim instanceof EntityPlayer)) return null;

        ItemPreservationCondition condition = LegacyWitherStormConfig.itemPreservation;
        if (condition == ItemPreservationCondition.DISABLED) return null;
        Entity sourceEntity = condition.usesDirectEntity() ? source.getImmediateSource() : source.getTrueSource();
        if (!(sourceEntity instanceof EntityWitherStormLegacy) || sourceEntity.isDead) return null;

        EntityWitherStormLegacy storm = (EntityWitherStormLegacy) sourceEntity;
        Vec3d victimEyes = victim.getPositionEyes(1.0F);
        Vec3d closest = null;
        double closestDistance = -1.0D;
        for (int head = 0; head < storm.getTotalHeads(); head++) {
            Vec3d headPosition = storm.getHeadPosition(head, 1.0F);
            double distance = headPosition.distanceTo(victimEyes);
            if ((closestDistance < 0.0D || distance < closestDistance) && distance < 30.0D) {
                closestDistance = distance;
                closest = headPosition;
            }
        }
        if (closest == null && condition.fallsBackToVictim()) return victimEyes;
        return closest;
    }

    @Nullable
    public static SupplementalEntities.BlockCluster spawnForEntity(EntityLivingBase victim, Vec3d position,
                                                                    List<ItemStack> drops, int experience) {
        if (victim == null || position == null || drops == null || drops.isEmpty() || victim.world.isRemote) {
            return null;
        }
        Random random = victim.getRNG();
        ClusterData data = buildClusterData(random, drops, victim.getDisplayName().getFormattedText(), experience);
        if (data.blocks.isEmpty()) return null;

        SupplementalEntities.BlockCluster cluster = new SupplementalEntities.BlockCluster(victim.world);
        cluster.setBlocks(data.blocks);
        for (NBTTagCompound tile : data.tileData) cluster.addTileData(tile);
        cluster.setPosition(position.x, position.y, position.z);
        cluster.motionX = random.nextGaussian() * 0.3D;
        cluster.motionY = 0.0D;
        cluster.motionZ = random.nextGaussian() * 0.3D;
        cluster.setRotationDelta(random.nextInt(20) * 0.15F, random.nextInt(20) * 0.15F);
        victim.world.spawnEntity(cluster);
        cluster.setSink(1);
        cluster.setAntiStacking(true);
        return cluster;
    }

    static ClusterData buildClusterData(Random random, List<ItemStack> drops,
                                        @Nullable String customName, int experience) {
        List<NBTTagList> inventories = splitInventories(random, drops);
        if (inventories.isEmpty()) return new ClusterData();
        int experiencePerBlock = experience / inventories.size();

        List<BlockPos> availablePositions = new ArrayList<BlockPos>();
        for (int x = -CLUSTER_RADIUS; x <= CLUSTER_RADIUS; x++) {
            for (int y = -CLUSTER_RADIUS; y <= CLUSTER_RADIUS; y++) {
                for (int z = -CLUSTER_RADIUS; z <= CLUSTER_RADIUS; z++) {
                    availablePositions.add(new BlockPos(x, y, z));
                }
            }
        }

        Map<BlockPos, NBTTagCompound> tileByPosition = new HashMap<BlockPos, NBTTagCompound>();
        for (NBTTagList inventory : inventories) {
            if (availablePositions.isEmpty()) break;
            BlockPos offset = availablePositions.remove(random.nextInt(availablePositions.size()));
            NBTTagCompound tile = new NBTTagCompound();
            tile.setTag("Items", inventory);
            tile.setInteger("StoredXp", experiencePerBlock);
            tileByPosition.put(offset, tile);
        }

        ClusterData data = new ClusterData();
        IBlockState phlegm = ModBlocks.get("withered_phlegm_block").getDefaultState();
        for (Map.Entry<BlockPos, NBTTagCompound> entry : tileByPosition.entrySet()) {
            data.blocks.put(entry.getKey(), phlegm);
        }
        for (BlockPos offset : availablePositions) {
            if (random.nextFloat() > 0.6F) data.blocks.put(offset, randomChewedBlock(random));
        }

        BlockPos startPos = calculateStartPos(data.blocks.keySet());
        for (Map.Entry<BlockPos, NBTTagCompound> entry : tileByPosition.entrySet()) {
            BlockPos tilePos = startPos.add(entry.getKey());
            NBTTagCompound tile = entry.getValue();
            tile.setString("id", Tags.MOD_ID + ":withered_phlegm");
            tile.setInteger("x", tilePos.getX());
            tile.setInteger("y", tilePos.getY());
            tile.setInteger("z", tilePos.getZ());
            if (customName != null) tile.setString("CustomName", customName);
            data.tileData.add(tile);
        }
        return data;
    }

    static List<NBTTagList> splitInventories(Random random, List<ItemStack> drops) {
        List<NBTTagList> inventories = new ArrayList<NBTTagList>();
        NBTTagList current = new NBTTagList();
        int slot = 0;
        for (ItemStack stack : drops) {
            if (stack == null || stack.isEmpty()) continue;
            if (slot >= TILE_SIZE) {
                slot -= TILE_SIZE;
                inventories.add(current);
                current = new NBTTagList();
            }
            NBTTagCompound stackTag = new NBTTagCompound();
            stackTag.setByte("Slot", (byte) slot);
            stack.copy().writeToNBT(stackTag);
            current.appendTag(stackTag);
            slot += 1 + random.nextInt(3);
        }
        if (current.tagCount() > 0) inventories.add(current);
        return inventories;
    }

    static IBlockState randomChewedBlock(Random random) {
        int value = random.nextInt(24);
        if (value < 10) return ModBlocks.get("tainted_flesh_block").getDefaultState();
        if (value < 20) return ModBlocks.get("infected_flesh_block").getDefaultState();
        if (value < 22) return ModBlocks.get("tainted_zombie_lying").getDefaultState();
        return ModBlocks.get("tainted_bone_pile").getDefaultState();
    }

    static BlockPos calculateStartPos(Iterable<BlockPos> positions) {
        int minX = 0, minY = 0, minZ = 0;
        int maxX = 0, maxY = 0, maxZ = 0;
        for (BlockPos pos : positions) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        return new BlockPos(minX + (maxX - minX) / 2.0D,
                minY + (maxY - minY) / 2.0D,
                minZ + (maxZ - minZ) / 2.0D);
    }

    static final class ClusterData {
        final Map<BlockPos, IBlockState> blocks = new HashMap<BlockPos, IBlockState>();
        final List<NBTTagCompound> tileData = new ArrayList<NBTTagCompound>();
    }
}
