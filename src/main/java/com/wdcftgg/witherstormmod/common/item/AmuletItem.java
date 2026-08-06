package com.wdcftgg.witherstormmod.common.item;

import com.wdcftgg.witherstormmod.common.advancement.ModCriteriaTriggers;
import com.wdcftgg.witherstormmod.common.config.WitherStormConfig;
import com.wdcftgg.witherstormmod.common.entity.WitherStormEntity;
import com.wdcftgg.witherstormmod.common.entity.SupplementalEntities;
import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import com.wdcftgg.witherstormmod.common.init.ModSounds;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AmuletItem extends Item {

    public static final String TRACKING_BLUE = "TrackingBlue";
    public static final String TRACKING_AQUA = "TrackingAqua";
    public static final String TRACKING_GREEN = "TrackingGreen";
    public static final String TRACKING_GRAY = "TrackingGray";
    public static final String TRACKING_RED = "TrackingRed";
    public static final String[] TRACKING = {
            TRACKING_BLUE, TRACKING_AQUA, TRACKING_GREEN, TRACKING_GRAY, TRACKING_RED
    };
    public static final String SELECTED_INDEX = "SelectedIndex";
    public static final String LOCKED_TAG = "Locked";
    public static final String TRACK_ENTITY_TYPES = "TrackEntityTypes";
    public static final int DEFAULT_SCAN_DISTANCE = 500;

    private static final String TYPE_SUFFIX = "Type";
    private static final String DISTANCE_SUFFIX = "Dist";
    private static final String NAME_SUFFIX = "Name";
    private static final String POSITION_SUFFIX = "Pos";

    public AmuletItem(String name) {
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
    public boolean hasCustomEntity(ItemStack stack) {
        return FireResistantItemEntity.isFireResistant(stack);
    }

    @Override
    public Entity createEntity(World world, Entity location, ItemStack stack) {
        return hasCustomEntity(stack) ? FireResistantItemEntity.create(world, location, stack) : null;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity holder, int slot, boolean selected) {
        if (!(world instanceof WorldServer) || !(holder instanceof EntityPlayer)) return;

        WorldServer serverWorld = (WorldServer) world;
        EntityPlayer player = (EntityPlayer) holder;
        NBTTagCompound tag = getOrCreateTag(stack);
        ensureSelectedIndex(tag);

        for (String tracking : TRACKING) {
            if (TRACKING_BLUE.equals(tracking)) {
                WitherStormEntity storm = nearestStorm(serverWorld, player);
                if (storm == null) {
                    tag.setInteger(key(tracking, DISTANCE_SUFFIX), -1);
                } else {
                    ResourceLocation type = entityType(storm);
                    if (type != null) tag.setString(key(tracking, TYPE_SUFFIX), type.toString());
                    tag.setUniqueId(tracking, storm.getUniqueID());
                    saveTrackedEntity(tag, player, storm, tracking);
                }
            } else if (tag.hasUniqueId(tracking)) {
                saveDistanceFor(serverWorld, tag, player, tracking);
            } else {
                tag.setInteger(key(tracking, DISTANCE_SUFFIX), -1);
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote && player.isSneaking() && stack.getItem() instanceof AmuletItem) {
            NBTTagCompound tag = getOrCreateTag(stack);
            cycleSelectedIndex(tag);
            player.playSound(ModSounds.get("amulet_swaps"), 1.0F, 2.0F);
        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player,
                                            EntityLivingBase target, EnumHand hand) {
        if (!(target instanceof EntityLiving || target instanceof EntityPlayer)
                || target instanceof WitherStormEntity
                || target instanceof SupplementalEntities.WitherStormSegmentEntity
                || target instanceof SupplementalEntities.CommandBlockEntity
                || player.isSneaking()) {
            return false;
        }

        ItemStack held = player.getHeldItem(hand);
        if (!(held.getItem() instanceof AmuletItem)) return false;
        NBTTagCompound tag = getOrCreateTag(held);
        if (tag.getBoolean(LOCKED_TAG)) return false;

        int selected = tag.getInteger(SELECTED_INDEX);
        if (selected < 1 || selected >= TRACKING.length) return false;
        if (!player.world.isRemote) {
            String tracking = TRACKING[selected];
            if (isNewBinding(tag, tracking, target.getUniqueID())) {
                ResourceLocation type = entityType(target);
                if (type == null) return false;
                tag.setString(key(tracking, TYPE_SUFFIX), type.toString());
                tag.setUniqueId(tracking, target.getUniqueID());
                player.playSound(ModSounds.get("amulet_bind"), 1.0F, 0.0F);
                if (player instanceof EntityPlayerMP) {
                    ModCriteriaTriggers.LINK_AMULET.trigger((EntityPlayerMP) player, target,
                            getTotalUniqueLinked(tag));
                }
            } else {
                clearTracking(tag, tracking);
                player.playSound(ModSounds.get("amulet_unbind"), 1.0F, 1.0F);
            }
        }
        return true;
    }

    private static void saveDistanceFor(WorldServer world, NBTTagCompound tag,
                                        EntityPlayer player, String tracking) {
        Entity target = tag.getBoolean(TRACK_ENTITY_TYPES)
                ? nearestEntityOfTrackedType(world, player, tag.getString(key(tracking, TYPE_SUFFIX)))
                : world.getEntityFromUuid(tag.getUniqueId(tracking));
        if (target == null) {
            tag.setInteger(key(tracking, DISTANCE_SUFFIX), -1);
        } else {
            saveTrackedEntity(tag, player, target, tracking);
        }
    }

    private static void saveTrackedEntity(NBTTagCompound tag, EntityPlayer player,
                                          Entity target, String tracking) {
        tag.setInteger(key(tracking, DISTANCE_SUFFIX), (int) player.getDistance(target));
        tag.setString(key(tracking, NAME_SUFFIX), target.getDisplayName().getUnformattedText());
        tag.setTag(key(tracking, POSITION_SUFFIX), NBTUtil.createPosTag(target.getPosition()));
    }

    @Nullable
    private static WitherStormEntity nearestStorm(World world, EntityPlayer player) {
        WitherStormEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Entity entity : world.loadedEntityList) {
            if (!(entity instanceof WitherStormEntity)) continue;
            double distance = player.getDistanceSq(entity);
            if (distance < nearestDistance) {
                nearest = (WitherStormEntity) entity;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    @Nullable
    private static Entity nearestEntityOfTrackedType(World world, EntityPlayer player, String typeName) {
        ResourceLocation trackedType;
        try {
            trackedType = new ResourceLocation(typeName);
        } catch (RuntimeException ignored) {
            return null;
        }

        Entity nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        List<Entity> candidates = world.getEntitiesWithinAABB(Entity.class,
                player.getEntityBoundingBox().grow(DEFAULT_SCAN_DISTANCE));
        for (Entity candidate : candidates) {
            if (candidate == player || !trackedType.equals(entityType(candidate))) continue;
            double distance = player.getDistanceSq(candidate);
            if (distance < nearestDistance) {
                nearest = candidate;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    @Nullable
    private static ResourceLocation entityType(Entity entity) {
        if (entity instanceof EntityPlayer) return new ResourceLocation("minecraft", "player");
        return EntityList.getKey(entity);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        NBTTagCompound tag = getOrCreateTag(stack);
        boolean locked = tag.getBoolean(LOCKED_TAG);
        if (WitherStormConfig.amuletOverride) {
            tooltip.add(TextFormatting.DARK_GRAY + I18n.format("description.amulet.mainUse"));
        }
        tooltip.add(TextFormatting.DARK_GRAY + I18n.format("description.amulet.trackingDesc"));
        tooltip.add(TextFormatting.DARK_GRAY + I18n.format("description.amulet.swap"));
        if (!locked) tooltip.add(TextFormatting.DARK_GRAY + I18n.format("description.amulet.bind"));

        TextFormatting[] colors = {
                TextFormatting.BLUE, TextFormatting.AQUA, TextFormatting.GREEN,
                TextFormatting.GRAY, TextFormatting.RED
        };
        for (int index = 0; index < TRACKING.length; index++) {
            String tracking = TRACKING[index];
            tooltip.add(colors[index] + I18n.format("description.amulet.tracking",
                    getTrackingName(tracking, tag), getDistanceString(tracking, tag)));
        }
        if (locked) tooltip.add(TextFormatting.YELLOW + I18n.format("description.amulet.locked"));
        if (tag.getBoolean(TRACK_ENTITY_TYPES)) {
            tooltip.add(TextFormatting.GOLD + I18n.format("description.amulet.tracksEntityTypes"));
        }
    }

    public static boolean isLocked(ItemStack stack) {
        return stack.hasTagCompound() && isLocked(stack.getTagCompound());
    }

    public static int getTotalUniqueLinked(ItemStack stack) {
        return getTotalUniqueLinked(getOrCreateTag(stack));
    }

    static boolean isLocked(NBTTagCompound tag) {
        return tag.getBoolean(LOCKED_TAG);
    }

    static int getTotalUniqueLinked(NBTTagCompound tag) {
        Set<UUID> linked = new HashSet<UUID>();
        for (int index = 1; index < TRACKING.length; index++) {
            String tracking = TRACKING[index];
            if (tag.hasUniqueId(tracking)) linked.add(tag.getUniqueId(tracking));
        }
        return linked.size();
    }

    static boolean isNewBinding(NBTTagCompound tag, String tracking, UUID target) {
        return !tag.hasUniqueId(tracking) || !target.equals(tag.getUniqueId(tracking));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged && super.shouldCauseReequipAnimation(oldStack, newStack, true);
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return newStack.getItem() != oldStack.getItem();
    }

    static NBTTagCompound getOrCreateTag(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        return tag;
    }

    static int ensureSelectedIndex(NBTTagCompound tag) {
        int selected = tag.getInteger(SELECTED_INDEX);
        if (selected <= 0) {
            selected = 1;
            tag.setInteger(SELECTED_INDEX, selected);
        }
        return selected;
    }

    static int cycleSelectedIndex(NBTTagCompound tag) {
        int selected = tag.getInteger(SELECTED_INDEX) + 1;
        if (selected >= TRACKING.length) selected = 1;
        tag.setInteger(SELECTED_INDEX, selected);
        return selected;
    }

    static void clearTracking(NBTTagCompound tag, String tracking) {
        tag.removeTag(tracking + "Most");
        tag.removeTag(tracking + "Least");
        tag.removeTag(key(tracking, TYPE_SUFFIX));
        tag.removeTag(key(tracking, NAME_SUFFIX));
        tag.removeTag(key(tracking, POSITION_SUFFIX));
        tag.setInteger(key(tracking, DISTANCE_SUFFIX), -1);
    }

    static String getDistanceString(String tracking, NBTTagCompound tag) {
        int distance = tag.getInteger(key(tracking, DISTANCE_SUFFIX));
        if (distance >= 0) return String.valueOf(distance);
        return tag.hasKey(key(tracking, NAME_SUFFIX)) ? "Could not find nearby" : "";
    }

    static String getTrackingName(String tracking, NBTTagCompound tag) {
        if (!tag.hasKey(key(tracking, NAME_SUFFIX))) {
            return TRACKING_BLUE.equals(tracking) ? "No Nearby Wither Storm" : "Empty";
        }
        return tag.getString(key(tracking, NAME_SUFFIX));
    }

    private static String key(String tracking, String suffix) {
        return tracking + suffix;
    }
}
