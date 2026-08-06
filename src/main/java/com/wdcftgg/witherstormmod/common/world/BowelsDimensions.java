package com.wdcftgg.witherstormmod.common.world;

import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;

public final class BowelsDimensions {
    public static final int DIMENSION_ID = 223;
    public static DimensionType BOWELS;

    private BowelsDimensions() {
    }

    public static void register() {
        if (BOWELS == null) {
            BOWELS = DimensionType.register("wither_storm_bowels", "_wither_storm_bowels", DIMENSION_ID,
                    BowelsWorldProvider.class, false);
        }
        if (!DimensionManager.isDimensionRegistered(DIMENSION_ID)) {
            DimensionManager.registerDimension(DIMENSION_ID, BOWELS);
        }
    }
}
