package com.wdcftgg.witherstormmod.common.util;

public enum ItemPreservationCondition {
    ANY_WITHER_STORM_DEATH(false, true),
    CHOMPED_OR_KILLED_NEAR_HEAD(false, false),
    CHOMPED(true, false),
    DISABLED(true, false);

    private final boolean directEntity;
    private final boolean fallBackToVictim;

    ItemPreservationCondition(boolean directEntity, boolean fallBackToVictim) {
        this.directEntity = directEntity;
        this.fallBackToVictim = fallBackToVictim;
    }

    public boolean usesDirectEntity() {
        return directEntity;
    }

    public boolean fallsBackToVictim() {
        return fallBackToVictim;
    }
}
