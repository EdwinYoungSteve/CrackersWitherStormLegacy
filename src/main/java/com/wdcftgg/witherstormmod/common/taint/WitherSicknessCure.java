package com.wdcftgg.witherstormmod.common.taint;

import com.wdcftgg.witherstormmod.common.capability.WitherSicknessCapability;
import com.wdcftgg.witherstormmod.common.capability.WitherSicknessTracker;
import net.minecraft.entity.EntityLivingBase;

public final class WitherSicknessCure {
    private WitherSicknessCure() {
    }

    public static boolean beginCure(EntityLivingBase entity) {
        WitherSicknessTracker tracker = WitherSicknessCapability.get(entity);
        if (tracker == null || !tracker.isInfected() || tracker.isBeingCured()
                || tracker.isActuallyImmune()) return false;
        tracker.beginCure();
        return true;
    }

    public static boolean isInfected(EntityLivingBase entity) {
        WitherSicknessTracker tracker = WitherSicknessCapability.get(entity);
        return tracker != null && tracker.isInfected();
    }

    public static boolean isBeingCured(EntityLivingBase entity) {
        WitherSicknessTracker tracker = WitherSicknessCapability.get(entity);
        return tracker != null && tracker.isBeingCured();
    }

    public static boolean isActuallyImmune(EntityLivingBase entity) {
        WitherSicknessTracker tracker = WitherSicknessCapability.get(entity);
        return tracker == null || tracker.isActuallyImmune();
    }
}
