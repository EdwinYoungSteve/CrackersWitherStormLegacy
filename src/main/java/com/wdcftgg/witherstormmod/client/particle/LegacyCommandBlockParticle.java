package com.wdcftgg.witherstormmod.client.particle;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.common.entity.EntityPowerfulExplosive;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

/** 上游 Formidibomb 引信期间的 command_block 动画粒子。 */
@SideOnly(Side.CLIENT)
public final class LegacyCommandBlockParticle extends Particle {
    public static final int SUPER_BEACON_RESUMMON_BURST = 0;
    public static final int SUPER_BEACON_ITEM_BURST = 1;
    private static final ResourceLocation[] SPRITE_LOCATIONS = {
            new ResourceLocation(Tags.MOD_ID, "particle/command_block"),
            new ResourceLocation(Tags.MOD_ID, "particle/command_block_1"),
            new ResourceLocation(Tags.MOD_ID, "particle/command_block_2"),
            new ResourceLocation(Tags.MOD_ID, "particle/command_block_3")
    };

    private LegacyCommandBlockParticle(World world, double x, double y, double z,
                                       double motionX, double motionY, double motionZ,
                                       TextureAtlasSprite[] sprites) {
        super(world, x, y, z);
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        this.particleMaxAge = 10 + this.rand.nextInt(12);
        this.particleGravity = 0.03F;
        this.canCollide = false;
        this.setParticleTexture(sprites[this.rand.nextInt(sprites.length)]);
    }

    /** 在方块图集中注册上游四张独立的动画贴图。 */
    public static void registerSprites(TextureMap textureMap) {
        for (ResourceLocation location : SPRITE_LOCATIONS) {
            textureMap.registerSprite(location);
        }
    }

    /** 按上游 FormidibombEntity.tick 的公式生成六个粒子。 */
    public static void spawnForBomb(EntityPowerfulExplosive.Formidibomb bomb) {
        if (bomb == null || bomb.world == null || !bomb.world.isRemote || bomb.isDead) return;
        int startFuse = bomb.getStartFuse();
        int currentFuse = bomb.getFuseLife();
        if (startFuse <= 0 || currentFuse <= 0) return;

        Minecraft minecraft = Minecraft.getMinecraft();
        TextureAtlasSprite[] sprites = resolveSprites(minecraft);
        if (sprites == null) return;

        float fuseProgress = calculateFuseProgress(currentFuse, startFuse);
        float radius = calculateRadius(fuseProgress);
        float speed = calculateSpeed(fuseProgress);
        Random random = bomb.world.rand;
        for (int i = 0; i < 6; i++) {
            Vec3d offset = sampleOffset(random, radius);
            double offsetX = offset.x;
            double offsetY = offset.y;
            double offsetZ = offset.z;
            Vec3d delta = new Vec3d(offsetX, offsetY, offsetZ).scale(speed);
            if (fuseProgress > 0.5F) delta = delta.normalize();
            minecraft.effectRenderer.addEffect(new LegacyCommandBlockParticle(bomb.world,
                    bomb.posX + offsetX, bomb.posY + offsetY + 0.5D, bomb.posZ + offsetZ,
                    -delta.x, -delta.y, -delta.z, sprites));
        }
    }

    /** 上游复活仪式每 tick 在命令方块周围生成一颗向中心收拢的粒子。 */
    public static void spawnForSuperBeacon(World world, BlockPos beaconPos, Random random) {
        if (world == null || !world.isRemote || beaconPos == null || random == null) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        TextureAtlasSprite[] sprites = resolveSprites(minecraft);
        if (sprites == null) return;

        double targetX = beaconPos.getX() + 0.5D;
        double targetY = beaconPos.getY() + 3.0D;
        double targetZ = beaconPos.getZ() + 0.5D;
        double x = targetX + random.nextGaussian();
        double y = targetY + random.nextGaussian();
        double z = targetZ + random.nextGaussian();
        Vec3d velocity = new Vec3d(targetX - x, targetY - y, targetZ - z).normalize().scale(0.1D);
        minecraft.effectRenderer.addEffect(new LegacyCommandBlockParticle(
                world, x, y, z, velocity.x, velocity.y, velocity.z, sprites));
    }

    /** 客户端重建上游服务端粒子包的一次性爆发。 */
    public static void spawnSuperBeaconBurst(BlockPos beaconPos, int type) {
        Minecraft minecraft = Minecraft.getMinecraft();
        World world = minecraft.world;
        if (world == null || beaconPos == null) return;
        TextureAtlasSprite[] sprites = resolveSprites(minecraft);
        if (sprites == null) return;

        Vec3d smokeCenter = getSuperBeaconBurstCenter(beaconPos, type, false);
        Vec3d commandCenter = getSuperBeaconBurstCenter(beaconPos, type, true);
        Random random = world.rand;
        for (int i = 0; i < 20; i++) {
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
                    smokeCenter.x + random.nextGaussian(), smokeCenter.y + random.nextGaussian(),
                    smokeCenter.z + random.nextGaussian(), random.nextGaussian() * 0.01D,
                    random.nextGaussian() * 0.01D, random.nextGaussian() * 0.01D);
        }
        for (int i = 0; i < 50; i++) {
            minecraft.effectRenderer.addEffect(new LegacyCommandBlockParticle(world,
                    commandCenter.x + random.nextGaussian(), commandCenter.y + random.nextGaussian(),
                    commandCenter.z + random.nextGaussian(), random.nextGaussian() * 0.015D,
                    random.nextGaussian() * 0.015D, random.nextGaussian() * 0.015D, sprites));
        }
    }

    static Vec3d getSuperBeaconBurstCenter(BlockPos beaconPos, int type, boolean commandBlockParticle) {
        boolean itemCraft = type == SUPER_BEACON_ITEM_BURST;
        double yOffset = itemCraft ? 2.5D : commandBlockParticle ? 3.5D : 0.5D;
        return new Vec3d(beaconPos.getX() + 0.5D, beaconPos.getY() + yOffset,
                beaconPos.getZ() + 0.5D);
    }

    private static TextureAtlasSprite[] resolveSprites(Minecraft minecraft) {
        if (minecraft == null || minecraft.effectRenderer == null) return null;
        TextureMap textureMap = minecraft.getTextureMapBlocks();
        TextureAtlasSprite[] sprites = new TextureAtlasSprite[SPRITE_LOCATIONS.length];
        for (int i = 0; i < SPRITE_LOCATIONS.length; i++) {
            sprites[i] = textureMap.getAtlasSprite(SPRITE_LOCATIONS[i].toString());
            if (sprites[i] == null || "missingno".equals(sprites[i].getIconName())) return null;
        }
        return sprites;
    }

    static Vec3d sampleOffset(Random random, float radius) {
        return new Vec3d(
                (random.nextFloat() * 2.0F - 1.0F) * radius,
                (random.nextFloat() * 2.0F - 1.0F) * radius,
                (random.nextFloat() * 2.0F - 1.0F) * radius);
    }

    static float calculateFuseProgress(int currentFuse, int startFuse) {
        return startFuse > 0 ? 1.0F - (float) currentFuse / (float) startFuse : 0.0F;
    }

    static float calculateRadius(float fuseProgress) {
        return fuseProgress < 0.5F ? 3.0F * (1.0F - fuseProgress) : 1.5F;
    }

    static float calculateSpeed(float fuseProgress) {
        return fuseProgress < 0.5F
                ? 0.1F * (1.0F - 2.0F * fuseProgress)
                : 0.1F * (8.0F * (fuseProgress - 0.5F));
    }

    static int getParticlesPerTick() {
        return 6;
    }

    @Override
    public int getFXLayer() {
        return 1;
    }

    @Override
    public boolean shouldDisableDepth() {
        return false;
    }
}
