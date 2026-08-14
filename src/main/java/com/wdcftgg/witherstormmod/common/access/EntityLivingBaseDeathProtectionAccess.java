package com.wdcftgg.witherstormmod.common.access;

/** 由 LivingEntity Mixin 提供的图腾死亡保护状态访问契约。 */
public interface EntityLivingBaseDeathProtectionAccess {

    void witherstormmod$setDeathProtectionActive(boolean active);

    boolean witherstormmod$isDeathProtectionActive();
}
