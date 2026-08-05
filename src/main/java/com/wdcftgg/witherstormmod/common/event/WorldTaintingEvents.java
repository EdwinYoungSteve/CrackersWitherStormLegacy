package com.wdcftgg.witherstormmod.common.event;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.common.entity.EntitySickenedMob;
import com.wdcftgg.witherstormmod.common.entity.EntityWitherStormLegacy;
import com.wdcftgg.witherstormmod.common.init.ModEffects;
import com.wdcftgg.witherstormmod.common.taint.TaintingManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class WorldTaintingEvents {

    private WorldTaintingEvents() {
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote || event.world.getTotalWorldTime() % 10L != 0L) {
            return;
        }
        List<EntityWitherStormLegacy> storms = new ArrayList<EntityWitherStormLegacy>();
        for (Entity entity : event.world.loadedEntityList) {
            if (entity instanceof EntityWitherStormLegacy && !entity.isDead) {
                storms.add((EntityWitherStormLegacy) entity);
            }
        }
        for (EntityWitherStormLegacy storm : storms) {
            taintAroundStorm(storm);
        }
    }

    private static void taintAroundStorm(EntityWitherStormLegacy storm) {
        int phase = storm.getPhase();
        int radius = 12 + phase * 8;
        int attempts = 12 + phase * 10;
        BlockPos center = new BlockPos(storm.posX, storm.posY - storm.height * 0.35D, storm.posZ);
        for (int attempt = 0; attempt < attempts; attempt++) {
            BlockPos target = center.add(storm.getRNG().nextInt(radius * 2 + 1) - radius,
                    storm.getRNG().nextInt(Math.max(4, radius / 2)) - radius / 3,
                    storm.getRNG().nextInt(radius * 2 + 1) - radius);
            if (storm.world.isBlockLoaded(target)) {
                TaintingManager.taintBlock(storm.world, target);
            }
        }
        if (storm.ticksExisted % 40 != 0) {
            return;
        }
        AxisAlignedBB sicknessArea = storm.getEntityBoundingBox().grow(radius * 1.5D, radius, radius * 1.5D);
        List<EntityLivingBase> livingEntities = storm.world.getEntitiesWithinAABB(EntityLivingBase.class, sicknessArea,
                entity -> entity != storm && !(entity instanceof EntitySickenedMob));
        for (EntityLivingBase entity : livingEntities) {
            entity.addPotionEffect(new PotionEffect(ModEffects.WITHER_SICKNESS, 240, Math.min(2, phase / 3), false, true));
        }
    }
}
