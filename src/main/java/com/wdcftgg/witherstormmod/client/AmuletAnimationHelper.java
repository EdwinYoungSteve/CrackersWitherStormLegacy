package com.wdcftgg.witherstormmod.client;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.common.item.AmuletItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import org.lwjgl.opengl.GL11;

import java.util.EnumMap;
import java.util.Map;

public final class AmuletAnimationHelper {
    public static final ResourceLocation GLARE =
            new ResourceLocation(Tags.MOD_ID, "textures/misc/glare.png");

    static final int SWAP_STEPS = 16;
    static final int MAX_TRACKING_DISTANCE = 1000;

    private static final Map<EnumHand, AnimationState> ANIMATIONS =
            new EnumMap<EnumHand, AnimationState>(EnumHand.class);

    static {
        ANIMATIONS.put(EnumHand.MAIN_HAND, new AnimationState());
        ANIMATIONS.put(EnumHand.OFF_HAND, new AnimationState());
    }

    private AmuletAnimationHelper() {
    }

    public static void tick(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.world == null) {
            reset();
            return;
        }
        if (minecraft.isGamePaused()) return;

        for (EnumHand hand : EnumHand.values()) {
            ItemStack stack = minecraft.player.getHeldItem(hand);
            if (!(stack.getItem() instanceof AmuletItem)) continue;

            AnimationState state = ANIMATIONS.get(hand);
            state.tickCount++;
            NBTTagCompound tag = stack.getTagCompound();
            int selectedIndex = tag == null ? 0 : tag.getInteger(AmuletItem.SELECTED_INDEX);
            state.tickRotation(getSwapDegrees(hand, selectedIndex));
            for (int index = 0; index < AmuletItem.TRACKING.length; index++) {
                state.pulseIntensityO[index] = state.pulseIntensity[index];
                state.pulseIntensity[index] = getTrackingIntensity(
                        minecraft.player, stack, AmuletItem.TRACKING[index], MAX_TRACKING_DISTANCE);
            }
        }
    }

    private static void reset() {
        for (EnumHand hand : EnumHand.values()) {
            ANIMATIONS.put(hand, new AnimationState());
        }
    }

    public static void render(RenderSpecificHandEvent event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof AmuletItem)) return;

        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayerSP player = minecraft.player;
        if (player == null) return;

        event.setCanceled(true);
        boolean mainHand = event.getHand() == EnumHand.MAIN_HAND;
        EnumHandSide handSide = mainHand ? player.getPrimaryHand() : player.getPrimaryHand().opposite();
        boolean rightSide = handSide == EnumHandSide.RIGHT;

        GlStateManager.pushMatrix();
        try {
            applySwingTransform(rightSide, event.getSwingProgress());
            transformSideFirstPerson(handSide, event.getEquipProgress());
            transformFirstPerson(handSide, event.getSwingProgress());
            renderAnimation(event.getHand(), rightSide, event.getPartialTicks());
            minecraft.getItemRenderer().renderItemSide(player, stack,
                    rightSide ? ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND
                            : ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND,
                    !rightSide);
        } finally {
            GlStateManager.popMatrix();
        }
    }

    private static void renderAnimation(EnumHand hand, boolean rightSide, float partialTicks) {
        AnimationState state = ANIMATIONS.get(hand);
        float angle = lerp(state.animO, state.anim, partialTicks);

        GlStateManager.translate(0.0F, 0.2F, 0.07F);
        GlStateManager.rotate(angle, 1.0F, 0.0F, 0.0F);

        Minecraft minecraft = Minecraft.getMinecraft();
        minecraft.getTextureManager().bindTexture(GLARE);
        float previousBrightnessX = OpenGlHelper.lastBrightnessX;
        float previousBrightnessY = OpenGlHelper.lastBrightnessY;
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        try {
            drawGlare(rightSide, 1.0F, -0.53D, 0.089D, -0.53D,
                    0.0F, 63.0F / 255.0F, 1.0F, 0.0F,
                    state.tickCount + 360, partialTicks,
                    lerp(state.pulseIntensityO[0], state.pulseIntensity[0], partialTicks));
            drawGlare(rightSide, 0.4F, -0.2D, 0.09D, -0.02D,
                    2.0F / 255.0F, 229.0F / 255.0F, 179.0F / 255.0F, 0.0F,
                    state.tickCount, partialTicks,
                    lerp(state.pulseIntensityO[1], state.pulseIntensity[1], partialTicks));
            drawGlare(rightSide, 0.4F, -0.2D, 0.09D, -0.02D,
                    240.0F / 255.0F, 39.0F / 255.0F, 7.0F / 255.0F,
                    hand == EnumHand.MAIN_HAND ? 270.0F : 90.0F,
                    state.tickCount, partialTicks,
                    lerp(state.pulseIntensityO[4], state.pulseIntensity[4], partialTicks));
            drawGlare(rightSide, 0.4F, -0.2D, 0.09D, -0.02D,
                    219.0F / 255.0F, 219.0F / 255.0F, 219.0F / 255.0F, 180.0F,
                    state.tickCount, partialTicks,
                    lerp(state.pulseIntensityO[3], state.pulseIntensity[3], partialTicks));
            drawGlare(rightSide, 0.4F, -0.2D, 0.09D, -0.02D,
                    66.0F / 255.0F, 221.0F / 255.0F, 6.0F / 255.0F,
                    hand == EnumHand.MAIN_HAND ? 90.0F : 270.0F,
                    state.tickCount, partialTicks,
                    lerp(state.pulseIntensityO[2], state.pulseIntensity[2], partialTicks));
        } finally {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,
                    previousBrightnessX, previousBrightnessY);
            GlStateManager.disableBlend();
            GlStateManager.enableCull();
            GlStateManager.enableAlpha();
            GlStateManager.enableLighting();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
        GlStateManager.translate(0.0F, -0.2F, -0.07F);
    }

    private static void drawGlare(boolean rightSide, float scale,
                                  double x, double sideOffset, double y,
                                  float red, float green, float blue, float rotation,
                                  int tickCount, float partialTicks, float intensity) {
        float alpha = getGlareAlpha(tickCount, rotation, partialTicks, intensity);
        if (alpha <= 0.0F) return;

        GlStateManager.pushMatrix();
        try {
            GlStateManager.rotate(rotation - 30.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(90.0F, 0.0F, -1.0F, 0.0F);
            GlStateManager.translate(x, y, rightSide ? -sideOffset : sideOffset);
            GlStateManager.scale(scale, scale, scale);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, 0.0D).color(red, green, blue, alpha).endVertex();
            buffer.pos(0.0D, 1.0D, 0.0D).tex(0.0D, 1.0D).color(red, green, blue, alpha).endVertex();
            buffer.pos(1.0D, 1.0D, 0.0D).tex(1.0D, 1.0D).color(red, green, blue, alpha).endVertex();
            buffer.pos(1.0D, 0.0D, 0.0D).tex(1.0D, 0.0D).color(red, green, blue, alpha).endVertex();
            tessellator.draw();
        } finally {
            GlStateManager.popMatrix();
        }
    }

    private static void applySwingTransform(boolean rightSide, float swingProgress) {
        float x = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
        float y = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * ((float) Math.PI * 2.0F));
        float z = -0.2F * MathHelper.sin(swingProgress * (float) Math.PI);
        GlStateManager.translate((rightSide ? 1.0F : -1.0F) * x, y, z);
    }

    private static void transformSideFirstPerson(EnumHandSide handSide, float equipProgress) {
        int direction = handSide == EnumHandSide.RIGHT ? 1 : -1;
        GlStateManager.translate(direction * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
    }

    private static void transformFirstPerson(EnumHandSide handSide, float swingProgress) {
        int direction = handSide == EnumHandSide.RIGHT ? 1 : -1;
        float squaredSwing = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        GlStateManager.rotate(direction * (45.0F + squaredSwing * -20.0F), 0.0F, 1.0F, 0.0F);
        float rootSwing = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
        GlStateManager.rotate(direction * rootSwing * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(rootSwing * -80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(direction * -45.0F, 0.0F, 1.0F, 0.0F);
    }

    static float getTrackingIntensity(EntityPlayerSP player, ItemStack stack,
                                      String tracking, int maximumDistance) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) return 0.0F;
        String positionKey = tracking + "Pos";
        if (!tag.hasKey(positionKey, 10)) return 0.0F;

        BlockPos target = NBTUtil.getPosFromTag(tag.getCompoundTag(positionKey));
        return calculateTrackingIntensity(player.posX, player.posZ, player.rotationYawHead,
                target.getX(), target.getZ(), tag.getInteger(tracking + "Dist"), maximumDistance);
    }

    static float calculateTrackingIntensity(double playerX, double playerZ, float headYaw,
                                            double targetX, double targetZ,
                                            int distance, int maximumDistance) {
        float targetYaw = (float) (MathHelper.atan2(targetX - playerX, targetZ - playerZ)
                * (180.0D / Math.PI));
        float yawDifference = (MathHelper.wrapDegrees(-headYaw) - targetYaw
                + 180.0F + 360.0F) % 360.0F - 180.0F;
        float directionIntensity = 1.0F - MathHelper.clamp(
                Math.abs(yawDifference) * 0.03F, 0.0F, 0.8F);
        if (distance < 0) return 0.0F;
        float distanceIntensity = MathHelper.clamp(
                (maximumDistance - distance) * 0.05F, 0.0F, 1.0F);
        return directionIntensity * distanceIntensity;
    }

    static float getSwapDegrees(EnumHand hand, int selectedIndex) {
        if (hand == EnumHand.OFF_HAND) {
            switch (selectedIndex) {
                case 1:
                    return 0.0F;
                case 2:
                    return 90.0F;
                case 3:
                    return 180.0F;
                case 4:
                    return 270.0F;
                default:
                    return 0.0F;
            }
        }
        if (hand == EnumHand.MAIN_HAND) {
            switch (selectedIndex) {
                case 1:
                    return 0.0F;
                case 2:
                    return 270.0F;
                case 3:
                    return 180.0F;
                case 4:
                    return 90.0F;
                default:
                    return 0.0F;
            }
        }
        return 0.0F;
    }

    static float advanceAngle(float current, float target, int steps) {
        if (steps <= 0) return current;
        return current + MathHelper.wrapDegrees(target - current) / (float) steps;
    }

    static float getGlareAlpha(int tickCount, float rotation, float partialTicks, float intensity) {
        return MathHelper.clamp(MathHelper.sin(
                (tickCount + rotation + partialTicks) * 0.2F) * intensity, 0.0F, 1.0F);
    }

    private static float lerp(float start, float end, float partialTicks) {
        return start + (end - start) * partialTicks;
    }

    static final class AnimationState {
        float anim;
        float animO;
        float targetO;
        int steps;
        int tickCount;
        final float[] pulseIntensity = new float[AmuletItem.TRACKING.length];
        final float[] pulseIntensityO = new float[AmuletItem.TRACKING.length];

        void tickRotation(float target) {
            if (target != targetO) steps = SWAP_STEPS;
            animO = anim;
            if (steps > 0) {
                anim = advanceAngle(anim, target, steps);
                steps--;
            }
            targetO = target;
        }
    }
}
