package com.wdcftgg.witherstormmod.common.entity;

import com.wdcftgg.witherstormmod.common.config.LegacyWitherStormConfig;
import com.wdcftgg.witherstormmod.common.init.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 负责保存 Wither Storm 的长期追逐状态。
 *
 * 1.20 版本把这部分拆成了 UltimateTargetManager、若干事件和配置。1.12
 * 没有同等的目标系统，因此这里将目标选择、停滞计时、随机游走和终末洞
 * 触发合并成一个可序列化的服务对象，避免每个 tick 重新丢失状态。
 */
public final class LegacyUltimateTargetManager {

    public enum DistractionReason { FINISHED_CHASING, FINISHED_CHASING_DELAYED, TIRED_OF_CHASING }

    private final EntityWitherStormLegacy storm;
    private EntityLivingBase ultimateTarget;
    private Vec3d previousTargetPosition;
    private BlockPos alternativeUltimateTarget;
    private BlockPos blockTargetOverride;
    private BlockPos distractedPos;
    private BlockPos randomStrollPos;
    private ChunkPos center;
    private UUID targetOverride;
    private final Map<UUID, Integer> ignoredTargets = new LinkedHashMap<UUID, Integer>();
    private boolean targetStationary;
    private boolean canBeDistracted;
    private boolean distracted;
    private int stationaryTicks;
    private int runawayAttempts;
    private int ticksSinceDistracted;
    private int distractionDuration;
    private int distractionWait;
    private int tillShowHole;
    private int cannotSeeTargetTicks;
    private int tiredOfChasingTicks;

    public LegacyUltimateTargetManager(EntityWitherStormLegacy storm) {
        this.storm = storm;
    }

    public void tick() {
        if (storm.world.isRemote || storm.isDead) return;

        tickIgnoredTargets();
        List<EntityPlayer> players = storm.world.playerEntities;
        EntityLivingBase selected = findUltimateTarget(players);
        if (selected != ultimateTarget) {
            stationaryTicks = 0;
            targetStationary = false;
            cannotSeeTargetTicks = 0;
        }
        ultimateTarget = selected;
        if (selected != null) {
            Vec3d currentPosition = selected.getPositionVector();
            Vec3d lastPosition = previousTargetPosition;
            if (lastPosition != null && currentPosition.squareDistanceTo(lastPosition) < 0.04D) {
                stationaryTicks = Math.min(12000, stationaryTicks + 1);
            } else {
                stationaryTicks = Math.max(0, stationaryTicks - 2);
            }
            targetStationary = stationaryTicks >= 2400;
            alternativeUltimateTarget = selected.getPosition();
            if (center == null || !isPosInChunkRadius(selected.getPosition())) center = new ChunkPos(selected.getPosition());

            if (!storm.canSee(0, selected)) {
                cannotSeeTargetTicks = Math.min(1200, cannotSeeTargetTicks + 1);
            } else {
                cannotSeeTargetTicks = Math.max(0, cannotSeeTargetTicks - 2);
            }

            if (storm.getPhase() > 6 && tillShowHole == 0 && carriesCommandBlockTool(players)) {
                tillShowHole = 1200 + storm.getRNG().nextInt(4800);
            }
            if (tillShowHole > 0 && --tillShowHole == 0) storm.setShouldShowHole(true);

            updateDistraction(currentPosition, lastPosition);
            previousTargetPosition = currentPosition;
        } else {
            alternativeUltimateTarget = null;
            targetStationary = false;
            stationaryTicks = 0;
        }

        if (distracted) {
            ticksSinceDistracted++;
            if (ticksSinceDistracted >= distractionDuration
                    || distractedPos == null
                    || storm.getPositionVector().squareDistanceTo(new Vec3d(distractedPos)) < 2500.0D) {
                makeFocused();
            }
        } else if (targetStationary && canBeDistracted && storm.getPhase() > 3
                && distractedPos == null && distractionWait <= 0
                && storm.getPositionVector().squareDistanceTo(getUltimateTargetPos()) > 90000.0D) {
            makeDistracted(DistractionReason.FINISHED_CHASING);
        }
        if (distractionWait > 0) --distractionWait;
        if (storm.getPhase() > 6 && !distracted && targetStationary && stationaryTicks > 4800) canBeDistracted = true;

        EntityLivingBase movementTarget = getMovementTarget();
        if (movementTarget != null && isValidTarget(movementTarget)) storm.setAttackTarget(movementTarget);
        else if (!distracted) storm.setAttackTarget(null);
    }

    private void updateDistraction(Vec3d targetPosition, @Nullable Vec3d lastPosition) {
        if (getUltimateTargetPos() == null) return;
        if (targetStationary && storm.getPositionVector().squareDistanceTo(targetPosition) > 14400.0D) {
            canBeDistracted = true;
        }
        if (lastPosition != null && targetPosition.squareDistanceTo(lastPosition) > 0.15D) {
            tiredOfChasingTicks = Math.max(0, tiredOfChasingTicks - 1);
        } else if (targetStationary) {
            tiredOfChasingTicks = Math.min(12000, tiredOfChasingTicks + 1);
        }
        if (tiredOfChasingTicks > 6000 && canBeDistracted && !distracted) {
            makeDistracted(DistractionReason.TIRED_OF_CHASING);
            tiredOfChasingTicks = 0;
        }
    }

    @Nullable
    public EntityLivingBase findUltimateTarget(List<EntityPlayer> players) {
        EntityLivingBase override = resolveOverride();
        if (override != null && isValidTarget(override)) return override;

        EntityPlayer amuletTarget = null;
        double amuletDistance = Double.MAX_VALUE;
        Item amulet = ModItems.get("amulet");
        if (LegacyWitherStormConfig.amuletOverride && amulet != null) {
            for (EntityPlayer player : players) {
                if (!isValidTarget(player) || !hasItem(player, amulet)) continue;
                double distance = storm.getDistanceSq(player);
                if (distance < amuletDistance) {
                    amuletDistance = distance;
                    amuletTarget = player;
                }
            }
        }
        if (amuletTarget != null) return amuletTarget;

        EntityWitherStormLegacy largerStorm = null;
        double largerDistance = Double.MAX_VALUE;
        for (Entity entity : storm.world.loadedEntityList) {
            if (!(entity instanceof EntityWitherStormLegacy) || entity == storm) continue;
            EntityWitherStormLegacy candidate = (EntityWitherStormLegacy) entity;
            if (candidate.isDeadOrPlayingDead() || candidate.getConsumedMass() <= storm.getConsumedMass()
                    || candidate.dimension != storm.dimension || candidate.getDistanceSq(storm) > 1000000.0D) continue;
            if (candidate.getDistanceSq(storm) < largerDistance) {
                largerDistance = candidate.getDistanceSq(storm);
                largerStorm = candidate;
            }
        }
        if (largerStorm != null) return largerStorm;

        EntityPlayer nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (EntityPlayer player : players) {
            if (!isValidTarget(player)) continue;
            double distance = storm.getDistanceSq(player);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = player;
            }
        }
        return nearest;
    }

    private EntityLivingBase resolveOverride() {
        if (targetOverride == null) return null;
        for (Entity entity : storm.world.loadedEntityList) {
            if (targetOverride.equals(entity.getUniqueID()) && entity instanceof EntityLivingBase) return (EntityLivingBase) entity;
        }
        return null;
    }

    private boolean isValidTarget(EntityLivingBase entity) {
        if (entity == null || entity.isDead || entity.world != storm.world || entity.dimension != storm.dimension) return false;
        if (entity == storm || entity instanceof SupplementalEntities.StormPartBase) return false;
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            return !player.capabilities.disableDamage && !player.isSpectator()
                    && !storm.hasRecentlyBeenRevived()
                    && !shouldIgnoreTarget(player)
                    && !LegacySymbiontSummoningManager.shouldIgnorePlayer(player);
        }
        return true;
    }

    private void tickIgnoredTargets() {
        Iterator<Map.Entry<UUID, Integer>> iterator = ignoredTargets.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = iterator.next();
            int remaining = entry.getValue() - 1;
            if (remaining <= 0) iterator.remove();
            else entry.setValue(remaining);
        }
    }

    public void ignoreTarget(EntityPlayer player, int ticks) {
        if (player == null || ticks <= 0) return;
        UUID id = player.getUniqueID();
        Integer current = ignoredTargets.get(id);
        ignoredTargets.put(id, current == null ? ticks : Math.max(current, ticks));
        if (ultimateTarget == player) {
            ultimateTarget = null;
            previousTargetPosition = null;
            stationaryTicks = 0;
            targetStationary = false;
        }
        if (storm.getAttackTarget() == player) storm.setAttackTarget(null);
    }

    public boolean shouldIgnoreTarget(@Nullable Entity entity) {
        return entity != null && ignoredTargets.containsKey(entity.getUniqueID());
    }

    static int getHeadEscapeTicks(int configuredSeconds, int randomBonus) {
        return Math.max(0, configuredSeconds) * 20 + MathHelper.clamp(randomBonus, 0, 79);
    }

    private boolean hasItem(EntityPlayer player, Item item) {
        if (player.getHeldItemMainhand().getItem() == item || player.getHeldItemOffhand().getItem() == item) return true;
        for (ItemStack stack : player.inventory.mainInventory) if (!stack.isEmpty() && stack.getItem() == item) return true;
        for (ItemStack stack : player.inventory.offHandInventory) if (!stack.isEmpty() && stack.getItem() == item) return true;
        return false;
    }

    private boolean carriesCommandBlockTool(List<EntityPlayer> players) {
        for (EntityPlayer player : players) {
            if (!isValidTarget(player)) continue;
            for (ItemStack stack : player.inventory.mainInventory) {
                if (!stack.isEmpty() && isCommandBlockTool(stack)) return true;
            }
            for (ItemStack stack : player.inventory.offHandInventory) {
                if (!stack.isEmpty() && isCommandBlockTool(stack)) return true;
            }
        }
        return false;
    }

    private boolean isCommandBlockTool(ItemStack stack) {
        ResourceLocation name = stack.getItem().getRegistryName();
        if (name == null || !"witherstormmod".equals(name.getNamespace())) return false;
        String path = name.getPath();
        return path.contains("command_block_") && (path.endsWith("_sword") || path.endsWith("_pickaxe")
                || path.endsWith("_axe") || path.endsWith("_shovel") || path.endsWith("_hoe"));
    }

    private EntityLivingBase getMovementTarget() {
        if (distracted && distractedPos != null) return null;
        return ultimateTarget;
    }

    public void accelerate() {
        stationaryTicks = 2400;
        targetStationary = true;
        makeFocused();
    }

    public void deaccelerate() {
        stationaryTicks = 0;
        targetStationary = false;
    }

    public void makeDistracted(DistractionReason reason) {
        if (distracted || distractionWait > 0) return;
        BlockPos position = findDistractPos();
        if (position == null) return;
        distractedPos = position;
        distracted = true;
        ticksSinceDistracted = 0;
        double distance = Math.sqrt(storm.getDistanceSq(position.getX() + 0.5D, position.getY() + 0.5D, position.getZ() + 0.5D));
        distractionDuration = Math.max(4800, 7200 - (int) Math.min(2400.0D, distance * 2.0D));
        canBeDistracted = false;
    }

    public void makeFocused() {
        distracted = false;
        ticksSinceDistracted = 0;
        canBeDistracted = false;
        distractedPos = null;
        distractionDuration = 0;
    }

    @Nullable
    private BlockPos findDistractPos() {
        EntityLivingBase target = ultimateTarget;
        if (target == null) return null;
        BlockPos base = target.getPosition();
        for (int attempt = 0; attempt < 16; attempt++) {
            int radius = 128 + storm.getRNG().nextInt(128);
            double angle = storm.getRNG().nextDouble() * Math.PI * 2.0D;
            BlockPos candidate = base.add(MathHelper.floor(Math.cos(angle) * radius), 0,
                    MathHelper.floor(Math.sin(angle) * radius));
            int y = storm.world.getHeight(candidate).getY();
            candidate = new BlockPos(candidate.getX(), y, candidate.getZ());
            if (storm.world.isBlockLoaded(candidate) && !storm.world.isAirBlock(candidate)) return candidate;
        }
        return null;
    }

    @Nullable
    public EntityLivingBase getUltimateTarget() { return ultimateTarget; }

    @Nullable
    public Vec3d getUltimateTargetPos() {
        if (blockTargetOverride != null) return new Vec3d(blockTargetOverride).add(0.5D, 0.0D, 0.5D);
        if (distracted && distractedPos != null) return new Vec3d(distractedPos).add(0.5D, 0.0D, 0.5D);
        if (ultimateTarget != null) return ultimateTarget.getPositionVector();
        if (alternativeUltimateTarget != null) return new Vec3d(alternativeUltimateTarget).add(0.5D, 0.0D, 0.5D);
        return null;
    }

    @Nullable public BlockPos getAlternativeUltimateTarget() { return alternativeUltimateTarget; }
    public void setAlternativeUltimateTarget(@Nullable BlockPos pos) { alternativeUltimateTarget = pos; }
    @Nullable public BlockPos getDistractedPos() { return distractedPos; }
    public void setDistractedPos(@Nullable BlockPos pos) { distractedPos = pos; }
    public boolean isTargetStationary() { return targetStationary; }
    public int targetStationaryTicks() { return stationaryTicks; }
    public int getRunawayAttempts() { return runawayAttempts; }
    public void countRunawayAttempt() { runawayAttempts++; }
    public void setRunawayAttempts(int amount) { runawayAttempts = Math.max(0, amount); }
    public boolean isDistracted() { return distracted; }
    public boolean canBeDistracted() { return canBeDistracted; }
    public int getTicksSinceDistracted() { return ticksSinceDistracted; }
    public int getDistractionWait() { return distractionWait; }
    public int tillShowHole() { return tillShowHole; }
    public void setTillShowHole(int ticks) { tillShowHole = Math.max(0, ticks); }
    public void setTargetOverride(@Nullable UUID uuid) { targetOverride = uuid; }
    @Nullable public UUID getTargetOverride() { return targetOverride; }
    public void setBlockTargetOverride(@Nullable BlockPos pos) { blockTargetOverride = pos; }
    @Nullable public BlockPos getBlockTargetOverride() { return blockTargetOverride; }
    @Nullable public ChunkPos getCenter() { return center; }
    public void setCenter(@Nullable ChunkPos value) { center = value; }
    public boolean cannotSeeTarget() { return cannotSeeTargetTicks > 600; }

    public boolean isPosInChunkRadius(BlockPos pos) {
        if (pos == null || center == null) return false;
        ChunkPos current = new ChunkPos(pos);
        return Math.abs(current.x - center.x) <= 2
                && Math.abs(current.z - center.z) <= 2;
    }

    public void save(NBTTagCompound compound) {
        if (alternativeUltimateTarget != null) compound.setLong("AlternativeUltimateTarget", alternativeUltimateTarget.toLong());
        if (blockTargetOverride != null) compound.setLong("BlockTargetOverride", blockTargetOverride.toLong());
        if (distractedPos != null) compound.setLong("DistractedPos", distractedPos.toLong());
        if (targetOverride != null) compound.setUniqueId("TargetOverride", targetOverride);
        NBTTagList ignored = new NBTTagList();
        for (Map.Entry<UUID, Integer> entry : ignoredTargets.entrySet()) {
            NBTTagCompound target = new NBTTagCompound();
            target.setUniqueId("UUID", entry.getKey());
            target.setInteger("Ticks", entry.getValue());
            ignored.appendTag(target);
        }
        compound.setTag("IgnoredTargets", ignored);
        compound.setInteger("TargetStationaryTicks", stationaryTicks);
        compound.setInteger("TargetRunawayAttempts", runawayAttempts);
        compound.setBoolean("TargetStationary", targetStationary);
        compound.setBoolean("CanBeDistracted", canBeDistracted);
        compound.setBoolean("IsDistracted", distracted);
        compound.setInteger("TicksSinceDistracted", ticksSinceDistracted);
        compound.setInteger("DistractionDuration", distractionDuration);
        compound.setInteger("DistractionWait", distractionWait);
        compound.setInteger("TillShowHole", tillShowHole);
        compound.setInteger("CannotSeeTargetTicks", cannotSeeTargetTicks);
    }

    public void read(NBTTagCompound compound) {
        if (compound.hasKey("AlternativeUltimateTarget", 4)) alternativeUltimateTarget = BlockPos.fromLong(compound.getLong("AlternativeUltimateTarget"));
        if (compound.hasKey("BlockTargetOverride", 4)) blockTargetOverride = BlockPos.fromLong(compound.getLong("BlockTargetOverride"));
        if (compound.hasKey("DistractedPos", 4)) distractedPos = BlockPos.fromLong(compound.getLong("DistractedPos"));
        targetOverride = compound.hasUniqueId("TargetOverride") ? compound.getUniqueId("TargetOverride") : null;
        ignoredTargets.clear();
        NBTTagList ignored = compound.getTagList("IgnoredTargets", 10);
        for (int index = 0; index < ignored.tagCount(); index++) {
            NBTTagCompound target = ignored.getCompoundTagAt(index);
            if (target.hasUniqueId("UUID") && target.getInteger("Ticks") > 0) {
                ignoredTargets.put(target.getUniqueId("UUID"), target.getInteger("Ticks"));
            }
        }
        if (ignoredTargets.isEmpty() && compound.hasUniqueId("IgnoredTarget")
                && compound.getInteger("IgnoredTargetTicks") > 0) {
            ignoredTargets.put(compound.getUniqueId("IgnoredTarget"), compound.getInteger("IgnoredTargetTicks"));
        }
        stationaryTicks = Math.max(0, compound.getInteger("TargetStationaryTicks"));
        runawayAttempts = Math.max(0, compound.getInteger("TargetRunawayAttempts"));
        targetStationary = compound.getBoolean("TargetStationary");
        canBeDistracted = compound.getBoolean("CanBeDistracted");
        distracted = compound.getBoolean("IsDistracted") && distractedPos != null;
        ticksSinceDistracted = Math.max(0, compound.getInteger("TicksSinceDistracted"));
        distractionDuration = Math.max(0, compound.getInteger("DistractionDuration"));
        distractionWait = Math.max(0, compound.getInteger("DistractionWait"));
        tillShowHole = Math.max(0, compound.getInteger("TillShowHole"));
        cannotSeeTargetTicks = Math.max(0, compound.getInteger("CannotSeeTargetTicks"));
    }
}
