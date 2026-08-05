package com.wdcftgg.witherstormmod.common.config;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.common.util.ItemPreservationCondition;
import net.minecraftforge.common.config.Config;

@Config(modid = Tags.MOD_ID, name = Tags.MOD_ID + "/server")
public final class LegacyWitherStormConfig {
    @Config.Name("itemPreservation")
    @Config.Comment("Condition under which player drops are preserved in a withered phlegm cluster.")
    public static ItemPreservationCondition itemPreservation =
            ItemPreservationCondition.CHOMPED_OR_KILLED_NEAR_HEAD;

    @Config.Name("preserveDropsForAllMobs")
    @Config.Comment("Preserve drops in phlegm clusters for all living mobs instead of players only.")
    public static boolean preserveDropsForAllMobs = false;

    @Config.Name("resummonedPhase")
    @Config.Comment("Phase assigned to a Wither Storm resummoned by a super beacon.")
    @Config.RangeInt(min = 0, max = 7)
    public static int resummonedPhase = 4;

    @Config.Name("shouldShowHole")
    @Config.Comment("Whether the bowels entrance hole is available at the end of phase 7.")
    public static boolean shouldShowHole = true;

    @Config.Name("amuletOverride")
    @Config.Comment("Whether carrying an amulet forces the Wither Storm to prioritize that player.")
    public static boolean amuletOverride = true;

    @Config.Name("witherStormInvulnerability")
    @Config.Comment("Whether the Wither Storm regenerates and ignores ordinary damage after phase 3.")
    public static boolean witherStormInvulnerability = true;

    @Config.Name("invulnerabilityTime")
    @Config.Comment("Initial Wither Storm invulnerability duration in seconds.")
    @Config.RangeInt(min = 1, max = 320)
    public static int invulnerabilityTime = 50;

    @Config.Name("flyingHeight")
    @Config.Comment("Target height above the highest nearby terrain during destroyer phases.")
    @Config.RangeInt(min = 10, max = 150)
    public static int flyingHeight = 75;

    @Config.Name("dynamicFlyingHeight")
    @Config.Comment("Whether destroyer phases periodically choose a new flying height.")
    public static boolean dynamicFlyingHeight = false;

    @Config.Name("dynamicFlyingHeightTime")
    @Config.Comment("Seconds between dynamic flying-height changes.")
    @Config.RangeInt(min = 15, max = 1200)
    public static int dynamicFlyingHeightTime = 60;

    @Config.Name("normalFlyingSpeed")
    @Config.Comment("Horizontal speed while chasing a moving target.")
    @Config.RangeDouble(min = 0.01D, max = 1.0D)
    public static double normalFlyingSpeed = 0.02D;

    @Config.Name("chasingFlyingSpeed")
    @Config.Comment("Horizontal speed while chasing a stationary target.")
    @Config.RangeDouble(min = 0.01D, max = 1.0D)
    public static double chasingFlyingSpeed = 0.4D;

    @Config.Name("tractorPullSpeedModifier")
    @Config.Comment("Base speed of entities pulled by tractor beams.")
    @Config.RangeDouble(min = 0.1D, max = 1.0D)
    public static double tractorPullSpeedModifier = 0.2D;

    @Config.Name("tractorBeamClusterPickUp")
    @Config.Comment("Whether tractor beams create and consume block clusters.")
    public static boolean tractorBeamClusterPickUp = true;

    @Config.Name("tractorBeamsRemoveFluids")
    @Config.Comment("Whether destroyer tractor beams remove fluids along their ray.")
    public static boolean tractorBeamsRemoveFluids = true;

    @Config.Name("tractorBeamFluidRemovalHeight")
    @Config.Comment("Minimum Y level at which tractor beams remove fluids.")
    @Config.RangeInt(min = -64, max = 320)
    public static int tractorBeamFluidRemovalHeight = 63;

    @Config.Name("canPickupMobClusters")
    @Config.Comment("Whether tractor beams can pull living entities into the storm.")
    public static boolean canPickupMobClusters = true;

    @Config.Name("canAttackHeads")
    @Config.Comment("Whether projectiles and attacks can injure individual storm heads.")
    public static boolean canAttackHeads = true;

    @Config.Name("headEscapeTime")
    @Config.Comment("Reserved head-escape protection duration in seconds.")
    @Config.RangeInt(min = 0, max = 60)
    public static int headEscapeTime = 40;

    @Config.Name("minimumRoarInterval")
    @Config.Comment("Minimum interval between storm head roars in seconds.")
    @Config.RangeInt(min = 1, max = 100)
    public static int minimumRoarInterval = 20;

    @Config.Name("maximumRoarInterval")
    @Config.Comment("Maximum interval between storm head roars in seconds.")
    @Config.RangeInt(min = 1, max = 100)
    public static int maximumRoarInterval = 50;

    @Config.Name("convertFallingBlocks")
    @Config.Comment("Whether nearby falling blocks become persistent storm clusters.")
    public static boolean convertFallingBlocks = false;

    @Config.Name("clusterPickupInterval")
    @Config.Comment("Block-cluster pickup interval for destroyer phases 4 and 5.")
    @Config.RangeInt(min = 10, max = 80)
    public static int clusterPickupInterval = 40;

    @Config.Name("devourerClusterPickupInterval")
    @Config.Comment("Block-cluster pickup interval for devourer phases 6 and 7.")
    @Config.RangeInt(min = 10, max = 80)
    public static int devourerClusterPickupInterval = 40;

    @Config.Name("clusterSizeModifier")
    @Config.Comment("Amount added to the radius of default block clusters.")
    @Config.RangeInt(min = 0, max = 16)
    public static int clusterSizeModifier = 0;

    @Config.Name("constantBlackhole")
    @Config.Comment("Remove small-cluster cooldowns. This can be extremely expensive.")
    public static boolean constantBlackhole = false;

    private LegacyWitherStormConfig() {
    }
}
