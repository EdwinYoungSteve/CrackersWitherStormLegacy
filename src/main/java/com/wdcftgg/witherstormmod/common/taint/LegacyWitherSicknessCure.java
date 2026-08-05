package com.wdcftgg.witherstormmod.common.taint;

import com.wdcftgg.witherstormmod.common.init.ModEffects;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;

public final class LegacyWitherSicknessCure {

    static final int CURE_DURATION = 1200;
    private static final String BEING_CURED_TAG = "WitherSicknessBeingCured";
    private static final String CURE_TICKS_TAG = "WitherSicknessCureTicks";
    private static final String PROGRESS_TAG = "WitherSicknessProgress";

    private LegacyWitherSicknessCure() {
    }

    public static boolean beginCure(EntityLivingBase entity) {
        return beginCure(entity.getEntityData(), isInfected(entity));
    }

    public static boolean isInfected(EntityLivingBase entity) {
        return entity.isPotionActive(ModEffects.WITHER_SICKNESS)
                || entity.getEntityData().getInteger(PROGRESS_TAG) > 0;
    }

    public static boolean isBeingCured(EntityLivingBase entity) {
        return isBeingCured(entity.getEntityData());
    }

    public static void tick(EntityLivingBase entity) {
        if (entity.world.isRemote || !isBeingCured(entity)) {
            return;
        }
        if (advanceCure(entity.getEntityData(), isInfected(entity))) {
            entity.removePotionEffect(ModEffects.WITHER_SICKNESS);
            entity.getEntityData().removeTag(PROGRESS_TAG);
        }
    }

    static boolean beginCure(NBTTagCompound data, boolean infected) {
        if (!infected || isBeingCured(data)) {
            return false;
        }
        data.setBoolean(BEING_CURED_TAG, true);
        data.setInteger(CURE_TICKS_TAG, 0);
        return true;
    }

    static boolean advanceCure(NBTTagCompound data, boolean infected) {
        if (!isBeingCured(data)) {
            return false;
        }
        if (!infected) {
            clearCure(data);
            return false;
        }
        int cureTicks = data.getInteger(CURE_TICKS_TAG) + 1;
        if (cureTicks < CURE_DURATION) {
            data.setInteger(CURE_TICKS_TAG, cureTicks);
            return false;
        }
        clearCure(data);
        return true;
    }

    static boolean isBeingCured(NBTTagCompound data) {
        return data.getBoolean(BEING_CURED_TAG);
    }

    static int getCureTicks(NBTTagCompound data) {
        return data.getInteger(CURE_TICKS_TAG);
    }

    private static void clearCure(NBTTagCompound data) {
        data.removeTag(BEING_CURED_TAG);
        data.removeTag(CURE_TICKS_TAG);
    }
}
