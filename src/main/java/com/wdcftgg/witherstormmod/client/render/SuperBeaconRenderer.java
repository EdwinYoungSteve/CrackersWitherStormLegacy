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
            if (animation > 0.001F) renderSupportLink(support, x, y, z, partialTicks, animation);
        } else {
            SuperBeaconTileEntity main = (SuperBeaconTileEntity) beacon;
            if (beacon.isActive()) {
                renderVerticalBeam(beacon, x, y, z, partialTicks, animation);
            }
            renderMainItems(main, x, y, z, partialTicks);
            if (main.showWorkingArea()) renderWorkingArea(main, x, y, z);
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
        float previousLightX = OpenGlHelper.lastBrightnessX;
        float previousLightY = OpenGlHelper.lastBrightnessY;
        int light = beacon.isActive()
                ? beacon.getWorld().getCombinedLight(beacon.getPos(), 0) : 0x00F000F0;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,
                light & 65535, light >>> 16);
        try {
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
        } finally {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,
                    previousLightX, previousLightY);
        }
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
        GlStateManager.pushAttrib();
        try {
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
            GlStateManager.disableFog();
            TileEntityBeaconRenderer.renderBeamSegment(x, y + 0.5D, z, partialTicks, 1.0D,
                    beacon.getWorld().getTotalWorldTime(), 0, height, color,
                    beacon.getBeamThickness(), beacon.getOuterBeamThickness());
        } finally {
            GlStateManager.popAttrib();
        }
    }

    private void renderSupportLink(SuperSupportBeaconTileEntity support, double x, double y, double z,
                                   float partialTicks, float animation) {
        BlockPos target = support.getBeamTarget();
        if (target == null) return;
        double endX = target.getX() - support.getPos().getX();
        double endY = target.getY() - support.getPos().getY();
        double endZ = target.getZ() - support.getPos().getZ();
        renderConnection(x + 0.5D, y + 0.6D, z + 0.5D,
                x + endX + 0.5D, y + endY + 0.5D, z + endZ + 0.5D,
                support.getBeamColor(), crystalScale(support, partialTicks), animation);
    }

    private void renderConnection(double startX, double startY, double startZ,
                                  double endX, double endY, double endZ,
                                  int[] rgb, double radius, float animation) {
        bindTexture(CONNECT_BEAM);
        GlStateManager.pushAttrib();
        float previousLightX = OpenGlHelper.lastBrightnessX;
        float previousLightY = OpenGlHelper.lastBrightnessY;
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.depthMask(false);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        double dx = endX - startX;
        double dy = endY - startY;
        double dz = endZ - startZ;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 1.0E-6D) {
            GlStateManager.depthMask(true);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,
                    previousLightX, previousLightY);
            GlStateManager.popAttrib();
            return;
        }
        double nx = dx / length;
        double ny = dy / length;
        double nz = dz / length;
        double horizontal = Math.sqrt(nx * nx + nz * nz);
        double sideX = horizontal < 1.0E-6D ? radius : -nz / horizontal * radius;
        double sideZ = horizontal < 1.0E-6D ? 0.0D : nx / horizontal * radius;
        double secondX = ny * sideZ;
        double secondY = nz * sideX - nx * sideZ;
        double secondZ = -ny * sideX;
        float red = 0.5F + animation * (rgb[0] / 255.0F - 0.5F);
        float green = 0.5F + animation * (rgb[1] / 255.0F - 0.5F);
        float blue = 0.5F + animation * (rgb[2] / 255.0F - 0.5F);
        texturedRibbon(buffer, startX, startY, startZ, endX, endY, endZ,
                sideX, 0.0D, sideZ, red, green, blue, animation);
        texturedRibbon(buffer, startX, startY, startZ, endX, endY, endZ,
                secondX, secondY, secondZ, red, green, blue, animation);
        tessellator.draw();
        GlStateManager.depthMask(true);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,
                previousLightX, previousLightY);
        GlStateManager.popAttrib();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void texturedRibbon(BufferBuilder buffer,
                                       double startX, double startY, double startZ,
                                       double endX, double endY, double endZ,
                                       double offsetX, double offsetY, double offsetZ,
                                       float red, float green, float blue, float alpha) {
        buffer.pos(startX - offsetX, startY - offsetY, startZ - offsetZ)
                .tex(0.0D, 0.0D).color(red, green, blue, alpha).endVertex();
        buffer.pos(startX + offsetX, startY + offsetY, startZ + offsetZ)
                .tex(1.0D, 0.0D).color(red, green, blue, alpha).endVertex();
        buffer.pos(endX + offsetX, endY + offsetY, endZ + offsetZ)
                .tex(1.0D, 1.0D).color(red, green, blue, alpha).endVertex();
        buffer.pos(endX - offsetX, endY - offsetY, endZ - offsetZ)
                .tex(0.0D, 1.0D).color(red, green, blue, alpha).endVertex();
    }

    private void renderWorkingArea(SuperBeaconTileEntity beacon, double x, double y, double z) {
        if (beacon.getConnected().isEmpty()) return;
        GlStateManager.pushAttrib();
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        for (Map.Entry<SuperBeaconTileEntity.SupportColor, BlockPos> entry
                : beacon.getConnected().entrySet()) {
            BlockPos support = entry.getValue();
            float angle = SuperBeaconLogic.angleDegrees(support.getX() - beacon.getPos().getX(),
                    support.getZ() - beacon.getPos().getZ());
            float[] color = entry.getKey().getLogic().getBeamColor();
            for (float edge : new float[] {angle + 45.0F, angle - 45.0F}) {
                float offset = edge > angle ? -0.05F : 0.05F;
                workingAreaSegment(buffer, x + 0.5D, y + 0.5D, z + 0.5D,
                        0.0F, 10.0F, edge, offset, color);
                workingAreaSegment(buffer, x + 0.5D, y + 0.5D, z + 0.5D,
                        11.0F, 1.0F, edge, offset, color);
                workingAreaSegment(buffer, x + 0.5D, y + 0.5D, z + 0.5D,
                        13.0F, 1.0F, edge, offset, color);
                workingAreaSegment(buffer, x + 0.5D, y + 0.5D, z + 0.5D,
                        15.0F, 0.5F, edge, offset, color);
            }
        }
        tessellator.draw();
        GlStateManager.popAttrib();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void workingAreaSegment(BufferBuilder buffer, double originX, double originY,
                                           double originZ, float zOffset, float distance,
                                           float angle, float xOffset, float[] color) {
        double radians = Math.toRadians(angle);
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);
        double startX = originX + cos * xOffset + sin * zOffset;
        double startZ = originZ - sin * xOffset + cos * zOffset;
        double endDistance = zOffset + distance;
        double endX = originX + cos * xOffset + sin * endDistance;
        double endZ = originZ - sin * xOffset + cos * endDistance;
        buffer.pos(startX, originY, startZ).color(color[0], color[1], color[2], 1.0F).endVertex();
        buffer.pos(endX, originY, endZ).color(color[0], color[1], color[2], 1.0F).endVertex();
    }

    @Override
    public boolean isGlobalRenderer(AbstractSuperBeaconTileEntity beacon) {
        return true;
    }
}
