package com.wdcftgg.witherstormmod.mixin;

import com.wdcftgg.witherstormmod.common.entity.DistantStormPart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 让风暴实体使用其注册范围，不被服务器普通实体视距上限截断。 */
@Mixin(EntityTrackerEntry.class)
public abstract class EntityTrackerEntryMixin {
    @Shadow @Final private Entity trackedEntity;
    @Shadow @Final private int range;
    @Shadow private long encodedPosX;
    @Shadow private long encodedPosZ;

    @Inject(method = "isVisibleTo(Lnet/minecraft/entity/player/EntityPlayerMP;)Z",
            at = @At("HEAD"), cancellable = true)
    private void witherstormmod$useDistantStormRange(EntityPlayerMP player,
                                                      CallbackInfoReturnable<Boolean> callback) {
        if (!(trackedEntity instanceof DistantStormPart)) return;
        double distanceX = player.posX - encodedPosX / 4096.0D;
        double distanceZ = player.posZ - encodedPosZ / 4096.0D;
        callback.setReturnValue(distanceX >= -range && distanceX <= range
                && distanceZ >= -range && distanceZ <= range
                && trackedEntity.isSpectatedByPlayer(player));
    }
}
