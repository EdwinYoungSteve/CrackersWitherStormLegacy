package com.wdcftgg.witherstormmod.common.entity;

import net.minecraft.util.math.Vec3d;


public interface FormidibombSource {
    int getFuseLife();

    int getStartFuse();

    Vec3d getFormidibombPosition();

    boolean isFormidibombAlive();
}
