package com.wdcftgg.witherstormmod.common.entity;

import net.minecraft.util.math.Vec3d;

/** 主风暴、分裂体和独立头共用的牵引光束几何与状态。 */
public interface TractorBeamProvider {
    int getTotalHeads();

    boolean tractorBeamActive(int head);

    boolean isDeadOrPlayingDead();

    Vec3d getHeadPositionForBeam(int head);

    default Vec3d getHeadPositionForBeam(int head, float partialTicks) {
        return getHeadPositionForBeam(head);
    }

    Vec3d getHeadDirectionForBeam(int head);

    default Vec3d getHeadDirectionForBeam(int head, float partialTicks) {
        return getHeadDirectionForBeam(head);
    }

    /** 返回该头部光束的有效距离；负数表示没有截断。 */
    default double getTractorBeamCutoffDistance(int head) {
        return -1.0D;
    }

    default double getTractorBeamCutoffDistance(int head, float partialTicks) {
        return getTractorBeamCutoffDistance(head);
    }
}
