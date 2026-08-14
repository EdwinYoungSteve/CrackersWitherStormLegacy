package com.wdcftgg.witherstormmod.client;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.common.entity.DistantStormPart;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/** 远距风暴位于未加载客户端区块时仍需推进网络插值和模型动画。 */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public final class DistantStormClientEvents {
    private DistantStormClientEvents() {
    }

    @SubscribeEvent
    public static void onCanUpdate(EntityEvent.CanUpdate event) {
        if (WitherStormClientConfig.distantRenderer
                && event.getEntity() instanceof DistantStormPart) {
            event.setCanUpdate(true);
        }
    }
}
