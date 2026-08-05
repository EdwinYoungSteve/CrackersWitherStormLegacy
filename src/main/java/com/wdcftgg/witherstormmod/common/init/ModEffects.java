package com.wdcftgg.witherstormmod.common.init;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.common.potion.PotionWitherSickness;
import net.minecraft.potion.Potion;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class ModEffects {

    public static final Potion WITHER_SICKNESS = new PotionWitherSickness();

    private ModEffects() {
    }

    public static void bootstrap() {
    }

    @SubscribeEvent
    public static void registerPotions(RegistryEvent.Register<Potion> event) {
        event.getRegistry().register(WITHER_SICKNESS);
    }
}
