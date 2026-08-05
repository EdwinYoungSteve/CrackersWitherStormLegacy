package com.wdcftgg.witherstormmod.common.entity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.EnumFacing;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.potion.PotionEffect;
import com.wdcftgg.witherstormmod.common.init.ModBlocks;
import com.wdcftgg.witherstormmod.common.init.ModSounds;
import com.wdcftgg.witherstormmod.common.world.BowelsBossfightController;
import com.wdcftgg.witherstormmod.common.world.LegacyChunkLoadingManager;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public final class SupplementalEntities {

    private SupplementalEntities() {
    }

    public static class FlamingWitherSkull extends EntityWitherSkull {
        public FlamingWitherSkull(World world) { super(world); setSize(0.8F, 0.8F); }
        public FlamingWitherSkull(World world, EntityLivingBase shooter, double accelerationX, double accelerationY, double accelerationZ) {
            super(world, shooter, accelerationX, accelerationY, accelerationZ);
            setSize(0.8F, 0.8F);
        }

        @Override
        protected void onImpact(RayTraceResult result) {
            if (!world.isRemote) {
                if (result.entityHit != null) {
                    result.entityHit.attackEntityFrom(DamageSource.causeIndirectMagicDamage(this, shootingEntity), 12.0F);
                    result.entityHit.setFire(8);
                }
                world.newExplosion(this, posX, posY, posZ, 3.0F, true, true);
                setDead();
            }
        }
    }

    public static class BlueFlamingWitherSkull extends FlamingWitherSkull {
        public BlueFlamingWitherSkull(World world) { super(world); setInvulnerable(true); }
        public BlueFlamingWitherSkull(World world, EntityLivingBase shooter, double accelerationX, double accelerationY, double accelerationZ) {
            super(world, shooter, accelerationX, accelerationY, accelerationZ);
            setInvulnerable(true);
        }
    }

    public static class TentacleSpike extends Entity {
        private UUID ownerUuid;
        private EntityLivingBase owner;
        private boolean sentSpikeEvent;
        private int warmupDelayTicks;
        private int lifeTicks = 22;
        private boolean clientAttackStarted;
        private float damageModifier;

        public TentacleSpike(World world) {
            super(world);
            setSize(0.5F, 1.4F);
            noClip = true;
        }

        public TentacleSpike(World world, double x, double y, double z, float yawRadians, int warmup,
                             EntityLivingBase owner, float damageModifier) {
            this(world);
            warmupDelayTicks = warmup;
            setOwner(owner);
            rotationYaw = yawRadians * 57.295776F;
            setPosition(x, y, z);
            this.damageModifier = damageModifier;
        }

        @Override
        protected void entityInit() {
        }

        public void setOwner(EntityLivingBase owner) {
            this.owner = owner;
            ownerUuid = owner == null ? null : owner.getUniqueID();
        }

        public EntityLivingBase getOwner() {
            if (owner == null && ownerUuid != null && world instanceof WorldServer) {
                Entity entity = ((WorldServer) world).getEntityFromUuid(ownerUuid);
                if (entity instanceof EntityLivingBase) owner = (EntityLivingBase) entity;
            }
            return owner;
        }

        @Override
        public void onUpdate() {
            super.onUpdate();
            motionX = motionY = motionZ = 0.0D;
            if (world.isRemote) {
                if (clientAttackStarted) lifeTicks--;
                return;
            }
            if (--warmupDelayTicks < 0) {
                if (warmupDelayTicks == -2) {
                    for (EntityLivingBase target : world.getEntitiesWithinAABB(EntityLivingBase.class,
                            getEntityBoundingBox().grow(0.6D, 0.0D, 0.6D))) {
                        dealDamageTo(target);
                    }
                }
                if (!sentSpikeEvent) {
                    world.setEntityState(this, (byte) 4);
                    sentSpikeEvent = true;
                }
                if (--lifeTicks < 0) setDead();
            }
        }

        private void dealDamageTo(EntityLivingBase target) {
            EntityLivingBase spikeOwner = getOwner();
            if (!target.isEntityAlive() || target == spikeOwner || target.isEntityInvulnerable(DamageSource.GENERIC)) return;
            if (spikeOwner != null && spikeOwner.isOnSameTeam(target)) return;
            DamageSource source = spikeOwner == null ? DamageSource.GENERIC : DamageSource.causeIndirectDamage(this, spikeOwner);
            target.attackEntityFrom(source, 6.0F + damageModifier);
        }

        @Override
        public void handleStatusUpdate(byte id) {
            super.handleStatusUpdate(id);
            if (id == 4) {
                clientAttackStarted = true;
                if (!isSilent()) {
                    world.playSound(posX, posY, posZ, ModSounds.get("tentacle_spike_stab"), SoundCategory.HOSTILE,
                            1.0F, rand.nextFloat() * 0.2F + 0.85F, false);
                }
            }
        }

        public float getAnimationProgress(float partialTicks) {
            if (!clientAttackStarted) return 0.0F;
            int remaining = lifeTicks - 2;
            return remaining <= 0 ? 1.0F : 1.0F - (remaining - partialTicks) / 20.0F;
        }

        @Override
        protected void readEntityFromNBT(NBTTagCompound compound) {
            warmupDelayTicks = compound.getInteger("WarmupDelay");
            if (compound.hasUniqueId("Owner")) ownerUuid = compound.getUniqueId("Owner");
            damageModifier = compound.getFloat("DamageModifier");
            lifeTicks = compound.hasKey("LifeTicks") ? compound.getInteger("LifeTicks") : 22;
            sentSpikeEvent = compound.getBoolean("SentSpikeEvent");
        }

        @Override
        protected void writeEntityToNBT(NBTTagCompound compound) {
            compound.setInteger("WarmupDelay", warmupDelayTicks);
            if (ownerUuid != null) compound.setUniqueId("Owner", ownerUuid);
            compound.setFloat("DamageModifier", damageModifier);
            compound.setInteger("LifeTicks", lifeTicks);
            compound.setBoolean("SentSpikeEvent", sentSpikeEvent);
        }

        @Override
        public boolean canBeCollidedWith() {
            return false;
        }

        @Override
        public boolean canBePushed() {
            return false;
        }
    }

    public static class BlockCluster extends EntityFallingBlock implements IEntityAdditionalSpawnData {
        private final Map<BlockPos, IBlockState> blocks = new LinkedHashMap<BlockPos, IBlockState>();
        private float clusterPitch;
        private float previousClusterPitch;
        private float clusterYaw;
        private float previousClusterYaw;
        private float pitchVelocity = 1.5F;
        private float yawVelocity = 2.0F;
        private float clusterSizeX = 1.0F;
        private float clusterSizeY = 1.0F;
        private float clusterSizeZ = 1.0F;
        private float shakeX;
        private float previousShakeX;
        private float shakeZ;
        private float previousShakeZ;
        private boolean physics = true;
        private int shakeTime;
        private int sink;
        private boolean antiStacking;
        private boolean shouldCrumble;
        private boolean dropItems;
        private boolean forceRender;
        private boolean createdFromBeam;
        private boolean createdFromFallingBlock;
        private int headCreatedFrom = -1;
        private int maximumAge = 600;
        private BlockPos fadePos;
        private float fadeStrength = 10.0F;
        private int fadeDistanceOffset;
        private float fadeAmount = 1.0F;
        private float previousFadeAmount = 1.0F;
        private double tractorBeamDistanceThreshold = 4.0D;
        private final List<NBTTagCompound> tileData = new ArrayList<NBTTagCompound>();
        private BlockPos startPos = BlockPos.ORIGIN;

        public BlockCluster(World world) {
            super(world);
            setClusterSize(1.0F, 1.0F, 1.0F);
        }

        public BlockCluster(World world, double positionX, double positionY, double positionZ, IBlockState state) {
            super(world, positionX, positionY, positionZ, state);
            addBlock(BlockPos.ORIGIN, state);
        }

        public BlockCluster(World world, double positionX, double positionY, double positionZ,
                            Map<BlockPos, IBlockState> states) {
            this(world);
            setPosition(positionX, positionY, positionZ);
            setBlocks(states);
        }

        public Map<BlockPos, IBlockState> getBlocks() {
            return blocks;
        }

        public void setBlocks(Map<BlockPos, IBlockState> states) {
            blocks.clear();
            blocks.putAll(states);
            startPos = calculateStartPos(blocks.keySet());
            updateClusterSize();
        }

        public void addBlock(BlockPos offset, IBlockState state) {
            blocks.put(offset, state);
            updateClusterSize();
        }

        /** 将一段方块区域转换为可移动的实体簇，并从世界中取走原方块。 */
        public void populate(BlockPos minimum, BlockPos maximum) {
            blocks.clear();
            tileData.clear();
            startPos = minimum.toImmutable();
            setPosition(minimum.getX(), minimum.getY(), minimum.getZ());
            for (int x = minimum.getX(); x <= maximum.getX(); x++) {
                for (int y = minimum.getY(); y <= maximum.getY(); y++) {
                    for (int z = minimum.getZ(); z <= maximum.getZ(); z++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        IBlockState state = world.getBlockState(pos);
                        if (state.getBlock() == net.minecraft.init.Blocks.AIR || state.getBlock().isReplaceable(world, pos)) continue;
                        TileEntity tile = world.getTileEntity(pos);
                        if (tile != null) addTileData(tile.writeToNBT(new NBTTagCompound()));
                        blocks.put(pos.subtract(minimum), state);
                        world.setBlockToAir(pos);
                    }
                }
            }
            updateClusterSize();
            setPhysics(false);
        }

        /** Converts the same strict-radius sphere used by the upstream entity. */
        public void populateWithRadius(BlockPos center, int radius, BlockStateSelector selector) {
            blocks.clear();
            tileData.clear();
            startPos = center.toImmutable();
            int radiusSquared = radius * radius;
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (!isInsidePopulateRadius(x, y, z, radiusSquared)) continue;
                        BlockPos worldPos = center.add(x, y, z);
                        IBlockState state = world.getBlockState(worldPos);
                        if (!selector.test(world, worldPos, state)) continue;
                        TileEntity tile = world.getTileEntity(worldPos);
                        if (tile != null) addTileData(tile.writeToNBT(new NBTTagCompound()));
                        blocks.put(new BlockPos(x, y, z), state);
                        world.setBlockToAir(worldPos);
                    }
                }
            }
            updateClusterSize();
            setPosition(center.getX() + 0.5D,
                    center.getY() - clusterSizeY / 2.0D + 0.5D,
                    center.getZ() + 0.5D);
        }

        public void setPhysics(boolean physics) {
            this.physics = physics;
        }

        public float getClusterPitch(float partialTicks) {
            return previousClusterPitch + (clusterPitch - previousClusterPitch) * partialTicks;
        }

        public float getClusterYaw(float partialTicks) {
            return previousClusterYaw + (clusterYaw - previousClusterYaw) * partialTicks;
        }

        @Override
        public void onUpdate() {
            prevPosX = posX;
            prevPosY = posY;
            prevPosZ = posZ;
            previousShakeX = shakeX;
            previousShakeZ = shakeZ;
            if (shakeTime > 0) {
                float time = shakeTime;
                shakeX = MathHelper.sin(time * 4.5F) * 0.05F + (rand.nextFloat() - 0.5F) * 0.05F;
                shakeZ = MathHelper.cos(time * 3.5F) * 0.15F + (rand.nextFloat() - 0.5F) * 0.2F;
                if (--shakeTime == 0) setShakeTime(0);
            } else {
                shakeX = 0.0F;
                shakeZ = 0.0F;
            }

            previousClusterPitch = clusterPitch;
            previousClusterYaw = clusterYaw;
            if (shakeTime <= 0) {
                clusterPitch += pitchVelocity;
                clusterYaw += yawVelocity;
            }
            if (!hasNoGravity()) motionY -= 0.04D;
            move(MoverType.SELF, motionX, motionY, motionZ);
            noClip = !physics;
            fallTime++;
            if (world.isRemote) {
                updateFadeAmount();
                return;
            }
            if (blocks.isEmpty() || containsOnlyAir()) {
                setDead();
            } else if (onGround) {
                place();
            } else if (posY + clusterSizeY <= 0.0D || fallTime > maximumAge) {
                discardOrDrop();
            }
        }

        private boolean containsOnlyAir() {
            for (IBlockState state : blocks.values()) {
                if (state.getBlock() != net.minecraft.init.Blocks.AIR) return false;
            }
            return true;
        }

        private void discardOrDrop() {
            if (dropItems && world.getGameRules().getBoolean("doEntityDrops")) {
                for (Map.Entry<BlockPos, IBlockState> entry : blocks.entrySet()) {
                    Block block = entry.getValue().getBlock();
                    net.minecraft.item.Item item = net.minecraft.item.Item.getItemFromBlock(block);
                    if (item != net.minecraft.init.Items.AIR) {
                        BlockPos target = new BlockPos(this).add(entry.getKey());
                        world.spawnEntity(new EntityItem(world, target.getX(), target.getY(), target.getZ(),
                                new net.minecraft.item.ItemStack(item, 1, block.damageDropped(entry.getValue()))));
                    }
                }
            }
            setDead();
        }

        private void updateFadeAmount() {
            if (shakeTime > 0) return;
            previousFadeAmount = fadeAmount;
            if (fadePos == null) return;
            double maximumDistance = Math.sqrt(startPos.distanceSq(fadePos)) - fadeDistanceOffset;
            Vec3d fadeCenter = new Vec3d(fadePos).add(0.5D, 0.5D, 0.5D);
            double distance = Math.max(0.0D, fadeCenter.distanceTo(getPositionVector()) - fadeDistanceOffset);
            fadeAmount = Math.min(1.0F,
                    (float) distance / Math.min((float) maximumDistance, fadeStrength));
        }

        @Override
        protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
            // A cluster owns many block states and deliberately has no EntityFallingBlock fallTile.
            // Landing, bouncing, crumbling, and placement are handled by onUpdate()/crumble().
            fallDistance = 0.0F;
        }

        @Override
        public void fall(float distance, float damageMultiplier) {
            // Prevent EntityFallingBlock from dereferencing its unused fallTile.
        }

        private void repelOverlappingClusters() {
            for (BlockCluster other : world.getEntitiesWithinAABB(BlockCluster.class, getEntityBoundingBox().grow(0.25D))) {
                if (other == this || getEntityId() > other.getEntityId()) continue;
                double dx = posX - other.posX;
                double dz = posZ - other.posZ;
                double length = Math.max(0.01D, Math.sqrt(dx * dx + dz * dz));
                motionX += dx / length * 0.05D;
                motionZ += dz / length * 0.05D;
            }
        }

        private void crumble() {
            if (dropItems) {
                for (Map.Entry<BlockPos, IBlockState> entry : blocks.entrySet()) {
                    Block block = entry.getValue().getBlock();
                    net.minecraft.item.Item item = net.minecraft.item.Item.getItemFromBlock(block);
                    if (item != net.minecraft.init.Items.AIR) {
                        BlockPos offset = entry.getKey();
                        world.spawnEntity(new EntityItem(world, posX + offset.getX(), posY + offset.getY(), posZ + offset.getZ(),
                                new net.minecraft.item.ItemStack(item, 1, block.damageDropped(entry.getValue()))));
                    }
                }
            } else {
                for (Map.Entry<BlockPos, IBlockState> entry : blocks.entrySet()) {
                    BlockPos target = new BlockPos(this).add(entry.getKey());
                    if (world.isAirBlock(target) || world.getBlockState(target).getBlock().isReplaceable(world, target)) {
                        world.setBlockState(target, entry.getValue(), 3);
                    }
                }
            }
            setDead();
        }

        /** 在阶段结束时将簇中的方块完整放回世界。 */
        public void place() {
            if (world.isRemote) return;
            BlockPos base = new BlockPos(this);
            if (antiStacking) {
                BlockPos scan = base;
                IBlockState scanState = world.getBlockState(scan);
                for (int i = 0; i < 50 && scanState.getBlock() == net.minecraft.init.Blocks.AIR; i++) {
                    scan = scan.down();
                    scanState = world.getBlockState(scan);
                }
                base = new BlockPos(base.getX(), scan.getY(), base.getZ());
            }
            int verticalCenter = MathHelper.floor(
                    (getEntityBoundingBox().maxY - getEntityBoundingBox().minY) / 2.0D - 0.5D);
            for (Map.Entry<BlockPos, IBlockState> entry : blocks.entrySet()) {
                BlockPos offset = entry.getKey();
                BlockPos target = base.add(offset.getX(), offset.getY() - sink, offset.getZ()).up(verticalCenter);
                IBlockState existing = world.getBlockState(target);
                Block existingBlock = existing.getBlock();
                boolean protectedBlock = existingBlock == net.minecraft.init.Blocks.BEDROCK
                        || existingBlock == net.minecraft.init.Blocks.BARRIER
                        || existingBlock == net.minecraft.init.Blocks.END_PORTAL
                        || existingBlock == net.minecraft.init.Blocks.END_PORTAL_FRAME;
                if (world.getTileEntity(target) == null && !protectedBlock
                        && world.setBlockState(target, entry.getValue(), 3)) {
                    NBTTagCompound tile = getTileDataFromOffset(offset);
                    TileEntity placedTile = world.getTileEntity(target);
                    if (tile != null && placedTile != null) {
                        tile.setInteger("x", target.getX());
                        tile.setInteger("y", target.getY());
                        tile.setInteger("z", target.getZ());
                        placedTile.readFromNBT(tile);
                        placedTile.markDirty();
                    }
                    world.notifyNeighborsOfStateChange(target, entry.getValue().getBlock(), false);
                } else if (dropItems) {
                    Block block = entry.getValue().getBlock();
                    net.minecraft.item.Item item = net.minecraft.item.Item.getItemFromBlock(block);
                    if (item != net.minecraft.init.Items.AIR) {
                        world.spawnEntity(new EntityItem(world, target.getX(), target.getY(), target.getZ(),
                                new net.minecraft.item.ItemStack(item, 1, block.damageDropped(entry.getValue()))));
                    }
                }
            }
            blocks.clear();
            setDead();
        }

        public void setShakeTime(int value) { shakeTime = Math.max(0, value); }
        public int getShakeTime() { return shakeTime; }
        public void setSink(int value) { sink = Math.max(-1, value); }
        public int getSink() { return sink; }
        public void setAntiStacking(boolean value) { antiStacking = value; }
        public boolean isAntiStacking() { return antiStacking; }
        public void setShouldCrumble(boolean value) { shouldCrumble = value; }
        public boolean shouldCrumble() { return shouldCrumble; }
        public void setDropItems(boolean value) { dropItems = value; }
        public void setForceRender(boolean value) { forceRender = value; }
        public boolean forceRender() { return forceRender; }
        public void setFadePos(BlockPos value) { fadePos = value; }
        public BlockPos getFadePos() { return fadePos; }
        public void setFadeStrength(float value) { fadeStrength = value; }
        public float getFadeStrength() { return fadeStrength; }
        public void setFadeDistanceOffset(int value) { fadeDistanceOffset = value; }
        public int getFadeDistanceOffset() { return fadeDistanceOffset; }
        public void setCreatedFromTractorBeam(boolean value) { createdFromBeam = value; }
        public boolean createdFromTractorBeam() { return createdFromBeam; }
        public void setCreatedFromFallingBlock(boolean value) { createdFromFallingBlock = value; }
        public boolean createdFromFallingBlock() { return createdFromFallingBlock; }
        public void setHeadCreatedFrom(int value) { headCreatedFrom = value; }
        public int getHeadCreatedFrom() { return headCreatedFrom; }
        public void setMaximumAge(int value) { maximumAge = Math.max(1, value); }
        public int getMaximumAge() { return maximumAge; }
        public void setTime(int value) { fallTime = Math.max(0, value); }
        public int getTime() { return fallTime; }
        public void setTractorBeamDistanceThreshold(double value) { tractorBeamDistanceThreshold = value; }
        public double getTractorBeamDistanceThreshold() { return tractorBeamDistanceThreshold; }
        public List<NBTTagCompound> getTileData() { return tileData; }
        public void addTileData(NBTTagCompound value) { if (value != null) tileData.add(value.copy()); }
        public BlockPos getStartPos() { return startPos; }
        public void setRotationDelta(float pitch, float yaw) { pitchVelocity = pitch; yawVelocity = yaw; }
        public float getClusterSizeX() { return clusterSizeX; }
        public float getClusterSizeY() { return clusterSizeY; }
        public float getClusterSizeZ() { return clusterSizeZ; }
        public float getShakeX(float partialTicks) { return previousShakeX + (shakeX - previousShakeX) * partialTicks; }
        public float getShakeZ(float partialTicks) { return previousShakeZ + (shakeZ - previousShakeZ) * partialTicks; }
        public float getFadeAmount(float partialTicks) {
            return previousFadeAmount + (fadeAmount - previousFadeAmount) * partialTicks;
        }

        static boolean isInsidePopulateRadius(int x, int y, int z, int radiusSquared) {
            return x * x + y * y + z * z < radiusSquared;
        }

        public NBTTagCompound getTileDataFromOffset(BlockPos offset) {
            BlockPos expected = startPos.add(offset);
            for (NBTTagCompound data : tileData) {
                if (data.getInteger("x") == expected.getX() && data.getInteger("y") == expected.getY()
                        && data.getInteger("z") == expected.getZ()) return data.copy();
            }
            return null;
        }

        public interface BlockStateSelector {
            boolean test(World world, BlockPos pos, IBlockState state);
        }

        public BlockCluster splitAt(EnumFacing.Axis axis) {
            if (blocks.size() < 2 || world.isRemote) return null;
            int minimum = Integer.MAX_VALUE;
            int maximum = Integer.MIN_VALUE;
            for (BlockPos offset : blocks.keySet()) {
                int coordinate = axis == EnumFacing.Axis.X ? offset.getX() : axis == EnumFacing.Axis.Y ? offset.getY() : offset.getZ();
                minimum = Math.min(minimum, coordinate);
                maximum = Math.max(maximum, coordinate);
            }
            int splitCoordinate = minimum + (maximum - minimum) / 2;
            Map<BlockPos, IBlockState> separated = new LinkedHashMap<BlockPos, IBlockState>();
            java.util.Iterator<Map.Entry<BlockPos, IBlockState>> iterator = blocks.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<BlockPos, IBlockState> entry = iterator.next();
                BlockPos offset = entry.getKey();
                int coordinate = axis == EnumFacing.Axis.X ? offset.getX() : axis == EnumFacing.Axis.Y ? offset.getY() : offset.getZ();
                if (coordinate > splitCoordinate) {
                    separated.put(offset, entry.getValue());
                    iterator.remove();
                }
            }
            if (separated.isEmpty() || blocks.isEmpty()) return null;
            updateClusterSize();
            BlockCluster split = new BlockCluster(world, posX, posY, posZ, separated);
            split.motionX = motionX;
            split.motionY = motionY;
            split.motionZ = motionZ;
            split.clusterPitch = split.previousClusterPitch = clusterPitch;
            split.clusterYaw = split.previousClusterYaw = clusterYaw;
            split.pitchVelocity = -pitchVelocity;
            split.yawVelocity = -yawVelocity;
            split.physics = physics;
            split.shakeTime = shakeTime;
            split.sink = sink;
            split.antiStacking = antiStacking;
            split.shouldCrumble = shouldCrumble;
            split.dropItems = dropItems;
            split.createdFromBeam = createdFromBeam;
            split.createdFromFallingBlock = createdFromFallingBlock;
            split.headCreatedFrom = headCreatedFrom;
            split.fadePos = fadePos;
            split.fadeStrength = fadeStrength;
            split.fadeDistanceOffset = fadeDistanceOffset;
            split.tractorBeamDistanceThreshold = tractorBeamDistanceThreshold;
            world.spawnEntity(split);
            return split;
        }

        private void updateClusterSize() {
            if (blocks.isEmpty()) {
                setClusterSize(1.0F, 1.0F, 1.0F);
                return;
            }
            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
            for (BlockPos pos : blocks.keySet()) {
                minX = Math.min(minX, pos.getX());
                minY = Math.min(minY, pos.getY());
                minZ = Math.min(minZ, pos.getZ());
                maxX = Math.max(maxX, pos.getX());
                maxY = Math.max(maxY, pos.getY());
                maxZ = Math.max(maxZ, pos.getZ());
            }
            setClusterSize(Math.max(1.0F, maxX - minX + 1.0F),
                    Math.max(1.0F, maxY - minY + 1.0F),
                    Math.max(1.0F, maxZ - minZ + 1.0F));
        }

        private void setClusterSize(float sizeX, float sizeY, float sizeZ) {
            clusterSizeX = sizeX;
            clusterSizeY = sizeY;
            clusterSizeZ = sizeZ;
            super.setSize(Math.max(sizeX, sizeZ), sizeY);
            refreshClusterBoundingBox();
        }

        private void refreshClusterBoundingBox() {
            setEntityBoundingBox(new AxisAlignedBB(posX - clusterSizeX / 2.0D, posY,
                    posZ - clusterSizeZ / 2.0D, posX + clusterSizeX / 2.0D,
                    posY + clusterSizeY, posZ + clusterSizeZ / 2.0D));
        }

        @Override
        public void setPosition(double x, double y, double z) {
            super.setPosition(x, y, z);
            if (clusterSizeX > 0.0F && clusterSizeY > 0.0F && clusterSizeZ > 0.0F) {
                refreshClusterBoundingBox();
            }
        }

        @Override
        protected void writeEntityToNBT(NBTTagCompound compound) {
            compound.setTag("ClusterBlocks", writeBlocks());
            compound.setFloat("ClusterPitch", clusterPitch);
            compound.setFloat("ClusterYaw", clusterYaw);
            compound.setFloat("PitchVelocity", pitchVelocity);
            compound.setFloat("YawVelocity", yawVelocity);
            compound.setBoolean("Physics", physics);
            compound.setInteger("ClusterTime", fallTime);
            compound.setInteger("ShakeTime", shakeTime);
            compound.setInteger("Sink", sink);
            compound.setBoolean("AntiStacking", antiStacking);
            compound.setBoolean("ShouldCrumble", shouldCrumble);
            compound.setBoolean("DropItems", dropItems);
            compound.setBoolean("ForceRender", forceRender);
            compound.setBoolean("CreatedFromBeam", createdFromBeam);
            compound.setBoolean("CreatedFromFallingBlock", createdFromFallingBlock);
            compound.setInteger("HeadCreatedFrom", headCreatedFrom);
            compound.setInteger("MaximumAge", maximumAge);
            if (fadePos != null) compound.setLong("FadePos", fadePos.toLong());
            compound.setFloat("FadeStrength", fadeStrength);
            compound.setInteger("FadeDistanceOffset", fadeDistanceOffset);
            compound.setDouble("TractorBeamDistanceThreshold", tractorBeamDistanceThreshold);
            compound.setLong("ClusterStartPos", startPos.toLong());
            NBTTagList tiles = new NBTTagList();
            for (NBTTagCompound data : tileData) tiles.appendTag(data.copy());
            compound.setTag("TileData", tiles);
        }

        @Override
        protected void readEntityFromNBT(NBTTagCompound compound) {
            readBlocks(compound.getTagList("ClusterBlocks", 10));
            clusterPitch = previousClusterPitch = compound.getFloat("ClusterPitch");
            clusterYaw = previousClusterYaw = compound.getFloat("ClusterYaw");
            pitchVelocity = compound.getFloat("PitchVelocity");
            yawVelocity = compound.getFloat("YawVelocity");
            physics = !compound.hasKey("Physics") || compound.getBoolean("Physics");
            fallTime = compound.getInteger("ClusterTime");
            shakeTime = compound.getInteger("ShakeTime");
            sink = compound.getInteger("Sink");
            antiStacking = compound.getBoolean("AntiStacking");
            shouldCrumble = compound.getBoolean("ShouldCrumble");
            dropItems = compound.getBoolean("DropItems");
            forceRender = compound.getBoolean("ForceRender");
            createdFromBeam = compound.getBoolean("CreatedFromBeam");
            createdFromFallingBlock = compound.getBoolean("CreatedFromFallingBlock");
            headCreatedFrom = compound.getInteger("HeadCreatedFrom");
            maximumAge = compound.hasKey("MaximumAge")
                    ? Math.max(1, compound.getInteger("MaximumAge")) : 600;
            fadePos = compound.hasKey("FadePos") ? BlockPos.fromLong(compound.getLong("FadePos")) : null;
            fadeStrength = compound.getFloat("FadeStrength");
            fadeDistanceOffset = compound.getInteger("FadeDistanceOffset");
            if (compound.hasKey("TractorBeamDistanceThreshold")) tractorBeamDistanceThreshold = compound.getDouble("TractorBeamDistanceThreshold");
            if (compound.hasKey("ClusterStartPos")) startPos = BlockPos.fromLong(compound.getLong("ClusterStartPos"));
            tileData.clear();
            NBTTagList tiles = compound.getTagList("TileData", 10);
            for (int i = 0; i < tiles.tagCount(); i++) tileData.add(tiles.getCompoundTagAt(i));
        }

        private NBTTagList writeBlocks() {
            NBTTagList list = new NBTTagList();
            for (Map.Entry<BlockPos, IBlockState> entry : blocks.entrySet()) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setInteger("X", entry.getKey().getX());
                tag.setInteger("Y", entry.getKey().getY());
                tag.setInteger("Z", entry.getKey().getZ());
                tag.setInteger("State", Block.getStateId(entry.getValue()));
                list.appendTag(tag);
            }
            return list;
        }

        private void readBlocks(NBTTagList list) {
            blocks.clear();
            for (int index = 0; index < list.tagCount(); index++) {
                NBTTagCompound tag = list.getCompoundTagAt(index);
                IBlockState state = Block.getStateById(tag.getInteger("State"));
                if (state != null) blocks.put(new BlockPos(tag.getInteger("X"), tag.getInteger("Y"), tag.getInteger("Z")), state);
            }
            updateClusterSize();
        }

        private static BlockPos calculateStartPos(Iterable<BlockPos> positions) {
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

        @Override
        public void writeSpawnData(ByteBuf buffer) {
            buffer.writeInt(blocks.size());
            for (Map.Entry<BlockPos, IBlockState> entry : blocks.entrySet()) {
                buffer.writeInt(entry.getKey().getX());
                buffer.writeInt(entry.getKey().getY());
                buffer.writeInt(entry.getKey().getZ());
                buffer.writeInt(Block.getStateId(entry.getValue()));
            }
            buffer.writeFloat(clusterPitch);
            buffer.writeFloat(clusterYaw);
            buffer.writeFloat(pitchVelocity);
            buffer.writeFloat(yawVelocity);
            buffer.writeBoolean(physics);
            buffer.writeInt(shakeTime);
            buffer.writeInt(sink);
            buffer.writeBoolean(antiStacking);
            buffer.writeBoolean(shouldCrumble);
            buffer.writeBoolean(forceRender);
            buffer.writeBoolean(fadePos != null);
            if (fadePos != null) buffer.writeLong(fadePos.toLong());
            buffer.writeFloat(fadeStrength);
            buffer.writeInt(fadeDistanceOffset);
        }

        @Override
        public void readSpawnData(ByteBuf buffer) {
            blocks.clear();
            int size = buffer.readInt();
            for (int index = 0; index < size; index++) {
                BlockPos offset = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
                IBlockState state = Block.getStateById(buffer.readInt());
                if (state != null) blocks.put(offset, state);
            }
            clusterPitch = previousClusterPitch = buffer.readFloat();
            clusterYaw = previousClusterYaw = buffer.readFloat();
            pitchVelocity = buffer.readFloat();
            yawVelocity = buffer.readFloat();
            physics = buffer.readBoolean();
            shakeTime = buffer.readInt();
            sink = buffer.readInt();
            antiStacking = buffer.readBoolean();
            shouldCrumble = buffer.readBoolean();
            forceRender = buffer.readBoolean();
            fadePos = buffer.readBoolean() ? BlockPos.fromLong(buffer.readLong()) : null;
            fadeStrength = buffer.readFloat();
            fadeDistanceOffset = buffer.readInt();
            updateClusterSize();
        }

    }

    public abstract static class StormPartBase extends EntitySickenedMob {
        private static final DataParameter<Integer> OWNER_ID = EntityDataManager.createKey(StormPartBase.class, DataSerializers.VARINT);
        private static final DataParameter<Integer> PART_INDEX = EntityDataManager.createKey(StormPartBase.class, DataSerializers.VARINT);
        private static final DataParameter<Integer> STORM_PHASE = EntityDataManager.createKey(StormPartBase.class, DataSerializers.VARINT);
        private UUID ownerUuid;
        private int orphanTicks;
        private boolean independentBowelsPart;

        protected StormPartBase(World world) {
            super(world);
            noClip = true;
            setNoAI(true);
            enablePersistence();
        }

        @Override
        protected void entityInit() {
            super.entityInit();
            dataManager.register(OWNER_ID, -1);
            dataManager.register(PART_INDEX, 0);
            dataManager.register(STORM_PHASE, 0);
        }

        public void bindTo(EntityWitherStormLegacy owner, int index) {
            ownerUuid = owner.getUniqueID();
            dataManager.set(OWNER_ID, owner.getEntityId());
            dataManager.set(PART_INDEX, index);
            dataManager.set(STORM_PHASE, owner.getPhase());
        }

        public void setIndependentBowelsPart() {
            independentBowelsPart = true;
            ownerUuid = null;
            dataManager.set(OWNER_ID, -1);
        }

        public boolean isIndependentBowelsPart() {
            return independentBowelsPart;
        }

        public int getPartIndex() {
            return dataManager.get(PART_INDEX);
        }

        protected UUID getOwnerUuid() {
            return ownerUuid;
        }

        protected void setOwnerUuid(UUID uuid) {
            ownerUuid = uuid;
            if (uuid == null) dataManager.set(OWNER_ID, -1);
        }

        protected EntityWitherStormLegacy getOwnerStorm() {
            Entity entity = world.getEntityByID(dataManager.get(OWNER_ID));
            if (entity instanceof EntityWitherStormLegacy) return (EntityWitherStormLegacy) entity;
            if (ownerUuid == null) return null;
            List<EntityWitherStormLegacy> storms = world.getEntities(EntityWitherStormLegacy.class,
                    storm -> ownerUuid.equals(storm.getUniqueID()));
            if (storms.isEmpty()) return null;
            EntityWitherStormLegacy owner = storms.get(0);
            dataManager.set(OWNER_ID, owner.getEntityId());
            return owner;
        }

        @Override
        public void onLivingUpdate() {
            if (independentBowelsPart) {
                super.onLivingUpdate();
                motionX = motionY = motionZ = 0.0D;
                return;
            }
            EntityWitherStormLegacy owner = getOwnerStorm();
            if (owner == null || owner.isDead) {
                if (!world.isRemote && ++orphanTicks > 200) setDead();
                return;
            }
            orphanTicks = 0;
            double[] offset = getOffset(owner, dataManager.get(PART_INDEX));
            updateAttachedPosition(owner, owner.posX + offset[0], owner.posY + offset[1], owner.posZ + offset[2]);
            rotationYaw = owner.rotationYaw;
            rotationYawHead = owner.rotationYawHead;
            motionX = motionY = motionZ = 0.0D;
            dataManager.set(STORM_PHASE, owner.getPhase());
        }

        protected void updateAttachedPosition(EntityWitherStormLegacy owner, double x, double y, double z) {
            setPosition(x, y, z);
        }

        protected abstract double[] getOffset(EntityWitherStormLegacy owner, int index);

        @Override
        public boolean attackEntityFrom(DamageSource source, float amount) {
            if (independentBowelsPart) return attackPartDirectly(source, amount);
            EntityWitherStormLegacy owner = getOwnerStorm();
            return owner != null && owner.attackEntityFrom(source, amount * getDamageTransfer());
        }

        protected boolean attackPartDirectly(DamageSource source, float amount) {
            return super.attackEntityFrom(source, amount);
        }

        protected float getDamageTransfer() { return 1.0F; }

        @Override
        public void writeEntityToNBT(NBTTagCompound compound) {
            super.writeEntityToNBT(compound);
            if (ownerUuid != null) compound.setUniqueId("WitherStormOwner", ownerUuid);
            compound.setInteger("WitherStormPartIndex", dataManager.get(PART_INDEX));
            compound.setInteger("WitherStormPartPhase", dataManager.get(STORM_PHASE));
            compound.setBoolean("IndependentBowelsPart", independentBowelsPart);
        }

        @Override
        public void readEntityFromNBT(NBTTagCompound compound) {
            super.readEntityFromNBT(compound);
            ownerUuid = compound.hasUniqueId("WitherStormOwner") ? compound.getUniqueId("WitherStormOwner") : null;
            dataManager.set(PART_INDEX, compound.getInteger("WitherStormPartIndex"));
            dataManager.set(STORM_PHASE, compound.getInteger("WitherStormPartPhase"));
            independentBowelsPart = compound.getBoolean("IndependentBowelsPart");
        }

        @Override protected void despawnEntity() { }

        @Override
        public void setDead() {
            if (!world.isRemote && this instanceof WitherStormSegment && !independentBowelsPart) {
                LegacyChunkLoadingManager.INSTANCE.releaseEntity(world, "segment", getUniqueID());
            }
            super.setDead();
        }
    }

    public static class CommandBlockCore extends StormPartBase {
        private UUID podiumClusterUuid;
        private BlockCluster podiumCluster;

        public CommandBlockCore(World world) { super(world); setSize(2.0F, 2.0F); experienceValue = 500; }
        @Override protected double getSickenedHealth() { return 500.0D; }
        @Override protected double getSickenedDamage() { return 18.0D; }
        @Override protected double getSickenedSpeed() { return 0.0D; }
        @Override public String getSickenedType() { return "command_block"; }
        @Override protected double[] getOffset(EntityWitherStormLegacy owner, int index) {
            BlockPos podium = owner.getPlayingDeadPodiumPosition();
            if (podium != null) {
                return new double[]{podium.getX() + 0.5D - owner.posX,
                        podium.getY() + 11.0D - owner.posY,
                        podium.getZ() + 0.5D - owner.posZ};
            }
            return new double[]{0.0D, 1.0D, 0.0D};
        }

        @Override
        public boolean attackEntityFrom(DamageSource source, float amount) {
            if (isIndependentBowelsPart()) return BowelsBossfightController.attack(this, source);
            boolean damaged = attackPartDirectly(source, amount);
            if (damaged && getHealth() <= 0.0F) {
                EntityWitherStormLegacy owner = getOwnerStorm();
                if (owner != null) owner.reviveFromPlayingDead();
            }
            return damaged;
        }

        @Override
        public void onLivingUpdate() {
            super.onLivingUpdate();
            if (isIndependentBowelsPart()) BowelsBossfightController.tick(this);
        }

        public void createPodiumCluster() {
            findPodiumCluster();
            if (world.isRemote || podiumCluster != null || podiumClusterUuid != null) return;
            BlockPos center = getPosition();
            BlockCluster cluster = new BlockCluster(world);
            cluster.populate(center.add(-5, -13, -5), center.add(5, 6, 5));
            if (cluster.getBlocks().isEmpty()) return;
            cluster.setForceRender(true);
            cluster.setAntiStacking(true);
            if (world.spawnEntity(cluster)) {
                podiumCluster = cluster;
                podiumClusterUuid = cluster.getUniqueID();
            }
        }

        public void movePodiumCluster(double x, double y, double z) {
            findPodiumCluster();
            if (podiumCluster != null && !podiumCluster.isDead) {
                podiumCluster.setPosition(podiumCluster.posX + x, podiumCluster.posY + y, podiumCluster.posZ + z);
            }
        }

        public void finishPodiumMove() {
            findPodiumCluster();
            if (podiumCluster != null && !podiumCluster.isDead) podiumCluster.place();
            podiumCluster = null;
            podiumClusterUuid = null;
        }

        public void findPodiumCluster() {
            if (podiumCluster != null && !podiumCluster.isDead) return;
            podiumCluster = null;
            if (podiumClusterUuid == null || !(world instanceof WorldServer)) return;
            Entity entity = ((WorldServer) world).getEntityFromUuid(podiumClusterUuid);
            if (entity instanceof BlockCluster && !entity.isDead) podiumCluster = (BlockCluster) entity;
        }

        public BlockCluster getPodiumCluster() {
            findPodiumCluster();
            return podiumCluster;
        }

        @Override
        public void writeEntityToNBT(NBTTagCompound compound) {
            super.writeEntityToNBT(compound);
            if (podiumClusterUuid != null) compound.setUniqueId("PodiumCluster", podiumClusterUuid);
        }

        @Override
        public void readEntityFromNBT(NBTTagCompound compound) {
            super.readEntityFromNBT(compound);
            podiumClusterUuid = compound.hasUniqueId("PodiumCluster") ? compound.getUniqueId("PodiumCluster") : null;
        }
    }

    public static class WitherStormHead extends StormPartBase {
        private static final DataParameter<Boolean> ACTIVE = EntityDataManager.createKey(WitherStormHead.class, DataSerializers.BOOLEAN);
        private static final DataParameter<Boolean> ROARING = EntityDataManager.createKey(WitherStormHead.class, DataSerializers.BOOLEAN);
        private static final DataParameter<Boolean> BITING = EntityDataManager.createKey(WitherStormHead.class, DataSerializers.BOOLEAN);
        private static final DataParameter<Boolean> HURT = EntityDataManager.createKey(WitherStormHead.class, DataSerializers.BOOLEAN);

        private Vec3d distractedPos;
        private int distractedTime;
        private int nextRoar;
        private int roarTime;
        private int shootTime = 100;
        private int biteTime;
        private float mouthAnimation;
        private float previousMouthAnimation;
        private float fadeAnimation;
        private float previousFadeAnimation;
        private boolean shaking;
        private float shakeAnimation;
        private float previousShakeAnimation;
        private int specialDeathTime;

        public WitherStormHead(World world) { super(world); setSize(5.0F, 5.0F); }

        @Override
        protected void entityInit() {
            super.entityInit();
            dataManager.register(ACTIVE, true);
            dataManager.register(ROARING, false);
            dataManager.register(BITING, false);
            dataManager.register(HURT, false);
        }
        @Override protected double getSickenedHealth() { return 60.0D; }
        @Override protected double getSickenedSpeed() { return 0.0D; }
        @Override protected double getSickenedDamage() { return 3.5D; }
        @Override protected double getSickenedFollowRange() { return 40.0D; }
        @Override protected double getSickenedArmor() { return 8.0D; }
        @Override protected double getSickenedKnockbackResistance() { return 1.0D; }
        @Override public String getSickenedType() { return "wither_storm_head"; }
        @Override protected double[] getOffset(EntityWitherStormLegacy owner, int index) {
            double side = index == 0 ? 0.0D : (index == 1 ? -1.0D : 1.0D) * owner.width * 0.42D;
            return new double[]{side, owner.height * 0.72D, -owner.width * 0.18D};
        }

        @Override
        protected void initEntityAI() {
            tasks.addTask(0, new DoNothingGoal(this));
            tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 12.0F));
            tasks.addTask(4, new EntityAILookIdle(this));
            targetTasks.addTask(0, new EntityAIHurtByTarget(this, true));
            targetTasks.addTask(1, new DistractionTargetGoal(this));
            targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityLivingBase>(this, EntityLivingBase.class,
                    100, true, false, target -> canAttackTarget(target) && !isATarget(target)));
        }

        @Override
        public void setIndependentBowelsPart() {
            super.setIndependentBowelsPart();
            setNoAI(false);
            setNoGravity(true);
        }

        @Override
        public boolean attackEntityFrom(DamageSource source, float amount) {
            EntityWitherStormLegacy owner = getOwnerStorm();
            if (owner != null && !isIndependentBowelsPart()) {
                owner.attackHead(getPartIndex(), source.getTrueSource());
                return true;
            }
            if (!isIndependentBowelsPart() || !isActive() || isHurt()) {
                return source == DamageSource.OUT_OF_WORLD && super.attackEntityFrom(source, amount);
            }
            if (source != DamageSource.OUT_OF_WORLD && !isRoaring()) {
                setRoar(true);
                setRoarTime(20);
            }
            boolean damaged = attackPartDirectly(source, amount);
            if (damaged && getHealth() < getMaxHealth() / 1.5F) setHurt(true);
            return damaged;
        }

        @Override
        public void onLivingUpdate() {
            super.onLivingUpdate();
            setNoGravity(true);
            previousMouthAnimation = mouthAnimation;
            mouthAnimation = LegacyWitherStormPartLogic.advanceMouth(mouthAnimation, isRoaring(), isBiting());
            previousFadeAnimation = fadeAnimation;
            fadeAnimation = LegacyWitherStormPartLogic.advanceFade(fadeAnimation, isPlayingDead(), rand);
            previousShakeAnimation = shakeAnimation;
            if (shaking) {
                shakeAnimation = LegacyWitherStormPartLogic.advanceShake(shakeAnimation, true, rand);
                if (shakeAnimation >= 2.0F) {
                    shakeAnimation = previousShakeAnimation = 0.0F;
                    shaking = false;
                }
            }

            if (!isIndependentBowelsPart() || world.isRemote) return;
            if (nextRoar <= 0) nextRoar = LegacyWitherStormPartLogic.initialRoarDelay(rand);
            if (distractedTime > 0 && --distractedTime == 0) distractedPos = null;
            if (!isDeadOrPlayingDead()) {
                if (--nextRoar == 0) {
                    setRoar(false);
                    setRoarTime(40);
                    nextRoar = LegacyWitherStormPartLogic.nextRoarDelay(rand);
                }
                if (roarTime > 0 && --roarTime == 0) disableRoar();
                if (biteTime > 0 && --biteTime == 0) {
                    setBiting(false);
                    playSound(ModSounds.get("wither_storm_bite"), getSoundVolume(), 1.0F);
                }
                if (isHurt() && ticksExisted % 20 == 0 && shootTime > 60) shaking = true;
                if (isHurt() && shootTime > 0) {
                    --shootTime;
                    if (shootTime < 60 && getAttackTarget() != null) {
                        EntityLivingBase target = getAttackTarget();
                        getLookHelper().setLookPosition(target.posX, target.posY + target.getEyeHeight(), target.posZ,
                                10.0F, 10.0F);
                        shaking = false;
                    }
                    if (shootTime == 0) {
                        shootSkullAtTarget();
                        shootTime = LegacyWitherStormPartLogic.nextShotDelay(rand);
                        shaking = false;
                    }
                }
            } else {
                setAttackTarget(null);
                getNavigator().clearPath();
                motionX = motionY = motionZ = 0.0D;
            }
        }

        public boolean isActive() { return dataManager.get(ACTIVE); }
        public void setActive(boolean active) {
            dataManager.set(ACTIVE, active);
            if (!active) {
                setAttackTarget(null);
                getNavigator().clearPath();
            }
        }
        public boolean isRoaring() { return dataManager.get(ROARING); }
        /** Starts a roar; screaming=true selects the hurt roar variant. */
        public void setRoar(boolean screaming) {
            dataManager.set(ROARING, true);
            playSound(ModSounds.get(screaming ? "wither_storm_hurt" : "wither_storm_roar"), getSoundVolume(), 1.0F);
        }
        public int getRoarTime() { return roarTime; }
        public void setRoarTime(int ticks) {
            roarTime = Math.max(0, ticks);
        }
        public void disableRoar() { dataManager.set(ROARING, false); }
        public boolean isBiting() { return dataManager.get(BITING); }
        public void setBiting(boolean biting) {
            if (biting) biteTime = 10;
            dataManager.set(BITING, biting);
        }
        public boolean isPlayingDead() { return !isActive(); }
        public boolean isDeadOrPlayingDead() { return isDead || getHealth() <= 0.0F || isPlayingDead(); }
        public boolean isHurt() { return dataManager.get(HURT); }
        public void setHurt(boolean hurt) { dataManager.set(HURT, hurt); }

        @Override
        protected void updateAITasks() {
            super.updateAITasks();
            if (!isIndependentBowelsPart() || !isActive() || isHurt()) return;
            EntityLivingBase target = getAttackTarget();
            if (target == null || !canAttackTarget(target)) return;
            Vec3d delta = new Vec3d(posX - target.posX, posY + getEyeHeight() * 0.5D - target.posY,
                    posZ - target.posZ);
            double length = Math.sqrt(delta.x * delta.x + delta.y * delta.y + delta.z * delta.z);
            if (length > 0.001D) {
                delta = delta.scale(0.2D / length);
                Entity vehicle = target.getRidingEntity();
                if (vehicle instanceof EntityLivingBase && canAttackTarget((EntityLivingBase) vehicle)) {
                    vehicle.motionX += delta.x;
                    vehicle.motionY += delta.y;
                    vehicle.motionZ += delta.z;
                    vehicle.velocityChanged = true;
                } else {
                    target.motionX += delta.x;
                    target.motionY += delta.y;
                    target.motionZ += delta.z;
                    target.velocityChanged = true;
                }
            }
            if (getEntityBoundingBox().intersects(target.getEntityBoundingBox())) {
                if (target instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) target;
                    if (!player.capabilities.disableDamage && player.isEntityAlive()) {
                        player.attackEntityFrom(DamageSource.causeMobDamage(this), 3.5F);
                    }
                } else if (target.isEntityAlive()) {
                    target.attackEntityFrom(DamageSource.causeMobDamage(this), Float.MAX_VALUE);
                }
                setBiting(true);
            }
            if (ticksExisted % 80 == 0) heal(10.0F);
        }

        private void shootSkullAtTarget() {
            EntityLivingBase target = getAttackTarget();
            if (target == null || !canAttackTarget(target)) return;
            Vec3d direction = new Vec3d(target.posX - posX,
                    target.posY + target.getEyeHeight() * 0.5D - (posY + getEyeHeight() * 0.5D),
                    target.posZ - posZ).normalize();
            EntityWitherSkull skull = new EntityWitherSkull(world, this, direction.x, direction.y, direction.z);
            skull.setPosition(posX, posY + getEyeHeight() * 0.5D, posZ);
            if (rand.nextInt(16) == 1) skull.setInvulnerable(true);
            world.spawnEntity(skull);
            playSound(ModSounds.get("wither_storm_shoot"), getSoundVolume(), 1.0F);
        }

        private boolean canAttackTarget(EntityLivingBase target) {
            return target != this && target.isEntityAlive() && !(target instanceof EntitySickenedMob)
                    && !(target instanceof EntityWitherStormLegacy) && !(target instanceof StormPartBase)
                    && !(target instanceof net.minecraft.entity.boss.EntityWither)
                    && !(target instanceof net.minecraft.entity.monster.EntityWitherSkeleton)
                    && !(target instanceof net.minecraft.entity.monster.EntityCreeper)
                    && !(target instanceof net.minecraft.entity.monster.EntityEnderman)
                    && (!(target instanceof EntityPlayer) || !((EntityPlayer) target).capabilities.disableDamage);
        }

        private boolean isATarget(EntityLivingBase target) {
            double range = getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getAttributeValue();
            for (WitherStormHead head : world.getEntitiesWithinAABB(WitherStormHead.class,
                    getEntityBoundingBox().grow(range))) {
                if (head != this && !head.isHurt() && head.getAttackTarget() == target) return true;
            }
            return false;
        }

        public void setDistractedPos(int head, Vec3d pos) { distractedPos = pos; }
        public Vec3d getDistractedPos(int head) { return distractedPos; }
        public void makeDistracted(Vec3d pos, int time, int head) {
            distractedPos = pos;
            distractedTime = Math.max(0, time);
            if (pos != null) getLookHelper().setLookPosition(pos.x, pos.y, pos.z, 10.0F, 10.0F);
        }

        public float getMouthAnimation(float partialTicks) {
            return previousMouthAnimation + (mouthAnimation - previousMouthAnimation) * partialTicks;
        }

        public float getMouthAnimation(int head, float partialTicks) { return getMouthAnimation(partialTicks); }
        public float getBrokenJawAnimation(int head, float partialTicks) { return 0.0F; }

        public float getFadeAnimation(float partialTicks) {
            return previousFadeAnimation + (fadeAnimation - previousFadeAnimation) * partialTicks;
        }

        public float getFadeAnimation() { return fadeAnimation; }

        public float getHeadShakeAnimation(float partialTicks) {
            return LegacyWitherStormPartLogic.shakeRoll(previousShakeAnimation, shakeAnimation, partialTicks);
        }

        public float getHeadShakeAnim(int head, float partialTicks) { return getHeadShakeAnimation(partialTicks); }
        public float getTentacleAnimation(float partialTicks) { return 0.0F; }
        public float getHeadYRot(int head) { return rotationYawHead; }
        public float getHeadYRotO(int head) { return prevRotationYawHead; }
        public float getHeadXRot(int head) { return rotationPitch; }
        public float getHeadXRotO(int head) { return prevRotationPitch; }
        public float getXBodyRot() { return 0.0F; }
        public float getXBodyRotO() { return 0.0F; }
        public boolean isPosBehindBack(Vec3d pos) { return false; }
        public boolean areOtherHeadsDisabled() { return false; }
        public boolean isHeadInjured(int head) { return isHurt(); }

        public boolean tractorBeamActive(int head) { return isActive() && !isPlayingDead() && !isHurt(); }
        public boolean canSee(int head, Entity entity) { return canEntityBeSeen(entity); }
        public int getTotalHeads() { return 1; }
        public EntityLivingBase getTarget(int head) { return getAttackTarget(); }
        public void setTarget(int head, EntityLivingBase target) { setAttackTarget(target); }
        public Vec3d getHeadPos(int head) { return getPositionEyes(1.0F); }
        public void setLookAt(int head, Vec3d pos, int steps) {
            if (pos != null) getLookHelper().setLookPosition(pos.x, pos.y, pos.z, 10.0F, 10.0F);
        }

        @Override
        public float getEyeHeight() { return height / 1.5F; }

        @Override
        public boolean isPotionApplicable(PotionEffect effect) { return false; }

        @Override
        public boolean canBePushed() { return false; }

        @Override
        public void applyEntityCollision(Entity entityIn) { }

        @Override
        public void knockBack(Entity entityIn, float strength, double xRatio, double zRatio) { }

        @Override
        public boolean isEntityInsideOpaqueBlock() { return false; }

        @Override
        public boolean canBeLeashedTo(EntityPlayer player) { return false; }

        @Override
        protected SoundEvent getAmbientSound() {
            return isPlayingDead() ? null : ModSounds.get("wither_storm_ambient");
        }

        @Override
        protected SoundEvent getHurtSound(DamageSource source) { return ModSounds.get("wither_storm_hurt"); }

        @Override
        protected SoundEvent getDeathSound() { return null; }

        @Override
        protected float getSoundVolume() { return 8.0F; }

        @Override
        public int getTalkInterval() { return 80 + rand.nextInt(40); }

        @Override
        public void onDeath(DamageSource cause) {
            super.onDeath(cause);
            setRoar(true);
        }

        @Override
        protected void onDeathUpdate() {
            ++specialDeathTime;
            if (!world.isRemote) rotationPitch = Math.max(-50.0F, rotationPitch - 1.0F);
            if (specialDeathTime > 120) setDead();
        }

        private static class DoNothingGoal extends EntityAIBase {
            private final WitherStormHead head;

            DoNothingGoal(WitherStormHead head) {
                this.head = head;
                setMutexBits(7);
            }

            @Override
            public boolean shouldExecute() { return head.isPlayingDead(); }
        }

        private static class DistractionTargetGoal extends EntityAIBase {
            private final WitherStormHead head;
            DistractionTargetGoal(WitherStormHead head) { this.head = head; }
            @Override
            public boolean shouldExecute() { return head.distractedPos != null && head.isActive() && !head.isHurt(); }
            @Override
            public void updateTask() {
                Vec3d pos = head.distractedPos;
                if (pos != null) head.getLookHelper().setLookPosition(pos.x, pos.y, pos.z, 10.0F, 10.0F);
            }
        }

        @Override
        public void writeEntityToNBT(NBTTagCompound compound) {
            super.writeEntityToNBT(compound);
            compound.setBoolean("Active", isActive());
            compound.setBoolean("Roaring", isRoaring());
            compound.setInteger("RoarTime", roarTime);
            compound.setBoolean("Biting", isBiting());
            compound.setInteger("BiteTime", biteTime);
            compound.setInteger("NextRoar", nextRoar);
            compound.setInteger("ShootTime", shootTime);
            compound.setBoolean("Hurt", isHurt());
            compound.setInteger("DistractedTime", distractedTime);
            compound.setDouble("MouthAnimation", mouthAnimation);
            compound.setDouble("FadeAnimation", fadeAnimation);
            compound.setDouble("ShakeAnimation", shakeAnimation);
            compound.setFloat("BodyYaw", renderYawOffset);
            if (distractedPos != null) {
                compound.setDouble("DistractedX", distractedPos.x);
                compound.setDouble("DistractedY", distractedPos.y);
                compound.setDouble("DistractedZ", distractedPos.z);
            }
        }

        @Override
        public void readEntityFromNBT(NBTTagCompound compound) {
            super.readEntityFromNBT(compound);
            setActive(!compound.hasKey("Active") || compound.getBoolean("Active"));
            dataManager.set(ROARING, compound.getBoolean("Roaring"));
            roarTime = Math.max(0, compound.getInteger("RoarTime"));
            dataManager.set(BITING, compound.getBoolean("Biting"));
            biteTime = Math.max(0, compound.getInteger("BiteTime"));
            nextRoar = Math.max(0, compound.getInteger("NextRoar"));
            shootTime = compound.hasKey("ShootTime") ? Math.max(0, compound.getInteger("ShootTime")) : 100;
            setHurt(compound.hasKey("Hurt") ? compound.getBoolean("Hurt") : compound.getInteger("HurtTime") > 0);
            distractedTime = Math.max(0, compound.getInteger("DistractedTime"));
            if (compound.hasKey("DistractedX") && compound.hasKey("DistractedY") && compound.hasKey("DistractedZ")) {
                distractedPos = new Vec3d(compound.getDouble("DistractedX"), compound.getDouble("DistractedY"),
                        compound.getDouble("DistractedZ"));
            }
            mouthAnimation = (float) compound.getDouble("MouthAnimation");
            previousMouthAnimation = mouthAnimation;
            fadeAnimation = (float) compound.getDouble("FadeAnimation");
            previousFadeAnimation = fadeAnimation;
            shakeAnimation = (float) compound.getDouble("ShakeAnimation");
            previousShakeAnimation = shakeAnimation;
            if (compound.hasKey("BodyYaw")) renderYawOffset = rotationYawHead = compound.getFloat("BodyYaw");
        }
    }

    public static class WitherStormSegment extends StormPartBase {
        private static final DataParameter<Boolean> DYING = EntityDataManager.createKey(WitherStormSegment.class,
                DataSerializers.BOOLEAN);
        private final int tillFreeFall;
        private int dropTime;
        private int nextDropTime;
        private int timeWithParent;
        private double dropOffset;
        private double dropVelocity;
        private Vec3d wantedSegmentPos;
        private int deathTicks;

        public WitherStormSegment(World world) {
            super(world);
            setSize(15.0F, 17.5F);
            tillFreeFall = LegacyWitherStormPartLogic.segmentFreeFallDelay(rand);
            nextDropTime = 120 + rand.nextInt(160);
        }

        @Override
        protected void entityInit() {
            super.entityInit();
            dataManager.register(DYING, false);
        }

        @Override protected double getSickenedHealth() { return 4000.0D; }
        @Override protected double getSickenedSpeed() { return 0.0D; }
        @Override protected double getSickenedDamage() { return 12.0D; }
        @Override protected double getSickenedFollowRange() { return 160.0D; }
        @Override protected double getSickenedArmor() { return 6.0D; }
        @Override protected double getSickenedKnockbackResistance() { return 1.0D; }
        @Override public String getSickenedType() { return "wither_storm_segment"; }
        @Override protected double[] getOffset(EntityWitherStormLegacy owner, int index) {
            return new double[]{owner.getDesiredSegmentX(index + 1) - owner.posX,
                    owner.getDesiredSegmentY(index + 1) - owner.posY,
                    owner.getDesiredSegmentZ(index + 1) - owner.posZ};
        }
        @Override protected float getDamageTransfer() { return 0.5F; }

        @Override
        public void bindTo(EntityWitherStormLegacy owner, int index) {
            super.bindTo(owner, index);
            timeWithParent = 0;
            wantedSegmentPos = null;
        }

        @Override
        protected void updateAttachedPosition(EntityWitherStormLegacy owner, double x, double y, double z) {
            wantedSegmentPos = new Vec3d(x, y, z);
            updateDropState(owner);
            y += dropOffset;
            if (timeWithParent < 2 || owner.isPlayDeadAiDisabled()
                    || getDistanceSq(x, y, z) > 40000.0D) {
                setPosition(x, y, z);
                return;
            }
            double blend = Math.min(1.0D, 0.15D + getDistanceSq(x, y, z) * 0.001D);
            setPosition(posX + (x - posX) * blend, posY + (y - posY) * blend, posZ + (z - posZ) * blend);
        }

        private void updateDropState(EntityWitherStormLegacy owner) {
            SupplementalEntities.CommandBlockCore commandBlock = owner.getBowelsCommandBlock();
            if (commandBlock != null && commandBlock.getHealth() < commandBlock.getMaxHealth()) {
                if (nextDropTime > 0) --nextDropTime;
                if (nextDropTime == 0) {
                    dropTime = LegacyWitherStormPartLogic.segmentDropDuration(rand);
                    float ratio = commandBlock.getHealth() / Math.max(1.0F, commandBlock.getMaxHealth());
                    nextDropTime = LegacyWitherStormPartLogic.segmentDropCooldown(rand, ratio);
                }
            }
            if (dropTime > 0) --dropTime;
            if (dropTime > 0) {
                dropVelocity = Math.max(-0.8D, dropVelocity - 0.08D);
                dropOffset = Math.max(-8.0D, dropOffset + dropVelocity);
            } else if (dropOffset < 0.0D) {
                dropVelocity = Math.min(0.15D, dropVelocity + 0.08D);
                dropOffset = Math.min(0.0D, dropOffset + dropVelocity);
            }
        }

        public int getTimeWithParent() { return timeWithParent; }
        public int getDropTime() { return dropTime; }
        public int getTimeTillFreeFall() { return tillFreeFall; }
        public Vec3d getWantedSegmentPos() { return wantedSegmentPos; }

        @Override
        public void onLivingUpdate() {
            if (isInDeathSequence()) {
                tickDeathSequence();
                return;
            }
            super.onLivingUpdate();
            if (getOwnerStorm() != null) ++timeWithParent;
        }

        public void beginDeathSequence() {
            if (isInDeathSequence() || isDead) return;
            dataManager.set(DYING, true);
            deathTicks = 0;
            setNoAI(true);
            setNoGravity(false);
            motionX = motionY = motionZ = 0.0D;
        }

        public boolean isInDeathSequence() { return dataManager.get(DYING); }

        private void tickDeathSequence() {
            prevPosX = posX;
            prevPosY = posY;
            prevPosZ = posZ;
            ++deathTicks;
            motionX *= 0.98D;
            motionZ *= 0.98D;
            motionY = Math.max(-2.5D, motionY - (deathTicks < tillFreeFall ? 0.015D : 0.08D));
            move(MoverType.SELF, motionX, motionY, motionZ);
            rotationPitch = Math.min(90.0F, rotationPitch + 0.6F);
            if (!world.isRemote) {
                if (deathTicks > 10 && deathTicks % 10 == 0) dropSmallMassCluster(1);
                if (onGround && deathTicks > tillFreeFall) {
                    onBigFall();
                    setDead();
                }
            } else if (!onGround) {
                world.spawnParticle(EnumParticleTypes.BLOCK_DUST, posX, posY + height * 0.5D, posZ,
                        0.0D, -0.1D, 0.0D, Block.getStateId(net.minecraft.init.Blocks.OBSIDIAN.getDefaultState()));
            }
            if (deathTicks > tillFreeFall + 240) setDead();
        }

        protected void onBigFall() {
            for (int i = 0; i < 6; i++) {
                world.newExplosion(this, posX, posY - i, posZ, 16.0F, false, false);
            }
        }

        protected void dropSmallMassCluster(int radius) {
            BlockCluster cluster = new BlockCluster(world);
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (x * x + y * y + z * z > radius * radius) continue;
                        if (rand.nextFloat() > 0.975F && ModBlocks.get("withered_phlegm_block") != null) {
                            cluster.addBlock(new BlockPos(x, y, z), ModBlocks.get("withered_phlegm_block").getDefaultState());
                        } else {
                            String[] names = {"tainted_flesh_block", "tainted_dirt", "tainted_stone", "tainted_cobblestone", "tainted_planks"};
                            Block block = ModBlocks.get(names[rand.nextInt(names.length)]);
                            if (block != null) cluster.addBlock(new BlockPos(x, y, z), block.getDefaultState());
                        }
                    }
                }
            }
            if (cluster.getBlocks().isEmpty()) return;
            cluster.setPosition(posX + rand.nextGaussian() * 5.0D, posY + height * 0.5D + rand.nextGaussian() * 5.0D,
                    posZ + rand.nextGaussian() * 5.0D);
            cluster.setSink(-1);
            cluster.motionX = rand.nextGaussian() * 0.4D;
            cluster.motionY = rand.nextGaussian() * 0.3D;
            cluster.motionZ = rand.nextGaussian() * 0.4D;
            cluster.setRotationDelta(rand.nextInt(90) * 0.15F, rand.nextInt(90) * 0.15F);
            world.spawnEntity(cluster);
        }

        @Override
        public void writeEntityToNBT(NBTTagCompound compound) {
            super.writeEntityToNBT(compound);
            UUID parent = getOwnerUuid();
            if (parent != null) compound.setUniqueId("Parent", parent);
            compound.setInteger("TimeWithParent", timeWithParent);
            compound.setInteger("DropTime", dropTime);
            compound.setInteger("NextDropTime", nextDropTime);
            compound.setDouble("DropOffset", dropOffset);
            compound.setDouble("DropVelocity", dropVelocity);
            compound.setBoolean("DeathSequence", isInDeathSequence());
            compound.setInteger("DeathTicks", deathTicks);
        }

        @Override
        public void readEntityFromNBT(NBTTagCompound compound) {
            super.readEntityFromNBT(compound);
            if (compound.hasUniqueId("Parent") && getOwnerUuid() == null) setOwnerUuid(compound.getUniqueId("Parent"));
            timeWithParent = Math.max(0, compound.getInteger("TimeWithParent"));
            dropTime = Math.max(0, compound.getInteger("DropTime"));
            nextDropTime = Math.max(0, compound.getInteger("NextDropTime"));
            dropOffset = compound.getDouble("DropOffset");
            dropVelocity = compound.getDouble("DropVelocity");
            dataManager.set(DYING, compound.getBoolean("DeathSequence"));
            deathTicks = Math.max(0, compound.getInteger("DeathTicks"));
            if (isInDeathSequence()) {
                setNoAI(true);
                setNoGravity(false);
            }
        }
    }
}
