package com.wdcftgg.witherstormmod.mixin;

import com.wdcftgg.witherstormmod.common.entity.WitherStormEntity;
import com.wdcftgg.witherstormmod.common.entity.SupplementalEntities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityFishHook.class)
public abstract class EntityFishHookMixin {

    @Shadow
    public Entity caughtEntity;

    @Inject(method = "bringInHookedEntity()V", at = @At("HEAD"))
    private void witherstormmod$ignoreHookedTractorBeamTarget(CallbackInfo callbackInfo) {
        EntityFishHook hook = (EntityFishHook) (Object) this;
        Entity hookedEntity = caughtEntity;
        if (hook.world.isRemote || hookedEntity == null || hookedEntity.isDead) return;

        AxisAlignedBB searchArea = hook.getEntityBoundingBox().grow(100.0D, 200.0D, 100.0D);
        for (WitherStormEntity storm : hook.world.getEntitiesWithinAABB(
                WitherStormEntity.class, searchArea)) {
            if (!storm.isDeadOrPlayingDead() && storm.isInsideTractorBeam(hookedEntity, 4.0D)) {
                storm.getIgnoredTargetsManager().addEntityToIgnore(hookedEntity);
            }
        }
        for (SupplementalEntities.WitherStormSegmentEntity segment :
                hook.world.getEntitiesWithinAABB(
                        SupplementalEntities.WitherStormSegmentEntity.class, searchArea)) {
            if (!segment.isDeadOrPlayingDead() && segment.isInsideTractorBeam(hookedEntity, 4.0D)) {
                segment.ignoreTractorBeamTarget(hookedEntity);
            }
        }
    }
}
