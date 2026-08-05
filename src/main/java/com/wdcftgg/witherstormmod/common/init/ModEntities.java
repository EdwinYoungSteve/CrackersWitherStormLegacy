package com.wdcftgg.witherstormmod.common.init;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.common.entity.EntityWitherStormLegacy;
import com.wdcftgg.witherstormmod.common.entity.EntityPowerfulExplosive;
import com.wdcftgg.witherstormmod.common.entity.SickenedEntities;
import com.wdcftgg.witherstormmod.common.entity.SupplementalEntities;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public final class ModEntities {

    private ModEntities() {
    }

    public static void register() {
        int entityId = 1;
        registerLiving("wither_storm", EntityWitherStormLegacy.class, entityId++, 0x21162C, 0x7F3FBA, 512);
        registerLiving("sickened_bee", SickenedEntities.SickenedBee.class, entityId++, 0xD6B34A, 0x6D2A83, 80);
        registerLiving("sickened_cat", SickenedEntities.SickenedCat.class, entityId++, 0x795548, 0xB22AFF, 80);
        registerLiving("sickened_chicken", SickenedEntities.SickenedChicken.class, entityId++, 0xD8D8D8, 0x6D2A83, 80);
        registerLiving("sickened_cow", SickenedEntities.SickenedCow.class, entityId++, 0x44352E, 0xA12AFF, 80);
        registerLiving("sickened_creeper", SickenedEntities.SickenedCreeper.class, entityId++, 0x3E8E31, 0xA12AFF, 80);
        registerLiving("sickened_iron_golem", SickenedEntities.SickenedIronGolem.class, entityId++, 0xC0A98B, 0xA12AFF, 96);
        registerLiving("sickened_mushroom_cow", SickenedEntities.SickenedMushroomCow.class, entityId++, 0xA93D37, 0x6D2A83, 80);
        registerLiving("sickened_parrot", SickenedEntities.SickenedParrot.class, entityId++, 0xCC2E2E, 0xA12AFF, 80);
        registerLiving("sickened_phantom", SickenedEntities.SickenedPhantom.class, entityId++, 0x52636A, 0xA12AFF, 96);
        registerLiving("sickened_pig", SickenedEntities.SickenedPig.class, entityId++, 0xE49A9A, 0x6D2A83, 80);
        registerLiving("sickened_pillager", SickenedEntities.SickenedPillager.class, entityId++, 0x53605C, 0xA12AFF, 96);
        registerLiving("sickened_skeleton", SickenedEntities.SickenedSkeleton.class, entityId++, 0xC7C7C7, 0x6D2A83, 80);
        registerLiving("sickened_snow_golem", SickenedEntities.SickenedSnowGolem.class, entityId++, 0xE8F1F1, 0xA12AFF, 80);
        registerLiving("sickened_spider", SickenedEntities.SickenedSpider.class, entityId++, 0x342D27, 0xA12AFF, 80);
        registerLiving("sickened_villager", SickenedEntities.SickenedVillager.class, entityId++, 0x7A5A42, 0x6D2A83, 80);
        registerLiving("sickened_vindicator", SickenedEntities.SickenedVindicator.class, entityId++, 0x596560, 0xA12AFF, 96);
        registerLiving("sickened_wolf", SickenedEntities.SickenedWolf.class, entityId++, 0xA9A9A9, 0x6D2A83, 80);
        registerLiving("sickened_zombie", SickenedEntities.SickenedZombie.class, entityId++, 0x507A4A, 0xA12AFF, 80);
        registerLiving("tentacle", SickenedEntities.Tentacle.class, entityId++, 0x201323, 0x913CC4, 160);
        registerLiving("withered_symbiont", SickenedEntities.WitheredSymbiont.class, entityId++, 0x251A2A, 0xD053FF, 160);
        registerLiving("tainted_slime", SickenedEntities.TaintedSlime.class, entityId++, 0x34203B, 0xA64DCF, 80);
        registerProjectile("super_tnt", EntityPowerfulExplosive.SuperTnt.class, entityId++);
        registerProjectile("formidibomb", EntityPowerfulExplosive.Formidibomb.class, entityId++);
        registerProjectile("flaming_wither_skull", SupplementalEntities.FlamingWitherSkull.class, entityId++);
        registerProjectile("blue_flaming_wither_skull", SupplementalEntities.BlueFlamingWitherSkull.class, entityId++);
        registerProjectile("tentacle_spike", SupplementalEntities.TentacleSpike.class, entityId++);
        registerProjectile("block_cluster", SupplementalEntities.BlockCluster.class, entityId++);
        registerLiving("command_block", SupplementalEntities.CommandBlockCore.class, entityId++, 0xBA6B33, 0x232323, 160);
        registerLiving("wither_storm_head", SupplementalEntities.WitherStormHead.class, entityId++, 0x21162C, 0x7F3FBA, 256);
        registerLiving("wither_storm_segment", SupplementalEntities.WitherStormSegment.class, entityId, 0x21162C, 0x4C285B, 256);
    }

    private static void registerLiving(String name, Class<? extends Entity> entityClass, int entityId, int primaryColor, int secondaryColor, int trackingRange) {
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, name), entityClass, name, entityId,
                Tags.MOD_ID, trackingRange, 2, true, primaryColor, secondaryColor);
    }

    private static void registerProjectile(String name, Class<? extends Entity> entityClass, int entityId) {
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, name), entityClass, name, entityId,
                Tags.MOD_ID, 160, 10, true);
    }
}
