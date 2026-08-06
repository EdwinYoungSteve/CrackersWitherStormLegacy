package com.wdcftgg.witherstormmod.common.entity;

import com.wdcftgg.witherstormmod.common.config.WitherStormConfig;
import com.wdcftgg.witherstormmod.common.init.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/** 还原分段从主风暴继承到的独立三头选敌、牵引与吸收状态。 */
final class WitherStormSegmentManager {
    private static final double[][] HEAD_OFFSETS = {
            {0.0D, 9.0D, 14.0D},
            {6.0D, 8.0D, 12.0D},
            {-6.0D, 8.0D, 12.0D}
    };

    private final SupplementalEntities.WitherStormSegmentEntity segment;
    private final EntityLivingBase[] targets = new EntityLivingBase[3];
    private final int[] nextRoarTicks = new int[3];
    private final int[] nextClusterTicks = new int[3];
    private final Map<UUID, Entity> trackedEntities = new LinkedHashMap<UUID, Entity>();
    private final List<UUID> savedTrackedEntities = new ArrayList<UUID>();
    private List<SupplementalEntities.WitherStormSegmentEntity> familySegments = Collections.emptyList();
    private int nextTargetRefresh;
    private int trackedEntityTicks;

    WitherStormSegmentManager(SupplementalEntities.WitherStormSegmentEntity segment) {
        this.segment = segment;
    }

    void tick() {
        if (segment.world.isRemote || segment.isDead || segment.isInDeathSequence()) return;
        WitherStormEntity owner = segment.getOwnerStorm();
        if (owner == null || owner.isDeadOrPlayingDead()) return;
        familySegments = findFamilySegments();
        resolveSavedTrackedEntities();
        refreshTargets(owner);
        tickDefaultClusterSource(owner);
        tickHeads(owner);
        tickTrackedEntities(owner);
    }

    private void tickDefaultClusterSource(WitherStormEntity owner) {
        int interval = Math.max(1, WitherStormConfig.devourerClusterPickupInterval);
        if (!segment.world.getGameRules().getBoolean("mobGriefing")
                || segment.ticksExisted % interval != 0) return;
        SupplementalEntities.BlockClusterEntity cluster = owner.createDefaultClusterForSegment(segment);
        if (cluster != null) trackEntity(cluster);
    }

    private void refreshTargets(WitherStormEntity owner) {
        boolean refreshAll = segment.ticksExisted >= nextTargetRefresh;
        if (refreshAll) nextTargetRefresh = segment.ticksExisted + 10;
        for (int head = 0; head < targets.length; head++) {
            if (!isHeadEnabled(owner, head)) {
                targets[head] = null;
                continue;
            }
            EntityLivingBase current = targets[head];
            if (!refreshAll && isValidTarget(owner, current, head, true)) continue;
            targets[head] = null;
            targets[head] = findTarget(owner, head);
        }
    }

    @Nullable
    private EntityLivingBase findTarget(WitherStormEntity owner, int head) {
        AxisAlignedBB search = segment.getEntityBoundingBox().grow(160.0D, 80.0D, 160.0D);
        EntityLivingBase nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (EntityLivingBase candidate : segment.world.getEntitiesWithinAABB(EntityLivingBase.class, search)) {
            if (!isValidTarget(owner, candidate, head, false)) continue;
            double distance = segment.getDistanceSq(candidate);
            if (distance < nearestDistance) {
                nearest = candidate;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private boolean isValidTarget(WitherStormEntity owner, @Nullable EntityLivingBase entity,
                                  int head, boolean allowCurrentTarget) {
        if (entity == null || entity == segment || entity == owner || !entity.isEntityAlive()
                || entity.world != segment.world || entity.dimension != segment.dimension
                || !owner.isValidStormTarget(entity, false)
                || entity.isInvisible()
                || owner.getUltimateTargetManager().shouldIgnoreTarget(entity)
                || owner.isTrackedForConsumption(entity)
                || isTrackedByAnySegment(entity)
                || !canSee(head, entity)) {
            return false;
        }
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            if (player.capabilities.disableDamage || player.isSpectator() || owner.hasRecentlyBeenRevived()
                    || player.isHandActive() && player.getActiveItemStack().getItem() == Items.SHIELD) return false;
        }
        return !isTargetInUseByStormFamily(entity, head, allowCurrentTarget);
    }

    private boolean isTargetInUseByStormFamily(Entity entity, int requesterHead, boolean allowCurrentTarget) {
        WitherStormEntity owner = segment.getOwnerStorm();
        if (owner != null && owner.isTargetedByMainHeadFamily(entity)) return true;
        for (SupplementalEntities.WitherStormSegmentEntity other : familySegments) {
            for (int head = 0; head < 3; head++) {
                if (other == segment && head == requesterHead && allowCurrentTarget) continue;
                if (other.getSegmentTarget(head) == entity) return true;
            }
        }
        return false;
    }

    private void tickHeads(WitherStormEntity owner) {
        AxisAlignedBB search = segment.getEntityBoundingBox().grow(320.0D);
        List<Entity> pullable = segment.world.getEntitiesWithinAABB(Entity.class, search,
                entity -> isBasicPullable(entity) && entity != segment && entity != owner && !entity.isDead
                        && entity.dimension == segment.dimension
                        && !(entity instanceof SupplementalEntities.StormPartBase)
                        && !(entity instanceof WitherStormEntity)
                        && !(entity instanceof PowerfulExplosiveEntity.FormidibombEntity)
                        && !owner.isTrackedForConsumption(entity) && !isTrackedByAnySegment(entity)
                        && (!(entity instanceof EntityPlayer)
                        || !((EntityPlayer) entity).capabilities.disableDamage));
        Set<UUID> pulled = new HashSet<UUID>();
        for (int head = 0; head < 3; head++) {
            if (!isHeadEnabled(owner, head)) continue;
            Vec3d headPosition = getHeadPosition(head);
            Vec3d direction = getHeadDirection(head, headPosition);
            double cutoff = getBeamCutoff(headPosition, direction);
            tickHeadRoar(owner, head, headPosition, direction);
            tickHeadClusterPickup(owner, head, headPosition, direction);
            for (Entity entity : pullable) {
                if (pulled.contains(entity.getUniqueID())
                        || !isInsideBeam(entity, headPosition, direction, cutoff)) continue;
                boolean selectedTarget = targets[head] == entity;
                if (!selectedTarget && !canPullUntargeted(owner, entity, head)) continue;
                double speed = getPullSpeed(entity);
                pullInTarget(owner, entity, speed, headPosition, direction);
                pulled.add(entity.getUniqueID());
            }
        }
    }

    private void tickHeadRoar(WitherStormEntity owner, int head, Vec3d position, Vec3d direction) {
        if (nextRoarTicks[head] == 0) {
            nextRoarTicks[head] = segment.ticksExisted + 200 + segment.getRNG().nextInt(200);
        }
        if (segment.ticksExisted <= nextRoarTicks[head]) return;
        int minimum = Math.max(1, WitherStormConfig.minimumRoarInterval) * 20;
        int maximum = Math.max(minimum, WitherStormConfig.maximumRoarInterval * 20);
        nextRoarTicks[head] = segment.ticksExisted + minimum
                + (maximum > minimum ? segment.getRNG().nextInt(maximum - minimum) : 0);
        segment.world.playSound(null, position.x, position.y, position.z,
                ModSounds.get("wither_storm_roar"), SoundCategory.HOSTILE, 17.5F, 1.0F);
        SupplementalEntities.FlamingWitherSkullEntity skull = new SupplementalEntities.FlamingWitherSkullEntity(
                segment.world, segment, direction.x * 0.75D, direction.y * 0.75D, direction.z * 0.75D);
        skull.setPosition(position.x, position.y, position.z);
        segment.world.spawnEntity(skull);
    }

    private void tickHeadClusterPickup(WitherStormEntity owner, int head,
                                       Vec3d position, Vec3d direction) {
        if (segment.ticksExisted < nextClusterTicks[head]) return;
        nextClusterTicks[head] = segment.ticksExisted + 12;
        SupplementalEntities.BlockClusterEntity cluster = owner.createTractorBeamCluster(position, direction, 1, head);
        if (cluster != null) trackEntity(cluster);
        owner.removeFluidFromRay(position, direction);
    }

    private boolean canPullUntargeted(WitherStormEntity owner, Entity entity, int head) {
        if (!WitherStormConfig.canPickupMobClusters
                || owner.getUltimateTargetManager().shouldIgnoreTarget(entity)
                || isTargetInUseByStormFamily(entity, head, false)
                || !canSee(head, entity)) return false;
        return !(entity instanceof EntityLivingBase)
                || isValidTarget(owner, (EntityLivingBase) entity, head, false)
                && !owner.isBlockingWithShield((EntityLivingBase) entity);
    }

    private double getPullSpeed(Entity entity) {
        if (entity instanceof EntityItem || entity instanceof EntityBoat || entity instanceof EntityMinecart) {
            return 0.4D;
        }
        if (entity instanceof EntityPlayer) return WitherStormConfig.tractorPullSpeedModifier;
        return WitherStormConfig.tractorPullSpeedModifier - 0.05D
                + new Random(entity.getEntityId()).nextDouble() * 0.1D;
    }

    private void pullInTarget(WitherStormEntity owner, Entity target, double speed,
                              Vec3d headPosition, Vec3d direction) {
        Vec3d targetPosition = headPosition;
        if (!(target instanceof EntityPlayer) && target.getPositionVector().distanceTo(headPosition) >= 25.0D) {
            Vec3d relative = target.getPositionVector().subtract(headPosition);
            double projection = Math.max(0.0D, relative.dotProduct(direction));
            targetPosition = headPosition.add(direction.scale(projection));
        }
        Vec3d delta = targetPosition.subtract(target.getPositionVector());
        if (delta.lengthSquared() > 0.0001D) {
            Vec3d velocity = delta.normalize().scale(Math.min(0.5D, Math.max(0.05D, speed)));
            target.motionX = velocity.x;
            target.motionY = velocity.y;
            target.motionZ = velocity.z;
            target.velocityChanged = true;
            if (target instanceof EntityPlayerMP) {
                com.wdcftgg.witherstormmod.common.network.ModNetwork.setPlayerMotion(
                        (EntityPlayerMP) target, velocity);
            }
        }
        if (!(target instanceof EntityPlayer)
                && target.getPositionVector().squareDistanceTo(headPosition) < 400.0D) {
            trackEntity(target);
        }
        if (target.getPositionVector().distanceTo(headPosition) >= 20.0D) return;
        if (target instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) target;
            if (owner.isBeingTornApart() && !player.capabilities.isCreativeMode) {
                owner.pullPlayerIntoBowels(player);
            } else {
                player.attackEntityFrom(DamageSource.causeMobDamage(segment), 3.5F);
            }
        } else {
            trackEntity(target);
        }
    }

    private void trackEntity(Entity entity) {
        if (entity == null || entity.isDead || entity instanceof EntityPlayer
                || entity instanceof WitherStormEntity
                || entity instanceof SupplementalEntities.StormPartBase
                || entity instanceof PowerfulExplosiveEntity.FormidibombEntity) return;
        WitherStormEntity owner = segment.getOwnerStorm();
        if (owner != null && owner.isTrackedForConsumption(entity)) return;
        if (isTrackedByAnySegment(entity)) return;
        trackedEntities.put(entity.getUniqueID(), entity);
        savedTrackedEntities.remove(entity.getUniqueID());
        if (entity instanceof SupplementalEntities.BlockClusterEntity) {
            entity.setNoGravity(true);
            ((SupplementalEntities.BlockClusterEntity) entity).setPhysics(false);
        }
    }

    private void tickTrackedEntities(WitherStormEntity owner) {
        if (trackedEntities.isEmpty()) return;
        Vec3d absorptionPoint = new Vec3d(segment.posX, segment.posY + segment.height * 0.5D, segment.posZ);
        AxisAlignedBB absorptionBox = segment.getEntityBoundingBox().grow(
                Math.max(1.0D, segment.width / 1.5D), 0.0D, Math.max(1.0D, segment.width / 1.5D));
        List<Entity> splitClusters = new ArrayList<Entity>();
        Iterator<Map.Entry<UUID, Entity>> iterator = trackedEntities.entrySet().iterator();
        while (iterator.hasNext()) {
            Entity entity = iterator.next().getValue();
            if (entity == null || entity.isDead || entity.world != segment.world) {
                iterator.remove();
                continue;
            }
            Vec3d delta = absorptionPoint.subtract(entity.getPositionVector());
            double distance = delta.length();
            if (distance >= 320.0D) entity.setPosition(absorptionPoint.x, absorptionPoint.y, absorptionPoint.z);
            if (distance > 0.001D) {
                Vec3d velocity = delta.normalize().scale(0.5D);
                entity.motionX = velocity.x;
                entity.motionY = velocity.y;
                entity.motionZ = velocity.z;
                entity.velocityChanged = true;
            }
            if (entity instanceof SupplementalEntities.BlockClusterEntity) {
                SupplementalEntities.BlockClusterEntity cluster = (SupplementalEntities.BlockClusterEntity) entity;
                if (cluster.shouldCrumble() && cluster.getShakeTime() <= 0
                        && segment.ticksExisted % 20 == 0 && segment.getRNG().nextInt(3) == 0) {
                    SupplementalEntities.BlockClusterEntity split = cluster.splitAt(
                            EnumFacing.Axis.values()[segment.getRNG().nextInt(3)]);
                    if (split != null) splitClusters.add(split);
                }
            }
            if (absorptionBox.contains(entity.getPositionVector())) {
                owner.consumeEntityFromSegment(entity);
                iterator.remove();
            }
        }
        for (Entity split : splitClusters) trackEntity(split);
    }

    private void resolveSavedTrackedEntities() {
        if (savedTrackedEntities.isEmpty()) return;
        ++trackedEntityTicks;
        Iterator<UUID> iterator = savedTrackedEntities.iterator();
        while (iterator.hasNext()) {
            UUID uuid = iterator.next();
            Entity entity = resolve(uuid);
            if (entity != null && !entity.isDead) {
                trackedEntities.put(uuid, entity);
                if (entity instanceof SupplementalEntities.BlockClusterEntity) {
                    entity.setNoGravity(true);
                    ((SupplementalEntities.BlockClusterEntity) entity).setPhysics(false);
                }
                iterator.remove();
            } else if (trackedEntityTicks > 80) {
                iterator.remove();
            }
        }
    }

    @Nullable
    private Entity resolve(UUID uuid) {
        for (Entity entity : segment.world.loadedEntityList) {
            if (uuid.equals(entity.getUniqueID())) return entity;
        }
        return null;
    }

    boolean isTracking(Entity entity) {
        return entity != null && (trackedEntities.containsKey(entity.getUniqueID())
                || savedTrackedEntities.contains(entity.getUniqueID()));
    }

    @Nullable
    EntityLivingBase getTarget(int head) {
        return targets[MathHelper.clamp(head, 0, 2)];
    }

    Vec3d getHeadPosition(int head) {
        double[] offset = HEAD_OFFSETS[MathHelper.clamp(head, 0, 2)];
        float yaw = (segment.renderYawOffset + 180.0F) * 0.017453292F;
        double x = MathHelper.cos(yaw) * offset[0]
                + MathHelper.cos(yaw + (float) Math.PI / 2.0F) * offset[2];
        double z = MathHelper.sin(yaw) * offset[0]
                + MathHelper.sin(yaw + (float) Math.PI / 2.0F) * offset[2];
        return new Vec3d(segment.posX + x, segment.posY + offset[1], segment.posZ + z);
    }

    private Vec3d getHeadDirection(int head, Vec3d headPosition) {
        EntityLivingBase target = targets[head];
        if (target != null && target.isEntityAlive()) {
            Vec3d direction = target.getPositionVector().add(0.0D, target.height * 0.5D, 0.0D)
                    .subtract(headPosition);
            if (direction.lengthSquared() > 0.0001D) return direction.normalize();
        }
        float yaw = (segment.renderYawOffset + 180.0F + (head - 1) * 16.0F) * 0.017453292F;
        return new Vec3d(MathHelper.cos(yaw), -0.08D, MathHelper.sin(yaw)).normalize();
    }

    private double getBeamCutoff(Vec3d position, Vec3d direction) {
        RayTraceResult result = segment.world.rayTraceBlocks(position,
                position.add(direction.scale(250.0D)), false, true, false);
        return result != null && result.typeOfHit == RayTraceResult.Type.BLOCK
                ? position.distanceTo(result.hitVec) : -1.0D;
    }

    private boolean isInsideBeam(Entity entity, Vec3d origin, Vec3d direction, double cutoff) {
        Vec3d relative = entity.getPositionVector().subtract(origin);
        double projection = relative.dotProduct(direction);
        if (projection < 0.0D || projection > 320.0D || cutoff >= 0.0D && projection > cutoff) return false;
        Vec3d closest = origin.add(direction.scale(projection));
        double radius = 1.5D + projection * 0.045D;
        return entity.getDistanceSq(closest.x, closest.y, closest.z) <= radius * radius;
    }

    private boolean canSee(int head, Entity entity) {
        Vec3d start = getHeadPosition(head);
        Vec3d end = entity instanceof EntityLivingBase
                ? ((EntityLivingBase) entity).getPositionEyes(1.0F) : entity.getPositionVector();
        RayTraceResult hit = segment.world.rayTraceBlocks(start, end, false, true, false);
        return hit == null || hit.typeOfHit == RayTraceResult.Type.MISS;
    }

    private boolean isTrackedByAnySegment(Entity entity) {
        if (entity == null) return false;
        for (SupplementalEntities.WitherStormSegmentEntity other : familySegments) {
            if (other.getSegmentManager().isTracking(entity)) return true;
        }
        return false;
    }

    private List<SupplementalEntities.WitherStormSegmentEntity> findFamilySegments() {
        UUID ownerUuid = segment.getOwnerUuid();
        if (ownerUuid == null) return Collections.singletonList(segment);
        return segment.world.getEntities(SupplementalEntities.WitherStormSegmentEntity.class,
                candidate -> !candidate.isDead && ownerUuid.equals(candidate.getOwnerUuid()));
    }

    private static boolean isBasicPullable(Entity entity) {
        return entity instanceof EntityLivingBase || entity instanceof EntityItem
                || entity instanceof EntityBoat || entity instanceof EntityMinecart;
    }

    private static boolean isHeadEnabled(WitherStormEntity owner, int head) {
        return head == 0 || !owner.areOtherHeadsDisabled();
    }

    void transferTrackedEntitiesToOwner() {
        for (UUID uuid : savedTrackedEntities) {
            Entity entity = resolve(uuid);
            if (entity != null && !entity.isDead) trackedEntities.put(uuid, entity);
        }
        WitherStormEntity owner = segment.getOwnerStorm();
        if (owner != null && !owner.isDead) {
            for (Entity entity : trackedEntities.values()) owner.trackEntityFromSegment(entity);
        } else {
            for (Entity entity : trackedEntities.values()) {
                if (entity instanceof SupplementalEntities.BlockClusterEntity) {
                    entity.setNoGravity(false);
                    ((SupplementalEntities.BlockClusterEntity) entity).setPhysics(true);
                }
            }
        }
        trackedEntities.clear();
        savedTrackedEntities.clear();
    }

    void writeToNBT(NBTTagCompound compound) {
        compound.setInteger("TimerFormat", 1);
        compound.setInteger("TargetRefresh", Math.max(0, nextTargetRefresh - segment.ticksExisted));
        NBTTagList tracked = new NBTTagList();
        for (UUID uuid : trackedEntities.keySet()) {
            NBTTagCompound entry = new NBTTagCompound();
            entry.setUniqueId("UUID", uuid);
            tracked.appendTag(entry);
        }
        for (UUID uuid : savedTrackedEntities) {
            NBTTagCompound entry = new NBTTagCompound();
            entry.setUniqueId("UUID", uuid);
            tracked.appendTag(entry);
        }
        compound.setTag("TrackedEntities", tracked);
        for (int head = 0; head < 3; head++) {
            compound.setInteger("NextRoar" + head, Math.max(0, nextRoarTicks[head] - segment.ticksExisted));
            compound.setInteger("NextCluster" + head, Math.max(0, nextClusterTicks[head] - segment.ticksExisted));
        }
    }

    void readFromNBT(NBTTagCompound compound) {
        boolean relativeTimers = compound.getInteger("TimerFormat") == 1;
        nextTargetRefresh = relativeTimers
                ? segment.ticksExisted + Math.max(0, compound.getInteger("TargetRefresh")) : 0;
        trackedEntities.clear();
        savedTrackedEntities.clear();
        trackedEntityTicks = 0;
        NBTTagList tracked = compound.getTagList("TrackedEntities", 10);
        for (int index = 0; index < tracked.tagCount(); index++) {
            NBTTagCompound entry = tracked.getCompoundTagAt(index);
            if (entry.hasUniqueId("UUID")) savedTrackedEntities.add(entry.getUniqueId("UUID"));
        }
        for (int head = 0; head < 3; head++) {
            nextRoarTicks[head] = relativeTimers
                    ? segment.ticksExisted + Math.max(0, compound.getInteger("NextRoar" + head)) : 0;
            nextClusterTicks[head] = relativeTimers
                    ? segment.ticksExisted + Math.max(0, compound.getInteger("NextCluster" + head)) : 0;
        }
    }
}
