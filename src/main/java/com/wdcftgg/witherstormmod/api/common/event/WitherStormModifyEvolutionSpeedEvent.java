package com.wdcftgg.witherstormmod.api.common.event;

import com.wdcftgg.witherstormmod.common.entity.WitherStormEntity;

public class WitherStormModifyEvolutionSpeedEvent extends WitherStormEvent {
    private double evolutionSpeedModifier;

    public WitherStormModifyEvolutionSpeedEvent(WitherStormEntity storm,
                                                double evolutionSpeedModifier) {
        super(storm);
        this.evolutionSpeedModifier = evolutionSpeedModifier;
    }

    public double getOriginalEvolutionSpeedModifier() {
        return evolutionSpeedModifier;
    }

    public void setEvolutionSpeedModifier(double evolutionSpeedModifier) {
        this.evolutionSpeedModifier = evolutionSpeedModifier;
    }
}
