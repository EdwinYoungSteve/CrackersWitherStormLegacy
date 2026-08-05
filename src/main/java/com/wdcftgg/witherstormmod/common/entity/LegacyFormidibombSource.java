package com.wdcftgg.witherstormmod.common.entity;

import net.minecraft.util.math.Vec3d;

/** 统一实体和方块形态 Formidibomb 的客户端引信表现数据。 */
public interface LegacyFormidibombSource {
    int getFuseLife();

    int getStartFuse();

    Vec3d getFormidibombPosition();

    boolean isFormidibombAlive();
}
