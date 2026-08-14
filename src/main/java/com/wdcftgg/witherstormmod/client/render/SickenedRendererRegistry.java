package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.client.model.WitheredSymbiontModel;
import com.wdcftgg.witherstormmod.client.model.SickenedIllagerModel;
import com.wdcftgg.witherstormmod.client.model.SickenedVillagerModel;
import com.wdcftgg.witherstormmod.client.model.SickenedBeeModel;
import com.wdcftgg.witherstormmod.client.model.SickenedCatModel;
import com.wdcftgg.witherstormmod.client.model.SickenedPhantomModel;
import com.wdcftgg.witherstormmod.client.model.SickenedSkeletonModel;
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
                SickenedChickenRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedCowEntity.class,
                manager -> new SickenedMobRenderer<SickenedEntities.SickenedCowEntity>(manager, new ModelCow(), 0.7F, sickenedTexture("sickened_cow")));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedCreeperEntity.class,
                SickenedCreeperRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedIronGolemEntity.class,
                SickenedIronGolemRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedMushroomCowEntity.class,
                SickenedMushroomCowRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedParrotEntity.class,
                SickenedParrotRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedPhantomEntity.class,
                SickenedPhantomRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedPigEntity.class,
                manager -> new SickenedMobRenderer<SickenedEntities.SickenedPigEntity>(manager, new ModelPig(0.0F), 0.7F, sickenedTexture("sickened_pig")));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedPillagerEntity.class,
                manager -> new SickenedIllagerRenderer<SickenedEntities.SickenedPillagerEntity>(
                        manager, true, false, sickenedTexture("sickened_pillager")));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedSkeletonEntity.class,
                manager -> new SickenedBipedRenderer<SickenedEntities.SickenedSkeletonEntity>(
                        manager, new SickenedSkeletonModel(), 0.5F,
                        sickenedTexture("sickened_skeleton"), true));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedSnowGolemEntity.class,
                SickenedSnowGolemRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedSpiderEntity.class,
                manager -> new SickenedMobRenderer<SickenedEntities.SickenedSpiderEntity>(manager, new ModelSpider(), 0.8F,
                        sickenedTexture("sickened_spider"), 1.2F, 1.2F, 1.2F));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedVillagerEntity.class,
                SickenedVillagerRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedVindicatorEntity.class,
                manager -> new SickenedIllagerRenderer<SickenedEntities.SickenedVindicatorEntity>(
                        manager, false, true, sickenedTexture("sickened_vindicator")));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedWolfEntity.class,
                SickenedWolfRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.SickenedZombieEntity.class,
                manager -> new SickenedBipedRenderer<SickenedEntities.SickenedZombieEntity>(
                        manager, new SickenedZombieModel(), 0.5F,
                        sickenedTexture("sickened_zombie"), false));
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.TentacleEntity.class,
                TentacleRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.WitheredSymbiontEntity.class,
                WitheredSymbiontRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(SickenedEntities.TaintedSlimeEntity.class,
                TaintedSlimeRenderer::new);
    }

    private static String sickenedTexture(String name) {
        return "textures/entity/sickened/" + name + ".png";
    }
}
