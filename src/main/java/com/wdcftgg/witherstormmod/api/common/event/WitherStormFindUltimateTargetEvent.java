package com.wdcftgg.witherstormmod.api.common.event;

import com.wdcftgg.witherstormmod.common.entity.WitherStormEntity;
import net.minecraft.entity.EntityLivingBase;

import javax.annotation.Nullable;

public class WitherStormFindUltimateTargetEvent extends WitherStormEvent {
    @Nullable
    private EntityLivingBase target;

    public WitherStormFindUltimateTargetEvent(WitherStormEntity storm,
                                              @Nullable EntityLivingBase originalTarget) {
        super(storm);
        target = originalTarget;
    }

    @Nullable
    public EntityLivingBase getOriginalUltimateTarget() {
        return target;
    }

    public void setUltimateTarget(@Nullable EntityLivingBase target) {
        this.target = target;
    }
}
