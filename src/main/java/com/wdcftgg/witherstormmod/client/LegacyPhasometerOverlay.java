package com.wdcftgg.witherstormmod.client;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.common.item.PhasometerItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

public final class LegacyPhasometerOverlay {

    private static final ResourceLocation SCOPE = new ResourceLocation(
            Tags.MOD_ID, "textures/misc/phasometer_scope.png");

    private LegacyPhasometerOverlay() {
    }

    public static boolean isScoping(EntityPlayer player) {
        return player != null && player.isHandActive()
                && player.getActiveItemStack().getItem() instanceof PhasometerItem;
    }

    public static float applyScopeFov(float fov) {
        return fov * 0.1F;
    }

    static int searchDotCount(int ticksExisted) {
        return Math.floorMod(ticksExisted / 5, 4);
    }

    public static void render(Minecraft minecraft, ScaledResolution resolution) {
        EntityPlayer player = minecraft.player;
        if (!isScoping(player)) return;

        int width = resolution.getScaledWidth();
        int height = resolution.getScaledHeight();
        int size = Math.min(width, height);
        int left = (width - size) / 2;
        int top = (height - size) / 2;

        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        if (width > size) {
            Gui.drawRect(0, 0, left, height, 0xFF000000);
            Gui.drawRect(left + size, 0, width, height, 0xFF000000);
        } else if (height > size) {
            Gui.drawRect(0, 0, width, top, 0xFF000000);
            Gui.drawRect(0, top + size, width, height, 0xFF000000);
        }
        minecraft.getTextureManager().bindTexture(SCOPE);
        Gui.drawModalRectWithCustomSizedTexture(left, top, 0.0F, 0.0F,
                size, size, 256.0F, 256.0F);

        ItemStack stack = player.getActiveItemStack();
        NBTTagCompound tag = stack.getTagCompound();
        int line = 0;
        if (tag != null && tag.hasKey(PhasometerItem.DataEntry.PHASE.tagName)) {
            for (PhasometerItem.DataEntry entry : PhasometerItem.getEntries(tag)) {
                minecraft.fontRenderer.drawString(entry.getDisplayText(tag), width / 2,
                        30 + line * (minecraft.fontRenderer.FONT_HEIGHT + 2), 0xFFFFFFFF);
                line++;
            }
        } else if (tag != null && tag.hasKey(PhasometerItem.DataEntry.OBSTRUCTED.tagName)) {
            minecraft.fontRenderer.drawString(
                    PhasometerItem.DataEntry.OBSTRUCTED.getDisplayText(tag),
                    width / 2, 30, 0xFFFFFFFF);
        } else {
            StringBuilder dots = new StringBuilder();
            for (int index = 0; index < searchDotCount(player.ticksExisted); index++) dots.append('.');
            minecraft.fontRenderer.drawString(TextFormatting.GRAY
                            + I18n.format("description.phasometer.searching", dots.toString()),
                    width / 2, 30, 0xFFFFFFFF);
        }

        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
    }
}
