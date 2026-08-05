package com.wdcftgg.witherstormmod.common.entity;

import com.wdcftgg.witherstormmod.common.init.ModItems;
import com.wdcftgg.witherstormmod.common.init.ModEffects;
import com.wdcftgg.witherstormmod.common.init.ModSounds;
import com.wdcftgg.witherstormmod.common.taint.TaintingManager;
import net.minecraft.block.BlockBed;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class EntitySickenedMob extends EntityMob {

    private static final DataParameter<Boolean> CONVERTING = EntityDataManager.createKey(EntitySickenedMob.class, DataSerializers.BOOLEAN);

    private static final java.util.Set<String> UPSTREAM_LOOT_TABLES = new java.util.HashSet<String>(java.util.Arrays.asList(
            "sickened_chicken", "sickened_cow", "sickened_creeper", "sickened_iron_golem",
            "sickened_mushroom_cow", "sickened_phantom", "sickened_pig", "sickened_skeleton",
            "sickened_snow_golem", "sickened_spider", "sickened_villager", "sickened_zombie",
            "withered_symbiont"));

    private int conversionTime = -1;
    private UUID conversionStarter;
    private ResourceLocation originalType;
    private NBTTagCompound originalData;

    protected EntitySickenedMob(World worldIn) {
        super(worldIn);
        experienceValue = 8;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(CONVERTING, false);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(2, new EntityAIAttackMelee(this, 1.15D, true));
        tasks.addTask(6, new EntityAIWanderAvoidWater(this, 0.9D));
        tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 12.0F));
        tasks.addTask(8, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true));
        addSickenedMobTargetGoal(3);
    }

    protected final void addSickenedMobTargetGoal(int priority) {
        targetTasks.addTask(priority, new EntityAINearestAttackableTarget<EntityLiving>(this, EntityLiving.class, 10, true, false,
                target -> target != null
                        && !(target instanceof EntitySickenedMob)
                        && !(target instanceof EntityWitherStormLegacy)
                        && !(target instanceof SupplementalEntities.StormPartBase)
                        && !(target instanceof EntityWither)
                        && !(target instanceof EntityWitherSkeleton)
                        && !(target instanceof EntityCreeper)
                        && !(target instanceof EntityEnderman)));
    }

    protected final void initStandardAnimalAI(double attackSpeed) {
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new EntityAIAttackMelee(this, attackSpeed, false));
        tasks.addTask(2, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(3, new EntityAIWanderAvoidWater(this, 1.0D));
        tasks.addTask(4, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true));
        addSickenedMobTargetGoal(2);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(getSickenedHealth());
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(getSickenedSpeed());
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(getSickenedDamage());
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(getSickenedFollowRange());
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(getSickenedArmor());
        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(getSickenedKnockbackResistance());
    }

    protected double getSickenedHealth() {
        return 24.0D;
    }

    protected double getSickenedSpeed() {
        return 0.28D;
    }

    protected double getSickenedDamage() {
        return 5.0D;
    }

    protected double getSickenedFollowRange() {
        return 32.0D;
    }

    protected double getSickenedArmor() {
        return 0.0D;
    }

    protected double getSickenedKnockbackResistance() {
        return 0.0D;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!world.isRemote && isEntityAlive() && isConverting()) {
            conversionTime -= getConversionProgress();
            if (conversionTime <= 0) {
                TaintingManager.cureEntity(this);
            }
        }
    }

    private int getConversionProgress() {
        int progress = 1;
        if (rand.nextFloat() >= 0.01F) return progress;
        int found = 0;
        BlockPos origin = new BlockPos((int) posX, (int) posY, (int) posZ);
        for (int x = -4; x < 4 && found < 14; x++) {
            for (int y = -4; y < 4 && found < 14; y++) {
                for (int z = -4; z < 4 && found < 14; z++) {
                    net.minecraft.block.Block block = world.getBlockState(origin.add(x, y, z)).getBlock();
                    if (block != Blocks.IRON_BARS && !(block instanceof BlockBed)) continue;
                    if (rand.nextFloat() < 0.3F) progress++;
                    found++;
                }
            }
        }
        return progress;
    }

    public void startConverting(@Nullable UUID player, int duration) {
        if (getOriginalType() == null || isConverting()) return;
        conversionStarter = player;
        conversionTime = duration;
        dataManager.set(CONVERTING, true);
        int amplifier = Math.min(world.getDifficulty().getId() - 1, 0);
        addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, duration, amplifier));
        playSound(net.minecraft.init.SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0F + rand.nextFloat(), rand.nextFloat() * 0.7F + 0.3F);
    }

    public boolean isConverting() {
        return dataManager.get(CONVERTING);
    }

    public int getConversionTime() {
        return conversionTime;
    }

    @Nullable
    public UUID getConversionStarter() {
        return conversionStarter;
    }

    public void rememberOriginal(EntityLivingBase original) {
        originalType = EntityList.getKey(original);
        NBTTagCompound saved = new NBTTagCompound();
        original.writeToNBT(saved);
        saved.removeTag("UUIDMost");
        saved.removeTag("UUIDLeast");
        saved.removeTag("Pos");
        saved.removeTag("Motion");
        saved.removeTag("Rotation");
        saved.removeTag("Dimension");
        saved.removeTag("id");
        originalData = saved;
    }

    @Nullable
    public ResourceLocation getOriginalType() {
        return originalType != null ? originalType : TaintingManager.getOriginalType(getSickenedType());
    }

    @Nullable
    public NBTTagCompound getOriginalData() {
        return originalData == null ? null : originalData.copy();
    }

    public abstract String getSickenedType();

    @Nullable
    @Override
    protected ResourceLocation getLootTable() {
        String type = getSickenedType();
        return UPSTREAM_LOOT_TABLES.contains(type)
                ? new ResourceLocation("witherstormmod", "entities/" + type)
                : null;
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.UNDEAD;
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        boolean attacked = super.attackEntityAsMob(entityIn);
        if (attacked && getHeldItemMainhand().isEmpty() && entityIn instanceof EntityLivingBase) {
            int difficulty = (int) world.getDifficultyForLocation(new BlockPos(this)).getAdditionalDifficulty();
            ((EntityLivingBase) entityIn).addPotionEffect(new PotionEffect(MobEffects.WITHER, 120 * difficulty));
        }
        return attacked;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        Entity projectile = source.getImmediateSource();
        Entity shooter = null;
        if (projectile instanceof EntityArrow) {
            shooter = ((EntityArrow) projectile).shootingEntity;
        } else if (projectile instanceof EntityFireball) {
            shooter = ((EntityFireball) projectile).shootingEntity;
        }
        if (shooter instanceof EntitySickenedMob) {
            return false;
        }
        return super.attackEntityFrom(source, amount);
    }

    @Override
    public void onKillEntity(EntityLivingBase victim) {
        super.onKillEntity(victim);
        if (!world.isRemote && TaintingManager.convertEntity(victim)) {
            int amount = getInfectedHealAmount();
            if (amount > 0) {
                heal(amount);
            }
        }
    }

    protected int getInfectedHealAmount() {
        return 8;
    }

    @Override
    public boolean isPotionApplicable(PotionEffect effect) {
        return effect.getPotion() != ModEffects.WITHER_SICKNESS
                && effect.getPotion() != MobEffects.WITHER
                && super.isPotionApplicable(effect);
    }

    @Override
    public boolean canAttackClass(Class<? extends EntityLivingBase> cls) {
        return !EntitySickenedMob.class.isAssignableFrom(cls)
                && cls != EntityWitherStormLegacy.class
                && !SupplementalEntities.StormPartBase.class.isAssignableFrom(cls)
                && super.canAttackClass(cls);
    }

    @Override
    protected boolean canDespawn() {
        return !isConverting();
    }

    @Override
    protected float getSoundPitch() {
        return (rand.nextFloat() - rand.nextFloat()) * 0.2F + 0.85F;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (originalType != null) compound.setString("OriginalType", originalType.toString());
        if (originalData != null) compound.setTag("OriginalData", originalData);
        compound.setInteger("ConversionTime", isConverting() ? conversionTime : -1);
        if (conversionStarter != null) compound.setUniqueId("ConversionPlayer", conversionStarter);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("OriginalType", 8)) originalType = new ResourceLocation(compound.getString("OriginalType"));
        if (compound.hasKey("OriginalData", 10)) originalData = compound.getCompoundTag("OriginalData");
        int savedTime = compound.getInteger("ConversionTime");
        if (savedTime > -1) {
            conversionStarter = compound.hasUniqueId("ConversionPlayer") ? compound.getUniqueId("ConversionPlayer") : null;
            conversionTime = savedTime;
            dataManager.set(CONVERTING, true);
        }
    }

    @Nullable
    @Override
    protected Item getDropItem() {
        return rand.nextBoolean() ? ModItems.get("withered_flesh") : ModItems.get("withered_bone");
    }
}
