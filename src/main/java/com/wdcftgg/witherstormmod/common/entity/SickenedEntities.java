package com.wdcftgg.witherstormmod.common.entity;

import com.wdcftgg.witherstormmod.common.init.ModSounds;
import com.wdcftgg.witherstormmod.common.taint.TaintingManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityFlyHelper;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAIOcelotAttack;
import net.minecraft.entity.ai.EntityAIMoveTowardsTarget;
import net.minecraft.entity.ai.EntityAIAttackRanged;
import net.minecraft.entity.ai.EntityAIAttackRangedBow;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateFlying;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.DifficultyInstance;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public final class SickenedEntities {

    private SickenedEntities() {
    }

    private abstract static class FlyingSickenedMob extends SickenedMobEntity {
        FlyingSickenedMob(World world) {
            super(world);
            moveHelper = new EntityFlyHelper(this);
        }

        @Override
        protected void applyEntityAttributes() {
            super.applyEntityAttributes();
            getAttributeMap().registerAttribute(SharedMonsterAttributes.FLYING_SPEED);
            getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED).setBaseValue(getFlyingSpeed());
        }

        protected double getFlyingSpeed() { return 0.4D; }

        @Override
        protected PathNavigate createNavigator(World world) {
            PathNavigateFlying navigator = new PathNavigateFlying(this, world);
            navigator.setCanOpenDoors(false);
            navigator.setCanFloat(true);
            navigator.setCanEnterDoors(true);
            return navigator;
        }

        @Override
        public void onLivingUpdate() {
            setNoGravity(true);
            super.onLivingUpdate();
            setNoGravity(true);
        }

        @Override public void fall(float distance, float damageMultiplier) { }
        @Override protected void updateFallState(double y, boolean onGround, IBlockState state, BlockPos pos) { }
    }

    public static class SickenedBeeEntity extends FlyingSickenedMob {
        public SickenedBeeEntity(World world) { super(world); setSize(0.7F, 0.6F); }
        @Override public String getSickenedType() { return "sickened_bee"; }
        @Override protected double getSickenedHealth() { return 15.0D; }
        @Override protected double getSickenedSpeed() { return 0.3D; }
        @Override protected double getSickenedDamage() { return 2.0D; }
        @Override protected double getSickenedFollowRange() { return 48.0D; }
        @Override protected double getFlyingSpeed() { return 1.2D; }

        @Override
        protected void initEntityAI() {
            tasks.addTask(1, new FlyingAttackAI(this, 1.2D, 10));
            tasks.addTask(5, new RandomFlyingAI(this, 1.0D, 10, 6));
            tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
            targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
            targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true));
        }

        @Override protected boolean canDespawn() { return false; }
    }

    public static class SickenedCatEntity extends SickenedMobEntity {
        public SickenedCatEntity(World world) { super(world); setSize(0.6F, 0.7F); }
        @Override protected double getSickenedHealth() { return 20.0D; }
        @Override protected double getSickenedSpeed() { return 0.32D; }
        @Override protected double getSickenedDamage() { return 4.0D; }
        @Override protected double getSickenedFollowRange() { return 24.0D; }
        @Override public String getSickenedType() { return "sickened_cat"; }

        @Override
        protected void initEntityAI() {
            tasks.addTask(0, new EntityAISwimming(this));
            tasks.addTask(1, new EntityAILeapAtTarget(this, 0.3F));
            tasks.addTask(1, new EntityAIOcelotAttack(this));
            tasks.addTask(3, new EntityAIWanderAvoidWater(this, 1.0D));
            tasks.addTask(4, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
            targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true));
            addSickenedMobTargetGoal(3);
        }

        public boolean isSickenedSitting() {
            return false;
        }

        @Override protected boolean canDespawn() { return false; }
    }

    public static class SickenedChickenEntity extends SickenedMobEntity {
        public SickenedChickenEntity(World world) { super(world); setSize(0.4F, 0.7F); }
        @Override protected double getSickenedHealth() { return 16.0D; }
        @Override protected double getSickenedSpeed() { return 0.25D; }
        @Override protected double getSickenedDamage() { return 2.0D; }
        @Override protected double getSickenedFollowRange() { return 24.0D; }
        @Override public String getSickenedType() { return "sickened_chicken"; }

        @Override protected void initEntityAI() { initStandardAnimalAI(1.125D); }
        @Override protected boolean canDespawn() { return false; }
    }

    public static class SickenedCowEntity extends SickenedMobEntity {
        public SickenedCowEntity(World world) { super(world); setSize(0.9F, 1.4F); }
        @Override protected double getSickenedHealth() { return 25.0D; }
        @Override protected double getSickenedSpeed() { return 0.2D; }
        @Override protected double getSickenedDamage() { return 2.0D; }
        @Override protected double getSickenedFollowRange() { return 24.0D; }
        @Override public String getSickenedType() { return "sickened_cow"; }

        @Override protected void initEntityAI() { initStandardAnimalAI(1.125D); }
        @Override protected boolean canDespawn() { return false; }
    }

    public static class SickenedCreeperEntity extends SickenedMobEntity {
        private static final DataParameter<Integer> SWELL_STATE = EntityDataManager.createKey(SickenedCreeperEntity.class, DataSerializers.VARINT);
        private static final DataParameter<Boolean> POWERED = EntityDataManager.createKey(SickenedCreeperEntity.class, DataSerializers.BOOLEAN);
        private int oldSwell;
        private int swell;
        private int maxSwell = 40;
        private int explosionRadius = 5;

        public void ignite() {
            dataManager.set(SWELL_STATE, 1);
        }

        public SickenedCreeperEntity(World world) { super(world); setSize(0.6F, 1.7F); }
        @Override protected double getSickenedHealth() { return 26.0D; }
        @Override protected double getSickenedSpeed() { return 0.255D; }
        @Override protected double getSickenedFollowRange() { return 18.0D; }
        @Override public String getSickenedType() { return "sickened_creeper"; }

        @Override
        protected void entityInit() {
            super.entityInit();
            dataManager.register(SWELL_STATE, Integer.valueOf(-1));
            dataManager.register(POWERED, Boolean.FALSE);
        }

        @Override
        protected void initEntityAI() {
            tasks.addTask(1, new EntityAISwimming(this));
            tasks.addTask(2, new SickenedCreeperSwellAI(this));
            tasks.addTask(5, new EntityAIWanderAvoidWater(this, 0.8D));
            tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
            targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
            targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true));
        }

        @Override
        public void onUpdate() {
            if (isEntityAlive()) {
                oldSwell = swell;
                int state = getSwellState();
                if (state > 0 && swell == 0) {
                    playSound(SoundEvents.ENTITY_CREEPER_PRIMED, 1.0F, 0.5F);
                }
                swell += state;
                if (swell < 0) {
                    swell = 0;
                }
                if (swell >= maxSwell) {
                    swell = maxSwell;
                    explode();
                }
            }
            super.onUpdate();
        }

        public int getSwellState() {
            return dataManager.get(SWELL_STATE).intValue();
        }

        public void setSwellState(int state) {
            dataManager.set(SWELL_STATE, Integer.valueOf(state));
        }

        public float getCreeperFlashIntensity(float partialTicks) {
            return MathHelper.clamp((oldSwell + (swell - oldSwell) * partialTicks) / (maxSwell - 2.0F), 0.0F, 1.0F);
        }

        public boolean isPowered() {
            return dataManager.get(POWERED).booleanValue();
        }

        @Override
        public void onStruckByLightning(EntityLightningBolt lightningBolt) {
            super.onStruckByLightning(lightningBolt);
            dataManager.set(POWERED, Boolean.TRUE);
        }

        private void explode() {
            if (!world.isRemote) {
                float multiplier = isPowered() ? 2.0F : 1.0F;
                boolean damagesTerrain = world.getGameRules().getBoolean("mobGriefing");
                setDead();
                world.newExplosion(this, posX, posY, posZ, explosionRadius * multiplier, false, damagesTerrain);
            }
        }

        @Override protected int getInfectedHealAmount() { return 0; }

        @Override
        public void writeEntityToNBT(NBTTagCompound compound) {
            super.writeEntityToNBT(compound);
            compound.setShort("Fuse", (short) maxSwell);
            compound.setByte("ExplosionRadius", (byte) explosionRadius);
            compound.setBoolean("powered", isPowered());
        }

        @Override
        public void readEntityFromNBT(NBTTagCompound compound) {
            super.readEntityFromNBT(compound);
            if (compound.hasKey("Fuse", 99)) maxSwell = compound.getShort("Fuse");
            if (compound.hasKey("ExplosionRadius", 99)) explosionRadius = compound.getByte("ExplosionRadius");
            dataManager.set(POWERED, Boolean.valueOf(compound.getBoolean("powered")));
        }

        private static class SickenedCreeperSwellAI extends EntityAIBase {
            private final SickenedCreeperEntity creeper;
            private EntityLivingBase target;

            SickenedCreeperSwellAI(SickenedCreeperEntity creeper) {
                this.creeper = creeper;
                setMutexBits(1);
            }

            @Override
            public boolean shouldExecute() {
                EntityLivingBase current = creeper.getAttackTarget();
                return creeper.getSwellState() > 0 || current != null && creeper.getDistanceSq(current) < 9.0D;
            }

            @Override
            public void startExecuting() {
                creeper.getNavigator().clearPath();
                target = creeper.getAttackTarget();
            }

            @Override
            public void resetTask() {
                target = null;
            }

            @Override
            public void updateTask() {
                if (target == null || creeper.getDistanceSq(target) > 49.0D || !creeper.getEntitySenses().canSee(target)) {
                    creeper.setSwellState(-1);
                } else {
                    creeper.setSwellState(1);
                }
            }
        }
    }

    public static class SickenedIronGolemEntity extends SickenedMobEntity {
        private int attackAnimationTick;

        public SickenedIronGolemEntity(World world) { super(world); setSize(1.4F, 2.7F); }
        @Override protected double getSickenedHealth() { return 60.0D; }
        @Override protected double getSickenedSpeed() { return 0.25D; }
        @Override protected double getSickenedDamage() { return 10.0D; }
        @Override protected double getSickenedFollowRange() { return 48.0D; }
        @Override protected double getSickenedKnockbackResistance() { return 1.0D; }
        @Override public String getSickenedType() { return "sickened_iron_golem"; }

        @Override
        protected void initEntityAI() {
            tasks.addTask(0, new EntityAIAttackMelee(this, 1.1D, true));
            tasks.addTask(1, new EntityAIMoveTowardsTarget(this, 1.0D, 32.0F));
            tasks.addTask(2, new EntityAIWanderAvoidWater(this, 1.0D));
            tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
            tasks.addTask(4, new EntityAILookIdle(this));
            targetTasks.addTask(0, new EntityAIHurtByTarget(this, false));
            targetTasks.addTask(1, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true));
            addSickenedMobTargetGoal(2);
        }

        @Override
        public void onLivingUpdate() {
            super.onLivingUpdate();
            if (attackAnimationTick > 0) {
                --attackAnimationTick;
            }
        }

        @Override
        public boolean attackEntityAsMob(net.minecraft.entity.Entity entityIn) {
            attackAnimationTick = 10;
            if (!world.isRemote) {
                world.setEntityState(this, (byte) 4);
            }
            return super.attackEntityAsMob(entityIn);
        }

        @Override
        public void handleStatusUpdate(byte id) {
            if (id == 4) {
                attackAnimationTick = 10;
            } else {
                super.handleStatusUpdate(id);
            }
        }

        public int getAttackAnimationTick() {
            return attackAnimationTick;
        }

        @Override protected int getInfectedHealAmount() { return 6; }
    }

    public static class SickenedMushroomCowEntity extends SickenedMobEntity {
        public SickenedMushroomCowEntity(World world) { super(world); setSize(0.9F, 1.4F); }
        @Override protected double getSickenedHealth() { return 26.0D; }
        @Override protected double getSickenedSpeed() { return 0.3D; }
        @Override protected double getSickenedDamage() { return 2.0D; }
        @Override public String getSickenedType() { return "sickened_mushroom_cow"; }
        @Override protected boolean canDespawn() { return false; }
    }

    public static class SickenedParrotEntity extends FlyingSickenedMob {
        public SickenedParrotEntity(World world) { super(world); setSize(0.5F, 0.9F); }
        @Override protected double getSickenedHealth() { return 16.0D; }
        @Override protected double getSickenedSpeed() { return 0.4D; }
        @Override protected double getSickenedDamage() { return 2.0D; }
        @Override protected double getSickenedFollowRange() { return 24.0D; }
        @Override protected double getFlyingSpeed() { return 0.9D; }
        @Override public String getSickenedType() { return "sickened_parrot"; }

        @Override
        protected void initEntityAI() {
            tasks.addTask(0, new EntityAISwimming(this));
            tasks.addTask(1, new FlyingAttackAI(this, 1.1D, 10));
            tasks.addTask(3, new RandomFlyingAI(this, 1.0D, 8, 5));
            tasks.addTask(4, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
            targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true));
            addSickenedMobTargetGoal(3);
        }

        public boolean isSickenedFlying() {
            return !onGround;
        }

        public boolean isSickenedSitting() {
            return false;
        }

        public boolean isSickenedPartying() {
            return false;
        }

        @Override protected boolean canDespawn() { return false; }
    }

    public static class SickenedPhantomEntity extends FlyingSickenedMob {
        public SickenedPhantomEntity(World world) { super(world); setSize(0.9F, 0.5F); }
        @Override public String getSickenedType() { return "sickened_phantom"; }
        @Override protected double getSickenedHealth() { return 20.0D; }
        @Override protected double getSickenedSpeed() { return 0.25D; }
        @Override protected double getSickenedDamage() { return 3.0D; }
        @Override protected double getFlyingSpeed() { return 0.5D; }

        @Override
        protected void initEntityAI() {
            tasks.addTask(1, new FlyingAttackAI(this, 1.35D, 20));
            tasks.addTask(5, new RandomFlyingAI(this, 1.0D, 16, 10));
            targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
            targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true));
        }
    }

    public static class SickenedPigEntity extends SickenedMobEntity {
        public SickenedPigEntity(World world) { super(world); setSize(0.9F, 0.9F); }
        @Override protected double getSickenedHealth() { return 20.0D; }
        @Override protected double getSickenedSpeed() { return 0.25D; }
        @Override protected double getSickenedDamage() { return 2.0D; }
        @Override protected double getSickenedFollowRange() { return 24.0D; }
        @Override public String getSickenedType() { return "sickened_pig"; }

        @Override protected void initEntityAI() { initStandardAnimalAI(1.125D); }
        @Override protected boolean canDespawn() { return false; }
    }

    public static class SickenedPillagerEntity extends SickenedMobEntity implements IRangedAttackMob {
        public SickenedPillagerEntity(World world) {
            super(world);
            setSize(0.6F, 1.95F);
            setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        }
        @Override protected double getSickenedHealth() { return 30.0D; }
        @Override protected double getSickenedSpeed() { return 0.37D; }
        @Override protected double getSickenedDamage() { return 6.0D; }
        @Override protected double getSickenedFollowRange() { return 48.0D; }
        @Override public String getSickenedType() { return "sickened_pillager"; }

        @Override
        protected void initEntityAI() {
            tasks.addTask(1, new EntityAISwimming(this));
            tasks.addTask(2, new EntityAIAttackRangedBow<SickenedPillagerEntity>(this, 1.0D, 20, 15.0F));
            tasks.addTask(5, new EntityAIWanderAvoidWater(this, 0.8D));
            tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
            tasks.addTask(6, new EntityAILookIdle(this));
            targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
            targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true));
            addSickenedMobTargetGoal(3);
        }

        @Override
        public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
            fireSickenedArrow(this, target, distanceFactor);
        }

        @Override public void setSwingingArms(boolean swingingArms) { }
    }

    public static class SickenedSkeletonEntity extends SickenedMobEntity implements IRangedAttackMob {
        public SickenedSkeletonEntity(World world) {
            super(world);
            setSize(0.6F, 1.99F);
            setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        }
        @Override protected double getSickenedHealth() { return 24.0D; }
        @Override protected double getSickenedSpeed() { return 0.28D; }
        @Override public String getSickenedType() { return "sickened_skeleton"; }

        @Override
        protected void initEntityAI() {
            tasks.addTask(1, new EntityAISwimming(this));
            tasks.addTask(4, new EntityAIAttackRangedBow<SickenedSkeletonEntity>(this, 1.0D, 20, 15.0F));
            tasks.addTask(5, new EntityAIWanderAvoidWater(this, 1.0D));
            tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
            tasks.addTask(6, new EntityAILookIdle(this));
            targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
            targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true));
            addSickenedMobTargetGoal(3);
        }

        @Override
        public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
            fireSickenedArrow(this, target, distanceFactor);
        }

        @Override public void setSwingingArms(boolean swingingArms) { }
    }

    public static class SickenedSnowGolemEntity extends SickenedMobEntity implements IRangedAttackMob {
        public SickenedSnowGolemEntity(World world) { super(world); setSize(0.7F, 1.9F); }
        @Override protected double getSickenedHealth() { return 8.0D; }
        @Override protected double getSickenedSpeed() { return 0.24D; }
        @Override public String getSickenedType() { return "sickened_snow_golem"; }

        @Override
        protected void initEntityAI() {
            tasks.addTask(0, new EntityAISwimming(this));
            tasks.addTask(1, new EntityAIAttackRanged(this, 1.25D, 12, 10.0F));
            tasks.addTask(2, new EntityAIWanderAvoidWater(this, 1.0D));
            tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
            tasks.addTask(4, new EntityAILookIdle(this));
            targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true));
            addSickenedMobTargetGoal(3);
        }

        @Override
        public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
            SickenedSnowball snowball = new SickenedSnowball(world, this, rand.nextFloat() < 0.1F);
            double targetY = target.posY + target.getEyeHeight() - 1.1D;
            double dx = target.posX - posX;
            double dy = targetY - snowball.posY;
            double dz = target.posZ - posZ;
            double arc = MathHelper.sqrt(dx * dx + dz * dz) * 0.2D;
            snowball.shoot(dx, dy + arc, dz, 1.6F, 12.0F);
            playSound(SoundEvents.ENTITY_SNOWMAN_SHOOT, 1.0F, 0.4F / (rand.nextFloat() * 0.4F + 0.8F));
            world.spawnEntity(snowball);
        }

        @Override public void setSwingingArms(boolean swingingArms) { }
        @Override protected boolean canDespawn() { return false; }
    }

    public static class SickenedSpiderEntity extends SickenedMobEntity {
        public SickenedSpiderEntity(World world) { super(world); setSize(1.6F, 1.1F); }
        @Override protected double getSickenedHealth() { return 20.0D; }
        @Override protected double getSickenedSpeed() { return 0.34D; }
        @Override protected double getSickenedDamage() { return 3.0D; }
        @Override protected double getSickenedFollowRange() { return 32.0D; }
        @Override public String getSickenedType() { return "sickened_spider"; }

        @Override
        protected void initEntityAI() {
            tasks.addTask(1, new EntityAISwimming(this));
            tasks.addTask(3, new EntityAILeapAtTarget(this, 0.45F));
            tasks.addTask(4, new EntityAIAttackMelee(this, 1.0D, true));
            tasks.addTask(5, new EntityAIWanderAvoidWater(this, 0.8D));
            tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
            tasks.addTask(6, new EntityAILookIdle(this));
            targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
            targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true));
            addSickenedMobTargetGoal(3);
        }
    }

    public static class SickenedVillagerEntity extends SickenedZombieEntity {
        public SickenedVillagerEntity(World world) { super(world); setSize(0.6F, 1.95F); }
        @Override public String getSickenedType() { return "sickened_villager"; }
    }

    public static class SickenedVindicatorEntity extends SickenedMobEntity {
        public SickenedVindicatorEntity(World world) { super(world); setSize(0.6F, 1.95F); }
        @Override protected double getSickenedHealth() { return 30.0D; }
        @Override protected double getSickenedSpeed() { return 0.35D; }
        @Override protected double getSickenedDamage() { return 6.0D; }
        @Override protected double getSickenedFollowRange() { return 48.0D; }
        @Override public String getSickenedType() { return "sickened_vindicator"; }
    }

    public static class SickenedWolfEntity extends SickenedMobEntity {
        public SickenedWolfEntity(World world) { super(world); setSize(0.6F, 0.85F); }
        @Override protected double getSickenedHealth() { return 18.0D; }
        @Override protected double getSickenedSpeed() { return 0.3D; }
        @Override protected double getSickenedDamage() { return 3.0D; }
        @Override protected double getSickenedFollowRange() { return 24.0D; }
        @Override public String getSickenedType() { return "sickened_wolf"; }

        @Override
        protected void initEntityAI() {
            tasks.addTask(0, new EntityAISwimming(this));
            tasks.addTask(1, new EntityAILeapAtTarget(this, 0.4F));
            tasks.addTask(2, new EntityAIAttackMelee(this, 1.125D, false));
            tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
            tasks.addTask(4, new EntityAIWanderAvoidWater(this, 1.0D));
            tasks.addTask(5, new EntityAILookIdle(this));
            targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
            targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true));
            addSickenedMobTargetGoal(3);
        }

        public boolean isSickenedAngry() {
            return getAttackTarget() != null;
        }

        public boolean isSickenedSitting() {
            return false;
        }

        public float getInterestedAngle(float partialTicks) {
            return 0.0F;
        }

        public float getShakeAngle(float partialTicks, float offset) {
            return 0.0F;
        }

        @Override protected boolean canDespawn() { return false; }
    }

    public static class SickenedZombieEntity extends SickenedMobEntity {
        public SickenedZombieEntity(World world) { super(world); setSize(0.6F, 1.95F); }
        @Override protected double getSickenedHealth() { return 24.0D; }
        @Override protected double getSickenedSpeed() { return 0.28D; }
        @Override protected double getSickenedDamage() { return 3.5D; }
        @Override protected double getSickenedFollowRange() { return 48.0D; }
        @Override protected double getSickenedArmor() { return 2.2D; }
        @Override public String getSickenedType() { return "sickened_zombie"; }
    }

    public static class TentacleEntity extends SickenedMobEntity {
        private static final DataParameter<Boolean> DORMANT = EntityDataManager.createKey(TentacleEntity.class, DataSerializers.BOOLEAN);
        private static final DataParameter<Boolean> CURLING = EntityDataManager.createKey(TentacleEntity.class, DataSerializers.BOOLEAN);
        private static final DataParameter<Boolean> CAN_SWING = EntityDataManager.createKey(TentacleEntity.class, DataSerializers.BOOLEAN);
        private static final DataParameter<Boolean> CAN_STRANGLE = EntityDataManager.createKey(TentacleEntity.class, DataSerializers.BOOLEAN);
        private static final DataParameter<Integer> ANIMATION_OFFSET = EntityDataManager.createKey(TentacleEntity.class, DataSerializers.VARINT);
        private static final DataParameter<Float> X_OFFSET = EntityDataManager.createKey(TentacleEntity.class, DataSerializers.FLOAT);
        private static final DataParameter<Float> Y_OFFSET = EntityDataManager.createKey(TentacleEntity.class, DataSerializers.FLOAT);
        private static final DataParameter<Float> X_CURL = EntityDataManager.createKey(TentacleEntity.class, DataSerializers.FLOAT);
        private static final DataParameter<Float> Y_CURL = EntityDataManager.createKey(TentacleEntity.class, DataSerializers.FLOAT);
        private static final DataParameter<Float> Y_OFFSET_ANIMATION = EntityDataManager.createKey(TentacleEntity.class, DataSerializers.FLOAT);
        private static final DataParameter<Float> X_CURL_ANIMATION = EntityDataManager.createKey(TentacleEntity.class, DataSerializers.FLOAT);
        private static final DataParameter<Float> Y_CURL_ANIMATION = EntityDataManager.createKey(TentacleEntity.class, DataSerializers.FLOAT);
        private int awakeTicks;
        private boolean indefinitelyAwake;
        private int swingTicks;
        private int nextSwing;
        private int strangleTicks;
        private int knockbackWait;
        private double curlX;
        private double curlY;
        private double curlZ;

        public TentacleEntity(World world) { super(world); setSize(7.5F, 9.5F); }

        @Override
        protected void initEntityAI() {
            tasks.addTask(0, new DormantGoal(this));
            tasks.addTask(1, new SwingGoal(this));
            tasks.addTask(2, new StrangleGoal(this));
            targetTasks.addTask(0, new TentacleTargetGoal<EntityPlayer>(this, EntityPlayer.class));
            targetTasks.addTask(1, new TentacleTargetGoal<EntityAnimal>(this, EntityAnimal.class));
        }

        @Override
        protected void entityInit() {
            super.entityInit();
            dataManager.register(DORMANT, false);
            dataManager.register(CURLING, false);
            dataManager.register(CAN_SWING, true);
            dataManager.register(CAN_STRANGLE, true);
            dataManager.register(ANIMATION_OFFSET, 0);
            dataManager.register(X_OFFSET, 20.0F);
            dataManager.register(Y_OFFSET, 0.0F);
            dataManager.register(X_CURL, 1.3F);
            dataManager.register(Y_CURL, 1.0F);
            dataManager.register(Y_OFFSET_ANIMATION, 0.0F);
            dataManager.register(X_CURL_ANIMATION, 0.0F);
            dataManager.register(Y_CURL_ANIMATION, 0.0F);
        }

        @Override
        public void onLivingUpdate() {
            setNoGravity(true);
            super.onLivingUpdate();
            setNoGravity(true);
            motionX = motionY = motionZ = 0.0D;
            fallDistance = 0.0F;
            if (isDormant()) {
                setAttackTarget(null);
                getNavigator().clearPath();
                removePassengers();
                stopSwingAnimation();
                stopStrangleLocal();
                return;
            }
            tickAwakeAnimation();
            tickSwingAttack();
            if (nextSwing > 0) --nextSwing;
            if (strangleTicks > 0 && --strangleTicks == 0) stopStrangleLocal();
        }

        public boolean isDormant() { return dataManager.get(DORMANT); }
        public void setDormant(boolean dormant) {
            dataManager.set(DORMANT, dormant);
            if (dormant) {
                setAttackTarget(null);
                removePassengers();
            }
        }
        public void doAwakeAnimation() {
            startAwakeAnimation(false);
            if (!world.isRemote) world.setEntityState(this, (byte) 13);
            playSound(ModSounds.get("whoosh"), 3.0F, 1.0F);
        }
        private void startAwakeAnimation(boolean indefinite) {
            indefinitelyAwake = indefinite;
            awakeTicks = 40;
            dataManager.set(Y_CURL_ANIMATION, 0.05F * (rand.nextBoolean() ? 1.0F : -1.0F));
        }
        public void doIndefiniteAwakeAnimation() {
            startAwakeAnimation(true);
            awakeTicks = 0;
            if (!world.isRemote) world.setEntityState(this, (byte) 14);
            playSound(ModSounds.get("whoosh"), 3.0F, 1.0F);
        }
        private void stopAwakeAnimation() {
            indefinitelyAwake = false;
            dataManager.set(Y_CURL_ANIMATION, 0.0F);
        }
        public boolean isDoingSwingAttack() { return swingTicks > 0; }
        public boolean canDoSwingAttack() { return nextSwing <= 0 && !isDoingSwingAttack(); }
        public void setCanSwing(boolean canSwing) {
            dataManager.set(CAN_SWING, canSwing);
            if (!canSwing) stopSwingAnimation();
        }
        public boolean canSwing() { return dataManager.get(CAN_SWING); }
        public void setCanStrangle(boolean canStrangle) {
            dataManager.set(CAN_STRANGLE, canStrangle);
            if (!canStrangle) {
                removePassengers();
                stopStrangleLocal();
            }
        }
        public boolean canStrangle() { return dataManager.get(CAN_STRANGLE); }
        public void curlAround(Vec3d position) {
            dataManager.set(CURLING, true);
            curlX = position.x;
            curlY = position.y;
            curlZ = position.z;
            setTargetYaw(position, 180.0F);
            dataManager.set(X_CURL_ANIMATION, 0.1F);
            dataManager.set(Y_CURL_ANIMATION, 0.1F);
        }
        public void stopCurlingAround() {
            dataManager.set(CURLING, false);
            dataManager.set(Y_OFFSET_ANIMATION, 0.0F);
            dataManager.set(X_CURL_ANIMATION, 0.0F);
            dataManager.set(Y_CURL_ANIMATION, 0.0F);
        }
        public boolean isCurling() { return dataManager.get(CURLING); }

        @Override
        public boolean attackEntityAsMob(Entity entityIn) {
            return !isDormant() && super.attackEntityAsMob(entityIn);
        }
        @Override protected double getSickenedHealth() { return 80.0D; }
        @Override protected double getSickenedSpeed() { return 0.0D; }
        @Override protected double getSickenedDamage() { return 12.0D; }
        @Override protected double getSickenedFollowRange() { return 8.0D; }
        @Override protected double getSickenedKnockbackResistance() { return 1.0D; }
        @Override public String getSickenedType() { return "tentacle"; }

        @Override
        public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingData) {
            dataManager.set(Y_OFFSET, (float) rand.nextInt(360));
            dataManager.set(X_OFFSET, 15.0F + rand.nextFloat() * 5.0F);
            dataManager.set(X_CURL, 1.25F + rand.nextFloat() * 0.1F);
            dataManager.set(ANIMATION_OFFSET, rand.nextInt(35) * 10000);
            return super.onInitialSpawn(difficulty, livingData);
        }

        @Override public void move(MoverType type, double x, double y, double z) { }
        @Override public void applyEntityCollision(Entity entityIn) { }
        @Override public void knockBack(Entity entityIn, float strength, double xRatio, double zRatio) { }
        @Override public boolean canBePushed() { return false; }
        @Override public boolean isOnLadder() { return false; }
        @Override public void fall(float distance, float damageMultiplier) { }
        @Override public boolean isPotionApplicable(PotionEffect effect) { return false; }
        @Override public float getEyeHeight() { return height * 0.5F; }
        @Override protected boolean canDespawn() { return false; }

        @Override
        public boolean attackEntityFrom(DamageSource source, float amount) {
            return (!isDormant() || source == DamageSource.OUT_OF_WORLD) && super.attackEntityFrom(source, amount);
        }

        @Override
        public void updatePassenger(Entity passenger) {
            if (passenger.getRidingEntity() == this) {
                passenger.setPosition(posX, posY + height * 0.85D, posZ);
                passenger.motionX = passenger.motionY = passenger.motionZ = 0.0D;
            }
        }

        @Override
        public void handleStatusUpdate(byte id) {
            if (id == 11) {
                startStrangleLocal();
            } else if (id == 12) {
                stopStrangleLocal();
            } else if (id == 13) {
                startAwakeAnimation(false);
            } else if (id == 14) {
                startAwakeAnimation(true);
                awakeTicks = 0;
            } else if (id == 15) {
                swingTicks = 40;
                knockbackWait = 0;
            } else if (id == 16) {
                stopSwingAnimation();
            } else {
                super.handleStatusUpdate(id);
            }
        }

        public float getSegmentPitch(int segment, float partialTicks) {
            float animation = ticksExisted + partialTicks + dataManager.get(ANIMATION_OFFSET);
            float speed = strangleTicks > 0 ? 15.0F : awakeTicks > 0 || indefinitelyAwake ? 6.0F : 1.0F;
            float reach = awakeTicks > 0 || indefinitelyAwake ? 4.0F : 1.0F;
            float basePitch = (float) Math.toDegrees(MathHelper.sin(animation * 0.05F * speed))
                    * 0.05F * reach - 90.0F + dataManager.get(X_OFFSET);
            float chainPitch = basePitch;
            float previousPitch = 0.0F;
            for (int index = 0; index <= segment; index++) {
                if (index > 0) chainPitch = previousPitch * (dataManager.get(X_CURL) + dataManager.get(X_CURL_ANIMATION));
                float localPitch = index == 0
                        ? -(chainPitch + 90.0F)
                        : -(chainPitch - previousPitch);
                if (index == segment) return localPitch * 0.017453292F;
                previousPitch = chainPitch;
            }
            return 0.0F;
        }

        public float getSegmentYaw(int segment, float partialTicks) {
            if (segment != 0) return dataManager.get(Y_CURL) + dataManager.get(Y_CURL_ANIMATION);
            float animation = ticksExisted + partialTicks + dataManager.get(ANIMATION_OFFSET);
            float speed = strangleTicks > 0 ? 15.0F : awakeTicks > 0 || indefinitelyAwake ? 6.0F : 1.0F;
            float reach = awakeTicks > 0 || indefinitelyAwake ? 4.0F : 1.0F;
            float baseYaw = (float) Math.toDegrees(MathHelper.cos(animation * 0.06F * speed))
                    * 0.14F * reach - 270.0F + dataManager.get(Y_OFFSET) + dataManager.get(Y_OFFSET_ANIMATION);
            return baseYaw * 0.017453292F;
        }

        private void tickAwakeAnimation() {
            if (awakeTicks > 0 && --awakeTicks == 0 && !indefinitelyAwake) stopAwakeAnimation();
        }

        private void startSwingAttack() {
            swingTicks = 40;
            knockbackWait = 0;
            if (!world.isRemote) world.setEntityState(this, (byte) 15);
        }

        private void tickSwingAttack() {
            if (swingTicks <= 0) return;
            --swingTicks;
            if (swingTicks == 25) dataManager.set(Y_OFFSET_ANIMATION, 80.0F);
            if (swingTicks == 15 && getAttackTarget() != null) {
                setTargetYaw(getAttackTarget().getPositionVector(), 270.0F);
                dataManager.set(X_CURL_ANIMATION, 0.1F);
                dataManager.set(Y_CURL_ANIMATION, 0.1F);
                playSound(ModSounds.get("whoosh"), 3.0F, 1.0F);
            }
            if (!world.isRemote && ++knockbackWait >= 35) strikeSwingTarget();
            if (swingTicks == 0) stopSwingAnimation();
        }

        private void strikeSwingTarget() {
            EntityLivingBase target = getAttackTarget();
            if (target == null || !target.isEntityAlive()) return;
            if (target instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) target;
                if (!player.getActiveItemStack().isEmpty() && player.getActiveItemStack().getItem() == Items.SHIELD
                        && !player.getCooldownTracker().hasCooldown(Items.SHIELD)) {
                    player.getCooldownTracker().setCooldown(Items.SHIELD, 100);
                    player.resetActiveHand();
                    world.setEntityState(player, (byte) 30);
                }
            }
            if (target.attackEntityFrom(DamageSource.causeMobDamage(this), (float) getEntityAttribute(
                    SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue() * 3.5F)) {
                target.knockBack(this, (float) getEntityAttribute(
                        SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue(),
                        posX - target.posX, posZ - target.posZ);
            }
        }

        private void stopSwingAnimation() {
            boolean wasSwinging = swingTicks > 0;
            swingTicks = 0;
            knockbackWait = 0;
            dataManager.set(Y_OFFSET_ANIMATION, 0.0F);
            dataManager.set(X_CURL_ANIMATION, 0.0F);
            dataManager.set(Y_CURL_ANIMATION, 0.0F);
            if (wasSwinging && !world.isRemote) world.setEntityState(this, (byte) 16);
        }

        private void startStrangleLocal() {
            strangleTicks = 20;
        }

        private void doStrangle() {
            startStrangleLocal();
            if (!world.isRemote) world.setEntityState(this, (byte) 11);
        }

        private void stopStrangleLocal() {
            strangleTicks = 0;
        }

        private void stopStrangle() {
            stopStrangleLocal();
            if (!world.isRemote) world.setEntityState(this, (byte) 12);
        }

        private void setTargetYaw(Vec3d position, float offset) {
            Vec3d delta = position.subtract(getPositionVector());
            float angle = (float) Math.toDegrees(MathHelper.atan2(delta.x, delta.z));
            dataManager.set(Y_OFFSET_ANIMATION, -(angle + offset + dataManager.get(Y_OFFSET)) % 360.0F);
        }

        @Override
        public void writeEntityToNBT(NBTTagCompound compound) {
            super.writeEntityToNBT(compound);
            compound.setBoolean("Dormant", isDormant());
            compound.setBoolean("Curling", isCurling());
            compound.setBoolean("CanSwing", canSwing());
            compound.setBoolean("CanStrangle", canStrangle());
            compound.setInteger("AnimOffset", dataManager.get(ANIMATION_OFFSET));
            compound.setFloat("XOffset", dataManager.get(X_OFFSET));
            compound.setFloat("YOffset", dataManager.get(Y_OFFSET));
            compound.setFloat("XCurl", dataManager.get(X_CURL));
            compound.setFloat("YCurl", dataManager.get(Y_CURL));
            compound.setInteger("AwakeTicks", awakeTicks);
            compound.setBoolean("IndefinitelyAwake", indefinitelyAwake);
            compound.setInteger("SwingTicks", swingTicks);
            compound.setInteger("NextSwing", nextSwing);
            compound.setInteger("StrangleTicks", strangleTicks);
            compound.setDouble("CurlTargetX", curlX);
            compound.setDouble("CurlTargetY", curlY);
            compound.setDouble("CurlTargetZ", curlZ);
        }

        @Override
        public void readEntityFromNBT(NBTTagCompound compound) {
            super.readEntityFromNBT(compound);
            setDormant(compound.getBoolean("Dormant"));
            dataManager.set(CURLING, compound.getBoolean("Curling"));
            setCanSwing(!compound.hasKey("CanSwing") || compound.getBoolean("CanSwing"));
            setCanStrangle(!compound.hasKey("CanStrangle") || compound.getBoolean("CanStrangle"));
            if (compound.hasKey("AnimOffset")) dataManager.set(ANIMATION_OFFSET, compound.getInteger("AnimOffset"));
            if (compound.hasKey("XOffset")) dataManager.set(X_OFFSET, compound.getFloat("XOffset"));
            if (compound.hasKey("YOffset")) dataManager.set(Y_OFFSET, compound.getFloat("YOffset"));
            if (compound.hasKey("XCurl")) dataManager.set(X_CURL, compound.getFloat("XCurl"));
            if (compound.hasKey("YCurl")) dataManager.set(Y_CURL, compound.getFloat("YCurl"));
            awakeTicks = compound.getInteger("AwakeTicks");
            indefinitelyAwake = compound.getBoolean("IndefinitelyAwake");
            swingTicks = compound.getInteger("SwingTicks");
            nextSwing = compound.getInteger("NextSwing");
            strangleTicks = compound.getInteger("StrangleTicks");
            curlX = compound.getDouble("CurlTargetX");
            curlY = compound.getDouble("CurlTargetY");
            curlZ = compound.getDouble("CurlTargetZ");
        }

        private static final class DormantGoal extends EntityAIBase {
            private final TentacleEntity tentacle;

            private DormantGoal(TentacleEntity tentacle) {
                this.tentacle = tentacle;
                setMutexBits(7);
            }

            @Override public boolean shouldExecute() { return tentacle.isDormant(); }
            @Override public boolean shouldContinueExecuting() { return tentacle.isDormant(); }
            @Override public void startExecuting() { tentacle.setAttackTarget(null); }
        }

        private static final class SwingGoal extends EntityAIBase {
            private final TentacleEntity tentacle;

            private SwingGoal(TentacleEntity tentacle) { this.tentacle = tentacle; }

            @Override
            public boolean shouldExecute() {
                EntityLivingBase target = tentacle.getAttackTarget();
                return tentacle.canSwing() && !tentacle.isDormant() && target != null && target.isEntityAlive()
                        && tentacle.isEntityAlive()
                        && (tentacle.getHealth() < tentacle.getMaxHealth() || !tentacle.canStrangle())
                        && tentacle.canDoSwingAttack();
            }

            @Override
            public boolean shouldContinueExecuting() {
                EntityLivingBase target = tentacle.getAttackTarget();
                return target != null && target.isEntityAlive() && tentacle.isEntityAlive()
                        && tentacle.isDoingSwingAttack();
            }

            @Override
            public void startExecuting() {
                tentacle.startSwingAttack();
                tentacle.nextSwing = 120 + tentacle.getRNG().nextInt(120);
            }
        }

        private static final class StrangleGoal extends EntityAIBase {
            private final TentacleEntity tentacle;
            private int grabWait;
            private int nextStrangle;

            private StrangleGoal(TentacleEntity tentacle) { this.tentacle = tentacle; }

            @Override
            public boolean shouldExecute() {
                EntityLivingBase target = tentacle.getAttackTarget();
                return tentacle.canStrangle() && !tentacle.isDormant() && target != null && target.isEntityAlive()
                        && tentacle.isEntityAlive() && !tentacle.isDoingSwingAttack();
            }

            @Override public boolean shouldContinueExecuting() { return shouldExecute(); }

            @Override
            public void startExecuting() {
                EntityLivingBase target = tentacle.getAttackTarget();
                if (target != null) tentacle.curlAround(target.getPositionVector());
                nextStrangle = 20 + tentacle.getRNG().nextInt(40);
            }

            @Override
            public void updateTask() {
                EntityLivingBase target = tentacle.getAttackTarget();
                if (target == null) return;
                if (!tentacle.getEntitySenses().canSee(target)) {
                    target.dismountRidingEntity();
                    tentacle.setAttackTarget(null);
                    return;
                }
                if (++grabWait > 5 && target.getRidingEntity() != tentacle) target.startRiding(tentacle, true);
                if (nextStrangle > 0 && --nextStrangle <= 0) {
                    tentacle.doStrangle();
                    nextStrangle = 20 + tentacle.getRNG().nextInt(40);
                    tentacle.attackEntityAsMob(target);
                }
            }

            @Override
            public void resetTask() {
                tentacle.removePassengers();
                grabWait = 0;
                tentacle.stopCurlingAround();
                tentacle.stopStrangle();
            }
        }

        private static final class TentacleTargetGoal<T extends EntityLivingBase>
                extends EntityAINearestAttackableTarget<T> {
            private final TentacleEntity tentacle;

            private TentacleTargetGoal(TentacleEntity tentacle, Class<T> targetClass) {
                super(tentacle, targetClass, 10, true, true,
                        target -> target != null && !(target instanceof SickenedMobEntity));
                this.tentacle = tentacle;
            }

            @Override
            protected AxisAlignedBB getTargetableArea(double targetDistance) {
                return tentacle.getEntityBoundingBox().grow(targetDistance);
            }

            @Override
            public void startExecuting() {
                for (TentacleEntity other : tentacle.world.getEntitiesWithinAABB(TentacleEntity.class,
                        getTargetableArea(getTargetDistance() + 10.0D))) {
                    if (other != tentacle && other.isEntityAlive() && other.getAttackTarget() == targetEntity) return;
                }
                super.startExecuting();
            }
        }
    }

    public static class WitheredSymbiontEntity extends SickenedMobEntity {
        private static final DataParameter<Integer> BOSSFIGHT_STAGE = EntityDataManager.createKey(WitheredSymbiontEntity.class, DataSerializers.VARINT);
        private static final DataParameter<Integer> SPELL_TYPE = EntityDataManager.createKey(WitheredSymbiontEntity.class, DataSerializers.VARINT);
        private static final DataParameter<Boolean> NON_BOSS_MODE = EntityDataManager.createKey(WitheredSymbiontEntity.class, DataSerializers.BOOLEAN);
        private static final DataParameter<Boolean> RUSH_MODE = EntityDataManager.createKey(WitheredSymbiontEntity.class, DataSerializers.BOOLEAN);
        private static final DataParameter<Boolean> SHOULD_NOT_GO_OVER_HALF = EntityDataManager.createKey(WitheredSymbiontEntity.class, DataSerializers.BOOLEAN);
        private static final DataParameter<Integer> SPELL_CASTING_TIME = EntityDataManager.createKey(WitheredSymbiontEntity.class, DataSerializers.VARINT);
        private static final DataParameter<Boolean> SMASHING = EntityDataManager.createKey(WitheredSymbiontEntity.class, DataSerializers.BOOLEAN);
        private static final DataParameter<Integer> ATTACK_DELAY = EntityDataManager.createKey(WitheredSymbiontEntity.class, DataSerializers.VARINT);
        private final BossInfoServer bossInfo = new BossInfoServer(getDisplayName(), BossInfo.Color.PURPLE, BossInfo.Overlay.PROGRESS);
        private UUID ownerUuid;
        private List<EntityAIBase> bossFightGoals;
        private SymbiontAttackGoal attackGoal;
        private PrepareSpellGoal prepareSpellGoal;
        private UseSpellGoal useSpellGoal;
        private SummonMobsGoal summonMobsGoal;
        private DoNothingGoal doNothingGoal;
        private SymbiontSpells.Spell spellInstance;
        private int stageTicks;
        private int nextSpellPickCount;
        private int spellsUsed;
        private int smashAirTime;
        private final List<EntityLivingBase> entitiesToThrow = new ArrayList<EntityLivingBase>();
        private final List<ItemStack> dropItems = new ArrayList<ItemStack>();
        private final List<UUID> fightContributors = new ArrayList<UUID>();
        private float crouchAnimation;
        private float previousCrouchAnimation;
        private float tearAlpha;
        private float previousTearAlpha;
        private boolean healthScaled;
        private boolean attackableWhenNotVulnerable;

        public WitheredSymbiontEntity(World world) {
            super(world);
            setSize(1.2F, 3.8F);
            stepHeight = 1.0F;
            experienceValue = 150;
            enablePersistence();
        }

        @Override
        protected void entityInit() {
            super.entityInit();
            dataManager.register(BOSSFIGHT_STAGE, BossfightStage.ATTACKING.ordinal());
            dataManager.register(SPELL_TYPE, SymbiontSpells.Type.EMPTY.ordinal());
            dataManager.register(NON_BOSS_MODE, false);
            dataManager.register(RUSH_MODE, false);
            dataManager.register(SHOULD_NOT_GO_OVER_HALF, true);
            dataManager.register(SPELL_CASTING_TIME, 0);
            dataManager.register(SMASHING, false);
            dataManager.register(ATTACK_DELAY, 0);
        }

        @Override
        protected void initEntityAI() {
            bossFightGoals = new ArrayList<EntityAIBase>();
            attackGoal = new SymbiontAttackGoal(this);
            prepareSpellGoal = new PrepareSpellGoal(this);
            useSpellGoal = new UseSpellGoal(this);
            summonMobsGoal = new SummonMobsGoal(this);
            doNothingGoal = new DoNothingGoal(this);
            Collections.addAll(bossFightGoals, attackGoal, prepareSpellGoal, useSpellGoal, summonMobsGoal, doNothingGoal);

            tasks.addTask(1, prepareSpellGoal);
            tasks.addTask(2, useSpellGoal);
            tasks.addTask(3, attackGoal);
            tasks.addTask(4, new EntityAISwimming(this));
            tasks.addTask(5, new EntityAIWanderAvoidWater(this, 0.7D));
            tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
            tasks.addTask(7, new EntityAILookIdle(this));
            targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
            targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class,
                    10, true, false, player -> player != null && player.isEntityAlive()));
        }

        @Override protected double getSickenedHealth() { return 60.0D; }
        @Override protected double getSickenedSpeed() { return 0.15D; }
        @Override protected double getSickenedArmor() { return 1.0D; }
        @Override protected double getSickenedDamage() { return 16.0D; }
        @Override protected double getSickenedFollowRange() { return 45.0D; }
        @Override public String getSickenedType() { return "withered_symbiont"; }

        @Override
        public void onLivingUpdate() {
            super.onLivingUpdate();
            previousCrouchAnimation = crouchAnimation;
            if (isVulnerable()) {
                crouchAnimation = Math.min(0.6F, crouchAnimation + (1.0F - crouchAnimation) * 0.1F + 0.02F);
            } else {
                crouchAnimation = Math.max(0.0F, crouchAnimation - crouchAnimation * 0.4F - 0.1F);
            }
            previousTearAlpha = tearAlpha;
            if (isVulnerable()) tearAlpha = Math.min(1.0F, tearAlpha + 0.05F);
            else tearAlpha = Math.max(0.0F, tearAlpha - 0.05F);

            if (world.isRemote || !isEntityAlive()) return;
            if (!healthScaled && ticksExisted <= 2) scaleHealthForNearbyPlayers();
            stageTicks++;
            tickSpellCasting();
            if (nextSpellPickCount > 0) nextSpellPickCount--;
            tickSmashing();
            if (getStage().shouldMoveToNextStage(this)) nextStage();
            if (getAttackDelay() > 0) {
                setAttackDelay(getAttackDelay() - 1);
                if (getAttackDelay() <= 0 && isVulnerable()) setStage(BossfightStage.ATTACKING);
            }
            bossInfo.setName(getDisplayName());
            bossInfo.setPercent(MathHelper.clamp(getHealth() / getMaxHealth(), 0.0F, 1.0F));
        }

        private void scaleHealthForNearbyPlayers() {
            healthScaled = true;
            List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class,
                    getEntityBoundingBox().grow(150.0D), player -> player != null && player.isEntityAlive()
                            && !player.isSpectator());
            if (players.size() <= 1) return;
            double maximum = getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue()
                    + players.size() * 20.0D;
            getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(maximum);
            setHealth((float) maximum);
        }

        private void tickSpellCasting() {
            if (!isCastingSpell()) return;
            setSpellCastingTime(getSpellCastingTime() - 1);
            EntityLivingBase target = getAttackTarget();
            if (spellInstance != null) {
                if (target != null && target.isEntityAlive()) spellInstance.tick(target);
                else if ((getSpell().spellTime - getSpellCastingTime()) % 20 == 0) breakSpell();
            }
            applySpellProtection();
            if (getSpellCastingTime() <= 0) castSpell();
        }

        private void applySpellProtection() {
            SymbiontSpells.Type spell = getSpell();
            if (!spell.protectsCaster) return;
            double radius = spell.protectionRadius;
            for (EntityPlayer player : world.getEntitiesWithinAABB(EntityPlayer.class,
                    getEntityBoundingBox().grow(radius), p -> p != null && p.isEntityAlive())) {
                if (!entitiesToThrow.contains(player)) {
                    entitiesToThrow.add(player);
                    playSound(ModSounds.get("withered_symbiont_launch_mob"), 16.0F, 1.0F);
                }
            }
            for (int index = entitiesToThrow.size() - 1; index >= 0; index--) {
                EntityLivingBase target = entitiesToThrow.get(index);
                if (!target.isEntityAlive() || getDistance(target) > radius) {
                    entitiesToThrow.remove(index);
                    continue;
                }
                Vec3d pulled = getPositionVector().subtract(target.getPositionVector()).normalize();
                Vec3d movement = new Vec3d(pulled.x, pulled.y - 0.5D, pulled.z).scale(-spell.protectionStrength);
                target.motionX = movement.x;
                target.motionY = movement.y;
                target.motionZ = movement.z;
                target.velocityChanged = true;
            }
        }

        private void tickSmashing() {
            if (!isSmashing()) return;
            if (smashAirTime > 0) {
                smashAirTime--;
                if (smashAirTime <= 0) {
                    motionY = -5.0D;
                    velocityChanged = true;
                }
            } else if (onGround) {
                setSmashing(false);
                float strength = shouldIncreaseDifficulty() ? 2.5F : 1.5F;
                world.newExplosion(this, posX, posY, posZ, strength, false,
                        world.getGameRules().getBoolean("mobGriefing"));
            }
        }

        public BossfightStage getStage() {
            return BossfightStage.byOrdinal(dataManager.get(BOSSFIGHT_STAGE));
        }

        public void setStage(BossfightStage stage) {
            BossfightStage previous = getStage();
            if (previous != stage) previous.finish(this);
            dataManager.set(BOSSFIGHT_STAGE, stage.ordinal());
            stageTicks = 0;
            configureBossFightGoals(stage);
        }

        private void configureBossFightGoals(BossfightStage stage) {
            if (world.isRemote || bossFightGoals == null) return;
            for (EntityAIBase goal : bossFightGoals) tasks.removeTask(goal);
            if (stage == BossfightStage.ATTACKING) {
                spellsUsed = 0;
                tasks.addTask(1, prepareSpellGoal);
                tasks.addTask(2, useSpellGoal);
                tasks.addTask(3, attackGoal);
            } else if (stage == BossfightStage.SUMMONING) {
                tasks.addTask(1, summonMobsGoal);
            } else {
                tasks.addTask(1, doNothingGoal);
                playSound(ModSounds.get("withered_symbiont_power_down"), 4.0F, 1.0F);
            }
        }

        public void nextStage() {
            BossfightStage next = getStage().next();
            if (next == BossfightStage.SUMMONING && isNonBossMode()) next = next.next();
            setStage(next);
        }

        public int getStageTicks() { return stageTicks; }
        public void setStageTicks(int ticks) { stageTicks = Math.max(0, ticks); }

        public SymbiontSpells.Type getSpell() {
            return SymbiontSpells.Type.byOrdinal(dataManager.get(SPELL_TYPE));
        }

        public void setSpell(SymbiontSpells.Type spell) {
            if (spellInstance != null && getSpell() != spell) spellInstance.finish();
            dataManager.set(SPELL_TYPE, spell.ordinal());
            if (!world.isRemote) spellInstance = SymbiontSpells.create(this, spell);
        }

        public boolean hasSpell() { return getSpell() != SymbiontSpells.Type.EMPTY; }
        public boolean isCastingSpell() { return getSpellCastingTime() > 0; }
        public boolean isSummoningMobs() { return getStage() == BossfightStage.SUMMONING; }
        public boolean isVulnerable() { return getStage() == BossfightStage.VULNERABLE; }
        public int getSpellCastingTime() { return dataManager.get(SPELL_CASTING_TIME); }
        private void setSpellCastingTime(int time) { dataManager.set(SPELL_CASTING_TIME, Math.max(0, time)); }

        public void beginSpellCasting() {
            if (world.isRemote || spellInstance == null || getAttackTarget() == null) return;
            spellInstance.start(getAttackTarget());
            setSpellCastingTime(getSpell().spellTime);
        }

        public void breakSpell() {
            if (!isCastingSpell()) return;
            setSpellCastingTime(0);
            if (spellInstance != null) spellInstance.finish();
            entitiesToThrow.clear();
            if (!world.isRemote) world.setEntityState(this, (byte) 11);
        }

        private void castSpell() {
            if (spellInstance != null) {
                EntityLivingBase target = getAttackTarget();
                if (target != null && target.isEntityAlive()) spellInstance.cast(target);
                spellInstance.finish();
            }
            entitiesToThrow.clear();
        }

        @Override
        public void handleStatusUpdate(byte id) {
            if (id == 11) {
                setSpellCastingTime(0);
                entitiesToThrow.clear();
            } else if (id == 12) {
                setAttackDelay(20);
            } else {
                super.handleStatusUpdate(id);
            }
        }

        public boolean canPickSpell() { return !hasSpell() || nextSpellPickCount <= 0; }
        public void setAndCastSpell(SymbiontSpells.Type type) {
            if (type == SymbiontSpells.Type.EMPTY || isVulnerable()) return;
            nextSpellPickCount = 0;
            setSpell(type);
            useSpellGoal.nextAttackTick = ticksExisted + 1;
            playSound(ModSounds.get("withered_symbiont_prepare_spell"), 4.0F, 1.0F);
            nextSpellPickCount = 400 + rand.nextInt(400) - (shouldIncreaseDifficulty() ? 320 : 0);
            if (shouldNotGoOverHalfHealth() && getHealth() / getMaxHealth() <= 0.5F) setHalfHealthLimit(false);
        }
        public int getNextSpellPickCount() { return nextSpellPickCount; }
        public void setNextSpellPickCount(int count) { nextSpellPickCount = Math.max(0, count); }
        public int getSpellsUsed() { return spellsUsed; }
        public void spellUsed() { spellsUsed++; }
        public boolean shouldIncreaseDifficulty() { return isRushMode() || getHealth() / getMaxHealth() <= 0.5F; }
        public boolean shouldNotGoOverHalfHealth() { return !isNonBossMode() && dataManager.get(SHOULD_NOT_GO_OVER_HALF); }
        public void setHalfHealthLimit(boolean flag) { dataManager.set(SHOULD_NOT_GO_OVER_HALF, flag); }
        public boolean isSmashing() { return dataManager.get(SMASHING); }

        public void setSmashing(boolean flag) {
            dataManager.set(SMASHING, flag);
            if (flag) smashAirTime = 20;
        }

        public int getAttackDelay() { return dataManager.get(ATTACK_DELAY); }
        public boolean hasAttackDelay() { return getAttackDelay() > 0; }
        public boolean isStillAlive() { return isEntityAlive(); }
        private void setAttackDelay(int delay) { dataManager.set(ATTACK_DELAY, Math.max(0, delay)); }

        public void activateAttackDelay() {
            setAttackDelay(20);
            if (!world.isRemote) world.setEntityState(this, (byte) 12);
        }

        public float getVulnerableAnimation(float partialTicks) {
            return previousCrouchAnimation + (crouchAnimation - previousCrouchAnimation) * partialTicks;
        }

        public float getVulnerableAnim(float partialTicks) {
            return getVulnerableAnimation(partialTicks);
        }

        public float getTearAlpha(float partialTicks) {
            return previousTearAlpha + (tearAlpha - previousTearAlpha) * partialTicks;
        }

        @Override
        public boolean attackEntityAsMob(Entity target) {
            float base = (float) getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            float damage = (int) base > 0 ? base / 2.0F + rand.nextInt((int) base) : base;
            boolean attacked = target.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
            if (attacked) {
                target.motionY += 0.8D;
                target.velocityChanged = true;
                applyEnchantments(this, target);
            }
            return attacked;
        }

        @Override
        public boolean attackEntityFrom(DamageSource source, float amount) {
            if (source == DamageSource.OUT_OF_WORLD) return super.attackEntityFrom(source, amount);
            Entity attacker = source.getTrueSource();
            if (attacker == null) return false;
            if (!isVulnerable() && !attackableWhenNotVulnerable) return false;

            double angle = Math.atan2(attacker.posX - posX, attacker.posZ - posZ) * 180.0D / Math.PI;
            double difference = (-renderYawOffset - angle + 180.0D + 360.0D) % 360.0D;
            if (difference > 40.0D && difference < 320.0D) return false;
            if (isVulnerable() && getAttackDelay() <= 0) activateAttackDelay();
            if (!isVulnerable() && attacker instanceof EntityLivingBase && !isDead) {
                new SymbiontSpells.SmashSpell(this, SymbiontSpells.Type.SMASH)
                        .cast((EntityLivingBase) attacker);
            }
            if (source.isExplosion()) amount /= 4.0F;
            if (shouldNotGoOverHalfHealth()) {
                amount = Math.min(amount, Math.max(0.0F, getHealth() - getMaxHealth() * 0.5F));
            }
            float before = getHealth();
            boolean result = super.attackEntityFrom(source, amount);
            if (result && before - getHealth() >= 5.0F && attacker instanceof EntityPlayer
                    && !fightContributors.contains(attacker.getUniqueID())) {
                fightContributors.add(attacker.getUniqueID());
            }
            return result;
        }

        @Override
        public void onKillEntity(EntityLivingBase victim) {
            if (!world.isRemote) TaintingManager.convertEntity(victim);
        }

        @Override public boolean isOnLadder() { return false; }
        @Override public void fall(float distance, float damageMultiplier) { }
        @Override protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) { }

        @Override
        public void applyEntityCollision(Entity entityIn) {
            if (!world.isRemote && entityIn instanceof EntityPlayer && rand.nextInt(20) == 0) {
                setAttackTarget((EntityPlayer) entityIn);
            }
            super.applyEntityCollision(entityIn);
        }

        @Override
        public boolean startRiding(Entity entityIn, boolean force) {
            return !(entityIn instanceof EntityBoat) && !(entityIn instanceof EntityMinecart)
                    && super.startRiding(entityIn, force);
        }

        public List<EntityLivingBase> getNearbyMobTargets() {
            double range = getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getAttributeValue();
            return world.getEntitiesWithinAABB(EntityLivingBase.class, getEntityBoundingBox().grow(range), target ->
                    target != null && target != this && target.isEntityAlive()
                            && !(target instanceof SickenedMobEntity)
                            && !(target instanceof WitherStormEntity)
                            && !(target instanceof SupplementalEntities.StormPartBase)
                            && !(target instanceof TentacleEntity)
                            && !(target instanceof EntityEnderman)
                            && !(target instanceof EntityDragon)
                            && !(target instanceof EntityWither)
                            && !(target instanceof EntityWitherSkeleton)
                            && (target instanceof EntityVillager || target instanceof EntityGolem
                            || target instanceof IMob || target instanceof EntityAnimal || target instanceof EntityPlayer));
        }

        public List<EntityLivingBase> getNearbyPulseTargets() {
            double range = getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getAttributeValue();
            return world.getEntitiesWithinAABB(EntityLivingBase.class, getEntityBoundingBox().grow(range), target ->
                    target != null && target != this && target.isEntityAlive()
                            && !(target instanceof WitheredSymbiontEntity)
                            && !(target instanceof WitherStormEntity)
                            && !(target instanceof SupplementalEntities.StormPartBase)
                            && !(target instanceof TentacleEntity));
        }

        @Nullable
        public EntityLivingBase getRandomNearbyTargetOrFallback(@Nullable EntityLivingBase fallback, boolean playersOnly) {
            double range = getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getAttributeValue();
            List<EntityLivingBase> targets = world.getEntitiesWithinAABB(EntityLivingBase.class,
                    getEntityBoundingBox().grow(range), target -> target != null && target != this
                            && target != fallback && target.isEntityAlive()
                            && (!playersOnly || target instanceof EntityPlayer));
            if (!targets.isEmpty() && rand.nextInt(targets.size() + 1) != 0) {
                Collections.shuffle(targets, rand);
                return targets.get(0);
            }
            return fallback;
        }

        public void summonSupportMob(boolean illagersOnly) {
            if (world.isRemote) return;
            SickenedMobEntity mob = createWeightedSupportMob(illagersOnly, shouldIncreaseDifficulty());
            if (mob == null) return;
            BlockPos origin = new BlockPos(this);
            BlockPos spawn = null;
            for (int attempt = 0; attempt < 20 && spawn == null; attempt++) {
                BlockPos cursor = origin.add(rand.nextInt(33) - 16, rand.nextInt(17) - 8, rand.nextInt(33) - 16).up(8);
                for (int depth = 0; depth < 20; depth++, cursor = cursor.down()) {
                    BlockPos floor = cursor.down();
                    if (world.isSideSolid(floor, net.minecraft.util.EnumFacing.UP)
                            && world.isAirBlock(cursor) && world.isAirBlock(cursor.up())) {
                        spawn = cursor;
                        break;
                    }
                }
            }
            if (spawn == null) return;
            mob.setPosition(spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D);
            mob.setAttackTarget(getAttackTarget());
            mob.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(Math.max(1.0D,
                    mob.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue()
                            - (mob.getRNG().nextDouble() + 0.5D) * 2.0D));
            mob.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(Math.max(0.01D,
                    mob.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue() - 0.08D));
            mob.setHealth(mob.getMaxHealth());
            world.spawnEntity(mob);
        }

        @Nullable
        private SickenedMobEntity createWeightedSupportMob(boolean illagersOnly, boolean difficult) {
            String[] ids;
            int[] weights;
            if (illagersOnly) {
                ids = new String[] {"pillager", "vindicator"};
                weights = new int[] {3, 3};
            } else if (difficult) {
                ids = new String[] {"zombie", "villager", "skeleton", "spider", "creeper", "snow_golem",
                        "phantom", "bee", "parrot", "wolf", "cat", "pillager", "vindicator"};
                weights = new int[] {8, 6, 8, 6, 1, 1, 3, 6, 1, 2, 2, 3, 6};
            } else {
                ids = new String[] {"zombie", "villager", "skeleton", "spider", "creeper", "snow_golem",
                        "chicken", "cow", "mushroom_cow", "pig", "bee", "parrot", "wolf", "cat",
                        "pillager", "vindicator"};
                weights = new int[] {8, 4, 8, 4, 1, 2, 3, 3, 1, 3, 4, 4, 4, 4, 3, 3};
            }
            int total = 0;
            for (int weight : weights) total += weight;
            int selected = rand.nextInt(total);
            String id = ids[ids.length - 1];
            for (int index = 0; index < weights.length; index++) {
                selected -= weights[index];
                if (selected < 0) { id = ids[index]; break; }
            }
            if ("zombie".equals(id)) return new SickenedZombieEntity(world);
            if ("villager".equals(id)) return new SickenedVillagerEntity(world);
            if ("skeleton".equals(id)) return new SickenedSkeletonEntity(world);
            if ("spider".equals(id)) return new SickenedSpiderEntity(world);
            if ("creeper".equals(id)) return new SickenedCreeperEntity(world);
            if ("snow_golem".equals(id)) return new SickenedSnowGolemEntity(world);
            if ("chicken".equals(id)) return new SickenedChickenEntity(world);
            if ("cow".equals(id)) return new SickenedCowEntity(world);
            if ("mushroom_cow".equals(id)) return new SickenedMushroomCowEntity(world);
            if ("pig".equals(id)) return new SickenedPigEntity(world);
            if ("bee".equals(id)) return new SickenedBeeEntity(world);
            if ("parrot".equals(id)) return new SickenedParrotEntity(world);
            if ("wolf".equals(id)) return new SickenedWolfEntity(world);
            if ("cat".equals(id)) return new SickenedCatEntity(world);
            if ("pillager".equals(id)) return new SickenedPillagerEntity(world);
            return new SickenedVindicatorEntity(world);
        }

        public void setOwner(@Nullable WitherStormEntity owner) {
            ownerUuid = owner == null ? null : owner.getUniqueID();
        }

        @Nullable
        public WitherStormEntity getOwner() {
            if (ownerUuid == null) return null;
            for (Entity entity : world.loadedEntityList) {
                if (entity instanceof WitherStormEntity && ownerUuid.equals(entity.getUniqueID())) {
                    return (WitherStormEntity) entity;
                }
            }
            return null;
        }

        public boolean isNonBossMode() { return dataManager.get(NON_BOSS_MODE); }

        public boolean canBeAttackedWhenNotVulnerable() { return attackableWhenNotVulnerable; }
        public void setAttackableWhenNotVulnerable(boolean value) { attackableWhenNotVulnerable = value; }

        public void setNonBossMode(boolean mode) {
            dataManager.set(NON_BOSS_MODE, mode);
            experienceValue = mode ? 25 : 150;
            bossInfo.setVisible(!mode);
        }

        public boolean isRushMode() { return dataManager.get(RUSH_MODE); }
        public void setRushMode(boolean mode) { dataManager.set(RUSH_MODE, mode); }

        @Override
        protected boolean canDespawn() {
            return isNonBossMode() && !isConverting();
        }

        @Override
        public void addTrackingPlayer(EntityPlayerMP player) {
            super.addTrackingPlayer(player);
            if (!isNonBossMode()) bossInfo.addPlayer(player);
        }

        @Override
        public void removeTrackingPlayer(EntityPlayerMP player) {
            super.removeTrackingPlayer(player);
            bossInfo.removePlayer(player);
        }

        @Override
        public void setCustomNameTag(String name) {
            super.setCustomNameTag(name);
            bossInfo.setName(getDisplayName());
        }

        @Override protected SoundEvent getAmbientSound() { return isVulnerable() ? null : ModSounds.get("withered_symbiont_ambient"); }
        @Override protected SoundEvent getHurtSound(DamageSource source) { return ModSounds.get("withered_symbiont_hurt"); }
        @Override protected SoundEvent getDeathSound() { return ModSounds.get(isNonBossMode() ? "withered_symbiont_normal_death" : "withered_symbiont_death"); }
        @Override protected float getSoundVolume() { return isDead ? 1.0F : super.getSoundVolume(); }
        protected float getDeathMaxRotation() { return 1.0F; }

        protected void playStepSound(BlockPos pos, IBlockState blockIn) {
            playSound(ModSounds.get("withered_symbiont_step"), 0.3F, 1.0F);
        }

        @Override
        protected void dropLoot(boolean wasRecentlyHit, int lootingModifier, DamageSource source) {
            if (isNonBossMode() || !(world instanceof WorldServer)) {
                super.dropLoot(wasRecentlyHit, lootingModifier, source);
                return;
            }
            ResourceLocation tableId = getLootTable();
            if (tableId == null) return;
            LootTable table = world.getLootTableManager().getLootTableFromLocation(tableId);
            LootContext.Builder context = new LootContext.Builder((WorldServer) world)
                    .withLootedEntity(this).withDamageSource(source);
            if (wasRecentlyHit && attackingPlayer != null) {
                context.withPlayer(attackingPlayer).withLuck(attackingPlayer.getLuck());
            }
            dropItems.clear();
            dropItems.addAll(table.generateLootForPools(rand, context.build()));
        }

        @Override
        protected void onDeathUpdate() {
            if (isNonBossMode()) {
                super.onDeathUpdate();
                return;
            }
            deathTime++;
            if (world instanceof WorldServer) {
                int particles = Math.max(0, (320 - deathTime) / 40);
                ((WorldServer) world).spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL,
                        posX, posY + height * 0.5D, posZ, particles,
                        width * 0.5D, height * 0.5D, width * 0.5D, 0.02D);
            }
            float turn = MathHelper.clamp(MathHelper.wrapDegrees(-50.0F - rotationPitch), -3.0F, 3.0F);
            rotationPitch += turn;
            if (deathTime < 320) return;
            if (!world.isRemote) {
                distributeCapturedDrops();
                if (world.getGameRules().getBoolean("doMobLoot")) {
                    int experience = getExperiencePoints(attackingPlayer);
                    while (experience > 0) {
                        int split = EntityXPOrb.getXPSplit(experience);
                        experience -= split;
                        world.spawnEntity(new EntityXPOrb(world, posX, posY, posZ, split));
                    }
                }
                if (world instanceof WorldServer) {
                    ((WorldServer) world).spawnParticle(EnumParticleTypes.EXPLOSION_HUGE,
                            posX, posY + height * 0.5D, posZ, 20,
                            width * 0.5D, height * 0.5D, width * 0.5D, 0.02D);
                }
            }
            setDead();
        }

        private void distributeCapturedDrops() {
            List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class,
                    getEntityBoundingBox().grow(20.0D), player -> player != null && player.isEntityAlive());
            if (players.size() > 1 && !fightContributors.isEmpty()) {
                for (UUID contributor : fightContributors) {
                    for (EntityPlayer player : players) {
                        if (!player.getUniqueID().equals(contributor)) continue;
                        for (ItemStack stack : dropItems) {
                            ItemStack copy = stack.copy();
                            if (!player.inventory.addItemStackToInventory(copy) && !copy.isEmpty()) {
                                EntityItem dropped = player.entityDropItem(copy, 0.0F);
                                if (dropped != null) dropped.setOwner(player.getName());
                            }
                        }
                    }
                }
            } else {
                for (ItemStack stack : dropItems) {
                    EntityItem dropped = entityDropItem(stack.copy(), 8.0F);
                    if (dropped != null) {
                        dropped.motionX = 0.0D;
                        dropped.motionY = -0.08D;
                        dropped.motionZ = 0.0D;
                        dropped.setNoPickupDelay();
                    }
                }
            }
            dropItems.clear();
        }

        @Override
        public void onDeath(DamageSource cause) {
            super.onDeath(cause);
            if (world.isRemote) return;
            WitherStormEntity owner = getOwner();
            Entity source = cause == null ? null : cause.getTrueSource();
            if (source instanceof EntityPlayer) {
                SymbiontSummoningManager.markKilledSymbiont((EntityPlayer) source, owner);
            }
            for (EntityPlayer player : world.getEntitiesWithinAABB(EntityPlayer.class,
                    getEntityBoundingBox().grow(20.0D))) {
                SymbiontSummoningManager.markKilledSymbiont(player, owner);
            }
        }

        @Override
        public void writeEntityToNBT(NBTTagCompound compound) {
            super.writeEntityToNBT(compound);
            if (ownerUuid != null) compound.setUniqueId("WitherStormOwner", ownerUuid);
            compound.setBoolean("IsNonBossMode", isNonBossMode());
            compound.setBoolean("IsRushMode", isRushMode());
            compound.setInteger("Stage", getStage().ordinal());
            compound.setInteger("StageTicks", stageTicks);
            compound.setInteger("Spell", getSpell().ordinal());
            compound.setInteger("SpellCastingTicks", getSpellCastingTime());
            compound.setInteger("NextSpellPick", nextSpellPickCount);
            compound.setBoolean("Smashing", isSmashing());
            compound.setInteger("SmashAirTime", smashAirTime);
            compound.setInteger("SpellsUsed", spellsUsed);
            compound.setInteger("AttackDelay", getAttackDelay());
            compound.setBoolean("ShouldNotGoOverHalf", dataManager.get(SHOULD_NOT_GO_OVER_HALF));
            compound.setBoolean("HealthScaled", healthScaled);
            compound.setBoolean("AttackableWhenNotVulnerable", attackableWhenNotVulnerable);
            NBTTagList savedDrops = new NBTTagList();
            for (ItemStack stack : dropItems) savedDrops.appendTag(stack.writeToNBT(new NBTTagCompound()));
            compound.setTag("DropItems", savedDrops);
            NBTTagList contributors = new NBTTagList();
            for (UUID id : fightContributors) contributors.appendTag(new NBTTagString(id.toString()));
            compound.setTag("FightContributors", contributors);
        }

        @Override
        public void readEntityFromNBT(NBTTagCompound compound) {
            super.readEntityFromNBT(compound);
            ownerUuid = compound.hasUniqueId("WitherStormOwner") ? compound.getUniqueId("WitherStormOwner") : null;
            setNonBossMode(compound.getBoolean("IsNonBossMode"));
            setRushMode(compound.getBoolean("IsRushMode"));
            setStage(BossfightStage.byOrdinal(compound.getInteger("Stage")));
            stageTicks = compound.getInteger("StageTicks");
            setSpell(SymbiontSpells.Type.byOrdinal(compound.getInteger("Spell")));
            setSpellCastingTime(compound.getInteger("SpellCastingTicks"));
            nextSpellPickCount = compound.getInteger("NextSpellPick");
            setSmashing(compound.getBoolean("Smashing"));
            smashAirTime = compound.getInteger("SmashAirTime");
            spellsUsed = compound.getInteger("SpellsUsed");
            setAttackDelay(compound.getInteger("AttackDelay"));
            if (compound.hasKey("ShouldNotGoOverHalf")) {
                dataManager.set(SHOULD_NOT_GO_OVER_HALF, compound.getBoolean("ShouldNotGoOverHalf"));
            }
            healthScaled = compound.getBoolean("HealthScaled");
            attackableWhenNotVulnerable = compound.getBoolean("AttackableWhenNotVulnerable");
            dropItems.clear();
            NBTTagList savedDrops = compound.getTagList("DropItems", 10);
            for (int index = 0; index < savedDrops.tagCount(); index++) {
                dropItems.add(new ItemStack(savedDrops.getCompoundTagAt(index)));
            }
            fightContributors.clear();
            NBTTagList contributors = compound.getTagList("FightContributors", 8);
            for (int index = 0; index < contributors.tagCount(); index++) {
                try { fightContributors.add(UUID.fromString(contributors.getStringTagAt(index))); }
                catch (IllegalArgumentException ignored) { }
            }
        }

        public enum BossfightStage {
            ATTACKING {
                @Override boolean shouldMoveToNextStage(WitheredSymbiontEntity entity) {
                    return entity.getSpellsUsed() > 5 && !entity.isCastingSpell()
                            && entity.getStageTicks() % 80 == 0 && entity.getAttackTarget() != null;
                }

                @Override void finish(WitheredSymbiontEntity entity) {
                    entity.spellsUsed = 0;
                    entity.setSpell(SymbiontSpells.Type.EMPTY);
                }
            },
            SUMMONING,
            VULNERABLE {
                @Override boolean shouldMoveToNextStage(WitheredSymbiontEntity entity) {
                    return entity.getStageTicks() > 4800;
                }
            };

            boolean shouldMoveToNextStage(WitheredSymbiontEntity entity) { return false; }
            void finish(WitheredSymbiontEntity entity) { }
            BossfightStage next() { return values()[(ordinal() + 1) % values().length]; }
            static BossfightStage byOrdinal(int value) {
                return value >= 0 && value < values().length ? values()[value] : ATTACKING;
            }
        }

        private static final class SymbiontAttackGoal extends EntityAIAttackMelee {
            private final WitheredSymbiontEntity entity;
            private SymbiontAttackGoal(WitheredSymbiontEntity entity) { super(entity, 1.0D, true); this.entity = entity; }
            @Override public boolean shouldExecute() {
                return entity.getStage() == BossfightStage.ATTACKING && !entity.isCastingSpell() && super.shouldExecute();
            }
            @Override public boolean shouldContinueExecuting() {
                return entity.getStage() == BossfightStage.ATTACKING && !entity.isCastingSpell()
                        && super.shouldContinueExecuting();
            }
        }

        private static final class PrepareSpellGoal extends EntityAIBase {
            private final WitheredSymbiontEntity entity;
            private PrepareSpellGoal(WitheredSymbiontEntity entity) { this.entity = entity; }
            @Override public boolean shouldExecute() {
                EntityLivingBase target = entity.getAttackTarget();
                return target != null && target.isEntityAlive() && !entity.isCastingSpell() && entity.canPickSpell();
            }
            @Override public boolean shouldContinueExecuting() {
                EntityLivingBase target = entity.getAttackTarget();
                return target != null && target.isEntityAlive() && entity.canPickSpell();
            }
            @Override public void startExecuting() {
                List<SymbiontSpells.Type> spells = new ArrayList<SymbiontSpells.Type>();
                for (SymbiontSpells.Type spell : SymbiontSpells.Type.values()) {
                    if (spell != SymbiontSpells.Type.EMPTY && spell != entity.getSpell()) spells.add(spell);
                }
                entity.setSpell(spells.get(entity.getRNG().nextInt(spells.size())));
                entity.useSpellGoal.nextAttackTick = entity.ticksExisted + 40 + entity.getRNG().nextInt(20);
                entity.playSound(ModSounds.get("withered_symbiont_prepare_spell"), 4.0F, 1.0F);
                entity.nextSpellPickCount = 400 + entity.getRNG().nextInt(400)
                        - (entity.shouldIncreaseDifficulty() ? 320 : 0);
                if (entity.shouldNotGoOverHalfHealth() && entity.getHealth() / entity.getMaxHealth() <= 0.5F) {
                    entity.setHalfHealthLimit(false);
                }
            }
        }

        private static final class UseSpellGoal extends EntityAIBase {
            private final WitheredSymbiontEntity entity;
            private int nextAttackTick;
            private UseSpellGoal(WitheredSymbiontEntity entity) { this.entity = entity; setMutexBits(1); }
            @Override public boolean shouldExecute() {
                EntityLivingBase target = entity.getAttackTarget();
                return target != null && target.isEntityAlive() && !entity.isCastingSpell()
                        && entity.hasSpell() && entity.spellInstance != null && entity.ticksExisted > nextAttackTick;
            }
            @Override public boolean shouldContinueExecuting() {
                EntityLivingBase target = entity.getAttackTarget();
                return target != null && target.isEntityAlive() && entity.hasSpell()
                        && entity.ticksExisted >= nextAttackTick || entity.isCastingSpell();
            }
            @Override public void startExecuting() {
                float modifier = worldDifficulty(entity) + (entity.shouldIncreaseDifficulty() ? 60.0F : 0.0F);
                int delay = entity.spellInstance.getDelay(modifier);
                delay = Math.max(delay, entity.getSpell().spellTime + 10);
                nextAttackTick = entity.ticksExisted + delay;
                entity.playSound(ModSounds.get("withered_symbiont_cast_spell"), 4.0F, 1.0F);
                entity.beginSpellCasting();
                if (entity.getAttackTarget() instanceof EntityPlayer) entity.spellUsed();
            }
            private static float worldDifficulty(WitheredSymbiontEntity entity) {
                return entity.world.getDifficultyForLocation(new BlockPos(entity)).getAdditionalDifficulty();
            }
        }

        private static final class SummonMobsGoal extends EntityAIBase {
            private final WitheredSymbiontEntity entity;
            private int time;
            private SummonMobsGoal(WitheredSymbiontEntity entity) { this.entity = entity; setMutexBits(7); }
            @Override public boolean shouldExecute() {
                EntityLivingBase target = entity.getAttackTarget();
                return target != null && target.isEntityAlive();
            }
            @Override public boolean shouldContinueExecuting() { return shouldExecute() && time > 0; }
            @Override public void startExecuting() {
                time = 60 + entity.getRNG().nextInt(60) + (entity.shouldIncreaseDifficulty() ? 40 : 0);
                entity.playSound(ModSounds.get("withered_symbiont_summon"), 4.0F, 1.0F);
            }
            @Override public void updateTask() {
                if (time > 0 && --time % 10 == 0) entity.summonSupportMob(false);
            }
            @Override public void resetTask() { entity.nextStage(); }
        }

        private static final class DoNothingGoal extends EntityAIBase {
            private final WitheredSymbiontEntity entity;
            private DoNothingGoal(WitheredSymbiontEntity entity) { this.entity = entity; setMutexBits(7); }
            @Override public boolean shouldExecute() { return entity.isVulnerable(); }
            @Override public boolean shouldContinueExecuting() { return entity.isVulnerable(); }
            @Override public void updateTask() {
                float turn = MathHelper.clamp(MathHelper.wrapDegrees(55.0F - entity.rotationPitch), -3.0F, 3.0F);
                entity.rotationPitch += turn;
            }
        }
    }

    public static class TaintedSlimeEntity extends EntitySlime {
        public TaintedSlimeEntity(World world) {
            super(world);
        }
    }

    private static class RandomFlyingAI extends EntityAIBase {
        private final FlyingSickenedMob entity;
        private final double speed;
        private final int horizontalRange;
        private final int verticalRange;

        RandomFlyingAI(FlyingSickenedMob entity, double speed, int horizontalRange, int verticalRange) {
            this.entity = entity;
            this.speed = speed;
            this.horizontalRange = horizontalRange;
            this.verticalRange = verticalRange;
        }

        @Override
        public boolean shouldExecute() {
            return entity.getAttackTarget() == null && (!entity.getMoveHelper().isUpdating() || entity.getRNG().nextInt(5) == 0);
        }

        @Override
        public void startExecuting() {
            double x = entity.posX + entity.getRNG().nextInt(horizontalRange * 2 + 1) - horizontalRange;
            double y = entity.posY + entity.getRNG().nextInt(verticalRange * 2 + 1) - verticalRange;
            double z = entity.posZ + entity.getRNG().nextInt(horizontalRange * 2 + 1) - horizontalRange;
            entity.getMoveHelper().setMoveTo(x, Math.max(2.0D, y), z, speed);
        }

        @Override public boolean shouldContinueExecuting() { return false; }
    }

    private static class FlyingAttackAI extends EntityAIBase {
        private final FlyingSickenedMob entity;
        private final double speed;
        private final int attackDelay;
        private int cooldown;

        FlyingAttackAI(FlyingSickenedMob entity, double speed, int attackDelay) {
            this.entity = entity;
            this.speed = speed;
            this.attackDelay = attackDelay;
            setMutexBits(1);
        }

        @Override public boolean shouldExecute() { return entity.getAttackTarget() != null; }
        @Override public boolean shouldContinueExecuting() { return shouldExecute(); }

        @Override
        public void updateTask() {
            EntityLivingBase target = entity.getAttackTarget();
            if (target == null) return;
            if (cooldown > 0) --cooldown;
            entity.getMoveHelper().setMoveTo(target.posX, target.posY + target.getEyeHeight() * 0.5D, target.posZ, speed);
            entity.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
            double reach = entity.width + target.width + 0.8D;
            if (cooldown <= 0 && entity.getDistanceSq(target) <= reach * reach) {
                entity.attackEntityAsMob(target);
                cooldown = attackDelay;
                entity.getMoveHelper().setMoveTo(entity.posX, entity.posY + 4.0D, entity.posZ, 0.8D);
            }
        }
    }

    private static void fireSickenedArrow(SickenedMobEntity shooter, EntityLivingBase target, float distanceFactor) {
        EntityTippedArrow arrow = new EntityTippedArrow(shooter.world, shooter);
        double dx = target.posX - shooter.posX;
        double dy = target.getEntityBoundingBox().minY + target.height / 3.0F - arrow.posY;
        double dz = target.posZ - shooter.posZ;
        double arc = MathHelper.sqrt(dx * dx + dz * dz);
        arrow.shoot(dx, dy + arc * 0.2D, dz, 1.6F, 14 - shooter.world.getDifficulty().getId() * 4);
        arrow.setDamage(2.0D + distanceFactor * 2.0D);
        shooter.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (shooter.getRNG().nextFloat() * 0.4F + 0.8F));
        shooter.world.spawnEntity(arrow);
    }

    private static class SickenedSnowball extends EntitySnowball {
        private final boolean potent;

        SickenedSnowball(World world, EntityLivingBase thrower, boolean potent) {
            super(world, thrower);
            this.potent = potent;
        }

        @Override
        protected void onImpact(RayTraceResult result) {
            if (!world.isRemote && potent && result.entityHit instanceof EntityLivingBase) {
                int difficulty = (int) world.getDifficultyForLocation(new BlockPos(result.entityHit)).getAdditionalDifficulty();
                ((EntityLivingBase) result.entityHit).addPotionEffect(new PotionEffect(MobEffects.WITHER, 75 * difficulty, 1));
            }
            super.onImpact(result);
        }
    }
}
