package com.wdcftgg.witherstormmod.api.common.ai.witherstorm.pullbehavior;

import com.wdcftgg.witherstormmod.common.entity.WitherStormEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

/**
 * 可注册到 WitherStormWorldInteractions 的牵引行为扩展点（1.12.2 语义）。
 */
public abstract class WitherStormPullBehavior<T extends Entity> {

    private final double defaultSpeed;

    public WitherStormPullBehavior(double defaultSpeed) {
        this.defaultSpeed = defaultSpeed;
    }

    public WitherStormPullBehavior() {
        this(0.5D);
    }

    /**
     * @param absorptionPoint 风暴用于吸收实体的目标点
     * @param defaultVelocity 指向吸收点、已按 {@code defaultSpeed} 缩放的默认速度
     * @param defaultSpeed {@link #getSpeed(Entity, WitherStormEntity, Vec3d)} 的返回值
     */
    public abstract Vec3d pullEntity(T entity, WitherStormEntity storm, Vec3d absorptionPoint,
                                     Vec3d defaultVelocity, double defaultSpeed);

    public double getSpeed(T entity, WitherStormEntity storm, Vec3d destination) {
        return defaultSpeed;
    }

    public boolean canPullIn(T entity, WitherStormEntity storm) {
        return true;
    }

    public boolean doClientsideVelocityUpdates(T entity, WitherStormEntity storm) {
        return false;
    }
}
