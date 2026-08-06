package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.common.tile.WitheredPhlegmTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;

import java.util.Random;

public class WitheredPhlegmRenderer extends TileEntitySpecialRenderer<WitheredPhlegmTileEntity> {
    @Override
    public void render(WitheredPhlegmTileEntity tile, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);

        int occupied = 0;
        for (ItemStack stack : tile.getVisibleItems()) if (!stack.isEmpty()) occupied++;
        float fullness = occupied / (float) tile.getSizeInventory();
        float scale = 0.5F + (1.0F - fullness) / 2.0F;
        Random random = new Random(tile.getPos().toLong());

        for (ItemStack stack : tile.getVisibleItems()) {
            if (stack.isEmpty()) continue;
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, scale);
            GlStateManager.translate(random.nextDouble() - 0.5D,
                    random.nextDouble() - 0.5D, random.nextDouble() - 0.5D);
            GlStateManager.rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
    }
}
