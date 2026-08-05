package com.wdcftgg.witherstormmod.common.tile;

import com.wdcftgg.witherstormmod.WitherStormMod;
import com.wdcftgg.witherstormmod.common.advancement.LegacyCriteriaTriggers;
import com.wdcftgg.witherstormmod.common.beacon.LegacySuperBeaconRecipes;
import com.wdcftgg.witherstormmod.common.beacon.LegacySuperBeaconLogic;
import com.wdcftgg.witherstormmod.common.config.LegacyWitherStormConfig;
import com.wdcftgg.witherstormmod.common.entity.EntityWitherStormLegacy;
import com.wdcftgg.witherstormmod.common.entity.SupplementalEntities;
import com.wdcftgg.witherstormmod.common.init.ModBlocks;
import com.wdcftgg.witherstormmod.common.init.ModSounds;
import com.wdcftgg.witherstormmod.common.network.LegacyNetwork;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TileEntitySuperBeacon extends TileEntityAbstractSuperBeacon implements ITickable, IInventory {

    public static final int MAX_ITEMS = 16;
    public static final int RESUMMON_START = LegacySuperBeaconLogic.RESUMMON_START;
    public static final int RESUMMON_TIME = LegacySuperBeaconLogic.RESUMMON_TIME;
    private static final double RESUMMON_ADVANCEMENT_RANGE = 100.0D;

    private final NonNullList<ItemStack> items = NonNullList.withSize(MAX_ITEMS, ItemStack.EMPTY);
    private final Map<SupportColor, BlockPos> connected = new EnumMap<SupportColor, BlockPos>(SupportColor.class);
    private final List<SupplementalEntities.BlockCluster> resummonClusters =
            new ArrayList<SupplementalEntities.BlockCluster>();
    private int resummonTicks;
    private String resummoningEntity = "";
    private String resummoningEntityNbt = "";
    private float shakeX;
    private float previousShakeX;
    private float shakeZ;
    private float previousShakeZ;

    @Override
    public void update() {
        if (world == null) return;
        int previousLevel = beaconLevel;
        beaconLevel = calculateBeaconLevel();
        if (beaconLevel != previousLevel) {
            boolean wasActive = active;
            setActive(beaconLevel > 0);
            if (active == wasActive) markAndNotify();
        }
        findSupportBeacons();
        if (effect != null && !getValidEffects().contains(effect)) effect = null;
        tickBeaconBase();
        if (world.isRemote) {
            if (isDoingResummonAnimation()) {
                resummonTicks++;
                updateResummonShake();
                if (resummonTicks > RESUMMON_START) {
                    WitherStormMod.proxy.spawnSuperBeaconResummonParticle(world, pos, world.rand);
                }
            } else {
                resetShake();
            }
            return;
        }

        if (isDoingResummonAnimation()) {
            tickResummoning();
            return;
        }

        LegacySuperBeaconRecipes.Recipe recipe = LegacySuperBeaconRecipes.find(items,
                condition -> canCraftCondition(condition));
        if (recipe == null) return;
        if (recipe.isEntityRecipe()) {
            resummoningEntity = recipe.entity;
            resummoningEntityNbt = recipe.entityNbt;
            resummonTicks = 0;
            activateResummonAnimation();
            markAndNotify();
        } else {
            ItemStack result = recipe.createResult();
            if (!result.isEmpty()) {
                clear();
                EntityItem item = new EntityItem(world, pos.getX() + 0.5D, pos.getY() + 2.0D, pos.getZ() + 0.5D, result);
                item.setNoDespawn();
                world.spawnEntity(item);
                LegacyNetwork.sendSuperBeaconParticles(world, pos,
                        LegacyNetwork.SUPER_BEACON_ITEM_BURST);
                playSoundAt(pos, "command_block_activates", 10.0F, 1.0F);
            }
        }
    }

    @Override
    protected void applyEffect() {
        int amplifier = Math.max(0, beaconLevel - 1);
        for (net.minecraft.entity.player.EntityPlayer player : world.playerEntities) {
            if (player.getDistanceSqToCenter(pos) <= LegacySuperBeaconLogic.MAIN_EFFECT_RADIUS
                    * LegacySuperBeaconLogic.MAIN_EFFECT_RADIUS) {
                player.addPotionEffect(new PotionEffect(effect,
                        LegacySuperBeaconLogic.MAIN_EFFECT_DURATION, amplifier, true, true));
            }
        }
    }

    @Override
    public Set<Potion> getValidEffects() {
        return LegacySuperBeaconLogic.getMainEffects();
    }

    @Override
    public int[] getBeamColor() {
        return new int[] {14, 62, 232};
    }

    @Override
    public float getBeamThickness() {
        return 0.25F;
    }

    @Override
    public float getOuterBeamThickness() {
        return 0.45F;
    }

    @Override
    public void doPowerUp(net.minecraft.entity.player.EntityPlayerMP player) {
        super.doPowerUp(player);
        NBTTagCompound persistent = player.getEntityData().getCompoundTag(
                net.minecraft.entity.player.EntityPlayer.PERSISTED_NBT_TAG);
        if (persistent.getBoolean("WitherStormActivatedSuperBeacon")
                || connected.size() < SupportColor.values().length || isPoweringUp()) return;
        poweringUpAnimation = LegacySuperBeaconLogic.POWER_UP_ANIMATION_TIME;
        activationAnimation = 0.0F;
        previousActivationAnimation = 0.0F;
        doActivationSequence();
        for (BlockPos supportPos : connected.values()) {
            net.minecraft.tileentity.TileEntity tile = world.getTileEntity(supportPos);
            if (tile instanceof TileEntityAbstractSuperBeacon) {
                TileEntityAbstractSuperBeacon support = (TileEntityAbstractSuperBeacon) tile;
                support.poweringUpAnimation = LegacySuperBeaconLogic.POWER_UP_ANIMATION_TIME;
                support.activationAnimation = 0.0F;
                support.previousActivationAnimation = 0.0F;
                support.doActivationSequence();
            }
        }
        com.wdcftgg.witherstormmod.common.network.LegacyNetwork.playGlobalSoundAll(
                world, com.wdcftgg.witherstormmod.common.init.ModSounds.get("withered_beacon_power_up"), 1.0F, 1.0F);
        markAndNotify();
    }

    @Override
    protected void doPoweringUpAnimation() {
        if (!world.isRemote && poweringUpAnimation == LegacySuperBeaconLogic.POWER_UP_CLIMAX) {
            com.wdcftgg.witherstormmod.common.network.LegacyNetwork.shakeAll(world, 120.0F, 12.0F);
            world.playEvent(2003, pos.up(), 0);
            for (net.minecraft.entity.player.EntityPlayer nearby : world.playerEntities) {
                if (nearby.getDistanceSqToCenter(pos) > 64.0D * 64.0D) continue;
                NBTTagCompound persistent = nearby.getEntityData().getCompoundTag(
                        net.minecraft.entity.player.EntityPlayer.PERSISTED_NBT_TAG);
                persistent.setBoolean("WitherStormActivatedSuperBeacon", true);
                nearby.getEntityData().setTag(
                        net.minecraft.entity.player.EntityPlayer.PERSISTED_NBT_TAG, persistent);
                if (nearby instanceof EntityPlayerMP) {
                    LegacyCriteriaTriggers.ACTIVATE_SUPER_BEACON.trigger(
                            (EntityPlayerMP) nearby, connected.size());
                }
            }
        }
    }

    private void tickResummoning() {
        resummonTicks++;
        updateResummonShake();
        boolean witherStorm = LegacySuperBeaconLogic.isWitherStormResummon(resummoningEntity);
        if (resummonTicks == RESUMMON_START) {
            clear();
            LegacyNetwork.sendSuperBeaconParticles(world, pos,
                    LegacyNetwork.SUPER_BEACON_RESUMMON_BURST);
            playSoundAt(pos.up(3), "command_block_activates", 10.0F, 1.0F);
            if (witherStorm) {
                playSoundAt(pos.up(3), "command_block_build", 10.0F, 1.0F);
            } else {
                spawnOrdinaryResummonedEntity();
                finishResummonState();
                return;
            }
            markAndNotify();
        }

        if (!witherStorm) return;
        if (LegacySuperBeaconLogic.shouldPulseWitherStormResummon(resummonTicks)) {
            playSound("bowels_loud_hurt", 10.0F, 1.0F);
            LegacyNetwork.shakeNear(world, pos.getX() + 0.5D, pos.getY() + 0.5D,
                    pos.getZ() + 0.5D, 20.0D, 80.0F, 4.0F);
        }
        if (LegacySuperBeaconLogic.shouldSpawnResummonCluster(resummonTicks)
                && world.getGameRules().getBoolean("mobGriefing")) {
            spawnResummonCluster();
            spawnResummonCluster();
        }
        if (resummonTicks > RESUMMON_START) updateResummonClusters();

        if (resummonTicks == LegacySuperBeaconLogic.getMainResummonThreshold()) {
            playSound("withered_beacon_activate", 1.0F, 1.0F);
            playSound("tremble", 10.0F, 1.0F);
            LegacyNetwork.shakeNear(world, pos.getX() + 0.5D, pos.getY() + 0.5D,
                    pos.getZ() + 0.5D, 20.0D, 80.0F, 10.0F);
        }
        if (LegacySuperBeaconLogic.shouldFinishResummon(resummoningEntity, resummonTicks)) {
            finishWitherStormResummon();
        }
    }

    private void activateResummonAnimation() {
        playSound("tremble", 10.0F, 1.0F);
        playSound("bowels_loud_hurt", 10.0F, 1.0F);
        LegacyNetwork.shakeNear(world, pos.getX() + 0.5D, pos.getY() + 0.5D,
                pos.getZ() + 0.5D, 20.0D, 80.0F, 10.0F);
    }

    private void spawnOrdinaryResummonedEntity() {
        Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(resummoningEntity), world);
        if (entity == null) return;
        if (!resummoningEntityNbt.isEmpty()) {
            try {
                NBTTagCompound entityData = JsonToNBT.getTagFromJson(resummoningEntityNbt);
                NBTTagCompound currentData = entity.writeToNBT(new NBTTagCompound());
                entity.readFromNBT(mergeResummonEntityData(currentData, entityData));
            } catch (Exception exception) {
                WitherStormMod.LOGGER.warn("Unable to apply super beacon entity NBT for {}",
                        resummoningEntity, exception);
            }
        }
        entity.setLocationAndAngles(pos.getX() + 0.5D, pos.getY() + 3.0D, pos.getZ() + 0.5D,
                world.rand.nextFloat() * 360.0F, 0.0F);
        if (world.spawnEntity(entity)) {
            for (EntityPlayerMP player : world.getEntitiesWithinAABB(EntityPlayerMP.class,
                    getResummonAdvancementBox(pos))) {
                LegacyCriteriaTriggers.SUMMON_MOB_SUPER_BEACON.trigger(player, entity);
            }
        }
    }

    static NBTTagCompound mergeResummonEntityData(NBTTagCompound currentData,
                                                    NBTTagCompound recipeData) {
        NBTTagCompound merged = currentData == null ? new NBTTagCompound() : currentData.copy();
        if (recipeData != null) merged.merge(recipeData);
        return merged;
    }

    private void spawnResummonCluster() {
        int x = pos.getX() + world.rand.nextInt(97) - 48;
        int z = pos.getZ() + world.rand.nextInt(97) - 48;
        BlockPos current = new BlockPos(x, pos.getY() + 3, z);
        for (int i = 0; i < 30 && world.isAirBlock(current.down()); i++) {
            current = current.down();
        }
        current = current.down();
        if (!world.isBlockLoaded(current)) return;

        SupplementalEntities.BlockCluster cluster = new SupplementalEntities.BlockCluster(world);
        cluster.populateWithRadius(current, 1, TileEntitySuperBeacon::canUseResummonBlock);
        if (cluster.getBlocks().isEmpty()) return;
        cluster.setFadePos(pos);
        cluster.setTime(200);
        cluster.setRotationDelta(world.rand.nextInt(20) * 0.05F,
                world.rand.nextInt(20) * 0.05F);
        cluster.setNoGravity(true);
        cluster.setPhysics(false);
        if (resummonTicks % LegacySuperBeaconLogic.RESUMMON_STORM_PULSE_INTERVAL == 0) {
            world.playSound(null, cluster.posX, cluster.posY, cluster.posZ,
                    ModSounds.get("block_cluster_shake"), SoundCategory.BLOCKS, 2.0F, 1.0F);
        }
        world.spawnEntity(cluster);
        resummonClusters.add(cluster);
    }

    private static boolean canUseResummonBlock(net.minecraft.world.World world, BlockPos blockPos,
                                                IBlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.AIR || state.getMaterial().isLiquid()) return false;
        if (block == Blocks.BEDROCK || block == Blocks.BARRIER || block == Blocks.END_PORTAL
                || block == Blocks.END_PORTAL_FRAME || block == Blocks.END_GATEWAY
                || block == Blocks.STRUCTURE_BLOCK || block == Blocks.STRUCTURE_VOID
                || block == Blocks.COMMAND_BLOCK || block == Blocks.CHAIN_COMMAND_BLOCK
                || block == Blocks.REPEATING_COMMAND_BLOCK) return false;
        if (block == Blocks.LEAVES || block == Blocks.LEAVES2
                || block == Blocks.LAPIS_BLOCK
                || LegacySuperBeaconLogic.SupportColor.forBase(block) != null) return false;
        if (block == ModBlocks.get("super_beacon") || block == ModBlocks.get("super_support_beacon")
                || block == ModBlocks.get("withered_phlegm_block")
                || block == ModBlocks.get("formidibomb")) return false;
        return state.getBlockHardness(world, blockPos) >= 0.0F;
    }

    private void updateResummonClusters() {
        Vec3d target = new Vec3d(pos).add(0.5D, 3.5D, 0.5D);
        Iterator<SupplementalEntities.BlockCluster> iterator = resummonClusters.iterator();
        while (iterator.hasNext()) {
            SupplementalEntities.BlockCluster cluster = iterator.next();
            if (cluster == null || cluster.isDead) {
                iterator.remove();
                continue;
            }
            if (cluster.getShakeTime() > 0) continue;
            Vec3d difference = target.subtract(cluster.getPositionVector());
            if (difference.lengthSquared() > 1.0E-6D) {
                Vec3d movement = difference.normalize().scale(0.5D);
                cluster.motionX = movement.x;
                cluster.motionY = movement.y;
                cluster.motionZ = movement.z;
            }
            if (new AxisAlignedBB(new BlockPos(cluster)).contains(target)) {
                cluster.setDead();
                iterator.remove();
            }
        }
    }

    private void finishWitherStormResummon() {
        List<BlockPos> supports = new ArrayList<BlockPos>(connected.values());
        for (SupplementalEntities.BlockCluster cluster : resummonClusters) {
            if (cluster != null && !cluster.isDead) cluster.setDead();
        }
        resummonClusters.clear();
        finishResummonState();

        world.setBlockToAir(pos);
        for (BlockPos support : supports) world.setBlockToAir(support);
        world.newExplosion(null, pos.getX() + 0.5D, pos.getY() + 0.5D,
                pos.getZ() + 0.5D, 8.0F, false, true);

        EntityWitherStormLegacy storm = new EntityWitherStormLegacy(world);
        storm.initializeFromSuperBeacon(LegacyWitherStormConfig.resummonedPhase);
        storm.setLocationAndAngles(pos.getX() + 0.5D, pos.getY() + 0.5D,
                pos.getZ() + 0.5D, world.rand.nextFloat() * 360.0F, 0.0F);
        LegacyNetwork.playGlobalSound(world, ModSounds.get("wither_storm_evolves"), 1.0F, 1.0F);
        if (world.spawnEntity(storm)) {
            for (EntityPlayerMP player : world.getEntitiesWithinAABB(EntityPlayerMP.class,
                    getResummonAdvancementBox(pos))) {
                CriteriaTriggers.SUMMONED_ENTITY.trigger(player, storm);
            }
        }
    }

    static AxisAlignedBB getResummonAdvancementBox(BlockPos beaconPos) {
        return new AxisAlignedBB(beaconPos).grow(RESUMMON_ADVANCEMENT_RANGE);
    }

    private void finishResummonState() {
        resummonTicks = 0;
        resummoningEntity = "";
        resummoningEntityNbt = "";
        resetShake();
        markAndNotify();
    }

    private void updateResummonShake() {
        previousShakeX = shakeX;
        previousShakeZ = shakeZ;
        if (!isDoingResummonAnimation()) {
            shakeX = shakeZ = 0.0F;
            return;
        }
        shakeX = net.minecraft.util.math.MathHelper.sin(resummonTicks * 4.0F) * 0.1F
                + (world.rand.nextFloat() - 0.5F) * 0.05F;
        shakeZ = net.minecraft.util.math.MathHelper.sin(resummonTicks * 3.0F) * 0.1F
                + (world.rand.nextFloat() - 0.5F) * 0.05F;
    }

    private void resetShake() {
        shakeX = previousShakeX = 0.0F;
        shakeZ = previousShakeZ = 0.0F;
    }

    private void playSoundAt(BlockPos soundPos, String name, float volume, float pitch) {
        if (ModSounds.get(name) == null) return;
        world.playSound(null, soundPos, ModSounds.get(name), SoundCategory.BLOCKS, volume,
                pitch + (world.rand.nextFloat() - 0.5F) * 0.35F);
    }

    private boolean canCraftCondition(String condition) {
        if ("main_activated".equals(condition)) return beaconLevel > 0;
        if ("all_supports".equals(condition)) return beaconLevel > 0 && connected.size() == SupportColor.values().length;
        if ("fully_completed".equals(condition)) return beaconLevel == 4 && connected.size() == SupportColor.values().length;
        return true;
    }

    private int calculateBeaconLevel() {
        int level = 0;
        for (int layer = 1; layer <= 4; layer++) {
            boolean valid = true;
            int y = pos.getY() - layer;
            for (int x = pos.getX() - layer; x <= pos.getX() + layer && valid; x++) {
                for (int z = pos.getZ() - layer; z <= pos.getZ() + layer; z++) {
                    Block block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
                    if (layer == 1 ? block != Blocks.LAPIS_BLOCK : !isVanillaBeaconBase(block)) {
                        valid = false;
                        break;
                    }
                }
            }
            if (!valid) break;
            level = layer;
        }
        return level;
    }

    private static boolean isVanillaBeaconBase(Block block) {
        return block == Blocks.IRON_BLOCK || block == Blocks.GOLD_BLOCK || block == Blocks.DIAMOND_BLOCK
                || block == Blocks.EMERALD_BLOCK;
    }

    private void findSupportBeacons() {
        connected.clear();
        if (!active) return;
        for (BlockPos check : BlockPos.getAllInBox(pos.add(-5, -5, -5), pos.add(5, 5, 5))) {
            net.minecraft.tileentity.TileEntity tile = world.getTileEntity(check);
            if (tile instanceof TileEntitySuperSupportBeacon && check.distanceSq(pos) <= 25.0D) {
                TileEntitySuperSupportBeacon support = (TileEntitySuperSupportBeacon) tile;
                SupportColor color = support.getColor();
                if (color != null) connected.put(color, check.toImmutable());
            }
        }
    }

    public boolean addItem(ItemStack stack) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isEmpty()) {
                items.set(i, stack);
                markAndNotify();
                return true;
            }
        }
        return false;
    }

    public ItemStack takeItem() {
        for (int i = items.size() - 1; i >= 0; i--) {
            if (!items.get(i).isEmpty()) {
                ItemStack result = items.get(i);
                items.set(i, ItemStack.EMPTY);
                markAndNotify();
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    public boolean isDoingResummonAnimation() { return !resummoningEntity.isEmpty(); }
    @Override public boolean isActive() { return super.isActive() && !isDoingResummonAnimation(); }
    @Override protected boolean shouldDoActivatedAnimation() {
        return super.shouldDoActivatedAnimation()
                || resummonTicks > LegacySuperBeaconLogic.getMainResummonThreshold();
    }
    public int getConnectedSupportCount() { return connected.size(); }
    public int getResummonTicks() { return resummonTicks; }
    public String getResummoningEntity() { return resummoningEntity; }
    public boolean isResummoningWitherStorm() {
        return LegacySuperBeaconLogic.isWitherStormResummon(resummoningEntity);
    }
    public float getShakeX(float partialTicks) {
        return previousShakeX + (shakeX - previousShakeX) * partialTicks;
    }
    public float getShakeZ(float partialTicks) {
        return previousShakeZ + (shakeZ - previousShakeZ) * partialTicks;
    }
    public List<ItemStack> getItemsForRendering() {
        List<ItemStack> visible = new ArrayList<ItemStack>();
        for (ItemStack stack : items) if (!stack.isEmpty()) visible.add(stack);
        return Collections.unmodifiableList(visible);
    }

    public boolean isConnected(BlockPos support) { return connected.containsValue(support); }
    public Map<SupportColor, BlockPos> getConnected() { return java.util.Collections.unmodifiableMap(connected); }

    @Override
    public void invalidate() {
        if (world != null && !world.isRemote) {
            for (SupplementalEntities.BlockCluster cluster : resummonClusters) {
                if (cluster != null && !cluster.isDead) {
                    cluster.setNoGravity(false);
                    cluster.setPhysics(true);
                }
            }
            resummonClusters.clear();
        }
        super.invalidate();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < items.size(); i++) {
            if (!items.get(i).isEmpty()) {
                NBTTagCompound item = new NBTTagCompound();
                item.setByte("Slot", (byte) i);
                items.get(i).writeToNBT(item);
                list.appendTag(item);
            }
        }
        compound.setTag("ResummonItems", list);
        compound.setInteger("ResummonTicks", resummonTicks);
        compound.setString("ResummoningEntity", resummoningEntity);
        compound.setString("ResummoningEntityNbt", resummoningEntityNbt);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        clear();
        NBTTagList list = compound.getTagList("ResummonItems", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound item = list.getCompoundTagAt(i);
            int slot = item.getByte("Slot") & 255;
            if (slot < items.size()) items.set(slot, new ItemStack(item));
        }
        resummonTicks = compound.getInteger("ResummonTicks");
        resummoningEntity = compound.getString("ResummoningEntity");
        resummoningEntityNbt = compound.getString("ResummoningEntityNbt");
    }

    @Override public int getSizeInventory() { return items.size(); }
    @Override public boolean isEmpty() { for (ItemStack stack : items) if (!stack.isEmpty()) return false; return true; }
    @Override public ItemStack getStackInSlot(int index) { return items.get(index); }
    @Override public ItemStack decrStackSize(int index, int count) { ItemStack result = items.get(index).splitStack(count); if (items.get(index).isEmpty()) items.set(index, ItemStack.EMPTY); markAndNotify(); return result; }
    @Override public ItemStack removeStackFromSlot(int index) { ItemStack result = items.get(index); items.set(index, ItemStack.EMPTY); markAndNotify(); return result; }
    @Override public void setInventorySlotContents(int index, ItemStack stack) { items.set(index, stack); markAndNotify(); }
    @Override public int getInventoryStackLimit() { return 1; }
    @Override public boolean isUsableByPlayer(net.minecraft.entity.player.EntityPlayer player) { return world.getTileEntity(pos) == this && player.getDistanceSq(pos) <= 64.0D; }
    @Override public void openInventory(net.minecraft.entity.player.EntityPlayer player) { }
    @Override public void closeInventory(net.minecraft.entity.player.EntityPlayer player) { }
    @Override public boolean isItemValidForSlot(int index, ItemStack stack) { return !isDoingResummonAnimation(); }
    @Override public int getFieldCount() { return 4; }
    @Override public void clear() { for (int i = 0; i < items.size(); i++) items.set(i, ItemStack.EMPTY); markAndNotify(); }
    @Override public String getName() { return "container.witherstormmod.super_beacon"; }
    @Override public boolean hasCustomName() { return false; }

    public enum SupportColor {
        AQUA(LegacySuperBeaconLogic.SupportColor.AQUA),
        GREEN(LegacySuperBeaconLogic.SupportColor.GREEN),
        GRAY(LegacySuperBeaconLogic.SupportColor.GRAY),
        RED(LegacySuperBeaconLogic.SupportColor.RED);

        private final LegacySuperBeaconLogic.SupportColor logic;
        SupportColor(LegacySuperBeaconLogic.SupportColor logic) { this.logic = logic; }
        public LegacySuperBeaconLogic.SupportColor getLogic() { return logic; }
    }
}
