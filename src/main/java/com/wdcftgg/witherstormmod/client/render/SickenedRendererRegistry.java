package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.client.model.TentacleModel;
import com.wdcftgg.witherstormmod.client.model.WitheredSymbiontModel;
import com.wdcftgg.witherstormmod.client.model.SickenedIllagerModel;
import com.wdcftgg.witherstormmod.client.model.SickenedVillagerModel;
import com.wdcftgg.witherstormmod.client.model.SickenedBeeModel;
import com.wdcftgg.witherstormmod.client.model.SickenedCatModel;
import com.wdcftgg.witherstormmod.client.model.SickenedPhantomModel;
import com.wdcftgg.witherstormmod.client.model.SickenedParrotModel;
import com.wdcftgg.witherstormmod.client.model.SickenedSkeletonModel;
import com.wdcftgg.witherstormmod.client.model.SickenedWolfModel;
import com.wdcftgg.witherstormmod.client.model.SickenedZombieModel;
import com.wdcftgg.witherstormmod.common.entity.SickenedEntities;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelChicken;
import net.minecraft.client.model.ModelCow;
import net.minecraft.client.model.ModelPig;
import net.minecraft.client.model.ModelSnowMan;
import net.minecraft.client.model.ModelSpider;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public final class SickenedRendererRegistry {

    private SickenedRendererRegistry() {
    }

    public static void register() {
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedBeeEntity.class,
                manager -> new SickenedMobRenderer<SickenedEntities.SickenedBeeEntity>(manager, new SickenedBeeModel(), 0.4F, sickenedTexture("sickened_bee")));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedCatEntity.class,
                manager -> new SickenedMobRenderer<SickenedEntities.SickenedCatEntity>(manager, new SickenedCatModel(), 0.4F, sickenedTexture("sickened_cat")));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedChickenEntity.class,
                manager -> new SickenedMobRenderer<SickenedEntities.SickenedChickenEntity>(manager, new ModelChicken(), 0.3F, sickenedTexture("sickened_chicken")));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedCowEntity.class,
                manager -> new SickenedMobRenderer<SickenedEntities.SickenedCowEntity>(manager, new ModelCow(), 0.7F, sickenedTexture("sickened_cow")));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedCreeperEntity.class,
                SickenedCreeperRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedIronGolemEntity.class,
                SickenedIronGolemRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedMushroomCowEntity.class,
                SickenedMushroomCowRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedParrotEntity.class,
                manager -> new SickenedMobRenderer<SickenedEntities.SickenedParrotEntity>(manager, new SickenedParrotModel(), 0.3F, sickenedTexture("sickened_parrot")));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedPhantomEntity.class,
                SickenedPhantomRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedPigEntity.class,
                manager -> new SickenedMobRenderer<SickenedEntities.SickenedPigEntity>(manager, new ModelPig(0.0F), 0.7F, sickenedTexture("sickened_pig")));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedPillagerEntity.class,
                manager -> new SickenedMobRenderer<SickenedEntities.SickenedPillagerEntity>(manager, new SickenedIllagerModel(true), 0.5F, sickenedTexture("sickened_pillager")));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedSkeletonEntity.class,
                manager -> new SickenedMobRenderer<SickenedEntities.SickenedSkeletonEntity>(manager, new SickenedSkeletonModel(), 0.5F, sickenedTexture("sickened_skeleton")));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedSnowGolemEntity.class,
                SickenedSnowGolemRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedSpiderEntity.class,
                manager -> new SickenedMobRenderer<SickenedEntities.SickenedSpiderEntity>(manager, new ModelSpider(), 0.8F,
                        sickenedTexture("sickened_spider"), 1.2F, 1.2F, 1.2F));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedVillagerEntity.class,
                manager -> new SickenedMobRenderer<SickenedEntities.SickenedVillagerEntity>(manager, new SickenedVillagerModel(), 0.5F, sickenedTexture("sickened_villager")));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedVindicatorEntity.class,
                manager -> new SickenedMobRenderer<SickenedEntities.SickenedVindicatorEntity>(manager, new SickenedIllagerModel(false), 0.5F, sickenedTexture("sickened_vindicator")));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedWolfEntity.class,
                manager -> new SickenedMobRenderer<SickenedEntities.SickenedWolfEntity>(manager, new SickenedWolfModel(), 0.45F, sickenedTexture("sickened_wolf")));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedZombieEntity.class,
                manager -> new SickenedMobRenderer<SickenedEntities.SickenedZombieEntity>(manager, new SickenedZombieModel(), 0.5F, sickenedTexture("sickened_zombie")));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.TentacleEntity.class,
                manager -> new SickenedMobRenderer<SickenedEntities.TentacleEntity>(manager, new TentacleModel(), 1.0F, "textures/entity/tentacle/tentacle.png"));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.WitheredSymbiontEntity.class,
                WitheredSymbiontRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.TaintedSlimeEntity.class,
                TaintedSlimeRenderer::new);
    }

    private static String sickenedTexture(String name) {
        return "textures/entity/sickened/" + name + ".png";
    }
}
