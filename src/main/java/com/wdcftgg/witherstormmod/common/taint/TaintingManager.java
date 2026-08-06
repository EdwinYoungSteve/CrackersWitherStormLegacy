package com.wdcftgg.witherstormmod.common.taint;

import com.google.common.base.Optional;
import com.wdcftgg.witherstormmod.common.advancement.ModCriteriaTriggers;
import com.wdcftgg.witherstormmod.common.entity.SickenedMobEntity;
import com.wdcftgg.witherstormmod.common.entity.SickenedEntities;
import com.wdcftgg.witherstormmod.common.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSand;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityLiving;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityVindicator;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityParrot;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public final class TaintingManager {

    private TaintingManager() {
    }

    public static boolean taintBlock(World world, BlockPos position) {
        IBlockState original = world.getBlockState(position);
        Block replacement = getReplacement(original);
        if (replacement == null || replacement == original.getBlock()) {
            return false;
        }
        IBlockState replacementState = copyProperties(original, replacement.getDefaultState());
        return world.setBlockState(position, replacementState, 3);
    }

    public static boolean convertEntity(EntityLivingBase original) {
        if (original.world.isRemote || original.isDead || original instanceof SickenedMobEntity) {
            return false;
        }
        SickenedMobEntity replacement = createReplacement(original);
        if (replacement == null) {
            return false;
        }
        replacement.setLocationAndAngles(original.posX, original.posY, original.posZ, original.rotationYaw, original.rotationPitch);
        replacement.renderYawOffset = original.renderYawOffset;
        replacement.rememberOriginal(original);
        if (original.hasCustomName()) {
            replacement.setCustomNameTag(original.getCustomNameTag());
            replacement.setAlwaysRenderNameTag(original.getAlwaysRenderNameTag());
        }
        replacement.setHealth(replacement.getMaxHealth() * Math.max(0.1F, original.getHealth() / original.getMaxHealth()));
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            ItemStack equipped = original.getItemStackFromSlot(slot);
            if (!equipped.isEmpty()) replacement.setItemStackToSlot(slot, equipped.copy());
        }
        original.world.spawnEntity(replacement);
        replacement.playSound(com.wdcftgg.witherstormmod.common.init.ModSounds.get("mob_infected"), 1.0F, 1.0F);
        original.setDead();
        return true;
    }

    public static boolean cureEntity(SickenedMobEntity original) {
        EntityLivingBase replacement = createCuredReplacement(original);
        if (replacement == null || original.world.isRemote || original.isDead) {
            return false;
        }
        replacement.setLocationAndAngles(original.posX, original.posY, original.posZ, original.rotationYaw, original.rotationPitch);
        replacement.renderYawOffset = original.renderYawOffset;
        NBTTagCompound saved = original.getOriginalData();
        if (saved != null) {
            replacement.readFromNBT(saved);
            replacement.setLocationAndAngles(original.posX, original.posY, original.posZ, original.rotationYaw, original.rotationPitch);
            replacement.renderYawOffset = original.renderYawOffset;
        }
        if (original.hasCustomName()) {
            replacement.setCustomNameTag(original.getCustomNameTag());
            replacement.setAlwaysRenderNameTag(original.getAlwaysRenderNameTag());
        }
        float healthRatio = original.getHealth() / original.getMaxHealth();
        replacement.setHealth(Math.max(1.0F, replacement.getMaxHealth() * healthRatio));
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            ItemStack equipped = original.getItemStackFromSlot(slot);
            if (!equipped.isEmpty()) replacement.setItemStackToSlot(slot, equipped.copy());
        }
        replacement.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 200));
        if (!original.world.spawnEntity(replacement)) return false;
        replacement.playSound(com.wdcftgg.witherstormmod.common.init.ModSounds.get("mob_cured"), 1.0F, 1.0F);
        if (original.getConversionStarter() != null && original.world instanceof WorldServer) {
            net.minecraft.entity.Entity starter = original.world.getMinecraftServer()
                    .getPlayerList().getPlayerByUUID(original.getConversionStarter());
            if (starter instanceof EntityPlayerMP) {
                ModCriteriaTriggers.CURED_SICKENED_MOB.trigger(
                        (EntityPlayerMP) starter, original, replacement);
            }
        }
        original.setDead();
        return true;
    }

    private static Block getReplacement(IBlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.STONE || block == Blocks.STONEBRICK || block == Blocks.MONSTER_EGG) return ModBlocks.get("tainted_stone");
        if (block == Blocks.COBBLESTONE || block == Blocks.MOSSY_COBBLESTONE) return ModBlocks.get("tainted_cobblestone");
        if (block == Blocks.DIRT || block == Blocks.GRASS || block == Blocks.GRASS_PATH || block instanceof BlockDirt) return ModBlocks.get("tainted_dirt");
        if (block == Blocks.SAND || block instanceof BlockSand) return ModBlocks.get("tainted_sand");
        if (block == Blocks.SANDSTONE || block == Blocks.RED_SANDSTONE) return ModBlocks.get("tainted_sandstone");
        if (block == Blocks.LOG || block == Blocks.LOG2) return ModBlocks.get("tainted_log");
        if (block == Blocks.PLANKS || block instanceof BlockPlanks) return ModBlocks.get("tainted_planks");
        if (block == Blocks.LEAVES || block == Blocks.LEAVES2) return ModBlocks.get("tainted_leaves");
        if (block == Blocks.GLASS) return ModBlocks.get("tainted_glass");
        if (block == Blocks.GLASS_PANE) return ModBlocks.get("tainted_glass_pane");
        if (block == Blocks.PUMPKIN) return ModBlocks.get("tainted_pumpkin");
        if (block == Blocks.LIT_PUMPKIN) return ModBlocks.get("tainted_jack_o_lantern");
        if (block == Blocks.BROWN_MUSHROOM || block == Blocks.RED_MUSHROOM) return ModBlocks.get("tainted_mushroom");
        if (block == Blocks.TORCH) return ModBlocks.get("tainted_torch");
        if (block == Blocks.COBBLESTONE_WALL) return ModBlocks.get("tainted_cobblestone_wall");
        if (block == Blocks.STONE_STAIRS) return ModBlocks.get("tainted_stone_stairs");
        if (block == Blocks.OAK_STAIRS || block == Blocks.SPRUCE_STAIRS || block == Blocks.BIRCH_STAIRS
                || block == Blocks.JUNGLE_STAIRS || block == Blocks.ACACIA_STAIRS || block == Blocks.DARK_OAK_STAIRS) return ModBlocks.get("tainted_stairs");
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static IBlockState copyProperties(IBlockState source, IBlockState target) {
        for (IProperty sourceProperty : source.getPropertyKeys()) {
            for (IProperty targetProperty : target.getPropertyKeys()) {
                if (!sourceProperty.getName().equals(targetProperty.getName())) {
                    continue;
                }
                String valueName = sourceProperty.getName((Comparable) source.getValue(sourceProperty));
                Optional<? extends Comparable> parsed = targetProperty.parseValue(valueName);
                if (parsed.isPresent()) {
                    target = target.withProperty(targetProperty, parsed.get());
                }
            }
        }
        return target;
    }

    private static SickenedMobEntity createReplacement(EntityLivingBase original) {
        World world = original.world;
        if (original instanceof EntityMooshroom) return new SickenedEntities.SickenedMushroomCowEntity(world);
        if (original instanceof EntityCow) return new SickenedEntities.SickenedCowEntity(world);
        if (original instanceof EntityChicken) return new SickenedEntities.SickenedChickenEntity(world);
        if (original instanceof EntityPig) return new SickenedEntities.SickenedPigEntity(world);
        if (original instanceof EntityWolf) return new SickenedEntities.SickenedWolfEntity(world);
        if (original instanceof EntityOcelot) return new SickenedEntities.SickenedCatEntity(world);
        if (original instanceof EntityParrot) return new SickenedEntities.SickenedParrotEntity(world);
        if (original instanceof EntityCreeper) return new SickenedEntities.SickenedCreeperEntity(world);
        if (original instanceof EntitySkeleton) return new SickenedEntities.SickenedSkeletonEntity(world);
        if (original instanceof EntitySpider) return new SickenedEntities.SickenedSpiderEntity(world);
        if (original instanceof EntityZombieVillager) return new SickenedEntities.SickenedVillagerEntity(world);
        if (original instanceof EntityZombie) return new SickenedEntities.SickenedZombieEntity(world);
        if (original instanceof EntityVillager) return new SickenedEntities.SickenedVillagerEntity(world);
        if (original instanceof EntityVindicator) return new SickenedEntities.SickenedVindicatorEntity(world);
        if (original instanceof EntityIronGolem) return new SickenedEntities.SickenedIronGolemEntity(world);
        if (original instanceof EntitySnowman) return new SickenedEntities.SickenedSnowGolemEntity(world);
        return null;
    }

    private static EntityLivingBase createCuredReplacement(SickenedMobEntity original) {
        World world = original.world;
        String type = original.getSickenedType();
        if ("sickened_cat".equals(type)) return new EntityOcelot(world);
        if ("sickened_chicken".equals(type)) return new EntityChicken(world);
        if ("sickened_cow".equals(type)) return new EntityCow(world);
        if ("sickened_creeper".equals(type)) return new EntityCreeper(world);
        if ("sickened_iron_golem".equals(type)) return new EntityIronGolem(world);
        if ("sickened_mushroom_cow".equals(type)) return new EntityMooshroom(world);
        if ("sickened_parrot".equals(type)) return new EntityParrot(world);
        if ("sickened_pig".equals(type)) return new EntityPig(world);
        if ("sickened_skeleton".equals(type)) return new EntitySkeleton(world);
        if ("sickened_snow_golem".equals(type)) return new EntitySnowman(world);
        if ("sickened_spider".equals(type)) return new EntitySpider(world);
        if ("sickened_villager".equals(type)) return new EntityVillager(world);
        if ("sickened_vindicator".equals(type)) return new EntityVindicator(world);
        if ("sickened_wolf".equals(type)) return new EntityWolf(world);
        if ("sickened_zombie".equals(type)) return new EntityZombie(world);
        return null;
    }

    public static ResourceLocation getOriginalType(String sickenedType) {
        if ("sickened_cat".equals(sickenedType)) return new ResourceLocation("minecraft", "ocelot");
        if ("sickened_chicken".equals(sickenedType)) return new ResourceLocation("minecraft", "chicken");
        if ("sickened_cow".equals(sickenedType)) return new ResourceLocation("minecraft", "cow");
        if ("sickened_creeper".equals(sickenedType)) return new ResourceLocation("minecraft", "creeper");
        if ("sickened_iron_golem".equals(sickenedType)) return new ResourceLocation("minecraft", "villager_golem");
        if ("sickened_mushroom_cow".equals(sickenedType)) return new ResourceLocation("minecraft", "mushroom_cow");
        if ("sickened_parrot".equals(sickenedType)) return new ResourceLocation("minecraft", "parrot");
        if ("sickened_pig".equals(sickenedType)) return new ResourceLocation("minecraft", "pig");
        if ("sickened_skeleton".equals(sickenedType)) return new ResourceLocation("minecraft", "skeleton");
        if ("sickened_snow_golem".equals(sickenedType)) return new ResourceLocation("minecraft", "snowman");
        if ("sickened_spider".equals(sickenedType)) return new ResourceLocation("minecraft", "spider");
        if ("sickened_villager".equals(sickenedType)) return new ResourceLocation("minecraft", "villager");
        if ("sickened_vindicator".equals(sickenedType)) return new ResourceLocation("minecraft", "vindication_illager");
        if ("sickened_wolf".equals(sickenedType)) return new ResourceLocation("minecraft", "wolf");
        if ("sickened_zombie".equals(sickenedType)) return new ResourceLocation("minecraft", "zombie");
        return null;
    }
}
