package com.wdcftgg.witherstormmod.common.entity;

import com.wdcftgg.witherstormmod.common.advancement.ModCriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityLlamaSpit;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.potion.PotionEffect;
import net.minecraft.init.MobEffects;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.util.Rotation;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import com.wdcftgg.witherstormmod.common.world.StructureTemplates;
import com.wdcftgg.witherstormmod.common.world.BowelsManager;
import com.wdcftgg.witherstormmod.common.world.BowelsDimensions;
import com.wdcftgg.witherstormmod.common.world.BowelsInstanceData;
import com.wdcftgg.witherstormmod.common.world.ChunkLoadingManager;
import com.wdcftgg.witherstormmod.common.config.WitherStormConfig;
import com.wdcftgg.witherstormmod.common.init.ModSounds;
import com.wdcftgg.witherstormmod.common.init.ModBlocks;
import com.wdcftgg.witherstormmod.common.network.ModNetwork;
import com.wdcftgg.witherstormmod.common.resource.UpstreamBlockTags;
import com.wdcftgg.witherstormmod.common.resource.UpstreamEntityTags;
import com.wdcftgg.witherstormmod.common.taint.TaintingManager;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import javax.annotation.Nullable;

public class WitherStormEntity extends EntityMob {

    public enum PlayDeadState { NORMAL_BEHAVIOR, FALLING, PLAYING_DEAD, REVIVING }

    private static final DataParameter<Integer> PHASE = EntityDataManager.createKey(WitherStormEntity.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> CONSUMED_MASS = EntityDataManager.createKey(WitherStormEntity.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> PLAY_DEAD_STATE = EntityDataManager.createKey(WitherStormEntity.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> INVULNERABLE_TICKS = EntityDataManager.createKey(WitherStormEntity.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> STARTING_INVULNERABLE_TICKS = EntityDataManager.createKey(WitherStormEntity.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> SHOULD_SHOW_HOLE = EntityDataManager.createKey(WitherStormEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> OTHER_HEADS_DISABLED = EntityDataManager.createKey(WitherStormEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> HEAD_ANIMATION_FLAGS = EntityDataManager.createKey(WitherStormEntity.class, DataSerializers.VARINT);
    private static final DataParameter<Float> BODY_X_ROTATION = EntityDataManager.createKey(WitherStormEntity.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> EVOLUTION_SPEED_MODIFIER = EntityDataManager.createKey(WitherStormEntity.class, DataSerializers.FLOAT);
    private static final DataParameter<Boolean> HOLE_ENABLED = EntityDataManager.createKey(WitherStormEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> FIRST_HEAD_TARGET = EntityDataManager.createKey(WitherStormEntity.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> SECOND_HEAD_TARGET = EntityDataManager.createKey(WitherStormEntity.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> THIRD_HEAD_TARGET = EntityDataManager.createKey(WitherStormEntity.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> HEAD_INJURY_FLAGS = EntityDataManager.createKey(WitherStormEntity.class, DataSerializers.VARINT);
    private static final String EVOLUTION_SPEED_NBT_KEY = "WitherStormEvolutionSpeedModifier";
    private static final double DEFAULT_EVOLUTION_SPEED_MODIFIER = 1.0D;
    private static final double RESUMMONED_EVOLUTION_SPEED_MODIFIER = 0.5D;
    private static final double BASE_MAX_HEALTH = 400.0D;
    private static final double PHASE_MAX_HEALTH_BONUS = 624.0D;
    private static final double BASE_ARMOR = 8.0D;
    private static final UUID PHASE_HEALTH_MODIFIER_UUID = UUID.fromString("9B8DA22B-138B-4B68-879D-3FD329FAF903");
    private static final UUID PHASE_ARMOR_MODIFIER_UUID = UUID.fromString("C806DBFA-2B10-4BEA-B16C-C3233707399C");
    private static final int[] PHASE_REQUIREMENTS = {100, 400, 1200, 18800, 195000, 351400, 580800, 2125000};
    private static final float[] PHASE_WIDTH = {0.9F, 0.9F, 0.9F, 0.9F, 10.0F, 10.0F, 15.0F, 15.0F};
    private static final float[] PHASE_HEIGHT = {3.5F, 3.5F, 3.5F, 3.5F, 30.0F, 60.0F, 90.0F, 120.0F};
    private static final float[] PHASE_SCALE = {0.72F, 0.9F, 1.1F, 1.4F, 2.67F, 2.67F, 4.0F, 4.0F};
    private final UUID[] segmentUuids = new UUID[2];
    private final WitherStormHeadManager headManager = new WitherStormHeadManager(this);
    private final UltimateTargetManager targetManager = new UltimateTargetManager(this);
    private final SymbiontSummoningManager summoningManager = new SymbiontSummoningManager(this);
    private final WitherStormClusterManager clusterManager = new WitherStormClusterManager(this);
    private final BossInfoServer legacyBossInfo = (BossInfoServer) new BossInfoServer(
            getDisplayName(), BossInfo.Color.PURPLE, BossInfo.Overlay.PROGRESS).setDarkenSky(true);
    private UUID commandBlockUuid;
    private BlockPos podiumPosition;
    private boolean podiumPlaced;
    private final Map<UUID, NBTTagCompound> consumedPets = new LinkedHashMap<UUID, NBTTagCompound>();
    private final Map<UUID, Entity> trackedEntities = new LinkedHashMap<UUID, Entity>();
    private final List<UUID> savedTrackedEntities = new ArrayList<UUID>();
    private final Set<UUID> pendingBowelsTransfers = new HashSet<UUID>();
    private int stateTicks;
    private int missingCommandBlockTicks;
    private int recentlyRevivedTicks;
    private int trackedEntityTicks;
    private int destroyBlocksTick;
    private int witherStormDeathTime;
    private int lastConsumedMass;
    private int lastFlyingHeightChange;
    private double currentFlyingHeight;
    private boolean deathRewardsReleased;
    private boolean resummoned;
    private float bodyXRotation;
    private float previousBodyXRotation;
    private float clientBodyXRotationTarget;
    private int clientBodyXRotationSteps;

    public WitherStormEntity(World worldIn) {
        super(worldIn);
        experienceValue = 2500;
        isImmuneToFire = true;
        setNoGravity(true);
        enablePersistence();
    }

    public void ignite() {
        makeInvulnerable();
    }

    public void makeInvulnerable() {
        int duration = Math.max(1, WitherStormConfig.invulnerabilityTime * 20);
        dataManager.set(STARTING_INVULNERABLE_TICKS, duration);
        dataManager.set(INVULNERABLE_TICKS, duration);
        setHealth(1.0F);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(PHASE, 0);
        dataManager.register(CONSUMED_MASS, 0);
        dataManager.register(PLAY_DEAD_STATE, PlayDeadState.NORMAL_BEHAVIOR.ordinal());
        dataManager.register(INVULNERABLE_TICKS, 0);
        dataManager.register(STARTING_INVULNERABLE_TICKS, Math.max(1, WitherStormConfig.invulnerabilityTime * 20));
        dataManager.register(SHOULD_SHOW_HOLE, false);
        dataManager.register(OTHER_HEADS_DISABLED, false);
        dataManager.register(HEAD_ANIMATION_FLAGS, 0);
        dataManager.register(BODY_X_ROTATION, 0.0F);
        dataManager.register(EVOLUTION_SPEED_MODIFIER, (float) DEFAULT_EVOLUTION_SPEED_MODIFIER);
        dataManager.register(HOLE_ENABLED, WitherStormConfig.shouldShowHole);
        dataManager.register(FIRST_HEAD_TARGET, 0);
        dataManager.register(SECOND_HEAD_TARGET, 0);
        dataManager.register(THIRD_HEAD_TARGET, 0);
        dataManager.register(HEAD_INJURY_FLAGS, 0);
    }

    /** 上游主实体不使用原版凋灵 AI，所有目标和攻击都由移植状态机管理。 */
    @Override
    protected void initEntityAI() {
    }

    @Override
    protected void updateAITasks() {
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(BASE_MAX_HEALTH);
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(BASE_ARMOR);
        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.0D);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(1024.0D);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.5D);
    }

    @Override
    public void onLivingUpdate() {
        if (world.isRemote) updateClientBodyRotation();
        if (!world.isRemote && dataManager.get(HOLE_ENABLED) != WitherStormConfig.shouldShowHole) {
            dataManager.set(HOLE_ENABLED, WitherStormConfig.shouldShowHole);
        }
        if (!world.isRemote && getInvulnerableTicks() > 0) {
            int remaining = getInvulnerableTicks() - 1;
            dataManager.set(INVULNERABLE_TICKS, remaining);
            int startingTicks = Math.max(1, getStartingInvulnerableTicks());
            if (ticksExisted % 10 == 0) {
                heal((getMaxHealth() - 1.0F) / startingTicks * 10.0F);
            }
            headManager.tick();
            updateBodyRotation();
            updateAttachedEntities();
            updateLegacyBossInfo();
            spawnStormParticles();
            if (remaining <= 0) {
                world.newExplosion(this, posX, posY, posZ, 7.0F, false, false);
                world.playEvent(1023, getPosition(), 0);
            }
            return;
        }
        if (!world.isRemote && isPlayDeadAiDisabled()) {
            headManager.tick();
            updatePlayDeadState();
            updateBodyRotation();
            updateAttachedEntities();
            updateLegacyBossInfo();
            return;
        }
        if (!world.isRemote && !isDead) {
            targetManager.tick();
            summoningManager.tick();
            updateCustomMovement();
        }
        super.onLivingUpdate();
        spawnStormParticles();
        headManager.tick();
        if (world.isRemote || isDead) return;
        if (getPlayDeadState() == PlayDeadState.REVIVING) updatePlayDeadState();
        updateBodyRotation();
        if (recentlyRevivedTicks > 0 && ++recentlyRevivedTicks > 3600) recentlyRevivedTicks = 0;
        updateEvolution();
        updateAttachedEntities();
        tickDelayedBlockDestruction();
        convertFallingBlocks();
        tickTrackedEntities();
        applyTractorBeam();
        tickProjectilesHittingHeads();
        applyMassAbsorption();
        applyTornApartCollision();
        clusterManager.tick();
        updateLegacyBossInfo();
    }

    private void spawnStormParticles() {
        if (getPhase() < 4) {
            for (int head = 0; head < getTotalHeads(); head++) {
                Vec3d position = getHeadPosition(head, 1.0F);
                world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
                        position.x + rand.nextGaussian() * 0.3D,
                        position.y + rand.nextGaussian() * 0.3D,
                        position.z + rand.nextGaussian() * 0.3D, 0.0D, 0.0D, 0.0D);
                if (isArmored() && rand.nextInt(4) == 0) {
                    world.spawnParticle(EnumParticleTypes.SPELL_MOB,
                            position.x + rand.nextGaussian() * 0.3D,
                            position.y + rand.nextGaussian() * 0.3D,
                            position.z + rand.nextGaussian() * 0.3D, 0.7D, 0.7D, 0.5D);
                }
            }
        }
        if (getInvulnerableTicks() > 0) {
            for (int index = 0; index < 3; index++) {
                world.spawnParticle(EnumParticleTypes.SPELL_MOB,
                        posX + rand.nextGaussian(), posY + rand.nextFloat() * 3.3F,
                        posZ + rand.nextGaussian(), 0.7D, 0.7D, 0.9D);
            }
        }
    }

    /** 1.12 没有 Monster 飞行基类；直接移动可保留上游无重力速度模型。 */
    @Override
    public void travel(float strafe, float vertical, float forward) {
        moveRelative(strafe, vertical, forward, 0.02F);
        move(MoverType.SELF, motionX, motionY, motionZ);
        prevLimbSwingAmount = limbSwingAmount;
        double movedX = posX - prevPosX;
        double movedZ = posZ - prevPosZ;
        float movement = Math.min(1.0F, MathHelper.sqrt(movedX * movedX + movedZ * movedZ) * 4.0F);
        limbSwingAmount += (movement - limbSwingAmount) * 0.4F;
        limbSwing += limbSwingAmount;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos position) {
        fallDistance = 0.0F;
    }

    @Override
    public void setInWeb() {
    }

    @Override
    public boolean isOnLadder() {
        return false;
    }

    @Override
    public boolean isPotionApplicable(PotionEffect effect) {
        return false;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public void applyEntityCollision(Entity entityIn) {
    }

    @Override
    public void knockBack(Entity entityIn, float strength, double xRatio, double zRatio) {
    }

    @Override
    public boolean isEntityInsideOpaqueBlock() {
        return false;
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.UNDEAD;
    }

    @Override
    public boolean isNonBoss() {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (isDeadOrPlayingDead()) return null;
        return getPhase() < 4 ? SoundEvents.ENTITY_WITHER_AMBIENT : ModSounds.get("wither_storm_ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return getPhase() < 4 ? SoundEvents.ENTITY_WITHER_HURT : ModSounds.get("wither_storm_hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return getPhase() < 4 ? SoundEvents.ENTITY_WITHER_DEATH : null;
    }

    @Override
    public int getTalkInterval() {
        return getPhase() > 3 ? Math.max(80, rand.nextInt(120)) : super.getTalkInterval();
    }

    @Override
    protected float getSoundVolume() {
        return getPhase() > 3 ? 25.0F : super.getSoundVolume();
    }

    private void updateEvolution() {
        int oldPhase = getPhase();
        int consumedMass = getConsumedMass();
        if (consumedMass == lastConsumedMass) return;
        lastConsumedMass = consumedMass;
        if (oldPhase >= 7 || oldPhase == 5 || consumedMass <= getConsumptionAmountForPhase(oldPhase)) return;
        int newPhase = oldPhase + 1;
        if (setPhase(newPhase, consumedMass)) {
            accelerateAfterEvolution();
            if (newPhase == 4) {
                ModNetwork.playGlobalSound(world, ModSounds.get("wither_storm_evolves"), 1.0F, 1.0F);
            }
        }
    }

    private void accelerateAfterEvolution() {
        if (WitherStormConfig.chaseOnPhaseChange && getPhase() > 3) targetManager.accelerate();
    }

    public boolean setPhase(int newPhase) {
        return setPhase(newPhase, newPhase == 0 ? 0 : getConsumptionAmountForPhase(newPhase - 1));
    }

    public boolean setPhase(int newPhase, int consumedMass) {
        if (newPhase < 0 || newPhase > 7) return false;
        dataManager.set(PHASE, newPhase);
        dataManager.set(CONSUMED_MASS, Math.max(0, consumedMass));
        if (newPhase < 7) dataManager.set(SHOULD_SHOW_HOLE, false);
        dataManager.set(OTHER_HEADS_DISABLED, newPhase == 6 && consumedMass < getSubPhaseRequirement(6));
        updatePhaseAttributes(newPhase);
        headManager.onPhaseChanged(newPhase);
        updateSizeForPlayDeadState();
        if (newPhase < 6) removeAttached(segmentUuids);
        return true;
    }

    private void updatePhaseAttributes(int phase) {
        IAttributeInstance maxHealth = getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        IAttributeInstance armor = getEntityAttribute(SharedMonsterAttributes.ARMOR);
        maxHealth.setBaseValue(BASE_MAX_HEALTH);
        armor.setBaseValue(BASE_ARMOR);
        if (phase < 4) {
            maxHealth.removeModifier(PHASE_HEALTH_MODIFIER_UUID);
            armor.removeModifier(PHASE_ARMOR_MODIFIER_UUID);
            return;
        }
        if (maxHealth.getModifier(PHASE_HEALTH_MODIFIER_UUID) == null) {
            maxHealth.applyModifier(new AttributeModifier(PHASE_HEALTH_MODIFIER_UUID,
                    "Phase health modifier", PHASE_MAX_HEALTH_BONUS, 0));
        }
        if (armor.getModifier(PHASE_ARMOR_MODIFIER_UUID) == null) {
            armor.applyModifier(new AttributeModifier(PHASE_ARMOR_MODIFIER_UUID,
                    "Phase armor modifier", getInitialPhaseArmorBonus(phase), 0));
        }
    }

    static double getInitialPhaseArmorBonus(int phase) {
        return (phase + 1) * 2.0D;
    }

    /** Applies the distinct state used by the upstream super-beacon resurrection path. */
    public void initializeFromSuperBeacon(int phase) {
        dataManager.set(EVOLUTION_SPEED_MODIFIER, (float) RESUMMONED_EVOLUTION_SPEED_MODIFIER);
        setPhase(MathHelper.clamp(phase, 0, 7));
        dataManager.set(INVULNERABLE_TICKS, 0);
        dataManager.set(STARTING_INVULNERABLE_TICKS, Math.max(1, WitherStormConfig.invulnerabilityTime * 20));
        recentlyRevivedTicks = 1;
        resummoned = true;
        dataManager.set(PLAY_DEAD_STATE, PlayDeadState.NORMAL_BEHAVIOR.ordinal());
        stateTicks = 0;
        missingCommandBlockTicks = 0;
    }

    public int getSubPhaseRequirement(int phase) {
        int previous = phase == 0 ? 0 : getConsumptionAmountForPhase(phase - 1);
        return previous + (getConsumptionAmountForPhase(phase) - previous) / 2;
    }

    public int getPhaseRequirement() {
        return getConsumptionAmountForPhase(getPhase());
    }

    public int getConsumptionAmountForPhase(int phase) {
        if (phase < 0 || phase >= PHASE_REQUIREMENTS.length) return 0;
        return adjustAmountForEvolutionSpeed(PHASE_REQUIREMENTS[phase]);
    }

    public int adjustAmountForEvolutionSpeed(int rawRequirement) {
        return scaleConsumptionRequirement(rawRequirement, getEvolutionSpeedModifier());
    }

    public double getEvolutionSpeedModifier() {
        return dataManager.get(EVOLUTION_SPEED_MODIFIER);
    }

    public int getPreviousPhaseRequirement() {
        return getPhase() == 0 ? 0 : getConsumptionAmountForPhase(getPhase() - 1);
    }

    static int scaleConsumptionRequirement(int rawRequirement, double modifier) {
        return (int) (rawRequirement * modifier);
    }

    static double readEvolutionSpeedModifier(NBTTagCompound compound, boolean isResummoned) {
        if (compound.hasKey(EVOLUTION_SPEED_NBT_KEY, 99)) {
            double modifier = compound.getDouble(EVOLUTION_SPEED_NBT_KEY);
            if (Double.isFinite(modifier) && modifier >= 0.0D) return modifier;
        }
        return isResummoned ? RESUMMONED_EVOLUTION_SPEED_MODIFIER : DEFAULT_EVOLUTION_SPEED_MODIFIER;
    }

    public float getPhaseProgress() {
        int previous = getPreviousPhaseRequirement();
        int required = getPhaseRequirement();
        return MathHelper.clamp((getConsumedMass() - previous) / (float) Math.max(1, required - previous), 0.0F, 1.0F);
    }

    public void onFormidibombExplosion() {
        if (!canStartFormidibombFall(getPhase(), isBeingTornApart(), getPlayDeadState())) return;
        setPlayDeadState(PlayDeadState.FALLING);
        setAttackTarget(null);
        navigator.clearPath();
    }

    static boolean canStartFormidibombFall(int phase, boolean beingTornApart,
                                           PlayDeadState playDeadState) {
        return phase >= 5 && !(phase > 6 && beingTornApart)
                && playDeadState == PlayDeadState.NORMAL_BEHAVIOR;
    }

    public void reviveFromPlayingDead() {
        if (getPlayDeadState() == PlayDeadState.PLAYING_DEAD) setPlayDeadState(PlayDeadState.REVIVING);
    }

    private void setPlayDeadState(PlayDeadState state) {
        PlayDeadState previous = getPlayDeadState();
        if (previous == state) return;
        dataManager.set(PLAY_DEAD_STATE, state.ordinal());
        stateTicks = 0;
        missingCommandBlockTicks = 0;
        updateSizeForPlayDeadState();
        legacyBossInfo.setVisible(!disablesAi(state));
        if (disablesAi(state) && !disablesAi(previous)) clearTrackedEntities(false);
        if (state == PlayDeadState.PLAYING_DEAD && getPhase() == 5) {
            setPhase(6, getConsumedMass());
            dataManager.set(OTHER_HEADS_DISABLED, true);
            ensureSegments();
        }
        if (state == PlayDeadState.REVIVING) {
            recentlyRevivedTicks = 1;
            ModNetwork.playGlobalSound(world, ModSounds.get("wither_storm_reactivates"), 10.0F, 1.0F);
        }
        if (!world.isRemote && state == PlayDeadState.FALLING) {
            headManager.onStartFalling();
            triggerNearby(ModCriteriaTriggers.PLAY_DEAD, 100.0D);
        } else if (!world.isRemote && state == PlayDeadState.REVIVING
                && previous == PlayDeadState.PLAYING_DEAD) {
            triggerNearby(ModCriteriaTriggers.REVIVAL, 100.0D);
        }
        if (state == PlayDeadState.NORMAL_BEHAVIOR) removeCommandBlockCore();
    }

    private void updatePlayDeadState() {
        ++stateTicks;
        prevPosX = posX; prevPosY = posY; prevPosZ = posZ;
        if (getPlayDeadState() == PlayDeadState.FALLING) {
            motionX *= 0.92D; motionZ *= 0.92D; motionY = Math.max(-2.5D, motionY - 0.08D);
            move(MoverType.SELF, motionX, motionY, motionZ);
            if (stateTicks % 8 == 0) spawnFallingDebris();
            if (getPhase() == 5 && stateTicks == 201) {
                setPhase(6, getConsumedMass());
                dataManager.set(OTHER_HEADS_DISABLED, true);
                ensureSegments();
                ModNetwork.playGlobalSound(world, ModSounds.get("wither_storm_splits"), 1.0F, 1.0F);
            }
            if (onGround) setPlayDeadState(PlayDeadState.PLAYING_DEAD);
        } else if (getPlayDeadState() == PlayDeadState.PLAYING_DEAD) {
            motionX = motionY = motionZ = 0.0D;
            if (isOnBack()) {
                ensurePlayingDeadPodium();
                if (podiumPosition != null && !consumedPets.isEmpty()) {
                    spawnConsumedPets(new Vec3d(podiumPosition.getX() + 0.5D,
                            podiumPosition.getY() + 12.0D, podiumPosition.getZ() + 0.5D));
                }
            }
            Entity core = resolve(commandBlockUuid);
            if (commandBlockUuid != null && (core == null || core.isDead || getDistance(core) > 64.0F)) {
                ++missingCommandBlockTicks;
            }
            if (missingCommandBlockTicks > 200) setPlayDeadState(PlayDeadState.REVIVING);
        } else if (getPlayDeadState() == PlayDeadState.REVIVING && stateTicks > 20) {
            ModNetwork.shakeTracking(this, 60.0F, 4.0F);
            double explosionX = podiumPosition == null ? posX : podiumPosition.getX();
            double explosionY = podiumPosition == null ? posY : podiumPosition.getY();
            double explosionZ = podiumPosition == null ? posZ : podiumPosition.getZ();
            world.newExplosion(this, explosionX, explosionY, explosionZ, 16.0F, false, false);
            world.playSound(null, getPosition(), ModSounds.get("tremble"), SoundCategory.AMBIENT, 10.0F, 1.0F);
            setPlayDeadState(PlayDeadState.NORMAL_BEHAVIOR);
        }
    }

    private void updateBodyRotation() {
        previousBodyXRotation = bodyXRotation;
        float updated = getNextBodyXRotation(bodyXRotation, getPlayDeadState());
        boolean landedOnBack = getPlayDeadState() == PlayDeadState.PLAYING_DEAD
                && bodyXRotation < 90.0F && updated >= 90.0F;
        bodyXRotation = updated;
        dataManager.set(BODY_X_ROTATION, bodyXRotation);
        if (landedOnBack) onFallOnBack();
    }

    private void updateClientBodyRotation() {
        previousBodyXRotation = bodyXRotation;
        if (clientBodyXRotationSteps > 0) {
            bodyXRotation += (clientBodyXRotationTarget - bodyXRotation) / clientBodyXRotationSteps;
            --clientBodyXRotationSteps;
        }
    }

    static float getNextBodyXRotation(float current, PlayDeadState state) {
        if (state == PlayDeadState.PLAYING_DEAD) {
            if (current < 90.0F) current += current * 0.04F + 0.05F;
            return Math.min(90.0F, current);
        }
        if (!disablesAi(state)) {
            current += -current * 0.015F - 0.02F;
            return Math.max(0.0F, current);
        }
        return current;
    }

    private void onFallOnBack() {
        world.playSound(null, getPosition(), ModSounds.get("wither_storm_thump"), SoundCategory.HOSTILE,
                width + 3.0F, 1.0F);
        ModNetwork.shakeTracking(this, 30.0F, 12.0F);
    }

    private void updateSizeForPlayDeadState() {
        int phase = getPhase();
        setSize(PHASE_WIDTH[phase], getPlayDeadState() == PlayDeadState.PLAYING_DEAD ? 0.1F : PHASE_HEIGHT[phase]);
    }

    private void updateLegacyBossInfo() {
        legacyBossInfo.setPercent(MathHelper.clamp(getHealth() / getMaxHealth(), 0.0F, 1.0F));
        legacyBossInfo.setVisible(!isPlayDeadAiDisabled());
    }

    private void spawnFallingDebris() {
        world.spawnParticle(EnumParticleTypes.BLOCK_DUST, posX, posY + height * 0.5D, posZ, 0.0D, -0.2D, 0.0D,
                Block.getStateId(Blocks.OBSIDIAN.getDefaultState()));
        if (!world.isRemote && getPhase() >= 5) {
            SupplementalEntities.BlockClusterEntity cluster = new SupplementalEntities.BlockClusterEntity(world, posX, posY + height * 0.35D, posZ,
                    Blocks.OBSIDIAN.getDefaultState());
            cluster.setSink(1);
            cluster.setShouldCrumble(true);
            cluster.motionX = rand.nextGaussian() * 0.08D;
            cluster.motionY = 0.18D + rand.nextFloat() * 0.12D;
            cluster.motionZ = rand.nextGaussian() * 0.08D;
            if (world.spawnEntity(cluster)) trackEntityToConsume(cluster);
        }
    }

    private void updateAttachedEntities() {
        if (getPhase() >= 6) ensureSegments();
    }

    private void ensureSegments() {
        for (int i = 0; i < segmentUuids.length; i++) {
            Entity entity = resolve(segmentUuids[i]);
            if (!(entity instanceof SupplementalEntities.WitherStormSegmentEntity) || entity.isDead) {
                SupplementalEntities.WitherStormSegmentEntity segment = new SupplementalEntities.WitherStormSegmentEntity(world);
                segment.bindTo(this, i);
                segment.setPosition(posX, posY, posZ);
                if (world.spawnEntity(segment)) segmentUuids[i] = segment.getUniqueID();
            }
        }
    }

    public double getDesiredSegmentX(int segment) {
        if (segment <= 0) return posX;
        double staticX = isPlayDeadAiDisabled() ? 45.0D : 75.0D;
        double staticZ = isPlayDeadAiDisabled() ? 0.0D : (segment == 1 ? 50.0D : -50.0D);
        float angle = (renderYawOffset + 180.0F * (segment - 1)) * 0.017453292F;
        float offset = (float) MathHelper.atan2(staticZ, staticX);
        return posX + MathHelper.sin(angle + offset) * Math.sqrt(staticX * staticX + staticZ * staticZ);
    }

    public double getDesiredSegmentY(int segment) {
        return isPlayDeadAiDisabled() ? getEntityBoundingBox().minY + 10.0D : posY;
    }

    public double getDesiredSegmentZ(int segment) {
        if (segment <= 0) return posZ;
        double staticX = isPlayDeadAiDisabled() ? 45.0D : 75.0D;
        double staticZ = isPlayDeadAiDisabled() ? 0.0D : (segment == 1 ? 50.0D : -50.0D);
        float angle = (renderYawOffset + 180.0F * (segment - 1)) * 0.017453292F;
        float offset = (float) MathHelper.atan2(staticZ, staticX);
        return posZ + MathHelper.cos(angle + offset) * Math.sqrt(staticX * staticX + staticZ * staticZ);
    }

    private void ensurePlayingDeadPodium() {
        if (!podiumPlaced && onGround) placePlayingDeadPodium();
        Entity entity = resolve(commandBlockUuid);
        if (entity instanceof SupplementalEntities.CommandBlockEntity && !entity.isDead) return;
        if (!podiumPlaced) return;
        SupplementalEntities.CommandBlockEntity core = new SupplementalEntities.CommandBlockEntity(world);
        core.bindTo(this, 0);
        core.setPosition(podiumPosition.getX() + 0.5D, podiumPosition.getY() + 11.0D, podiumPosition.getZ() + 0.5D);
        if (world.spawnEntity(core)) commandBlockUuid = core.getUniqueID();
    }

    private void placePlayingDeadPodium() {
        if (world.isRemote || podiumPlaced) return;
        float angle = (renderYawOffset - 90.0F) * 0.017453292F;
        BlockPos anchor = new BlockPos(posX + MathHelper.cos(angle) * 5.0D, posY - 4.0D,
                posZ + MathHelper.sin(angle) * 5.0D);
        if (!isPodiumAreaLoaded(anchor)) return;
        Template template = StructureTemplates.get("command_block_podium");
        if (template == null) return;
        Rotation rotation = StructureTemplates.getFeatureRotation(anchor);
        BlockPos origin = StructureTemplates.getCenteredFeatureOrigin(template, anchor, rotation);
        if (!StructureTemplates.place(world, "command_block_podium", origin, rotation, false)) return;
        podiumPosition = anchor;
        podiumPlaced = true;
    }

    private void removePlayingDeadPodium() {
        if (world.isRemote || podiumPosition == null || !podiumPlaced || !isPodiumAreaLoaded(podiumPosition)) return;
        Template template = StructureTemplates.get("command_block_podium");
        if (template == null) return;
        Rotation rotation = StructureTemplates.getFeatureRotation(podiumPosition);
        BlockPos origin = StructureTemplates.getCenteredFeatureOrigin(template, podiumPosition, rotation);
        if (StructureTemplates.remove(world, "command_block_podium", origin, rotation)) {
            podiumPosition = null;
            podiumPlaced = false;
        }
    }

    private boolean isPodiumAreaLoaded(BlockPos anchor) {
        if (!(world instanceof WorldServer)) return false;
        int centerChunkX = anchor.getX() >> 4;
        int centerChunkZ = anchor.getZ() >> 4;
        WorldServer serverWorld = (WorldServer) world;
        for (int offsetX = -3; offsetX <= 3; offsetX++) {
            for (int offsetZ = -3; offsetZ <= 3; offsetZ++) {
                if (serverWorld.getChunkProvider().getLoadedChunk(centerChunkX + offsetX, centerChunkZ + offsetZ) == null) return false;
            }
        }
        return true;
    }

    private Entity resolve(UUID uuid) {
        if (uuid == null) return null;
        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, getEntityBoundingBox().grow(256.0D),
                entity -> uuid.equals(entity.getUniqueID()));
        return entities.isEmpty() ? null : entities.get(0);
    }

    private void removeAttached(UUID[] uuids) {
        removeAttached(uuids, false);
    }

    private void removeAttached(UUID[] uuids, boolean deathSequence) {
        for (int i = 0; i < uuids.length; i++) {
            Entity entity = resolve(uuids[i]);
            if (entity instanceof SupplementalEntities.WitherStormSegmentEntity && deathSequence) {
                ((SupplementalEntities.WitherStormSegmentEntity) entity).beginDeathSequence();
            } else if (entity != null) {
                entity.setDead();
            }
            uuids[i] = null;
        }
    }

    private void removeCommandBlockCore() {
        Entity entity = resolve(commandBlockUuid);
        if (entity != null) entity.setDead();
        commandBlockUuid = null;
        removePlayingDeadPodium();
    }

    private void tickDelayedBlockDestruction() {
        if (destroyBlocksTick <= 0) return;
        if (--destroyBlocksTick > 0 || !world.getGameRules().getBoolean("mobGriefing")) return;
        boolean destroyed = false;
        int minX = MathHelper.floor(posX);
        int minY = MathHelper.floor(posY);
        int minZ = MathHelper.floor(posZ);
        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                for (int offsetY = 0; offsetY <= 3; offsetY++) {
                    BlockPos position = new BlockPos(minX + offsetX, minY + offsetY, minZ + offsetZ);
                    if (!world.isBlockLoaded(position)) continue;
                    IBlockState state = world.getBlockState(position);
                    Block block = state.getBlock();
                    if (block == Blocks.AIR || block == Blocks.BEDROCK || block == Blocks.BARRIER
                            || block == Blocks.COMMAND_BLOCK || block == Blocks.CHAIN_COMMAND_BLOCK
                            || block == Blocks.REPEATING_COMMAND_BLOCK
                            || UpstreamBlockTags.contains(UpstreamBlockTags.WITHER_STORM_BLOCK_BLACKLIST, state)) {
                        continue;
                    }
                    if (!block.canEntityDestroy(state, world, position, this)) continue;
                    destroyed = world.destroyBlock(position, true) || destroyed;
                }
            }
        }
        if (destroyed) world.playEvent(1022, getPosition(), 0);
    }

    void trackEntityToConsume(Entity entity) {
        if (entity == null || entity == this || entity.isDead || entity.dimension != dimension
                || entity instanceof EntityPlayer || entity instanceof WitherStormEntity
                || entity instanceof SupplementalEntities.StormPartBase
                || entity instanceof PowerfulExplosiveEntity.FormidibombEntity) return;
        UUID uuid = entity.getUniqueID();
        if (trackedEntities.containsKey(uuid)) return;
        trackedEntities.put(uuid, entity);
        savedTrackedEntities.remove(uuid);
        if (entity instanceof SupplementalEntities.BlockClusterEntity) {
            entity.setNoGravity(true);
            ((SupplementalEntities.BlockClusterEntity) entity).setPhysics(false);
        }
    }

    private void tickTrackedEntities() {
        ++trackedEntityTicks;
        if (!savedTrackedEntities.isEmpty()) {
            Iterator<UUID> saved = savedTrackedEntities.iterator();
            while (saved.hasNext()) {
                UUID uuid = saved.next();
                Entity entity = resolveAny(uuid);
                if (entity != null && !entity.isDead) {
                    trackedEntities.put(uuid, entity);
                    saved.remove();
                } else if (trackedEntityTicks > 80) {
                    saved.remove();
                }
            }
        }
        if (trackedEntities.isEmpty() || isDeadOrPlayingDead()) return;
        Vec3d absorptionPoint = new Vec3d(posX, posY + height * 0.5D, posZ);
        AxisAlignedBB absorptionBox = getEntityBoundingBox();
        if (getPhase() > 3) {
            absorptionBox = getEntityBoundingBox().grow(Math.max(1.0D, width / 1.5D), 0.0D,
                    Math.max(1.0D, width / 1.5D));
        }
        List<Entity> splitClusters = new ArrayList<Entity>();
        Iterator<Map.Entry<UUID, Entity>> tracked = trackedEntities.entrySet().iterator();
        while (tracked.hasNext()) {
            Map.Entry<UUID, Entity> entry = tracked.next();
            Entity entity = entry.getValue();
            if (entity == null || entity.isDead || entity.world != world) {
                tracked.remove();
                continue;
            }
            Vec3d delta = absorptionPoint.subtract(entity.getPositionVector());
            double distance = delta.length();
            if (distance >= 320.0D) entity.setPosition(absorptionPoint.x, absorptionPoint.y, absorptionPoint.z);
            if (distance > 0.001D) {
                double speed = entity instanceof SupplementalEntities.BlockClusterEntity ? 0.5D : 0.5D;
                Vec3d velocity = delta.normalize().scale(speed);
                entity.motionX = velocity.x;
                entity.motionY = velocity.y;
                entity.motionZ = velocity.z;
                entity.velocityChanged = true;
            }
            if (entity instanceof SupplementalEntities.BlockClusterEntity) {
                SupplementalEntities.BlockClusterEntity cluster = (SupplementalEntities.BlockClusterEntity) entity;
                if (cluster.shouldCrumble() && cluster.getShakeTime() <= 0 && ticksExisted % 20 == 0
                        && rand.nextInt(3) == 0) {
                    SupplementalEntities.BlockClusterEntity split = cluster.splitAt(EnumFacing.Axis.values()[rand.nextInt(3)]);
                    if (split != null) splitClusters.add(split);
                }
            }
            if (absorptionBox.contains(entity.getPositionVector())) {
                consumeTrackedEntity(entity);
                tracked.remove();
            }
        }
        for (Entity split : splitClusters) trackEntityToConsume(split);
    }

    private void convertFallingBlocks() {
        if (!WitherStormConfig.convertFallingBlocks) return;
        List<EntityFallingBlock> fallingBlocks = world.getEntitiesWithinAABB(EntityFallingBlock.class,
                getSearchBox(), falling -> !(falling instanceof SupplementalEntities.BlockClusterEntity)
                        && !falling.isDead && world.isBlockLoaded(falling.getPosition())
                        && (isInOpenArea(falling) || canSee(0, falling)));
        for (EntityFallingBlock falling : fallingBlocks) {
            SupplementalEntities.BlockClusterEntity cluster = new SupplementalEntities.BlockClusterEntity(world,
                    falling.posX, falling.posY, falling.posZ, falling.getBlock());
            cluster.setRotationDelta(rand.nextInt(20) * 0.05F, rand.nextInt(20) * 0.05F);
            cluster.setNoGravity(true);
            cluster.setPhysics(false);
            cluster.setCreatedFromFallingBlock(true);
            falling.setDead();
            if (world.spawnEntity(cluster)) trackEntityToConsume(cluster);
        }
    }

    private boolean isInOpenArea(Entity entity) {
        int lowestHeight = Integer.MAX_VALUE;
        BlockPos center = entity.getPosition();
        for (int offsetX = -5; offsetX < 5; offsetX++) {
            for (int offsetZ = -5; offsetZ < 5; offsetZ++) {
                int height = world.getHeight(center.add(offsetX, 0, offsetZ)).getY();
                lowestHeight = Math.min(lowestHeight, height);
            }
        }
        return entity.posY >= lowestHeight - 10.0D;
    }

    private void consumeTrackedEntity(Entity entity) {
        if (entity instanceof SupplementalEntities.BlockClusterEntity) {
            SupplementalEntities.BlockClusterEntity cluster = (SupplementalEntities.BlockClusterEntity) entity;
            if (!cluster.shouldNotCountToConsumedMass()) {
                addConsumedMass(Math.max(1, cluster.getBlocks().size()));
            }
        } else if (entity instanceof EntityItem) {
            addConsumedMass(Math.max(1, ((EntityItem) entity).getItem().getCount()));
        } else {
            addConsumedMass(1);
        }
        if (entity instanceof EntityLivingBase) {
            captureConsumedPet((EntityLivingBase) entity);
            entity.attackEntityFrom(DamageSource.causeMobDamage(this), Float.MAX_VALUE);
        } else {
            entity.setDead();
        }
    }

    private Entity resolveAny(UUID uuid) {
        if (uuid == null) return null;
        for (Entity entity : world.loadedEntityList) {
            if (uuid.equals(entity.getUniqueID())) return entity;
        }
        return null;
    }

    private void applyTornApartCollision() {
        if (getPhase() < 7 || !isBeingTornApart() || isDeadOrPlayingDead()) return;
        AxisAlignedBB entrance = getEntityBoundingBox().grow(8.0D, 8.0D, 8.0D);
        List<EntityPlayerMP> players = world.getEntitiesWithinAABB(EntityPlayerMP.class, entrance,
                player -> !player.isDead && player.dimension == dimension && !player.capabilities.isCreativeMode
                        && player.dimension != BowelsDimensions.DIMENSION_ID);
        for (EntityPlayerMP player : players) {
            sendPlayerToBowels(player);
        }
    }

    private void sendPlayerToBowels(EntityPlayerMP player) {
        if (player == null || world.getMinecraftServer() == null
                || !pendingBowelsTransfers.add(player.getUniqueID())) return;
        world.getMinecraftServer().addScheduledTask(() -> {
            try {
                if (!player.isDead && player.dimension == dimension && !player.capabilities.isCreativeMode) {
                    BowelsManager.enter(this, player);
                }
            } finally {
                pendingBowelsTransfers.remove(player.getUniqueID());
            }
        });
    }

    private void applyTractorBeam() {
        if (isDeadOrPlayingDead()) return;
        double defaultSpeed = WitherStormConfig.tractorPullSpeedModifier;
        if (getPhase() < 4) {
            for (int head = 0; head < getTotalHeads(); head++) {
                pullInTarget(getTarget(head), defaultSpeed, head);
            }
        }

        double range = getPhase() > 3 ? 120.0D : 40.0D;
        AxisAlignedBB area = getSearchBox();
        Set<UUID> pulled = new HashSet<UUID>();
        for (int head = 0; head < 3; head++) {
            if (!tractorBeamActive(head)) continue;
            final int headIndex = head;
            Vec3d headPosition = getHeadPosition(head, 1.0F);
            Vec3d direction = headManager.getLookVector(head);
            List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, area,
                    entity -> isBasicTractorBeamCandidate(entity) && entity != this && !entity.isDead
                            && entity.dimension == dimension
                            && !(entity instanceof SupplementalEntities.StormPartBase)
                            && !(entity instanceof WitherStormEntity)
                            && !(entity instanceof PowerfulExplosiveEntity.FormidibombEntity)
                            && !isTrackedForConsumption(entity)
                            && (!(entity instanceof EntityPlayer)
                            || !((EntityPlayer) entity).capabilities.disableDamage
                            && !((EntityPlayer) entity).isSpectator())
                            && isInsideBeam(entity, headPosition, direction, range, headIndex));
            for (Entity entity : entities) {
                if (pulled.contains(entity.getUniqueID())) continue;
                boolean selectedTarget = getTarget(head) == entity;
                if (getPhase() > 3 && selectedTarget) {
                    pullInTarget(entity, defaultSpeed, head);
                    pulled.add(entity.getUniqueID());
                    continue;
                }
                if (!canPullUntargeted(entity, head)) continue;
                pullInTarget(entity, getTractorPullSpeed(entity), head);
                pulled.add(entity.getUniqueID());
            }
        }
    }

    private static boolean isBasicTractorBeamCandidate(Entity entity) {
        return entity instanceof EntityLivingBase || isTractorBeamPullableObject(entity);
    }

    private static boolean isTractorBeamPullableObject(Entity entity) {
        return entity instanceof EntityItem
                || entity instanceof EntityBoat || entity instanceof EntityMinecart;
    }

    private boolean canPullUntargeted(Entity entity, int head) {
        if (!WitherStormConfig.canPickupMobClusters
                || targetManager.shouldIgnoreTarget(entity)
                || isTargetedByMainHeadFamily(entity)
                || isTargetInUseBySegment(entity)
                || !canSee(head, entity)) {
            return false;
        }
        if (!(entity instanceof EntityLivingBase)) return isTractorBeamPullableObject(entity);
        EntityLivingBase living = (EntityLivingBase) entity;
        return isValidStormTarget(living, false) && !isBlockingWithShield(living);
    }

    private double getTractorPullSpeed(Entity entity) {
        if (isTractorBeamPullableObject(entity)) return 0.4D;
        if (entity instanceof EntityPlayer) return WitherStormConfig.tractorPullSpeedModifier;
        return WitherStormConfig.tractorPullSpeedModifier - 0.05D
                + new java.util.Random(entity.getEntityId()).nextDouble() * 0.1D;
    }

    private void tickProjectilesHittingHeads() {
        if (getPhase() <= 3) return;
        for (int head = 0; head < getTotalHeads(); head++) {
            if (!tractorBeamActive(head)) continue;
            List<Entity> projectiles = world.getEntitiesWithinAABB(Entity.class, getHeadBounds(head),
                    entity -> !entity.isDead && isHeadHittingProjectile(entity));
            for (Entity projectile : projectiles) {
                Entity owner = getProjectileOwner(projectile);
                if (owner == this) continue;
                boolean wasInjured = isHeadInjured(head);
                boolean accepted = headManager.attemptAttack(head, owner, 40);
                if (accepted && !wasInjured && isHeadInjured(head) && owner instanceof EntityPlayer) {
                    world.playSound(null, owner.posX, owner.posY, owner.posZ,
                            SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 1.0F, 1.0F);
                }
                projectile.setDead();
            }
        }
    }

    private static boolean isHeadHittingProjectile(Entity entity) {
        return entity instanceof EntityArrow
                || entity instanceof EntityThrowable
                || entity instanceof EntityFireball
                || entity instanceof EntityFishHook
                || entity instanceof EntityShulkerBullet
                || entity instanceof EntityLlamaSpit
                || entity instanceof EntityFireworkRocket;
    }

    @Nullable
    private static Entity getProjectileOwner(Entity projectile) {
        if (projectile instanceof EntityArrow) return ((EntityArrow) projectile).shootingEntity;
        if (projectile instanceof EntityThrowable) return ((EntityThrowable) projectile).getThrower();
        if (projectile instanceof EntityFireball) return ((EntityFireball) projectile).shootingEntity;
        if (projectile instanceof EntityFishHook) return ((EntityFishHook) projectile).getAngler();
        if (projectile instanceof EntityLlamaSpit) return ((EntityLlamaSpit) projectile).owner;
        return null;
    }

    /** 让风暴在 1.12 的飞行实体实现上保留上游的追逐和悬浮行为。 */
    private void updateCustomMovement() {
        if (isDeadOrPlayingDead() || getInvulnerableTicks() > 0) return;
        Vec3d target = targetManager.getUltimateTargetPos();
        if (targetManager.isDistracted() && targetManager.getDistractedPos() != null) {
            BlockPos distraction = targetManager.getDistractedPos();
            target = new Vec3d(distraction).add(0.5D, 18.0D, 0.5D);
        }
        Vec3d velocity = new Vec3d(motionX, motionY * 0.6D, motionZ);
        double desiredHeight = getDesiredFlyingHeight(target);
        if (posY < desiredHeight || !onGround && posY < desiredHeight + 5.0D) {
            velocity = new Vec3d(velocity.x, (desiredHeight - posY)
                    * (getPhase() > 3 ? 0.005D : 0.02D), velocity.z);
        } else {
            velocity = new Vec3d(velocity.x, 0.0D, velocity.z);
        }
        if (target != null) {
            double horizontalX = target.x - posX;
            double horizontalZ = target.z - posZ;
            double horizontalDistance = Math.sqrt(horizontalX * horizontalX + horizontalZ * horizontalZ);
            double minimumDistance = getPhase() > 3 ? 6000.0D : 12000.0D;
            if (horizontalDistance * horizontalDistance > minimumDistance) {
                double speed = getPhase() > 3
                        ? (targetManager.isTargetStationary() ? WitherStormConfig.chasingFlyingSpeed
                        : WitherStormConfig.normalFlyingSpeed)
                        : WitherStormConfig.normalFlyingSpeed;
                if (targetManager.isTargetStationary() && getPhase() > 3 && horizontalDistance > 122.0D) {
                    speed = Math.min(WitherStormConfig.chasingFlyingSpeed, horizontalDistance * 0.001D);
                }
                velocity = velocity.add(horizontalX / horizontalDistance * speed - velocity.x * 0.6D,
                        0.0D, horizontalZ / horizontalDistance * speed - velocity.z * 0.6D);
            }
        }
        motionX = velocity.x;
        motionY = velocity.y;
        motionZ = velocity.z;
        if (motionX * motionX + motionZ * motionZ > 0.0025D) {
            rotationYaw = (float) (MathHelper.atan2(motionZ, motionX) * 180.0D / Math.PI) - 90.0F;
            renderYawOffset = rotationYaw;
        }
    }

    private double getDesiredFlyingHeight(@Nullable Vec3d target) {
        if (getPhase() > 3) {
            if (WitherStormConfig.dynamicFlyingHeight
                    && ticksExisted - lastFlyingHeightChange >= WitherStormConfig.dynamicFlyingHeightTime * 20) {
                lastFlyingHeightChange = ticksExisted;
                currentFlyingHeight = 40.0D + rand.nextInt(41);
            } else if (currentFlyingHeight <= 0.0D) {
                currentFlyingHeight = WitherStormConfig.flyingHeight;
            }
        } else {
            currentFlyingHeight = 10.0D;
        }
        BlockPos center = new BlockPos(posX, posY, posZ);
        int radius = Math.max(1, Math.min(32, MathHelper.floor(width * 1.5F)));
        int highest = center.getY();
        for (int x = -radius; x <= radius; x += Math.max(1, radius / 4)) {
            for (int z = -radius; z <= radius; z += Math.max(1, radius / 4)) {
                highest = Math.max(highest, world.getHeight(center.add(x, 0, z)).getY());
            }
        }
        double height = highest + currentFlyingHeight;
        if (getPhase() < 4 && target != null) height = Math.max(height, target.y + 8.0D);
        return height;
    }

    private void applyMassAbsorption() {
        if (isDeadOrPlayingDead()) return;
        double radius = getPhase() > 3 ? 80.0D : Math.min(48.0D, 12.0D + getConsumedMass() * 0.00445D);
        AxisAlignedBB search = getEntityBoundingBox().grow(radius, radius, radius);
        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, search, entity -> entity != this
                && !entity.isDead && entity.dimension == dimension
                && !(entity instanceof WitherStormEntity)
                && !(entity instanceof SupplementalEntities.StormPartBase)
                && !(entity instanceof PowerfulExplosiveEntity.FormidibombEntity)
                && !(entity instanceof EntityPlayer)
                && (entity instanceof EntityItem || entity instanceof SupplementalEntities.BlockClusterEntity
                || entity instanceof net.minecraft.entity.monster.EntitySlime)
                && !trackedEntities.containsKey(entity.getUniqueID())
                && rand.nextFloat() >= 0.9F);
        for (Entity entity : entities) {
            trackEntityToConsume(entity);
        }
    }

    private void captureConsumedPet(EntityLivingBase living) {
        if (!(living instanceof EntityTameable)) return;
        EntityTameable tameable = (EntityTameable) living;
        if (!tameable.isTamed() || tameable.getOwnerId() == null || consumedPets.containsKey(tameable.getOwnerId())) return;
        NBTTagCompound saved = new NBTTagCompound();
        living.writeToNBT(saved);
        saved.removeTag("Dimension");
        saved.removeTag("Motion");
        saved.removeTag("Pos");
        saved.removeTag("Rotation");
        consumedPets.put(tameable.getOwnerId(), saved);
    }

    public void spawnConsumedPets(Vec3d position) {
        if (world.isRemote || consumedPets.isEmpty()) return;
        List<NBTTagCompound> savedPets = new java.util.ArrayList<NBTTagCompound>(consumedPets.values());
        consumedPets.clear();
        for (NBTTagCompound saved : savedPets) {
            Entity pet = EntityList.createEntityFromNBT(saved.copy(), world);
            if (pet == null) continue;
            pet.setPosition(position.x, position.y, position.z);
            if (pet instanceof EntityLivingBase) {
                EntityLivingBase living = (EntityLivingBase) pet;
                living.setHealth(living.getMaxHealth());
                living.clearActivePotions();
                living.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 200, 0, false, false));
            }
            world.spawnEntity(pet);
        }
    }

    private Vec3d getBeamDirection(int head, Vec3d headPosition) {
        EntityLivingBase target = getTarget(head);
        if (target == null) target = getAttackTarget();
        if (target != null && !target.isDead && target.dimension == dimension) {
            Vec3d delta = target.getPositionVector().add(0.0D, target.height * 0.5D, 0.0D).subtract(headPosition);
            if (delta.lengthSquared() > 0.0001D) return delta.normalize();
        }
        float yaw = (renderYawOffset + 180.0F + head * 18.0F) * 0.017453292F;
        return new Vec3d(MathHelper.cos(yaw), 0.0D, MathHelper.sin(yaw));
    }

    private boolean isInsideBeam(Entity entity, Vec3d origin, Vec3d direction, double range, int head) {
        Vec3d relative = entity.getPositionVector().subtract(origin);
        double projection = relative.dotProduct(direction);
        double cutoff = headManager.getTractorBeamCutoff(head);
        if (projection < 0.0D || projection > range || cutoff >= 0.0D && projection > cutoff) return false;
        Vec3d closest = origin.add(direction.scale(projection));
        double radius = 1.5D + projection * 0.045D;
        return entity.getDistanceSq(closest.x, closest.y, closest.z) <= radius * radius;
    }

    public boolean tractorBeamActive(int head) {
        if (isDeadOrPlayingDead() || head < 0 || head > 2) return false;
        if (getPhase() < 2) return false;
        if (isHeadInjured(head)) return false;
        if (head > 0 && areOtherHeadsDisabled()) return false;
        return getPhase() >= 4 || head == 0;
    }

    public void pullInTarget(Entity target, double speed, int head) {
        if (target == null || target == this || target.isDead || !tractorBeamActive(head)) return;
        Vec3d headPosition = getHeadPosition(head, 1.0F);
        Vec3d pullPosition = headPosition;
        if (!(target instanceof EntityPlayer) && target.getPositionVector().squareDistanceTo(headPosition) >= 25.0D) {
            Vec3d look = headManager.getLookVector(head);
            Vec3d relative = target.getPositionVector().subtract(headPosition);
            double projection = Math.max(0.0D, relative.dotProduct(look) - 5.0D);
            double cutoff = headManager.getTractorBeamCutoff(head);
            if (cutoff >= 0.0D) projection = Math.min(projection, cutoff);
            pullPosition = headPosition.add(look.scale(projection));
        }
        Vec3d delta = pullPosition.subtract(target.getPositionVector());
        if (delta.lengthSquared() > 0.0001D) {
            double scaledSpeed = speed * MathHelper.clamp(Math.sqrt(delta.lengthSquared()), 0.1D, 1.0D);
            Vec3d velocity = delta.normalize().scale(scaledSpeed);
            Entity pulled = target;
            Entity vehicle = target.getRidingEntity();
            if (vehicle != null && WitherStormConfig.shouldPickUpVehicles
                    && (!(vehicle instanceof EntityLivingBase)
                    || isStormTargetType((EntityLivingBase) vehicle))) {
                pulled = vehicle;
            }
            pulled.motionX = velocity.x;
            pulled.motionY = velocity.y;
            pulled.motionZ = velocity.z;
            pulled.velocityChanged = true;
            if (target instanceof EntityPlayerMP) {
                ModNetwork.setPlayerMotion((EntityPlayerMP) target, velocity);
            }
        }
        target.velocityChanged = true;
        if (!(target instanceof EntityPlayer)
                && target.getPositionVector().squareDistanceTo(headPosition) < 400.0D) {
            trackEntityToConsume(target);
        }
        AxisAlignedBB headBox = new AxisAlignedBB(headPosition.x - 2.0D, headPosition.y - 4.0D, headPosition.z - 2.0D,
                headPosition.x + 2.0D, headPosition.y + 2.0D, headPosition.z + 2.0D);
        if (!headBox.intersects(target.getEntityBoundingBox())) return;
        if (isBeingTornApart() && target instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) target;
            if (!player.capabilities.isCreativeMode && player.dimension != BowelsDimensions.DIMENSION_ID) {
                sendPlayerToBowels(player);
            }
            return;
        }
        if (target instanceof EntityPlayer) {
            EntityLivingBase living = (EntityLivingBase) target;
            living.attackEntityFrom(DamageSource.causeMobDamage(this),
                    (float) getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
            setHeadFlag(1 << (3 + MathHelper.clamp(head, 0, 2)), true);
        } else if (target instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase) target;
            captureConsumedPet(living);
            addConsumedMass(1);
            living.attackEntityFrom(DamageSource.causeMobDamage(this), Float.MAX_VALUE);
            setHeadFlag(1 << (3 + MathHelper.clamp(head, 0, 2)), true);
            headManager.delayAfterChomp(head);
        }
    }

    public int getClusterRadius() {
        return (int) Math.max(1.0F, getPhase() * 0.75F);
    }

    public void removeFluidFromLook(float pitch, float yaw, int head) {
        Vec3d start = getHeadPosition(head, 1.0F);
        Vec3d direction = headManager.getLookVector(head);
        removeFluidFromRay(start, direction);
    }

    void removeFluidFromRay(Vec3d start, Vec3d direction) {
        if (world.isRemote || getPhase() <= 3 || !WitherStormConfig.tractorBeamsRemoveFluids
                || !world.getGameRules().getBoolean("mobGriefing")) return;
        RayTraceResult result = world.rayTraceBlocks(start, start.add(direction.scale(200.0D)), false, true, false);
        if (result == null || result.typeOfHit != RayTraceResult.Type.BLOCK || result.getBlockPos().getY()
                <= WitherStormConfig.tractorBeamFluidRemovalHeight) return;
        BlockPos hit = result.getBlockPos();
        for (int offsetX = -6; offsetX <= 6; offsetX++) {
            for (int offsetY = -6; offsetY <= 6; offsetY++) {
                for (int offsetZ = -6; offsetZ <= 6; offsetZ++) {
                    BlockPos position = hit.add(offsetX, offsetY, offsetZ);
                    IBlockState state = world.getBlockState(position);
                    if (state.getMaterial().isLiquid()) world.setBlockToAir(position);
                }
            }
        }
    }

    public void createClusterFromLook(float pitch, float yaw, int time, int head) {
        SupplementalEntities.BlockClusterEntity cluster = createTractorBeamCluster(
                getHeadPosition(head, 1.0F), headManager.getLookVector(head), time, head);
        if (cluster != null) trackEntityToConsume(cluster);
    }

    @Nullable
    SupplementalEntities.BlockClusterEntity createTractorBeamCluster(Vec3d start, Vec3d direction,
                                                                int time, int head) {
        if (world.isRemote || !WitherStormConfig.tractorBeamClusterPickUp
                || !world.getGameRules().getBoolean("mobGriefing")) return null;
        RayTraceResult result = world.rayTraceBlocks(start, start.add(direction.scale(200.0D)), false, true, false);
        if (result == null || result.typeOfHit != RayTraceResult.Type.BLOCK
                || !world.isBlockLoaded(result.getBlockPos())) return null;
        BlockPos hit = result.getBlockPos();
        for (int attempt = 0; attempt < 512; attempt++) {
            double offsetScale = getPhase() <= 3 ? 1.0D : 2.25D;
            BlockPos candidate = hit.add((int) Math.round(rand.nextGaussian() * offsetScale),
                    (int) Math.round(rand.nextGaussian() * offsetScale),
                    (int) Math.round(rand.nextGaussian() * offsetScale));
            IBlockState candidateState = world.getBlockState(candidate);
            if (candidateState.getBlock() == Blocks.AIR
                    || !isBlockExposed(candidate)
                    || UpstreamBlockTags.contains(UpstreamBlockTags.WITHER_STORM_BLOCK_BLACKLIST, candidateState)) continue;
            if (rand.nextDouble() > 0.999D && (candidateState.getBlock() == Blocks.STONE
                    || UpstreamBlockTags.contains("minecraft:dirt", candidateState)
                    || UpstreamBlockTags.contains("minecraft:sand", candidateState))) continue;
            float clusterRadius = getTractorBeamClusterRadius();
            SupplementalEntities.BlockClusterEntity cluster = new SupplementalEntities.BlockClusterEntity(world);
            cluster.populateWithRadius(candidate, clusterRadius,
                    (level, position, state) -> state.getBlock() != Blocks.AIR
                            && state.getBlock() != Blocks.BEDROCK && state.getBlock() != Blocks.BARRIER
                            && state.getBlock() != Blocks.COMMAND_BLOCK
                            && state.getBlock() != Blocks.CHAIN_COMMAND_BLOCK
                            && state.getBlock() != Blocks.REPEATING_COMMAND_BLOCK
                            && !UpstreamBlockTags.contains(UpstreamBlockTags.WITHER_STORM_BLOCK_BLACKLIST, state));
            if (cluster.getBlocks().isEmpty()) continue;
            cluster.setTime(time);
            cluster.setCreatedFromTractorBeam(true);
            cluster.setHeadCreatedFrom(head);
            cluster.setTractorBeamDistanceThreshold(rand.nextDouble() * 5.0D);
            cluster.setRotationDelta((rand.nextInt(120) - 60) * 0.05F / (clusterRadius * 3.0F),
                    (rand.nextInt(120) - 60) * 0.05F / (clusterRadius * 3.0F));
            cluster.setNoGravity(true);
            cluster.setPhysics(false);
            if (world.spawnEntity(cluster)) return cluster;
            return null;
        }
        return null;
    }

    private float getTractorBeamClusterRadius() {
        switch (getPhase()) {
            case 4:
                return MathHelper.clamp((float) (1.0D + 0.125D * rand.nextGaussian()), 1.0F, 1.5F);
            case 5:
                return MathHelper.clamp((float) (1.0D + 0.5D * rand.nextGaussian()), 1.0F, 3.0F);
            case 6:
                return MathHelper.clamp((float) (1.0D + 0.75D * rand.nextGaussian()), 1.0F, 4.5F);
            case 7:
                return MathHelper.clamp((float) (1.5D + 1.25D * rand.nextGaussian()), 1.0F, 8.0F);
            default:
                return 1.0F;
        }
    }

    private boolean isBlockExposed(BlockPos position) {
        IBlockState state = world.getBlockState(position);
        if (state.getBlock() == Blocks.AIR || state.getMaterial().isLiquid()) return false;
        for (EnumFacing facing : EnumFacing.values()) {
            IBlockState neighbor = world.getBlockState(position.offset(facing));
            if (neighbor.getBlock() == Blocks.AIR || neighbor.getMaterial().isLiquid()) return true;
        }
        return false;
    }

    public int getPhase() {
        return MathHelper.clamp(dataManager.get(PHASE), 0, 7);
    }

    public int getConsumedMass() {
        return dataManager.get(CONSUMED_MASS);
    }

    public void addConsumedMass(int amount) {
        if (amount > 0) {
            dataManager.set(CONSUMED_MASS, (int) Math.min(Integer.MAX_VALUE, (long) getConsumedMass() + amount));
            if (getPhase() == 6 && getConsumedMass() > getSubPhaseRequirement(6)) dataManager.set(OTHER_HEADS_DISABLED, false);
        }
    }

    public PlayDeadState getPlayDeadState() {
        return PlayDeadState.values()[MathHelper.clamp(dataManager.get(PLAY_DEAD_STATE), 0, PlayDeadState.values().length - 1)];
    }

    private static boolean disablesAi(PlayDeadState state) {
        return state == PlayDeadState.FALLING || state == PlayDeadState.PLAYING_DEAD;
    }

    public boolean isPlayDeadAiDisabled() { return disablesAi(getPlayDeadState()); }
    public int getInvulnerableTicks() { return dataManager.get(INVULNERABLE_TICKS); }
    public int getStartingInvulnerableTicks() { return dataManager.get(STARTING_INVULNERABLE_TICKS); }
    public boolean shouldShowHole() { return dataManager.get(SHOULD_SHOW_HOLE); }
    public BlockPos getPlayingDeadPodiumPosition() { return podiumPosition; }
    public boolean isOnBack() { return bodyXRotation >= 90.0F; }
    public float getBodyXRotation(float partialTicks) {
        return previousBodyXRotation + (bodyXRotation - previousBodyXRotation) * partialTicks;
    }
    public boolean areOtherHeadsDisabled() { return dataManager.get(OTHER_HEADS_DISABLED); }
    int getHeadAnimationFlags() { return dataManager.get(HEAD_ANIMATION_FLAGS); }
    boolean isHeadFlagSet(int bit) { return (getHeadAnimationFlags() & bit) != 0; }
    void setHeadFlag(int bit, boolean value) {
        int flags = getHeadAnimationFlags();
        dataManager.set(HEAD_ANIMATION_FLAGS, value ? flags | bit : flags & ~bit);
    }
    public boolean isDeadOrPlayingDead() { return isDead || getHealth() <= 0.0F || isPlayDeadAiDisabled(); }
    public boolean hasRecentlyBeenRevived() { return recentlyRevivedTicks > 0; }
    public boolean isResummoned() { return resummoned; }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        super.notifyDataManagerChange(key);
        if (BODY_X_ROTATION.equals(key) && world.isRemote) {
            clientBodyXRotationTarget = dataManager.get(BODY_X_ROTATION);
            clientBodyXRotationSteps = 3;
        }
        if (PHASE.equals(key) || PLAY_DEAD_STATE.equals(key)) updateSizeForPlayDeadState();
    }

    /** 返回与上游搜索范围对应的 1.12 轴对齐搜索盒。 */
    public AxisAlignedBB getSearchBox() {
        double range = getPhase() > 3 ? 256.0D : 96.0D;
        return getEntityBoundingBox().grow(range, getPhase() > 3 ? range + 255.0D : range * 2.0D, range);
    }

    public boolean isAttractingFormidibomb() {
        if (getPhase() < 5 || isDeadOrPlayingDead()) return false;
        for (PowerfulExplosiveEntity.FormidibombEntity bomb : world.getEntitiesWithinAABB(
                PowerfulExplosiveEntity.FormidibombEntity.class, getSearchBox().grow(32.0D))) {
            if (!bomb.isDead && bomb.getFuse() > 0) return true;
        }
        return false;
    }

    public boolean isNearbyTickingFormidibomb() {
        if (getPhase() < 5) return false;
        for (PowerfulExplosiveEntity.FormidibombEntity bomb : world.getEntitiesWithinAABB(
                PowerfulExplosiveEntity.FormidibombEntity.class, getSearchBox().grow(32.0D))) {
            if (!bomb.isDead && bomb.getFuse() > 0 && bomb.getFuse() <= 800) return true;
        }
        return false;
    }

    @javax.annotation.Nullable
    public SupplementalEntities.CommandBlockEntity getBowelsCommandBlock() {
        Entity local = resolve(commandBlockUuid);
        if (local instanceof SupplementalEntities.CommandBlockEntity && !local.isDead) {
            return (SupplementalEntities.CommandBlockEntity) local;
        }
        if (world.getMinecraftServer() == null) return null;
        WorldServer bowels = world.getMinecraftServer().getWorld(BowelsDimensions.DIMENSION_ID);
        if (bowels == null) return null;
        BowelsInstanceData.Instance instance = BowelsInstanceData.get(bowels).get(getUniqueID());
        if (instance == null || instance.commandBlockUuid == null) return null;
        Entity entity = bowels.getEntityFromUuid(instance.commandBlockUuid);
        return entity instanceof SupplementalEntities.CommandBlockEntity
                ? (SupplementalEntities.CommandBlockEntity) entity : null;
    }
    void playHeadRoarSound(int head) {
        if (!shouldPlayHeadVoice(head)) return;
        Vec3d p = getHeadPosition(head, 1.0F);
        world.playSound(null, p.x, p.y, p.z, ModSounds.get("wither_storm_roar"), SoundCategory.HOSTILE,
                Math.max(6.0F, width + 2.5F), 1.0F);
    }
    void playHeadBiteSound(int head) {
        Vec3d p = getHeadPosition(head, 1.0F);
        world.playSound(null, p.x, p.y, p.z, ModSounds.get("wither_storm_bite"), SoundCategory.HOSTILE,
                Math.max(2.0F, width), 1.0F);
    }
    void playHeadHurtSound(int head) {
        if (!shouldPlayHeadVoice(head)) return;
        Vec3d p = getHeadPosition(head, 1.0F);
        world.playSound(null, p.x, p.y, p.z, ModSounds.get("wither_storm_hurt"), SoundCategory.HOSTILE,
                Math.max(6.0F, width + 2.5F), 1.0F);
    }
    void playHeadTractorBeamActivationSound(int head) {
        if (!shouldPlayHeadVoice(head)) return;
        Vec3d p = getHeadPosition(head, 1.0F);
        world.playSound(null, p.x, p.y, p.z, ModSounds.get("wither_storm_tractor_beam_activate"),
                SoundCategory.HOSTILE, Math.max(2.5F, width + 2.5F), 1.0F);
    }
    private boolean shouldPlayHeadVoice(int head) {
        if (head < 0 || head >= getTotalHeads()) return false;
        if (areOtherHeadsDisabled() || getPhase() > 1 && getPhase() < 4) return head == 0;
        return getPhase() > 3;
    }

    public void performRangedAttack(int head, EntityLivingBase target) {
        if (target == null) return;
        performRangedAttack(head, target.posX, target.posY + target.getEyeHeight() * 0.5D, target.posZ,
                head == 0 && rand.nextFloat() < 0.001F);
    }

    public void performRangedAttack(int head, double x, double y, double z, boolean dangerous) {
        Vec3d origin = getHeadPosition(head, 1.0F);
        world.playEvent(null, 1024, new BlockPos(origin), 0);
        EntityWitherSkull skull = new EntityWitherSkull(world, this, x - origin.x, y - origin.y, z - origin.z);
        skull.setInvulnerable(dangerous);
        skull.setPosition(origin.x, origin.y, origin.z);
        world.spawnEntity(skull);
    }

    public void spawnFlamingWitherSkull(int head, double x, double y, double z) {
        Vec3d origin = getHeadPosition(head, 1.0F);
        world.playSound(null, origin.x, origin.y, origin.z, ModSounds.get("wither_storm_shoot"), SoundCategory.HOSTILE,
                Math.max(5.0F, width), 1.0F);
        SupplementalEntities.FlamingWitherSkullEntity skull = new SupplementalEntities.FlamingWitherSkullEntity(world, this,
                (x - origin.x) * 0.75D, (y - origin.y) * 0.75D, (z - origin.z) * 0.75D);
        skull.setPosition(origin.x, origin.y, origin.z);
        world.spawnEntity(skull);
    }

    public void spawnBlueFlamingWitherSkull(int head, double x, double y, double z) {
        Vec3d origin = getHeadPosition(head, 1.0F);
        world.playSound(null, origin.x, origin.y, origin.z, ModSounds.get("wither_storm_shoot"), SoundCategory.HOSTILE,
                Math.max(5.0F, width), 1.0F);
        SupplementalEntities.BlueFlamingWitherSkullEntity skull = new SupplementalEntities.BlueFlamingWitherSkullEntity(world, this,
                (x - origin.x) * 0.75D, (y - origin.y) * 0.75D, (z - origin.z) * 0.75D);
        skull.setPosition(origin.x, origin.y, origin.z);
        world.spawnEntity(skull);
    }
    public int getWatchedTargetId(int head) {
        return dataManager.get(getHeadTargetParameter(head));
    }
    public void updateWatchedTargetId(int head, int targetId) {
        dataManager.set(getHeadTargetParameter(head), Math.max(0, targetId));
    }
    private static DataParameter<Integer> getHeadTargetParameter(int head) {
        switch (MathHelper.clamp(head, 0, 2)) {
            case 1: return SECOND_HEAD_TARGET;
            case 2: return THIRD_HEAD_TARGET;
            default: return FIRST_HEAD_TARGET;
        }
    }
    boolean isHeadInjuryFlagSet(int head) {
        return (dataManager.get(HEAD_INJURY_FLAGS) & 1 << MathHelper.clamp(head, 0, 2)) != 0;
    }
    void setHeadInjuryFlag(int head, boolean injured) {
        int bit = 1 << MathHelper.clamp(head, 0, 2);
        int flags = dataManager.get(HEAD_INJURY_FLAGS);
        dataManager.set(HEAD_INJURY_FLAGS, injured ? flags | bit : flags & ~bit);
    }
    public Vec3d getHeadPosition(int head, float partialTicks) { return headManager.getPosition(head, partialTicks); }
    public int getTotalHeads() { return 3; }
    public AxisAlignedBB getHeadBounds(int head) { return headManager.getBounds(head); }
    public EntityLivingBase getTarget(int head) { return headManager.getTarget(head); }
    public void setTarget(int head, EntityLivingBase target) { headManager.setTarget(head, target); }
    public boolean isHeadInjured(int head) { return headManager.isHeadInjured(head); }
    public int getHeadInjuryTicks(int head) { return headManager.getHeadInjuryTicks(head); }
    public boolean attackHead(int head, Entity attacker) { return headManager.attemptAttack(head, attacker, 20); }
    public boolean attackHeadFromExplosion(int head, Entity attacker) {
        return headManager.attackFromExplosion(head, attacker);
    }
    public boolean canPlayerReachHead(EntityPlayer player, int head, double reach) {
        if (player == null || player.world != world || player.isDead || player.isSpectator()
                || head < 0 || head >= getTotalHeads() || reach <= 0.0D) return false;
        Vec3d eye = player.getPositionEyes(1.0F);
        Vec3d end = eye.add(player.getLook(1.0F).scale(reach));
        AxisAlignedBB bounds = getHeadBounds(head);
        return bounds.contains(eye) || bounds.calculateIntercept(eye, end) != null;
    }
    boolean isTrackedForConsumption(Entity entity) {
        return entity != null && trackedEntities.containsKey(entity.getUniqueID());
    }
    void trackEntityFromSegment(Entity entity) { trackEntityToConsume(entity); }
    @Nullable
    SupplementalEntities.BlockClusterEntity createDefaultClusterForSegment(
            SupplementalEntities.WitherStormSegmentEntity segment) {
        return clusterManager.createDefaultClusterForSegment(segment);
    }
    void consumeEntityFromSegment(Entity entity) {
        if (entity instanceof EntityLivingBase) {
            captureConsumedPet((EntityLivingBase) entity);
            entity.attackEntityFrom(DamageSource.causeMobDamage(this), Float.MAX_VALUE);
        } else if (entity != null) {
            entity.setDead();
        }
    }
    void pullPlayerIntoBowels(EntityPlayerMP player) { sendPlayerToBowels(player); }
    boolean isTargetedByMainHeadFamily(Entity entity) {
        if (entity == null) return false;
        for (int head = 0; head < getTotalHeads(); head++) {
            if (getTarget(head) == entity) return true;
        }
        return false;
    }
    boolean isTargetInUseBySegment(Entity entity) {
        if (entity == null) return false;
        for (UUID segmentUuid : segmentUuids) {
            Entity resolved = resolveAny(segmentUuid);
            if (resolved instanceof SupplementalEntities.WitherStormSegmentEntity
                    && ((SupplementalEntities.WitherStormSegmentEntity) resolved).isTargeting(entity)) {
                return true;
            }
        }
        return false;
    }
    boolean isValidStormTarget(@Nullable EntityLivingBase entity, boolean allowInvisible) {
        if (!isStormTargetType(entity)
                || entity == null || entity == this || !entity.isEntityAlive()
                || entity.world != world || entity.dimension != dimension
                || !allowInvisible && getPhase() > 3 && entity.isInvisible()
                || getPhase() <= 3 && entity instanceof EntitySquid) {
            return false;
        }
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            return !player.capabilities.disableDamage && !player.isSpectator()
                    && !hasRecentlyBeenRevived()
                    && !targetManager.shouldIgnoreTarget(player)
                    && !SymbiontSummoningManager.shouldIgnorePlayer(player);
        }
        return true;
    }
    private boolean isStormTargetType(@Nullable EntityLivingBase entity) {
        return entity != null
                && !(entity instanceof WitherStormEntity)
                && !(entity instanceof SupplementalEntities.StormPartBase)
                && !(entity instanceof SickenedMobEntity)
                && !(entity instanceof EntityWither)
                && !(entity instanceof EntityFlying)
                && !(entity instanceof EntityDragon)
                && !(entity instanceof EntityAmbientCreature)
                && !(entity instanceof EntityArmorStand)
                && !UpstreamEntityTags.contains(
                        UpstreamEntityTags.WITHER_STORM_TARGETING_BLACKLIST, entity);
    }
    boolean isBlockingWithShield(EntityLivingBase entity) {
        return entity instanceof EntityPlayer
                && ((EntityPlayer) entity).isHandActive()
                && ((EntityPlayer) entity).getActiveItemStack().getItem() == Items.SHIELD;
    }
    boolean isEntityBehindBack(Entity entity) {
        if (entity == null) return false;
        float angle = (float) (MathHelper.atan2(entity.posX - posX, entity.posZ - posZ)
                * 180.0D / Math.PI);
        float difference = MathHelper.wrapDegrees(-renderYawOffset - angle + 180.0F);
        return difference > 80.0F || difference < -80.0F;
    }
    boolean isInsideOtherTractorBeam(Entity entity, int excludedHead) {
        if (entity == null) return false;
        double range = getPhase() > 3 ? 320.0D : 12.0D + getConsumedMass() * 0.00445D;
        for (int head = 0; head < getTotalHeads(); head++) {
            if (head == excludedHead || !tractorBeamActive(head)) continue;
            Vec3d origin = getHeadPosition(head, 1.0F);
            if (isInsideBeam(entity, origin, headManager.getLookVector(head), range, head)) return true;
        }
        return false;
    }
    public UltimateTargetManager getUltimateTargetManager() { return targetManager; }
    public EntityLivingBase getUltimateTarget() { return targetManager.getUltimateTarget(); }
    public Vec3d getUltimateTargetPos() { return targetManager.getUltimateTargetPos(); }
    public boolean isUltimateTargetStationary() { return targetManager.isTargetStationary(); }
    public boolean isDistracted() { return targetManager.isDistracted(); }
    public boolean shouldTrackUltimateTarget() { return !isDeadOrPlayingDead(); }
    public boolean shouldRotateTowardsUltimateTarget() { return true; }
    public boolean canSee(int head, Entity entity) {
        if (entity == null || entity.world != world) return false;
        Vec3d start = getHeadPosition(head, 1.0F);
        Vec3d end = entity.getPositionEyes(1.0F);
        net.minecraft.util.math.RayTraceResult hit = world.rayTraceBlocks(start, end, false, true, false);
        return hit == null || hit.typeOfHit == net.minecraft.util.math.RayTraceResult.Type.MISS;
    }

    public float getHeadYRotation(int head) {
        return headManager.getYaw(head + 1, 1.0F);
    }

    public float getHeadXRotation(int head) {
        return headManager.getPitch(head + 1, 1.0F);
    }
    public boolean isArmored() { return getHealth() <= getMaxHealth() / 2.0F; }
    public float getMouthAnimation(int head, float partialTicks) {
        return headManager.getMouth(head, partialTicks);
    }
    public float getBrokenJawAnimation(int head, float partialTicks) {
        return headManager.getBrokenRoll(head, partialTicks);
    }
    public float getHeadShakeAnimation(int head, float partialTicks) {
        return headManager.getShakeRoll(head, partialTicks);
    }
    public boolean isBeingTornApart() {
        return isBeingTornApart(getPhase(), getConsumedMass(), getConsumptionAmountForPhase(7),
                shouldShowHole(), dataManager.get(HOLE_ENABLED));
    }
    static boolean isBeingTornApart(int phase, int consumedMass, int phaseRequirement,
                                    boolean explicitlyShown, boolean holeEnabled) {
        return holeEnabled && phase >= 7 && (consumedMass >= phaseRequirement || explicitlyShown);
    }
    public void setShouldShowHole(boolean value) { dataManager.set(SHOULD_SHOW_HOLE, value); }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        // 1.12.2 没有伤害类型标签，允许伤害创造模式是绕过实体无敌的对应语义。
        if (source.canHarmInCreative()) return super.attackEntityFrom(source, amount);
        Entity attacker = source.getTrueSource();
        if (isEntityInvulnerable(source)
                || source == DamageSource.DROWN
                || attacker instanceof WitherStormEntity
                || attacker instanceof SickenedMobEntity) {
            return false;
        }
        if (isPlayDeadAiDisabled() || getInvulnerableTicks() > 0
                || WitherStormConfig.witherStormInvulnerability && getPhase() > 3) {
            return false;
        }
        if (!world.isRemote) {
            if (destroyBlocksTick <= 0) destroyBlocksTick = 20;
            headManager.onHurt();
        }
        return super.attackEntityFrom(source, amount);
    }

    @Override
    protected void damageEntity(DamageSource source, float amount) {
        super.damageEntity(source, amount);
        if (getHealth() <= 0.0F) evolveFromNearDeath(source);
    }

    private boolean evolveFromNearDeath(DamageSource source) {
        if (world.isRemote || !WitherStormConfig.witherStormInvulnerability
                || getPhase() >= 4) return false;
        setPhase(4);
        setHealth(getMaxHealth());
        accelerateAfterEvolution();
        ModNetwork.playGlobalSound(world, ModSounds.get("wither_storm_evolves"), 1.0F, 1.0F);
        if (source.getTrueSource() instanceof EntityPlayerMP) {
            ModCriteriaTriggers.NEARLY_KILL_WITHER_STORM.trigger(
                    (EntityPlayerMP) source.getTrueSource(), this);
        }
        return true;
    }

    private void triggerNearby(com.wdcftgg.witherstormmod.common.advancement.EntityTrigger trigger,
                               double range) {
        for (EntityPlayerMP player : world.getEntitiesWithinAABB(EntityPlayerMP.class,
                getEntityBoundingBox().grow(range))) {
            trigger.trigger(player, this);
        }
    }

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        if (!world.isRemote) {
            headManager.onDeath();
            clearTrackedEntities(false);
        }
    }

    @Override
    protected void onDeathUpdate() {
        if (getPhase() <= 3) {
            super.onDeathUpdate();
            if (!world.isRemote && isDead && !deathRewardsReleased) releaseConsumedPetsAndCureSickened();
            return;
        }
        ++witherStormDeathTime;
        if (!world.isRemote) {
            if (getPhase() > 5 && witherStormDeathTime < 240 && world.getGameRules().getBoolean("mobGriefing")) {
                dropDeathClusters();
            }
            if (witherStormDeathTime >= 360) {
                releaseConsumedPetsAndCureSickened();
                setDead();
            }
        }
        legacyBossInfo.setPercent(MathHelper.clamp(1.0F - witherStormDeathTime / 360.0F, 0.0F, 1.0F));
    }

    private void dropDeathClusters() {
        int interval = Math.max(1, 240 / Math.max(1, getPhase()));
        if (witherStormDeathTime % interval == 0) spawnDeathCluster(Math.max(1, getPhase() - 2));
        if (witherStormDeathTime % 5 == 0) spawnDeathCluster(2);
        if (witherStormDeathTime > 5 && witherStormDeathTime % 2 == 0) {
            for (int index = 0; index < 3; index++) spawnDeathCluster(1);
        }
    }

    private void spawnDeathCluster(int radius) {
        SupplementalEntities.BlockClusterEntity cluster = new SupplementalEntities.BlockClusterEntity(world);
        String[] names = {"tainted_flesh_block", "tainted_dirt", "tainted_stone", "tainted_cobblestone", "tainted_planks"};
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + y * y + z * z > radius * radius) continue;
                    Block block = rand.nextFloat() < 0.025F
                            ? ModBlocks.get("withered_phlegm_block")
                            : ModBlocks.get(names[rand.nextInt(names.length)]);
                    if (block != null) cluster.addBlock(new BlockPos(x, y, z), block.getDefaultState());
                }
            }
        }
        if (cluster.getBlocks().isEmpty()) return;
        cluster.setPosition(posX + rand.nextGaussian() * 5.0D,
                posY + height * 0.5D + rand.nextGaussian() * 5.0D,
                posZ + rand.nextGaussian() * 5.0D);
        cluster.setSink(-1);
        cluster.motionX = rand.nextGaussian() * 0.4D;
        cluster.motionY = rand.nextGaussian() * 0.3D;
        cluster.motionZ = rand.nextGaussian() * 0.4D;
        cluster.setRotationDelta(rand.nextInt(90) * 0.15F, rand.nextInt(90) * 0.15F);
        world.spawnEntity(cluster);
    }

    private void clearTrackedEntities(boolean destroyClusters) {
        for (UUID uuid : savedTrackedEntities) {
            Entity entity = resolveAny(uuid);
            if (entity != null && !entity.isDead) trackedEntities.put(uuid, entity);
        }
        for (Entity entity : trackedEntities.values()) {
            if (entity == null || entity.isDead) continue;
            if (entity instanceof SupplementalEntities.BlockClusterEntity) {
                SupplementalEntities.BlockClusterEntity cluster = (SupplementalEntities.BlockClusterEntity) entity;
                if (destroyClusters) cluster.setDead();
                else {
                    cluster.setNoGravity(false);
                    cluster.setPhysics(true);
                }
            }
        }
        trackedEntities.clear();
        savedTrackedEntities.clear();
    }

    public void finishBowelsDeath() {
        if (!world.isRemote) releaseConsumedPetsAndCureSickened();
        setHealth(0.0F);
        setDead();
    }

    private void releaseConsumedPetsAndCureSickened() {
        if (deathRewardsReleased) return;
        deathRewardsReleased = true;
        BlockPos surface = world.getHeight(new BlockPos(this));
        spawnConsumedPets(new Vec3d(surface.getX() + 0.5D, surface.getY() + 1.0D, surface.getZ() + 0.5D));
        for (SickenedMobEntity sickened : world.getEntitiesWithinAABB(SickenedMobEntity.class, getSearchBox())) {
            if (!sickened.isDead) TaintingManager.cureEntity(sickened);
        }
        world.playSound(null, getPosition(), ModSounds.get("wither_storm_death"), SoundCategory.HOSTILE, 20.0F, 1.0F);
    }

    @Override
    public void setDead() {
        if (!world.isRemote && getHealth() <= 0.0F && !deathRewardsReleased) {
            releaseConsumedPetsAndCureSickened();
        }
        if (!world.isRemote) ChunkLoadingManager.INSTANCE.releaseEntity(world, "storm", getUniqueID());
        pendingBowelsTransfers.clear();
        removeAttached(segmentUuids, true);
        clearTrackedEntities(false);
        removeCommandBlockCore();
        super.setDead();
    }

    /** 父类私有 Boss 条无法在倒地时隐藏，因此由移植实体独占玩家跟踪。 */
    @Override
    public void addTrackingPlayer(EntityPlayerMP player) {
        legacyBossInfo.addPlayer(player);
    }

    @Override
    public void removeTrackingPlayer(EntityPlayerMP player) {
        legacyBossInfo.removePlayer(player);
    }

    @Override
    public void setCustomNameTag(String name) {
        super.setCustomNameTag(name);
        legacyBossInfo.setName(getDisplayName());
    }

    public float getRenderScale() {
        return PHASE_SCALE[getPhase()];
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("WitherStormPhase", getPhase());
        compound.setInteger("WitherStormConsumedMass", getConsumedMass());
        compound.setInteger("WitherStormPlayDeadState", getPlayDeadState().ordinal());
        compound.setInteger("WitherStormStateTicks", stateTicks);
        compound.setInteger("WitherStormMissingCoreTicks", missingCommandBlockTicks);
        compound.setInteger("WitherStormRecentlyRevivedTicks", recentlyRevivedTicks);
        compound.setBoolean("WitherStormResummoned", resummoned);
        compound.setDouble(EVOLUTION_SPEED_NBT_KEY, getEvolutionSpeedModifier());
        compound.setInteger("WitherStormInvulnerableTicks", getInvulnerableTicks());
        compound.setInteger("WitherStormStartingInvulnerableTicks", getStartingInvulnerableTicks());
        compound.setBoolean("WitherStormShouldShowHole", shouldShowHole());
        compound.setBoolean("WitherStormOtherHeadsDisabled", areOtherHeadsDisabled());
        compound.setInteger("WitherStormHeadAnimationFlags", getHeadAnimationFlags());
        compound.setFloat("WitherStormBodyXRotation", bodyXRotation);
        if (podiumPosition != null) compound.setLong("WitherStormPodiumPosition", podiumPosition.toLong());
        compound.setBoolean("WitherStormPodiumPlaced", podiumPlaced);
        NBTTagList pets = new NBTTagList();
        for (NBTTagCompound pet : consumedPets.values()) pets.appendTag(pet.copy());
        compound.setTag("WitherStormConsumedPets", pets);
        writeUuid(compound, "WitherStormCommandBlock", commandBlockUuid);
        for (int i = 0; i < segmentUuids.length; i++) writeUuid(compound, "WitherStormSegment" + i, segmentUuids[i]);
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
        compound.setTag("WitherStormTrackedEntities", tracked);
        compound.setInteger("WitherStormDeathTime", witherStormDeathTime);
        compound.setBoolean("WitherStormDeathRewardsReleased", deathRewardsReleased);
        headManager.writeToNBT(compound);
        NBTTagCompound targetData = new NBTTagCompound();
        targetManager.save(targetData);
        compound.setTag("WitherStormUltimateTarget", targetData);
        NBTTagCompound summoningData = new NBTTagCompound();
        summoningManager.writeToNBT(summoningData);
        compound.setTag("WitherStormSymbiontSummoning", summoningData);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        resummoned = compound.getBoolean("WitherStormResummoned");
        dataManager.set(EVOLUTION_SPEED_MODIFIER,
                (float) readEvolutionSpeedModifier(compound, resummoned));
        setPhase(MathHelper.clamp(compound.getInteger("WitherStormPhase"), 0, 7), Math.max(0, compound.getInteger("WitherStormConsumedMass")));
        lastConsumedMass = getConsumedMass();
        dataManager.set(PLAY_DEAD_STATE, MathHelper.clamp(compound.getInteger("WitherStormPlayDeadState"), 0, PlayDeadState.values().length - 1));
        stateTicks = Math.max(0, compound.getInteger("WitherStormStateTicks"));
        missingCommandBlockTicks = Math.max(0, compound.getInteger("WitherStormMissingCoreTicks"));
        recentlyRevivedTicks = Math.max(0, compound.getInteger("WitherStormRecentlyRevivedTicks"));
        dataManager.set(INVULNERABLE_TICKS, Math.max(0, compound.getInteger("WitherStormInvulnerableTicks")));
        dataManager.set(STARTING_INVULNERABLE_TICKS,
                Math.max(1, compound.hasKey("WitherStormStartingInvulnerableTicks", 3)
                        ? compound.getInteger("WitherStormStartingInvulnerableTicks")
                        : WitherStormConfig.invulnerabilityTime * 20));
        dataManager.set(SHOULD_SHOW_HOLE, compound.getBoolean("WitherStormShouldShowHole"));
        dataManager.set(OTHER_HEADS_DISABLED, compound.getBoolean("WitherStormOtherHeadsDisabled"));
        dataManager.set(HEAD_ANIMATION_FLAGS, compound.getInteger("WitherStormHeadAnimationFlags"));
        bodyXRotation = MathHelper.clamp(compound.getFloat("WitherStormBodyXRotation"), 0.0F, 90.0F);
        previousBodyXRotation = bodyXRotation;
        clientBodyXRotationTarget = bodyXRotation;
        clientBodyXRotationSteps = 0;
        dataManager.set(BODY_X_ROTATION, bodyXRotation);
        podiumPosition = compound.hasKey("WitherStormPodiumPosition", 4)
                ? BlockPos.fromLong(compound.getLong("WitherStormPodiumPosition")) : null;
        podiumPlaced = compound.getBoolean("WitherStormPodiumPlaced") && podiumPosition != null;
        consumedPets.clear();
        NBTTagList pets = compound.getTagList("WitherStormConsumedPets", 10);
        for (int index = 0; index < pets.tagCount(); index++) {
            NBTTagCompound pet = pets.getCompoundTagAt(index);
            if (pet.hasUniqueId("OwnerUUID")) consumedPets.put(pet.getUniqueId("OwnerUUID"), pet.copy());
            else if (pet.hasUniqueId("Owner")) consumedPets.put(pet.getUniqueId("Owner"), pet.copy());
            else consumedPets.put(UUID.randomUUID(), pet.copy());
        }
        commandBlockUuid = readUuid(compound, "WitherStormCommandBlock");
        for (int i = 0; i < segmentUuids.length; i++) segmentUuids[i] = readUuid(compound, "WitherStormSegment" + i);
        trackedEntities.clear();
        savedTrackedEntities.clear();
        trackedEntityTicks = 0;
        NBTTagList tracked = compound.getTagList("WitherStormTrackedEntities", 10);
        for (int index = 0; index < tracked.tagCount(); index++) {
            NBTTagCompound entry = tracked.getCompoundTagAt(index);
            if (entry.hasUniqueId("UUID")) savedTrackedEntities.add(entry.getUniqueId("UUID"));
        }
        witherStormDeathTime = Math.max(0, compound.getInteger("WitherStormDeathTime"));
        deathRewardsReleased = compound.getBoolean("WitherStormDeathRewardsReleased");
        headManager.readFromNBT(compound);
        if (compound.hasKey("WitherStormUltimateTarget", 10)) targetManager.read(compound.getCompoundTag("WitherStormUltimateTarget"));
        if (compound.hasKey("WitherStormSymbiontSummoning", 10)) {
            summoningManager.readFromNBT(compound.getCompoundTag("WitherStormSymbiontSummoning"));
        }
        updateSizeForPlayDeadState();
        legacyBossInfo.setName(getDisplayName());
        legacyBossInfo.setVisible(!isPlayDeadAiDisabled());
        setHealth(Math.min(getHealth(), getMaxHealth()));
    }

    private static void writeUuid(NBTTagCompound compound, String key, UUID uuid) {
        if (uuid != null) compound.setUniqueId(key, uuid);
    }

    private static UUID readUuid(NBTTagCompound compound, String key) {
        return compound.hasUniqueId(key) ? compound.getUniqueId(key) : null;
    }

    @Override
    protected void despawnEntity() {
    }
}
