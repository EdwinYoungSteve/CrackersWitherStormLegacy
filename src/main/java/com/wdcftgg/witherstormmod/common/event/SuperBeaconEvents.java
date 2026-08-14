package com.wdcftgg.witherstormmod.common.event;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.common.inventory.SuperBeaconContainer;
import com.wdcftgg.witherstormmod.common.network.ModNetwork;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class SuperBeaconEvents {
    private SuperBeaconEvents() {
    }

    @SubscribeEvent
    public static void onPlayerOpenContainer(PlayerContainerEvent.Open event) {
        if (!(event.getEntityPlayer() instanceof EntityPlayerMP)
                || !(event.getContainer() instanceof SuperBeaconContainer)) return;
        SuperBeaconContainer container = (SuperBeaconContainer) event.getContainer();
        ModNetwork.sendSuperBeaconValidEffects((EntityPlayerMP) event.getEntityPlayer(),
                container.getValidEffects());
    }
}
