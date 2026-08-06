package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.common.init.ModBlocks;
import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.common.tile.TaintedSignTileEntity;
import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.model.ModelSign;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class TaintedSignRenderer extends TileEntitySpecialRenderer<TaintedSignTileEntity> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/entity/signs/tainted.png");
    private final ModelSign model = new ModelSign();

    @Override
    public void render(TaintedSignTileEntity sign, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        Block block = sign.getBlockType();
        GlStateManager.pushMatrix();
        if (block == ModBlocks.get("tainted_sign")) {
            GlStateManager.translate((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
            GlStateManager.rotate(-(float) (sign.getBlockMetadata() * 360) / 16.0F, 0.0F, 1.0F, 0.0F);
            model.signStick.showModel = true;
        } else {
            int metadata = sign.getBlockMetadata();
            float rotation = metadata == 2 ? 180.0F : metadata == 4 ? 90.0F : metadata == 5 ? -90.0F : 0.0F;
            GlStateManager.translate((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
            GlStateManager.rotate(-rotation, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0F, -0.3125F, -0.4375F);
            model.signStick.showModel = false;
        }

        if (destroyStage >= 0) {
            bindTexture(DESTROY_STAGES[destroyStage]);
            GlStateManager.matrixMode(5890);
            GlStateManager.pushMatrix();
            GlStateManager.scale(4.0F, 2.0F, 1.0F);
            GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
            GlStateManager.matrixMode(5888);
        } else {
            bindTexture(TEXTURE);
        }

        GlStateManager.enableRescaleNormal();
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.6666667F, -0.6666667F, -0.6666667F);
        model.renderSign();
        GlStateManager.popMatrix();
        FontRenderer font = getFontRenderer();
        GlStateManager.translate(0.0F, 0.33333334F, 0.046666667F);
        GlStateManager.scale(0.010416667F, -0.010416667F, 0.010416667F);
        GlStateManager.glNormal3f(0.0F, 0.0F, -0.010416667F);
        GlStateManager.depthMask(false);
        if (destroyStage < 0) {
            for (int line = 0; line < sign.signText.length; line++) {
                ITextComponent component = sign.signText[line];
                if (component == null) continue;
                List<ITextComponent> wrapped = GuiUtilRenderComponents.splitText(component, 90, font, false, true);
                String text = wrapped.isEmpty() ? "" : wrapped.get(0).getFormattedText();
                if (line == sign.lineBeingEdited) text = "> " + text + " <";
                font.drawString(text, -font.getStringWidth(text) / 2, line * 10 - sign.signText.length * 5, 0);
            }
        }
        GlStateManager.depthMask(true);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
        if (destroyStage >= 0) {
            GlStateManager.matrixMode(5890);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
        }
    }
}
