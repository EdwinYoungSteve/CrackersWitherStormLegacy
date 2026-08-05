package com.wdcftgg.witherstormmod.common.potion;

import com.wdcftgg.witherstormmod.common.taint.TaintingManager;
import com.wdcftgg.witherstormmod.common.taint.LegacyWitherSicknessCure;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;

public class PotionWitherSickness extends Potion {

    public PotionWitherSickness() {
        super(true, 0x582E67);
        setRegistryName("wither_sickness");
        setPotionName("effect.witherstormmod.wither_sickness");
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        if (entity.world.isRemote || LegacyWitherSicknessCure.isBeingCured(entity)) {
            return;
        }
        int sickness = entity.getEntityData().getInteger("WitherSicknessProgress") + 1 + amplifier;
        entity.getEntityData().setInteger("WitherSicknessProgress", sickness);
        if (sickness % 40 == 0) {
            entity.attackEntityFrom(net.minecraft.util.DamageSource.WITHER, 1.0F + amplifier * 0.5F);
        }
        if (sickness >= Math.max(200, 600 - amplifier * 100)) {
            TaintingManager.convertEntity(entity);
        }
    }
}
