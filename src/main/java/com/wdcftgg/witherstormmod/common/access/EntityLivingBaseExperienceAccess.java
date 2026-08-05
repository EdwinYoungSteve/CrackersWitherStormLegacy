package com.wdcftgg.witherstormmod.common.access;

/**
 * Public contract added to living entities by the experience-drop mixin.
 *
 * <p>This interface intentionally lives outside the mixin package because
 * Mixin forbids ordinary game code from loading classes owned by a mixin
 * package directly.</p>
 */
public interface EntityLivingBaseExperienceAccess {

    int witherstormmod$captureExperienceDrop();

    void witherstormmod$skipNextExperienceDrop();
}
