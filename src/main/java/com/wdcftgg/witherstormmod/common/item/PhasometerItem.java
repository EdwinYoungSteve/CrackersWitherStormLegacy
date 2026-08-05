package com.wdcftgg.witherstormmod.common.item;

import com.wdcftgg.witherstormmod.common.advancement.LegacyCriteriaTriggers;
import com.wdcftgg.witherstormmod.common.entity.EntityWitherStormLegacy;
import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PhasometerItem extends Item {

    public static final String UPGRADED = "IsUpgraded";
    public static final int USE_DURATION = 1200;
    private static final double ENTITY_TRACE_DISTANCE = 10000.0D;
    private static final double OBSTRUCTION_TRACE_DISTANCE = 150.0D;

    public PhasometerItem(String name) {
        setRegistryName(name);
        setTranslationKey(name);
        setCreativeTab(ModCreativeTabs.MAIN);
        setMaxStackSize(1);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.UNCOMMON;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        clearDataTags(getOrCreateTag(stack));
        player.setActiveHand(hand);
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase user, int count) {
        if (user.world.isRemote) return;

        NBTTagCompound tag = getOrCreateTag(stack);
        EntityWitherStormLegacy storm = findLookedAtStorm(user.world, user);
        if (storm == null) {
            clearDataTags(tag);
            return;
        }

        RayTraceResult obstruction = rayTraceToward(user, storm, OBSTRUCTION_TRACE_DISTANCE);
        if (obstruction == null || obstruction.typeOfHit == RayTraceResult.Type.MISS) {
            if (user instanceof EntityPlayerMP) {
                LegacyCriteriaTriggers.OBSERVE_WITHER_STORM.trigger(
                        (EntityPlayerMP) user, stack, storm);
            }
            applyStormData(tag, storm, isUpgraded(stack));
            tag.setBoolean(DataEntry.OBSTRUCTED.tagName, false);
            return;
        }

        if (obstruction.typeOfHit == RayTraceResult.Type.BLOCK
                && !user.world.getBlockState(obstruction.getBlockPos()).isFullBlock()) {
            clearDataTags(tag, DataEntry.OBSTRUCTED);
            tag.setBoolean(DataEntry.OBSTRUCTED.tagName, true);
            return;
        }
        clearDataTags(tag);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase user, int timeLeft) {
        clearDataTags(getOrCreateTag(stack));
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase user) {
        clearDataTags(getOrCreateTag(stack));
        return stack;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        tooltip.add(TextFormatting.DARK_GRAY + I18n.format("description.phasometer.use"));
        if (isUpgraded(stack)) {
            tooltip.add(TextFormatting.GOLD + I18n.format("description.phasometer.use.upgraded"));
        }
    }

    public static boolean isUpgraded(ItemStack stack) {
        return hasUpgradeTag(getOrCreateTag(stack));
    }

    static boolean hasUpgradeTag(NBTTagCompound tag) {
        return tag.getBoolean(UPGRADED);
    }

    public static List<DataEntry> getEntries(NBTTagCompound tag) {
        List<DataEntry> entries = new ArrayList<DataEntry>();
        for (DataEntry entry : DataEntry.values()) {
            if (entry.informational && entry.hasData(tag)) entries.add(entry);
        }
        return entries;
    }

    static void clearDataTags(NBTTagCompound tag, DataEntry... retained) {
        List<DataEntry> retainedEntries = retained.length == 0
                ? Collections.<DataEntry>emptyList() : Arrays.asList(retained);
        for (DataEntry entry : DataEntry.values()) {
            if (!retainedEntries.contains(entry)) tag.removeTag(entry.tagName);
        }
    }

    private static void applyStormData(NBTTagCompound tag, EntityWitherStormLegacy storm,
                                       boolean upgraded) {
        for (DataEntry entry : DataEntry.values()) {
            if (!entry.requiresUpgraded || upgraded) entry.apply(tag, storm);
            else tag.removeTag(entry.tagName);
        }
    }

    @Nullable
    private static EntityWitherStormLegacy findLookedAtStorm(World world, EntityLivingBase user) {
        Vec3d start = user.getPositionEyes(1.0F);
        Vec3d end = start.add(user.getLookVec().scale(ENTITY_TRACE_DISTANCE));
        AxisAlignedBB searchBounds = new AxisAlignedBB(start, end).grow(1.0D);
        Entity nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Entity candidate : world.getEntitiesWithinAABBExcludingEntity(user, searchBounds)) {
            if (candidate.isDead
                    || candidate instanceof EntityPlayer && ((EntityPlayer) candidate).isSpectator()
                    || !candidate.canBeCollidedWith()) continue;
            AxisAlignedBB bounds = candidate.getEntityBoundingBox().grow(candidate.getCollisionBorderSize());
            RayTraceResult intercept = bounds.calculateIntercept(start, end);
            Vec3d hit = bounds.contains(start) ? start : intercept == null ? null : intercept.hitVec;
            if (hit == null) continue;
            double distance = start.squareDistanceTo(hit);
            if (distance < nearestDistance) {
                nearest = candidate;
                nearestDistance = distance;
            }
        }
        return nearest instanceof EntityWitherStormLegacy
                ? (EntityWitherStormLegacy) nearest : null;
    }

    @Nullable
    private static RayTraceResult rayTraceToward(EntityLivingBase user,
                                                  EntityWitherStormLegacy storm,
                                                  double maximumDistance) {
        Vec3d start = user.getPositionEyes(1.0F);
        Vec3d difference = storm.getPositionEyes(1.0F).subtract(start);
        double distance = difference.length();
        Vec3d end = distance > maximumDistance
                ? start.add(difference.scale(maximumDistance / distance))
                : start.add(difference);
        return user.world.rayTraceBlocks(start, end, false, true, false);
    }

    static boolean isFormidibombable(int phase, int consumedMass, int phaseRequirement) {
        return phase == 5 && consumedMass >= phaseRequirement;
    }

    static int phaseProgressPercent(int phase, float progress) {
        return phase < 7 ? Math.round(progress * 100.0F) : 100;
    }

    static String ultimateTargetDirection(Vec3d stormPosition, double stormEyeY,
                                          Vec3d targetPosition) {
        Vec3d direction = new Vec3d(targetPosition.x, stormEyeY, targetPosition.z)
                .subtract(stormPosition).normalize();
        return EnumFacing.getFacingFromVector((float) direction.x, (float) direction.y,
                (float) direction.z).getName();
    }

    static NBTTagCompound getOrCreateTag(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        return tag;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack,
                                               boolean slotChanged) {
        return slotChanged && super.shouldCauseReequipAnimation(oldStack, newStack, true);
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return newStack.getItem() != oldStack.getItem();
    }

    public enum DataEntry {
        OBSTRUCTED("IsObstructed", false, false),
        PHASE("LookingAtPhase", false, true),
        FORMIDIBOMBABLE("IsFormidibombable", false, true),
        BOWELS_ACCESSIBLE("BowelsAccessible", false, true),
        DISTRACTED("IsDistracted", false, true),
        CHASING("IsChasing", false, true),
        ULTIMATE_TARGET("UltimateTarget", true, true),
        ULTIMATE_TARGET_DIRECTION("UltimateTargetDirection", true, true),
        PHASE_PROGRESS("PhaseProgress", true, true);

        public final String tagName;
        public final boolean requiresUpgraded;
        private final boolean informational;

        DataEntry(String tagName, boolean requiresUpgraded, boolean informational) {
            this.tagName = tagName;
            this.requiresUpgraded = requiresUpgraded;
            this.informational = informational;
        }

        private void apply(NBTTagCompound tag, EntityWitherStormLegacy storm) {
            switch (this) {
                case PHASE:
                    tag.setInteger(tagName, storm.getPhase());
                    break;
                case FORMIDIBOMBABLE:
                    tag.setBoolean(tagName, isFormidibombable(storm.getPhase(),
                            storm.getConsumedMass(), storm.getConsumptionAmountForPhase(5)));
                    break;
                case BOWELS_ACCESSIBLE:
                    tag.setBoolean(tagName, storm.isBeingTornApart());
                    break;
                case DISTRACTED:
                    tag.setBoolean(tagName, storm.isDistracted());
                    break;
                case CHASING:
                    tag.setBoolean(tagName, storm.isUltimateTargetStationary());
                    break;
                case ULTIMATE_TARGET:
                    if (storm.getUltimateTarget() == null) tag.removeTag(tagName);
                    else tag.setString(tagName,
                            storm.getUltimateTarget().getDisplayName().getUnformattedText());
                    break;
                case ULTIMATE_TARGET_DIRECTION:
                    if (storm.getUltimateTargetPos() == null) tag.removeTag(tagName);
                    else tag.setString(tagName, ultimateTargetDirection(storm.getPositionVector(),
                            storm.posY + storm.getEyeHeight(), storm.getUltimateTargetPos()));
                    break;
                case PHASE_PROGRESS:
                    tag.setInteger(tagName,
                            phaseProgressPercent(storm.getPhase(), storm.getPhaseProgress()));
                    break;
                default:
                    break;
            }
        }

        public boolean hasData(NBTTagCompound tag) {
            switch (this) {
                case OBSTRUCTED:
                case PHASE:
                case ULTIMATE_TARGET:
                case ULTIMATE_TARGET_DIRECTION:
                case PHASE_PROGRESS:
                    return tag.hasKey(tagName);
                default:
                    return tag.getBoolean(tagName);
            }
        }

        @SideOnly(Side.CLIENT)
        public String getDisplayText(NBTTagCompound tag) {
            switch (this) {
                case OBSTRUCTED:
                    return TextFormatting.RED + I18n.format("description.phasometer.obstructed");
                case PHASE:
                    return TextFormatting.GREEN + I18n.format("description.phasometer.phase",
                            tag.getInteger(tagName));
                case FORMIDIBOMBABLE:
                    return TextFormatting.GOLD + I18n.format("description.phasometer.formidibombable");
                case BOWELS_ACCESSIBLE:
                    return TextFormatting.GOLD + I18n.format("description.phasometer.bowelsAccessible");
                case DISTRACTED:
                    return TextFormatting.GOLD + I18n.format("description.phasometer.distracted");
                case CHASING:
                    return TextFormatting.GOLD + I18n.format("description.phasometer.chasing");
                case ULTIMATE_TARGET:
                    return TextFormatting.LIGHT_PURPLE + I18n.format(
                            "description.phasometer.ultimateTarget", tag.getString(tagName));
                case ULTIMATE_TARGET_DIRECTION:
                    return TextFormatting.LIGHT_PURPLE + I18n.format(
                            "description.phasometer.ultimateTargetDirection", tag.getString(tagName));
                case PHASE_PROGRESS:
                    return TextFormatting.LIGHT_PURPLE + I18n.format(
                            "description.phasometer.phaseProgress", tag.getInteger(tagName) + "%");
                default:
                    return "";
            }
        }
    }
}
