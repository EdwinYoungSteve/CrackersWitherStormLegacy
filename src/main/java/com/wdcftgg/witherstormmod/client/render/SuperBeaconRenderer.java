package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.common.beacon.SuperBeaconLogic;
import com.wdcftgg.witherstormmod.common.tile.AbstractSuperBeaconTileEntity;
import com.wdcftgg.witherstormmod.common.tile.SuperBeaconTileEntity;
import com.wdcftgg.witherstormmod.common.tile.SuperSupportBeaconTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityBeaconRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class SuperBeaconRenderer extends TileEntitySpecialRenderer<AbstractSuperBeaconTileEntity> {
    private static final double AREA_RADIUS = 128.0D;
    private static final ResourceLocation MAIN_CRYSTAL =
            new ResourceLocation(Tags.MOD_ID, "block/tainted_dust_block");
    private static final ResourceLocation CONNECT_BEAM =
            new ResourceLocation(Tags.MOD_ID, "textures/misc/beam.png");
    private static final Map<SuperBeaconTileEntity.SupportColor, ResourceLocation> SUPPORT_TEXTURES =
            new EnumMap<SuperBeaconTileEntity.SupportColor, ResourceLocation>(
                    SuperBeaconTileEntity.SupportColor.class);

    static {
        SUPPORT_TEXTURES.put(SuperBeaconTileEntity.SupportColor.AQUA,
                new ResourceLocation(Tags.MOD_ID, "textures/block/support_beacon_diamond.png"));
        SUPPORT_TEXTURES.put(SuperBeaconTileEntity.SupportColor.GREEN,
                new ResourceLocation(Tags.MOD_ID, "textures/block/support_beacon_emerald.png"));
        SUPPORT_TEXTURES.put(SuperBeaconTileEntity.SupportColor.GRAY,
                new ResourceLocation(Tags.MOD_ID, "textures/block/support_beacon_iron.png"));
        SUPPORT_TEXTURES.put(SuperBeaconTileEntity.SupportColor.RED,
                new ResourceLocation(Tags.MOD_ID, "textures/block/support_beacon_redstone.png"));
    }

    public static void registerSprites(TextureMap textureMap) {
        textureMap.registerSprite(MAIN_CRYSTAL);
    }

    @Override
    public void render(AbstractSuperBeaconTileEntity beacon, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        if (beacon.getWorld() == null) return;
        float animation = beacon.getActivationAnimation(partialTicks);
        renderCrystal(beacon, x, y, z, partialTicks, animation);

        if (beacon instanceof SuperSupportBeaconTileEntity) {
            SuperSupportBeaconTileEntity support = (SuperSupportBeaconTileEntity) beacon;
            if (animation > 0.001F) renderSupportLink(support, x, y, z, animation);
            if (support.showWorkingArea()) renderSupportArea(support, x, y, z);
        } else {
            SuperBeaconTileEntity main = (SuperBeaconTileEntity) beacon;
            if (beacon.isActive() && animation > 0.001F) {
                renderVerticalBeam(beacon, x, y, z, partialTicks, animation);
            }
            renderMainItems(main, x, y, z, partialTicks);
            if (main.isResummoningWitherStorm()
                    && main.getResummonTicks() > SuperBeaconLogic.RESUMMON_START) {
                renderResummonCommandBlock(main, x, y, z, partialTicks);
                if (animation > 0.001F) {
                    renderConnection(x + 0.5D, y + 0.5D, z + 0.5D,
                            x + 0.5D, y + 3.5D, z + 0.5D,
                            main.getBeamColor(), crystalScale(main, partialTicks), animation);
                }
            }
        }
    }

    private void renderCrystal(AbstractSuperBeaconTileEntity beacon, double x, double y, double z,
                               float partialTicks, float animation) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);
        float speed;
        float scale = crystalScale(beacon, partialTicks);
        if (beacon instanceof SuperSupportBeaconTileEntity) {
            speed = 0.2F + animation * 0.8F;
            GlStateManager.translate(0.0F, 0.1F, 0.0F);
            GlStateManager.rotate((beacon.getTicks() + partialTicks) * 4.0F * speed, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate((beacon.getTicks() + partialTicks) * 8.0F * speed, 0.0F, 0.0F, 1.0F);
            SuperBeaconTileEntity.SupportColor color =
                    ((SuperSupportBeaconTileEntity) beacon).getColor();
            bindTexture(color == null ? SUPPORT_TEXTURES.get(SuperBeaconTileEntity.SupportColor.GRAY)
                    : SUPPORT_TEXTURES.get(color));
            renderOrb(scale, 0.375F, 0.375F, 0.625F, 0.625F, beacon);
        } else {
            speed = 0.1F + animation * 0.9F;
            GlStateManager.rotate((beacon.getTicks() + partialTicks) * 5.0F * speed, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate((beacon.getTicks() + partialTicks) * 12.0F * speed, 0.0F, 1.0F, 0.0F);
            bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks()
                    .getAtlasSprite(MAIN_CRYSTAL.toString());
            renderOrb(scale, sprite.getInterpolatedU(3.0D), sprite.getInterpolatedV(3.0D),
                    sprite.getInterpolatedU(13.0D), sprite.getInterpolatedV(13.0D), beacon);
        }
        GlStateManager.popMatrix();
    }

    private static float crystalScale(AbstractSuperBeaconTileEntity beacon, float partialTicks) {
        float animation = beacon.getActivationAnimation(partialTicks);
        float wave = MathHelper.sin((beacon.getTicks() + partialTicks) * 0.1F) + 10.0F;
        if (beacon instanceof SuperSupportBeaconTileEntity) {
            return 0.2F + animation * (wave * 0.035F - 0.2F);
        }
        return 0.4F + animation * (wave * 0.05F - 0.4F);
    }

    private static void renderOrb(float scale, float minU, float minV, float maxU, float maxV,
                                  AbstractSuperBeaconTileEntity beacon) {
        int light = beacon.isActive()
                ? beacon.getWorld().getCombinedLight(beacon.getPos(), 0) : 0x00F000F0;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,
                light & 65535, light >>> 16);
        GlStateManager.disableLighting();
        for (int axis = 0; axis < 3; axis++) {
            GlStateManager.pushMatrix();
            if (axis == 0) GlStateManager.rotate(45.0F, 1.0F, 0.0F, 0.0F);
            if (axis == 1) GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
            if (axis == 2) GlStateManager.rotate(45.0F, 0.0F, 0.0F, 1.0F);
            drawTexturedCube(scale * 0.5F, minU, minV, maxU, maxV);
            GlStateManager.popMatrix();
        }
        GlStateManager.enableLighting();
    }

    private static void drawTexturedCube(float radius, float minU, float minV, float maxU, float maxV) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        face(buffer, -radius, -radius, -radius, -radius, radius, -radius,
                radius, radius, -radius, radius, -radius, -radius, minU, minV, maxU, maxV);
        face(buffer, radius, -radius, radius, radius, radius, radius,
                -radius, radius, radius, -radius, -radius, radius, minU, minV, maxU, maxV);
        face(buffer, -radius, -radius, radius, -radius, radius, radius,
                -radius, radius, -radius, -radius, -radius, -radius, minU, minV, maxU, maxV);
        face(buffer, radius, -radius, -radius, radius, radius, -radius,
                radius, radius, radius, radius, -radius, radius, minU, minV, maxU, maxV);
        face(buffer, -radius, radius, -radius, -radius, radius, radius,
                radius, radius, radius, radius, radius, -radius, minU, minV, maxU, maxV);
        face(buffer, -radius, -radius, radius, -radius, -radius, -radius,
                radius, -radius, -radius, radius, -radius, radius, minU, minV, maxU, maxV);
        tessellator.draw();
    }

    private static void face(BufferBuilder buffer,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float x3, float y3, float z3, float x4, float y4, float z4,
                             float minU, float minV, float maxU, float maxV) {
        buffer.pos(x1, y1, z1).tex(minU, minV).endVertex();
        buffer.pos(x2, y2, z2).tex(minU, maxV).endVertex();
        buffer.pos(x3, y3, z3).tex(maxU, maxV).endVertex();
        buffer.pos(x4, y4, z4).tex(maxU, minV).endVertex();
    }

    private void renderMainItems(SuperBeaconTileEntity beacon, double x, double y, double z,
                                 float partialTicks) {
        List<ItemStack> items = beacon.getItemsForRendering();
        if (items.isEmpty()) return;
        float tick = beacon.getTicks() + partialTicks;
        float resummonTick = beacon.getResummonTicks() + partialTicks;
        float scale = beacon.getResummonTicks() > 0
                ? SuperBeaconLogic.getResummonItemScale(resummonTick) : 1.0F;
        if (scale <= 0.0F) return;
        float rotation = beacon.getResummonTicks() > 0
                ? (tick + resummonTick * resummonTick) * 0.02F : tick * 0.02F;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y + 1.5D, z + 0.5D);
        GlStateManager.scale(scale, scale, scale);
        float interval = (float) (Math.PI * 2.0D / items.size());
        for (int i = 0; i < items.size(); i++) {
            GlStateManager.pushMatrix();
            float angle = interval * i + rotation;
            GlStateManager.translate(MathHelper.sin(angle),
                    MathHelper.sin((tick + i * items.size()) * 0.2F) * 0.05F,
                    MathHelper.cos(angle));
            if (beacon.isDoingResummonAnimation()) {
                GlStateManager.translate(beacon.getShakeX(partialTicks), 0.0F,
                        beacon.getShakeZ(partialTicks));
            }
            GlStateManager.rotate(tick + i * 100.0F, 0.0F, 1.0F, 0.0F);
            Minecraft.getMinecraft().getRenderItem().renderItem(items.get(i),
                    ItemCameraTransforms.TransformType.FIXED);
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
    }

    private void renderResummonCommandBlock(SuperBeaconTileEntity beacon, double x, double y, double z,
                                             float partialTicks) {
        float tick = beacon.getTicks() + partialTicks;
        float ritual = beacon.getResummonTicks() + partialTicks;
        float speed = 200.0F / (ritual - 400.0F);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D + beacon.getShakeX(partialTicks) * speed,
                y + 3.5D, z + 0.5D + beacon.getShakeZ(partialTicks) * speed);
        GlStateManager.rotate(tick * speed, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(tick * speed, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(-0.5F, -0.5F, -0.5F);
        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlockBrightness(
                Blocks.COMMAND_BLOCK.getDefaultState(), 1.0F);
        GlStateManager.popMatrix();
    }

    private void renderVerticalBeam(AbstractSuperBeaconTileEntity beacon, double x, double y, double z,
                                    float partialTicks, float animation) {
        int[] rgb = beacon.getBeamColor();
        float[] color = {rgb[0] / 255.0F, rgb[1] / 255.0F, rgb[2] / 255.0F};
        int height = Math.max(1, Math.min(beacon.getBeamHeight(), 1024));
        bindTexture(TileEntityBeaconRenderer.TEXTURE_BEACON_BEAM);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.disableFog();
        TileEntityBeaconRenderer.renderBeamSegment(x, y, z, partialTicks, animation,
                beacon.getWorld().getTotalWorldTime(), 0, height, color,
                beacon.getBeamThickness(), beacon.getOuterBeamThickness());
        GlStateManager.enableFog();
    }

    private void renderSupportLink(SuperSupportBeaconTileEntity support, double x, double y, double z,
                                   float animation) {
        BlockPos target = support.getBeamTarget();
        if (target == null) return;
        double endX = target.getX() - support.getPos().getX();
        double endY = target.getY() - support.getPos().getY();
        double endZ = target.getZ() - support.getPos().getZ();
        renderConnection(x + 0.5D, y + 0.6D, z + 0.5D,
                x + endX + 0.5D, y + endY + 0.4D, z + endZ + 0.5D,
                support.getBeamColor(), crystalScale(support, 0.0F), animation);
    }

    private void renderConnection(double startX, double startY, double startZ,
                                  double endX, double endY, double endZ,
                                  int[] rgb, double radius, float animation) {
        bindTexture(CONNECT_BEAM);
        GlStateManager.pushAttrib();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.depthMask(false);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        double dx = endX - startX;
        double dz = endZ - startZ;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        double sideX = horizontal < 1.0E-4D ? radius : -dz / horizontal * radius;
        double sideZ = horizontal < 1.0E-4D ? 0.0D : dx / horizontal * radius;
        texturedRibbon(buffer, startX, startY, startZ, endX, endY, endZ,
                sideX, 0.0D, sideZ, rgb, animation);
        texturedRibbon(buffer, startX, startY, startZ, endX, endY, endZ,
                0.0D, radius, 0.0D, rgb, animation);
        tessellator.draw();
        GlStateManager.depthMask(true);
        GlStateManager.popAttrib();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void texturedRibbon(BufferBuilder buffer,
                                       double startX, double startY, double startZ,
                                       double endX, double endY, double endZ,
                                       double offsetX, double offsetY, double offsetZ,
                                       int[] rgb, float alpha) {
        float red = rgb[0] / 255.0F;
        float green = rgb[1] / 255.0F;
        float blue = rgb[2] / 255.0F;
        buffer.pos(startX - offsetX, startY - offsetY, startZ - offsetZ)
                .tex(0.0D, 0.0D).color(red, green, blue, alpha).endVertex();
        buffer.pos(startX + offsetX, startY + offsetY, startZ + offsetZ)
                .tex(1.0D, 0.0D).color(red, green, blue, alpha).endVertex();
        buffer.pos(endX + offsetX, endY + offsetY, endZ + offsetZ)
                .tex(1.0D, 1.0D).color(red, green, blue, alpha).endVertex();
        buffer.pos(endX - offsetX, endY - offsetY, endZ - offsetZ)
                .tex(0.0D, 1.0D).color(red, green, blue, alpha).endVertex();
    }

    private static void ribbonHorizontal(BufferBuilder buffer, double startX, double startY, double startZ,
                                         double endX, double endY, double endZ,
                                         double offsetX, double offsetZ,
                                         float red, float green, float blue) {
        buffer.pos(startX - offsetX, startY, startZ - offsetZ).color(red, green, blue, 0.8F).endVertex();
        buffer.pos(startX + offsetX, startY, startZ + offsetZ).color(red, green, blue, 0.8F).endVertex();
        buffer.pos(endX + offsetX, endY, endZ + offsetZ).color(red, green, blue, 0.8F).endVertex();
        buffer.pos(endX - offsetX, endY, endZ - offsetZ).color(red, green, blue, 0.8F).endVertex();
    }

    private static void ribbonVertical(BufferBuilder buffer, double startX, double startY, double startZ,
                                       double endX, double endY, double endZ, double offsetY,
                                       float red, float green, float blue) {
        buffer.pos(startX, startY - offsetY, startZ).color(red, green, blue, 0.8F).endVertex();
        buffer.pos(startX, startY + offsetY, startZ).color(red, green, blue, 0.8F).endVertex();
        buffer.pos(endX, endY + offsetY, endZ).color(red, green, blue, 0.8F).endVertex();
        buffer.pos(endX, endY - offsetY, endZ).color(red, green, blue, 0.8F).endVertex();
    }

    private void renderSupportArea(SuperSupportBeaconTileEntity support, double x, double y, double z) {
        BlockPos main = support.getConnectedBeacon();
        if (main == null) return;
        double originX = x + main.getX() - support.getPos().getX() + 0.5D;
        double originY = y + main.getY() - support.getPos().getY() + 0.04D;
        double originZ = z + main.getZ() - support.getPos().getZ() + 0.5D;
        float center = SuperBeaconLogic.angleDegrees(
                support.getPos().getX() - main.getX(), support.getPos().getZ() - main.getZ());
        int[] rgb = support.getBeamColor();
        float red = rgb[0] / 255.0F;
        float green = rgb[1] / 255.0F;
        float blue = rgb[2] / 255.0F;

        prepareTransparentGeometry(true);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(originX, originY, originZ).color(red, green, blue, 0.12F).endVertex();
        for (int step = 0; step <= 24; step++) {
            double angle = Math.toRadians(center - 45.0F + step * (90.0F / 24.0F));
            buffer.pos(originX + Math.sin(angle) * AREA_RADIUS, originY,
                    originZ + Math.cos(angle) * AREA_RADIUS).color(red, green, blue, 0.03F).endVertex();
        }
        tessellator.draw();

        GlStateManager.glLineWidth(2.0F);
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        for (float edge : new float[] {center - 45.0F, center + 45.0F}) {
            double angle = Math.toRadians(edge);
            buffer.pos(originX, originY, originZ).color(red, green, blue, 0.55F).endVertex();
            buffer.pos(originX + Math.sin(angle) * AREA_RADIUS, originY,
                    originZ + Math.cos(angle) * AREA_RADIUS).color(red, green, blue, 0.15F).endVertex();
        }
        tessellator.draw();
        GlStateManager.glLineWidth(1.0F);
        restoreGeometry();
    }

    private static void prepareTransparentGeometry(boolean disableDepth) {
        GlStateManager.pushAttrib();
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.depthMask(false);
        if (disableDepth) GlStateManager.disableDepth();
    }

    private static void restoreGeometry() {
        GlStateManager.depthMask(true);
        GlStateManager.popAttrib();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean isGlobalRenderer(AbstractSuperBeaconTileEntity beacon) {
        return true;
    }
}
