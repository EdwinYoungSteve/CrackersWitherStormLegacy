package com.wdcftgg.witherstormmod.mixin;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 1.12 may update an already scheduled melee task once after another task clears
 * its target. Vanilla updateTask dereferences that target without a null check.
 */
@Mixin(EntityAIAttackMelee.class)
public abstract class EntityAIAttackMeleeMixin {

    @Shadow
    protected EntityCreature attacker;

    @Inject(method = "updateTask()V", at = @At("HEAD"), cancellable = true)
    private void witherstormmod$skipUpdateWithoutTarget(CallbackInfo callback) {
        if (attacker.getAttackTarget() == null) callback.cancel();
    }
}
