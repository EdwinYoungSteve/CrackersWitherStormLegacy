package com.wdcftgg.witherstormmod.common.entity.ai;

import com.wdcftgg.witherstormmod.common.entity.SickenedEntities;
import com.wdcftgg.witherstormmod.common.entity.SickenedMobEntity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;

/** 让上游指定的原版生物主动对抗病化生物。 */
public final class FightSickenedMobsAI extends EntityAINearestAttackableTarget<SickenedMobEntity> {
    public FightSickenedMobsAI(EntityCreature creature) {
        super(creature, SickenedMobEntity.class, 10, true, false,
                target -> target != null && !(target instanceof SickenedEntities.SickenedCreeperEntity));
    }
}
