package com.wdcftgg.witherstormmod.mixin;

import com.wdcftgg.witherstormmod.common.access.EntityLivingBaseExperienceAccess;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityLivingBase.class)
public abstract class EntityLivingBaseMixin implements EntityLivingBaseExperienceAccess {

    @Shadow
    protected EntityPlayer attackingPlayer;

    @Shadow
    protected abstract int getExperiencePoints(EntityPlayer player);

    @Unique
    private boolean witherstormmod$skipNextExperienceDrop;

    @Override
    public int witherstormmod$captureExperienceDrop() {
        EntityLivingBase entity = (EntityLivingBase) (Object) this;
        int originalExperience = getExperiencePoints(attackingPlayer);
        return ForgeEventFactory.getExperienceDrop(entity, attackingPlayer, originalExperience);
    }

    @Override
    public void witherstormmod$skipNextExperienceDrop() {
        witherstormmod$skipNextExperienceDrop = true;
    }

    @WrapOperation(
            method = "onDeathUpdate()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/event/ForgeEventFactory;getExperienceDrop(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/entity/player/EntityPlayer;I)I",
                    remap = false))
    private int witherstormmod$skipCapturedExperience(
            EntityLivingBase entity, EntityPlayer player, int originalExperience,
            Operation<Integer> originalOperation) {
        if (!witherstormmod$skipNextExperienceDrop) {
            return originalOperation.call(entity, player, originalExperience);
        }
        witherstormmod$skipNextExperienceDrop = false;
        return 0;
    }
}
