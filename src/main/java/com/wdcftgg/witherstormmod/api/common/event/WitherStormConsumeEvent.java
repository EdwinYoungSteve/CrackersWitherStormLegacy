package com.wdcftgg.witherstormmod.api.common.event;

import com.wdcftgg.witherstormmod.common.entity.WitherStormEntity;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

import javax.annotation.Nullable;

@Cancelable
public class WitherStormConsumeEvent extends WitherStormEvent {
    private final Entity consumingEntity;
    @Nullable
    private final Entity consumedEntity;
    private int consumedAmount;

    public WitherStormConsumeEvent(WitherStormEntity storm, @Nullable Entity consumedEntity,
                                   int consumedAmount) {
        this(storm, storm, consumedEntity, consumedAmount);
    }

    public WitherStormConsumeEvent(WitherStormEntity storm, Entity consumingEntity,
                                   @Nullable Entity consumedEntity, int consumedAmount) {
        super(storm);
        this.consumingEntity = consumingEntity;
        this.consumedEntity = consumedEntity;
        this.consumedAmount = consumedAmount;
    }

    public Entity getConsumingEntity() {
        return consumingEntity;
    }

    @Nullable
    public Entity getConsumedEntity() {
        return consumedEntity;
    }

    public int getConsumedAmount() {
        return consumedAmount;
    }

    public void setConsumedAmount(int consumedAmount) {
        this.consumedAmount = consumedAmount;
    }
}
