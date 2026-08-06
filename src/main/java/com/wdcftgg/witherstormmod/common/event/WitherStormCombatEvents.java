package com.wdcftgg.witherstormmod.common.event;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.common.entity.WitherStormEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class WitherStormCombatEvents {

    private WitherStormCombatEvents() {
    }

    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        World world = event.getWorld();
        if (world.isRemote) return;
        EntityLivingBase source = event.getExplosion().getExplosivePlacedBy();
        if (source == null || source instanceof WitherStormEntity) return;
        Vec3d explosionPosition = event.getExplosion().getPosition();
        AxisAlignedBB search = new AxisAlignedBB(explosionPosition.x, explosionPosition.y, explosionPosition.z,
                explosionPosition.x, explosionPosition.y, explosionPosition.z).grow(100.0D);
        for (WitherStormEntity storm : world.getEntitiesWithinAABB(WitherStormEntity.class, search)) {
            for (int head = 0; head < storm.getTotalHeads(); head++) {
                if (!storm.tractorBeamActive(head) || storm.isHeadInjured(head)) continue;
                double radius = storm.getPhase() < 4 ? 5.0D : 12.0D;
                if (storm.getHeadPosition(head, 1.0F).squareDistanceTo(explosionPosition) < radius * radius) {
                    storm.attackHeadFromExplosion(head, source);
                }
            }
        }
    }
}
