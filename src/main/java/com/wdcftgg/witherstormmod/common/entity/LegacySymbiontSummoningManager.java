package com.wdcftgg.witherstormmod.common.entity;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.common.init.ModItems;
import com.wdcftgg.witherstormmod.common.init.ModSounds;
import com.wdcftgg.witherstormmod.common.item.FormidibombItem;
import com.wdcftgg.witherstormmod.common.world.BowelsDimensions;
import com.wdcftgg.witherstormmod.common.world.BowelsInstanceData;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * 对齐上游 SymbiontSummoningManager 的服务端状态和生成规则。
 * 召唤冷却、玩家保护和最近一次召唤记录都写入实体 NBT，避免区块重载后重复召唤。
 */
public final class LegacySymbiontSummoningManager {
    private static final int MINIMUM_CHECK_INTERVAL_TICKS = 60 * 20;
    private static final int SUMMONING_DELAY_MINUTES = 10;
    private static final int PLAYER_PROTECTION_MINUTES = 5;
    private static final String PLAYER_DATA_KEY = "WitherStormLegacySymbiont";
    private static final String LAST_SUMMONED_STORM = "LastSummonedStorm";
    private static final String LAST_SUMMONED_PHASE = "LastSummonedPhase";
    private static final String KILLED_UNTIL = "KilledUntil";

    private final EntityWitherStormLegacy storm;
    private int timeTillCanSummonSymbiont;

    public LegacySymbiontSummoningManager(EntityWitherStormLegacy storm) {
        this.storm = storm;
    }

    public void tick() {
        if (storm.world.isRemote || storm.isDead) return;
        if (timeTillCanSummonSymbiont > 0) --timeTillCanSummonSymbiont;

        int interval = MINIMUM_CHECK_INTERVAL_TICKS * (storm.getRNG().nextInt(3) + 1);
        if (storm.ticksExisted % interval != 0) return;

        List<EntityPlayer> players = storm.world.getEntitiesWithinAABB(EntityPlayer.class,
                storm.getSearchBox(), player -> player != null && player.isEntityAlive());
        Collections.sort(players, Comparator.comparingDouble(storm::getDistanceSq));
        for (EntityPlayer player : players) {
            if (!playerApplicable(player)) continue;
            if (canSummonSymbiont()) {
                summonSymbiont(player);
            }
            break;
        }
    }

    protected boolean canSummonSymbiont() {
        if (storm.isDeadOrPlayingDead() || !storm.isEntityAlive()) return false;
        if (storm.getPhase() < 5 || storm.getConsumedMass() < storm.getConsumptionAmountForPhase(5)) return false;
        if (timeTillCanSummonSymbiont > 0 || storm.hasRecentlyBeenRevived()) return false;
        if (storm.isAttractingFormidibomb()) return false;
        if (storm.getBowelsCommandBlock() != null && storm.getBowelsCommandBlock().getHealth()
                < storm.getBowelsCommandBlock().getMaxHealth()) return false;

        AxisAlignedBB search = storm.getSearchBox().grow(50.0D);
        for (Entity entity : storm.world.getEntitiesWithinAABB(Entity.class, search)) {
            if (entity == storm || entity.isDead) continue;
            if (entity instanceof EntityPowerfulExplosive.Formidibomb
                    || entity instanceof SickenedEntities.WitheredSymbiont) return false;
        }

        if (isPlayerInsideBowelsInstance()) return false;
        return true;
    }

    protected boolean playerApplicable(EntityPlayer player) {
        if (!player.isEntityAlive() || player.isSpectator() || player.capabilities.disableDamage) return false;
        if (storm.getDistanceSq(player) > storm.getSearchBox().getAverageEdgeLength()
                * storm.getSearchBox().getAverageEdgeLength()) return false;
        if (shouldIgnorePlayer(player)) return false;
        if (hasRecentSummon(player, storm)) return false;

        for (ItemStack stack : getAllInventoryStacks(player)) {
            if (stack.isEmpty()) continue;
            if (stack.getItem() == ModItems.get("command_block_book")) return false;
            if (stack.getItem() instanceof FormidibombItem && FormidibombItem.getStartFuse(stack) > 0) return false;
            if (storm.getPhase() > 5 && isCommandBlockTool(stack)) return false;
        }
        return true;
    }

    public void summonSymbiont(EntityPlayer player) {
        float angle = -(float) Math.atan2(player.posX - storm.posX, player.posZ - storm.posZ);
        float spawnX = MathHelper.sin(angle) * 30.0F + (float) storm.posX;
        float spawnZ = MathHelper.cos(angle) * 30.0F + (float) storm.posZ;

        for (int attempt = 0; attempt < 10; attempt++) {
            int randomX = MathHelper.floor(spawnX) + (int) (storm.getRNG().nextGaussian() * 10.0D) + 5;
            int randomZ = MathHelper.floor(spawnZ) + (int) (storm.getRNG().nextGaussian() * 10.0D) + 5;
            BlockPos spawnPos = findHighestSpawnPos(randomX, randomZ);
            if (spawnPos == null) continue;

            SickenedEntities.WitheredSymbiont symbiont = new SickenedEntities.WitheredSymbiont(storm.world);
            double y = spawnPos.getY() + 1.0D;
            symbiont.setPosition(spawnPos.getX() + 0.5D, y, spawnPos.getZ() + 0.5D);
            lookAt(symbiont, player);
            symbiont.setOwner(storm);
            symbiont.setAttackTarget(player);
            if (!storm.world.spawnEntity(symbiont)) continue;

            if (storm.world instanceof WorldServer) {
                WorldServer world = (WorldServer) storm.world;
                world.spawnParticle(net.minecraft.util.EnumParticleTypes.PORTAL,
                        symbiont.posX, symbiont.posY + 1.0D, symbiont.posZ,
                        20, 0.25D, 0.5D, 0.25D, 0.01D);
                world.spawnParticle(net.minecraft.util.EnumParticleTypes.SMOKE_LARGE,
                        symbiont.posX, symbiont.posY + 1.0D, symbiont.posZ,
                        20, 0.2D, 0.4D, 0.2D, 0.01D);
            }
            storm.world.playSound(null, storm.getPosition(), ModSounds.get("command_block_summon"),
                    SoundCategory.HOSTILE, 15.0F, 1.0F);
            symbiont.playSound(ModSounds.get("withered_symbiont_spawn"), 12.0F, 1.0F);
            timeTillCanSummonSymbiont = SUMMONING_DELAY_MINUTES * 1200 + storm.getRNG().nextInt(12000);
            markSummoned(player, storm);
            return;
        }
    }

    @Nullable
    private BlockPos findHighestSpawnPos(int x, int z) {
        if (!storm.world.isBlockLoaded(new BlockPos(x, 0, z))) return null;
        BlockPos heightPos = storm.world.getHeight(new BlockPos(x, 0, z)).down();
        int highest = Integer.MIN_VALUE;
        BlockPos result = null;
        for (int offsetX = -5; offsetX <= 5; offsetX++) {
            for (int offsetZ = -5; offsetZ <= 5; offsetZ++) {
                BlockPos candidate = new BlockPos(x + offsetX, heightPos.getY(), z + offsetZ);
                if (!storm.world.isBlockLoaded(candidate)) continue;
                int y = storm.world.getHeight(candidate).getY() - 1;
                if (y <= highest) continue;
                BlockPos floor = new BlockPos(candidate.getX(), y, candidate.getZ());
                IBlockState state = storm.world.getBlockState(floor);
                if (!storm.world.isSideSolid(floor, EnumFacing.UP) || state.getCollisionBoundingBox(storm.world, floor) == Block.NULL_AABB) continue;
                AxisAlignedBB body = new AxisAlignedBB(floor.getX() + 0.1D, y + 1.0D, floor.getZ() + 0.1D,
                        floor.getX() + 0.9D, y + 4.8D, floor.getZ() + 0.9D);
                if (!storm.world.checkNoEntityCollision(body)) continue;
                highest = y;
                result = floor;
            }
        }
        return result;
    }

    private static void lookAt(SickenedEntities.WitheredSymbiont symbiont, EntityPlayer player) {
        double dx = player.posX - symbiont.posX;
        double dy = player.posY + player.getEyeHeight() - (symbiont.posY + symbiont.getEyeHeight());
        double dz = player.posZ - symbiont.posZ;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        symbiont.rotationYaw = (float) (MathHelper.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
        symbiont.rotationYawHead = symbiont.rotationYaw;
        symbiont.rotationPitch = (float) (-(MathHelper.atan2(dy, horizontal) * 180.0D / Math.PI));
    }

    private boolean isPlayerInsideBowelsInstance() {
        if (storm.world.getMinecraftServer() == null) return false;
        WorldServer bowels = storm.world.getMinecraftServer().getWorld(BowelsDimensions.DIMENSION_ID);
        if (bowels == null) return false;
        BowelsInstanceData.Instance instance = BowelsInstanceData.get(bowels).get(storm.getUniqueID());
        if (instance == null || instance.completed) return false;
        AxisAlignedBB area = new AxisAlignedBB(instance.center).grow(50.0D);
        for (EntityPlayer player : bowels.getEntitiesWithinAABB(EntityPlayer.class, area)) {
            if (player.isEntityAlive()) return true;
        }
        return false;
    }

    private static List<ItemStack> getAllInventoryStacks(EntityPlayer player) {
        List<ItemStack> stacks = new ArrayList<ItemStack>();
        stacks.addAll(player.inventory.mainInventory);
        stacks.addAll(player.inventory.armorInventory);
        stacks.addAll(player.inventory.offHandInventory);
        return stacks;
    }

    private static boolean isCommandBlockTool(ItemStack stack) {
        ResourceLocation name = stack.getItem().getRegistryName();
        if (name == null || !Tags.MOD_ID.equals(name.getNamespace())) return false;
        String path = name.getPath();
        return path.contains("command_block_") && (path.endsWith("_sword") || path.endsWith("_pickaxe")
                || path.endsWith("_axe") || path.endsWith("_shovel") || path.endsWith("_hoe"));
    }

    public int getSummoningDelay() {
        return timeTillCanSummonSymbiont;
    }

    public void setSummoningDelay(int delay) {
        timeTillCanSummonSymbiont = Math.max(0, delay);
    }

    public void writeToNBT(NBTTagCompound compound) {
        compound.setInteger("SymbiontSummoningCooldown", timeTillCanSummonSymbiont);
    }

    public void readFromNBT(NBTTagCompound compound) {
        setSummoningDelay(compound.getInteger("SymbiontSummoningCooldown"));
    }

    public static void markSummoned(EntityPlayer player, EntityWitherStormLegacy storm) {
        NBTTagCompound data = player.getEntityData().getCompoundTag(PLAYER_DATA_KEY);
        data.setUniqueId(LAST_SUMMONED_STORM, storm.getUniqueID());
        data.setInteger(LAST_SUMMONED_PHASE, storm.getPhase());
        player.getEntityData().setTag(PLAYER_DATA_KEY, data);
    }

    public static boolean hasRecentSummon(EntityPlayer player, EntityWitherStormLegacy storm) {
        NBTTagCompound data = player.getEntityData().getCompoundTag(PLAYER_DATA_KEY);
        return data.hasUniqueId(LAST_SUMMONED_STORM)
                && storm.getUniqueID().equals(data.getUniqueId(LAST_SUMMONED_STORM))
                && data.getInteger(LAST_SUMMONED_PHASE) == storm.getPhase();
    }

    public static void markKilledSymbiont(EntityPlayer player, @Nullable EntityWitherStormLegacy storm) {
        NBTTagCompound data = player.getEntityData().getCompoundTag(PLAYER_DATA_KEY);
        long now = player.world.getTotalWorldTime();
        data.setLong(KILLED_UNTIL, now + PLAYER_PROTECTION_MINUTES * 1200L + player.getRNG().nextInt(1200));
        if (storm != null) data.setUniqueId(LAST_SUMMONED_STORM, storm.getUniqueID());
        player.getEntityData().setTag(PLAYER_DATA_KEY, data);
    }

    public static boolean shouldIgnorePlayer(EntityPlayer player) {
        NBTTagCompound data = player.getEntityData().getCompoundTag(PLAYER_DATA_KEY);
        return data.getLong(KILLED_UNTIL) > player.world.getTotalWorldTime();
    }
}
