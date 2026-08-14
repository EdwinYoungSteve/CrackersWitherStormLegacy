package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.common.init.ModBlocks;
import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.common.block.TaintedCeilingHangingSignBlock;
import com.wdcftgg.witherstormmod.common.tile.TaintedSignTileEntity;
import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelSign;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class TaintedSignRenderer extends TileEntitySpecialRenderer<TaintedSignTileEntity> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/entity/signs/tainted.png");
    private static final ResourceLocation HANGING_TEXTURE =
            new ResourceLocation("minecraft", "textures/entity/signs/hanging/tainted.png");
    private final ModelSign model = new ModelSign();
    private final HangingSignModel hangingModel = new HangingSignModel();

    @Override
    public void render(TaintedSignTileEntity sign, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        Block block = sign.getBlockType();
        boolean ceilingHanging = block == ModBlocks.get("tainted_hanging_sign");
        boolean wallHanging = block == ModBlocks.get("tainted_wall_hanging_sign");
        boolean hanging = ceilingHanging || wallHanging;
        GlStateManager.pushMatrix();
        if (ceilingHanging) {
            GlStateManager.translate((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
            GlStateManager.rotate(-(float) (sign.getBlockMetadata() * 360) / 16.0F, 0.0F, 1.0F, 0.0F);
        } else if (wallHanging) {
            int metadata = sign.getBlockMetadata();
            float rotation = metadata == 2 ? 180.0F : metadata == 4 ? 90.0F : metadata == 5 ? -90.0F : 0.0F;
            GlStateManager.translate((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
            GlStateManager.rotate(-rotation, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0F, 0.0F, -0.4375F);
        } else if (block == ModBlocks.get("tainted_sign")) {
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
            bindTexture(hanging ? HANGING_TEXTURE : TEXTURE);
        }

        GlStateManager.enableRescaleNormal();
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.6666667F, -0.6666667F, -0.6666667F);
        if (hanging) {
            boolean verticalChains = ceilingHanging
                    && sign.getWorld() != null
                    && ((TaintedCeilingHangingSignBlock) block).hasVerticalChains(sign.getWorld(), sign.getPos());
            hangingModel.render(ceilingHanging, verticalChains);
        } else {
            model.renderSign();
        }
        GlStateManager.popMatrix();
        FontRenderer font = getFontRenderer();
        GlStateManager.translate(0.0F, hanging ? 0.041666668F : 0.33333334F, 0.046666667F);
        GlStateManager.scale(0.010416667F, -0.010416667F, 0.010416667F);
        GlStateManager.glNormal3f(0.0F, 0.0F, -0.010416667F);
        GlStateManager.depthMask(false);
        if (destroyStage < 0) {
            for (int line = 0; line < sign.signText.length; line++) {
                ITextComponent component = sign.signText[line];
                if (component == null) continue;
                List<ITextComponent> wrapped = GuiUtilRenderComponents.splitText(component, hanging ? 60 : 90,
                        font, false, true);
                String text = wrapped.isEmpty() ? "" : wrapped.get(0).getFormattedText();
                if (line == sign.lineBeingEdited) text = "> " + text + " <";
                int lineHeight = hanging ? 9 : 10;
                font.drawString(text, -font.getStringWidth(text) / 2,
                        line * lineHeight - sign.signText.length * lineHeight / 2, 0);
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

    private static final class HangingSignModel extends ModelBase {
        private final ModelRenderer board;
        private final ModelRenderer plank;
        private final ModelRenderer[] normalChains;
        private final ModelRenderer[] verticalChains;

        private HangingSignModel() {
            textureWidth = 64;
            textureHeight = 32;

            board = new ModelRenderer(this, 0, 12);
            board.addBox(-7.0F, -6.0F, -1.0F, 14, 10, 2);
            plank = new ModelRenderer(this, 0, 0);
            plank.addBox(-8.0F, -8.0F, -1.0F, 16, 2, 2);

            normalChains = new ModelRenderer[] {
                    chain(-6.5F, -0.7853982F, false), chain(-6.5F, 0.7853982F, true),
                    chain(3.5F, 0.7853982F, false), chain(3.5F, -0.7853982F, true)
            };
            verticalChains = new ModelRenderer[] {
                    verticalChain(-6.0F), verticalChain(6.0F)
            };
        }

        private ModelRenderer chain(float x, float yRotation, boolean mirror) {
            ModelRenderer chain = new ModelRenderer(this, 0, 24);
            chain.mirror = mirror;
            chain.addBox(x, -13.0F, 0.0F, 3, 6, 0);
            chain.rotateAngleY = yRotation;
            return chain;
        }

        private ModelRenderer verticalChain(float x) {
            ModelRenderer chain = new ModelRenderer(this, 14, 24);
            chain.addBox(x, -13.0F, 0.0F, 0, 6, 2);
            return chain;
        }

        private void render(boolean ceiling, boolean attached) {
            board.render(0.0625F);
            if (!ceiling) plank.render(0.0625F);
            ModelRenderer[] chains = attached ? verticalChains : normalChains;
            if (ceiling) {
                for (ModelRenderer chain : chains) chain.render(0.0625F);
            }
        }
    }
}
