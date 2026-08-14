package com.wdcftgg.witherstormmod.common.init;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;

/** 上游四个可同步风暴属性在 1.12 属性系统中的对应定义。 */
public final class ModAttributes {
    public static final IAttribute TARGET_STATIONARY_FLYING_SPEED = create(
            "attribute.witherstormmod.name.target_stationary_flying_speed", 0.4D, 0.01D, 1.0D);
    public static final IAttribute SLOW_FLYING_SPEED = create(
            "attribute.witherstormmod.name.slow_flying_speed", 0.02D, 0.01D, 1.0D);
    public static final IAttribute EVOLUTION_SPEED = create(
            "attribute.witherstormmod.name.evolution_speed", 1.0D, 0.0D, 1024.0D);
    public static final IAttribute HUNCHBACK_FOLLOW_RANGE = create(
            "attribute.witherstormmod.name.hunchback_follow_range", 32.0D, 0.0D, 2048.0D);

    private ModAttributes() {
    }

    public static void bootstrap() {
        // 触发静态初始化，供 API 在实体创建前取得稳定实例。
    }

    private static IAttribute create(String name, double defaultValue,
                                     double minimum, double maximum) {
        return new RangedAttribute(null, name, defaultValue, minimum, maximum)
                .setShouldWatch(true);
    }
}
