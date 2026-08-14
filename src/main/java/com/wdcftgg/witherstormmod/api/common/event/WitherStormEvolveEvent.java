package com.wdcftgg.witherstormmod.api.common.event;

import com.wdcftgg.witherstormmod.common.entity.WitherStormEntity;

public class WitherStormEvolveEvent extends WitherStormEvent {
    private final int toPhase;

    public WitherStormEvolveEvent(WitherStormEntity storm, int toPhase) {
        super(storm);
        this.toPhase = toPhase;
    }

    public int getToPhase() {
        return toPhase;
    }
}
