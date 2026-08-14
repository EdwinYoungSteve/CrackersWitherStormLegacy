package com.wdcftgg.witherstormmod.common.init;

import net.minecraft.stats.StatBase;
import net.minecraft.util.text.TextComponentTranslation;

/** 1.12.2 使用 StatBase 承载上游 1.20 的命名统计。 */
public final class ModStats {

    public static final String INTERACT_WITH_SUPER_BEACON_ID =
            "stat.witherstormmod.interact_with_super_beacon";
    public static StatBase INTERACT_WITH_SUPER_BEACON;

    private ModStats() {
    }

    public static void register() {
        if (INTERACT_WITH_SUPER_BEACON != null) return;
        INTERACT_WITH_SUPER_BEACON = new StatBase(INTERACT_WITH_SUPER_BEACON_ID,
                new TextComponentTranslation(INTERACT_WITH_SUPER_BEACON_ID)).registerStat();
    }
}
