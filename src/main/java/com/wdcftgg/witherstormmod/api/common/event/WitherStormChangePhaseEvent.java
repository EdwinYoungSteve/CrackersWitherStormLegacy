package com.wdcftgg.witherstormmod.api.common.event;

import com.wdcftgg.witherstormmod.common.entity.WitherStormEntity;

public class WitherStormChangePhaseEvent extends WitherStormEvent {
    private final int toPhase;

    public WitherStormChangePhaseEvent(WitherStormEntity storm, int toPhase) {
        super(storm);
        this.toPhase = toPhase;
    }

    public int getToPhase() {
        return toPhase;
    }
}
