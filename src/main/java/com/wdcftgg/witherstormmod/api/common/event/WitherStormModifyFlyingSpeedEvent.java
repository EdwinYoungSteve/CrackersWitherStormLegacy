package com.wdcftgg.witherstormmod.api.common.event;

import com.wdcftgg.witherstormmod.common.entity.WitherStormEntity;

public class WitherStormModifyFlyingSpeedEvent extends WitherStormEvent {
    private double speed;

    public WitherStormModifyFlyingSpeedEvent(WitherStormEntity storm, double originalSpeed) {
        super(storm);
        speed = originalSpeed;
    }

    public double getOriginalSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}
