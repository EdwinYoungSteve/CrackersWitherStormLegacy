package com.wdcftgg.witherstormmod.api.common.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * 1.12 replacement for the newer Forge living-conversion hooks used upstream.
 * Both entities are fully initialized when the event is posted.
 */
public abstract class SickenedMobConversionEvent extends Event {
    private final EntityLivingBase original;
    private final EntityLivingBase replacement;
    private final Direction direction;

    protected SickenedMobConversionEvent(EntityLivingBase original,
                                         EntityLivingBase replacement,
                                         Direction direction) {
        this.original = original;
        this.replacement = replacement;
        this.direction = direction;
    }

    public EntityLivingBase getOriginal() {
        return original;
    }

    public EntityLivingBase getReplacement() {
        return replacement;
    }

    public Direction getDirection() {
        return direction;
    }

    public enum Direction {
        INFECTION,
        CURE
    }

    @Cancelable
    public static final class Pre extends SickenedMobConversionEvent {
        private int delayTicks;

        public Pre(EntityLivingBase original, EntityLivingBase replacement,
                   Direction direction) {
            super(original, replacement, direction);
        }

        /**
         * When a cure is canceled, this value replaces the remaining cure time.
         * Zero retries on the next tick, matching Forge's conversion callback.
         */
        public int getDelayTicks() {
            return delayTicks;
        }

        public void setDelayTicks(int delayTicks) {
            this.delayTicks = Math.max(0, delayTicks);
        }
    }

    public static final class Post extends SickenedMobConversionEvent {
        public Post(EntityLivingBase original, EntityLivingBase replacement,
                    Direction direction) {
            super(original, replacement, direction);
        }
    }
}
