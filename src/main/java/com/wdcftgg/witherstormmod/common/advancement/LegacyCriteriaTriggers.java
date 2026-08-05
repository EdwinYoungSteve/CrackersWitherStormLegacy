package com.wdcftgg.witherstormmod.common.advancement;

import com.wdcftgg.witherstormmod.Tags;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.util.ResourceLocation;

public final class LegacyCriteriaTriggers {

    public static final LegacyEntityTrigger PLAY_DEAD = entityTrigger("wither_storm_play_dead", "entity");
    public static final LegacyEntityTrigger REVIVAL = entityTrigger("wither_storm_revival", "entity");
    public static final LegacyEntityTrigger ESCAPE_WITHER_STORM = entityTrigger("escape_wither_storm", "entity");
    public static final LegacyEntityTrigger RING_BELL_NEAR_STORM = entityTrigger("ring_bell_near_storm", "entity");
    public static final LegacyEntityTrigger NEARLY_KILL_WITHER_STORM = entityTrigger("nearly_kill_wither_storm", "entity");
    public static final LegacyCuredSickenedMobTrigger CURED_SICKENED_MOB =
            new LegacyCuredSickenedMobTrigger(id("cured_sickened_mob"));
    public static final LegacyCountTrigger ACTIVATE_SUPER_BEACON =
            new LegacyCountTrigger(id("activate_super_beacon"), "total_activated");
    public static final LegacyEntityTrigger SUMMON_MOB_SUPER_BEACON =
            entityTrigger("summon_mob_withered_beacon", "resummoned");
    public static final LegacyLinkAmuletTrigger LINK_AMULET = new LegacyLinkAmuletTrigger();
    public static final LegacyObserveWitherStormTrigger OBSERVE_WITHER_STORM =
            new LegacyObserveWitherStormTrigger(id("observe_wither_storm"));
    private static boolean registered;

    private LegacyCriteriaTriggers() {
    }

    public static synchronized void register() {
        if (registered) return;
        CriteriaTriggers.register(PLAY_DEAD);
        CriteriaTriggers.register(REVIVAL);
        CriteriaTriggers.register(ESCAPE_WITHER_STORM);
        CriteriaTriggers.register(RING_BELL_NEAR_STORM);
        CriteriaTriggers.register(NEARLY_KILL_WITHER_STORM);
        CriteriaTriggers.register(CURED_SICKENED_MOB);
        CriteriaTriggers.register(ACTIVATE_SUPER_BEACON);
        CriteriaTriggers.register(SUMMON_MOB_SUPER_BEACON);
        CriteriaTriggers.register(LINK_AMULET);
        CriteriaTriggers.register(OBSERVE_WITHER_STORM);
        registered = true;
    }

    private static LegacyEntityTrigger entityTrigger(String path, String conditionKey) {
        return new LegacyEntityTrigger(id(path), conditionKey);
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(Tags.MOD_ID, path);
    }
}
