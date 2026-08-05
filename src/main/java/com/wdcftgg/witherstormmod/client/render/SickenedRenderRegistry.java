package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.client.model.ModelTentaclePort;
import com.wdcftgg.witherstormmod.client.model.ModelWitheredSymbiontPort;
import com.wdcftgg.witherstormmod.client.model.ModelSickenedIllagerPort;
import com.wdcftgg.witherstormmod.client.model.ModelSickenedVillagerPort;
import com.wdcftgg.witherstormmod.client.model.ModelSickenedBeePort;
import com.wdcftgg.witherstormmod.client.model.ModelSickenedCatPort;
import com.wdcftgg.witherstormmod.client.model.ModelSickenedPhantomPort;
import com.wdcftgg.witherstormmod.client.model.ModelSickenedParrotPort;
import com.wdcftgg.witherstormmod.client.model.ModelSickenedSkeletonPort;
import com.wdcftgg.witherstormmod.client.model.ModelSickenedWolfPort;
import com.wdcftgg.witherstormmod.client.model.ModelSickenedZombiePort;
import com.wdcftgg.witherstormmod.common.entity.SickenedEntities;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelChicken;
import net.minecraft.client.model.ModelCow;
import net.minecraft.client.model.ModelPig;
import net.minecraft.client.model.ModelSnowMan;
import net.minecraft.client.model.ModelSpider;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public final class SickenedRenderRegistry {

    private SickenedRenderRegistry() {
    }

    public static void register() {
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedBee.class,
                manager -> new RenderSickenedMob<SickenedEntities.SickenedBee>(manager, new ModelSickenedBeePort(), 0.4F, sickenedTexture("sickened_bee")));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedCat.class,
                manager -> new RenderSickenedMob<SickenedEntities.SickenedCat>(manager, new ModelSickenedCatPort(), 0.4F, sickenedTexture("sickened_cat")));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedChicken.class,
                manager -> new RenderSickenedMob<SickenedEntities.SickenedChicken>(manager, new ModelChicken(), 0.3F, sickenedTexture("sickened_chicken")));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedCow.class,
                manager -> new RenderSickenedMob<SickenedEntities.SickenedCow>(manager, new ModelCow(), 0.7F, sickenedTexture("sickened_cow")));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedCreeper.class,
                RenderSickenedCreeper::new);
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedIronGolem.class,
                RenderSickenedIronGolem::new);
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedMushroomCow.class,
                RenderSickenedMushroomCow::new);
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedParrot.class,
                manager -> new RenderSickenedMob<SickenedEntities.SickenedParrot>(manager, new ModelSickenedParrotPort(), 0.3F, sickenedTexture("sickened_parrot")));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedPhantom.class,
                RenderSickenedPhantom::new);
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedPig.class,
                manager -> new RenderSickenedMob<SickenedEntities.SickenedPig>(manager, new ModelPig(0.0F), 0.7F, sickenedTexture("sickened_pig")));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedPillager.class,
                manager -> new RenderSickenedMob<SickenedEntities.SickenedPillager>(manager, new ModelSickenedIllagerPort(true), 0.5F, sickenedTexture("sickened_pillager")));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedSkeleton.class,
                manager -> new RenderSickenedMob<SickenedEntities.SickenedSkeleton>(manager, new ModelSickenedSkeletonPort(), 0.5F, sickenedTexture("sickened_skeleton")));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedSnowGolem.class,
                RenderSickenedSnowGolem::new);
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedSpider.class,
                manager -> new RenderSickenedMob<SickenedEntities.SickenedSpider>(manager, new ModelSpider(), 0.8F,
                        sickenedTexture("sickened_spider"), 1.2F, 1.2F, 1.2F));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedVillager.class,
                manager -> new RenderSickenedMob<SickenedEntities.SickenedVillager>(manager, new ModelSickenedVillagerPort(), 0.5F, sickenedTexture("sickened_villager")));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedVindicator.class,
                manager -> new RenderSickenedMob<SickenedEntities.SickenedVindicator>(manager, new ModelSickenedIllagerPort(false), 0.5F, sickenedTexture("sickened_vindicator")));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedWolf.class,
                manager -> new RenderSickenedMob<SickenedEntities.SickenedWolf>(manager, new ModelSickenedWolfPort(), 0.45F, sickenedTexture("sickened_wolf")));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedZombie.class,
                manager -> new RenderSickenedMob<SickenedEntities.SickenedZombie>(manager, new ModelSickenedZombiePort(), 0.5F, sickenedTexture("sickened_zombie")));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.Tentacle.class,
                manager -> new RenderSickenedMob<SickenedEntities.Tentacle>(manager, new ModelTentaclePort(), 1.0F, "textures/entity/tentacle/tentacle.png"));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.WitheredSymbiont.class,
                RenderWitheredSymbiont::new);
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.TaintedSlime.class,
                RenderTaintedSlime::new);
    }

    private static String sickenedTexture(String name) {
        return "textures/entity/sickened/" + name + ".png";
    }
}
