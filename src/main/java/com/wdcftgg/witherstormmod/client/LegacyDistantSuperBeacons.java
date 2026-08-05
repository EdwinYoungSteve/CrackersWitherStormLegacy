package com.wdcftgg.witherstormmod.client;

import com.wdcftgg.witherstormmod.common.network.LegacyNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityBeaconRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(
        modid = com.wdcftgg.witherstormmod.Tags.MOD_ID,
        value = net.minecraftforge.fml.relauncher.Side.CLIENT)
public final class LegacyDistantSuperBeacons {
    private static final Map<BlockPos, State> STATES = new HashMap<BlockPos, State>();

    private LegacyDistantSuperBeacons() {
    }

    public static void update(LegacyNetwork.DistantSuperBeaconMessage message) {
        if (message.isRemoved()) STATES.remove(message.getPosition());
        else STATES.put(message.getPosition(), new State(message));
    }

    @SubscribeEvent
    public static void render(RenderWorldLastEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.world == null || minecraft.player == null || STATES.isEmpty()) return;
        long now = minecraft.world.getTotalWorldTime();
        Iterator<Map.Entry<BlockPos, State>> iterator = STATES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, State> entry = iterator.next();
            State state = entry.getValue();
            if (now - state.lastUpdated > 100L) {
                iterator.remove();
                continue;
            }
            BlockPos position = entry.getKey();
            if (minecraft.world.isBlockLoaded(position, false)) {
                TileEntity tile = minecraft.world.getTileEntity(position);
                if (tile != null) continue;
            }
            if (!state.active || state.beamHeight <= 0) continue;
            double x = position.getX() - minecraft.getRenderManager().viewerPosX;
            double y = position.getY() - minecraft.getRenderManager().viewerPosY;
            double z = position.getZ() - minecraft.getRenderManager().viewerPosZ;
            float[] color = {state.red / 255.0F, state.green / 255.0F, state.blue / 255.0F};
            Minecraft.getMinecraft().getTextureManager().bindTexture(
                    TileEntityBeaconRenderer.TEXTURE_BEACON_BEAM);
            GlStateManager.disableFog();
            TileEntityBeaconRenderer.renderBeamSegment(x, y, z, event.getPartialTicks(), 1.0D,
                    now, 0, state.beamHeight, color, state.thickness, state.outerThickness);
            GlStateManager.enableFog();
        }
    }

    private static final class State {
        private final int red;
        private final int green;
        private final int blue;
        private final boolean active;
        private final int beamHeight;
        private final float thickness;
        private final float outerThickness;
        private final long lastUpdated;

        private State(LegacyNetwork.DistantSuperBeaconMessage message) {
            int[] color = message.getColor();
            red = color[0];
            green = color[1];
            blue = color[2];
            active = message.isActive();
            beamHeight = message.getBeamHeight();
            thickness = message.getThickness();
            outerThickness = message.getOuterThickness();
            Minecraft minecraft = Minecraft.getMinecraft();
            lastUpdated = minecraft.world == null ? 0L : minecraft.world.getTotalWorldTime();
        }
    }
}
