package com.wdcftgg.witherstormmod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Accesses the vanilla 1.12 bullet state needed by the upstream spell transition. */
@Mixin(EntityShulkerBullet.class)
public interface EntityShulkerBulletAccessor {
    @Accessor("owner")
    void witherstormmod$setOwner(EntityLivingBase owner);

    @Accessor("target")
    void witherstormmod$setTarget(Entity target);

    @Accessor("direction")
    void witherstormmod$setDirection(EnumFacing direction);

    @Invoker("selectNextMoveDirection")
    void witherstormmod$selectNextMoveDirection(EnumFacing.Axis axis);
}
