package com.wdcftgg.witherstormmod.common.init;

import com.wdcftgg.witherstormmod.Tags;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class ModPotionTypes {

    public static final PotionType WITHER = create("wither", 900, 0);
    public static final PotionType LONG_WITHER = create("long_wither", 1800, 0);
    public static final PotionType STRONG_WITHER = create("strong_wither", 432, 1);

    private ModPotionTypes() {
    }

    public static void bootstrap() {
    }

    @SubscribeEvent
    public static void registerPotionTypes(RegistryEvent.Register<PotionType> event) {
        event.getRegistry().registerAll(WITHER, LONG_WITHER, STRONG_WITHER);
    }

    private static PotionType create(String name, int duration, int amplifier) {
        PotionType potionType = new PotionType(new PotionEffect(MobEffects.WITHER, duration, amplifier));
        potionType.setRegistryName(new ResourceLocation(Tags.MOD_ID, name));
        return potionType;
    }
}
