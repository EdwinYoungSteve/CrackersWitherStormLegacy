package com.wdcftgg.witherstormmod.mixin;

import com.wdcftgg.witherstormmod.common.entity.SickenedMobEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 对应上游 MixinLivingEntity.canAttack 的 1.12 等价：1.12 没有 canAttack API，
 * 改在凋灵发射头颅前跳过病化生物目标（覆盖其受击后反击病化生物的情况）。
 */
@Mixin(EntityWither.class)
public abstract class WitherMixin {

    @Inject(method = "attackEntityWithRangedAttack(Lnet/minecraft/entity/EntityLivingBase;F)V",
            at = @At("HEAD"), cancellable = true)
    private void witherstormmod$skipSickenedTargets(
            EntityLivingBase target, float distanceFactor, CallbackInfo callbackInfo) {
        if (target instanceof SickenedMobEntity) {
            callbackInfo.cancel();
        }
    }
}
