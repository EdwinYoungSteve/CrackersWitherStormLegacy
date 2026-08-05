package com.wdcftgg.witherstormmod.client.particle;

import com.wdcftgg.witherstormmod.Tags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class LegacyPhlegmParticle extends Particle {
    private static final double AIR_FRICTION = 0.91D;
    private static final int FULL_BRIGHT_LIGHT = 15728880;
    private static final ResourceLocation SPRITE_LOCATION =
            new ResourceLocation(Tags.MOD_ID, "particle/phlegm");

    private LegacyPhlegmParticle(World world, double x, double y, double z,
                                 double motionX, double motionY, double motionZ,
                                 TextureAtlasSprite sprite) {
        super(world, x, y, z);
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        this.particleMaxAge = 45;
        this.particleScale = 0.1F * this.rand.nextFloat();
        this.particleGravity = 0.0F;
        this.canCollide = false;
        setParticleTexture(sprite);
    }

    public static void registerSprite(TextureMap textureMap) {
        textureMap.registerSprite(SPRITE_LOCATION);
    }

    public static void spawnForBlock(World world, BlockPos pos, boolean powered, Random random) {
        if (world == null || !world.isRemote) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.effectRenderer == null) return;
        TextureAtlasSprite sprite = minecraft.getTextureMapBlocks().getAtlasSprite(SPRITE_LOCATION.toString());
        if (sprite == null || "missingno".equals(sprite.getIconName())) return;

        int count = powered ? 2 : 6;
        float radius = powered ? 1.0F : 3.0F;
        float speed = powered ? -0.01F : 0.1F;
        for (int i = 0; i < count; i++) {
            Vec3d offset = sampleOffset(random, radius);
            Vec3d motion = offset.scale(-speed);
            minecraft.effectRenderer.addEffect(new LegacyPhlegmParticle(world,
                    pos.getX() + offset.x, pos.getY() + offset.y, pos.getZ() + offset.z,
                    motion.x, motion.y, motion.z, sprite));
        }
    }

    static Vec3d sampleOffset(Random random, float radius) {
        return new Vec3d(
                (random.nextFloat() * 2.0F - 1.0F) * radius,
                (random.nextFloat() * 2.0F - 1.0F) * radius,
                (random.nextFloat() * 2.0F - 1.0F) * radius);
    }

    static int getParticleCount(boolean powered) {
        return powered ? 2 : 6;
    }

    static float getRadius(boolean powered) {
        return powered ? 1.0F : 3.0F;
    }

    static float getSpeed(boolean powered) {
        return powered ? -0.01F : 0.1F;
    }

    static double getAirFriction() {
        return AIR_FRICTION;
    }

    static float getAlphaForAge(int age, int lifetime) {
        if (age <= lifetime / 2) return 1.0F;
        return 1.0F - ((float) age - (float) (lifetime / 2)) / (float) lifetime;
    }

    @Override
    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        if (particleAge++ >= particleMaxAge) {
            setExpired();
            return;
        }

        motionY -= 0.04D * particleGravity;
        move(motionX, motionY, motionZ);
        motionX *= AIR_FRICTION;
        motionY *= AIR_FRICTION;
        motionZ *= AIR_FRICTION;
        if (onGround) {
            motionX *= 0.699999988079071D;
            motionZ *= 0.699999988079071D;
        }
        setAlphaF(getAlphaForAge(particleAge, particleMaxAge));
    }

    @Override
    public int getBrightnessForRender(float partialTick) {
        return FULL_BRIGHT_LIGHT;
    }

    @Override
    public int getFXLayer() {
        return 1;
    }
}
