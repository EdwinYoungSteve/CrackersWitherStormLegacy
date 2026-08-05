package com.wdcftgg.witherstormmod.common.entity;

import com.wdcftgg.witherstormmod.common.init.ModSounds;
import com.wdcftgg.witherstormmod.common.network.LegacyNetwork;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class LegacyFormidibombExplosion {

    private LegacyFormidibombExplosion() {
    }

    public static void explode(World world, @Nullable EntityPowerfulExplosive.Formidibomb bomb, int radius, int squish,
                               double x, double y, double z) {
        Entity owner = bomb == null ? null : bomb.getTntPlacedBy();
        Explosion explosion = new Explosion(world, owner, x, y, z, radius, true, true);
        world.playSound(null, x, y, z, ModSounds.get("formidibomb_explosion"), SoundCategory.BLOCKS, 16.0F, 1.0F);
        world.playSound(null, x, y, z, ModSounds.get("tremble"), SoundCategory.BLOCKS, 32.0F, 1.0F);

        float diameter = radius * 2.0F;
        AxisAlignedBB area = new AxisAlignedBB(
                MathHelper.floor(x - diameter - 1.0D), MathHelper.floor(y - diameter - 1.0D), MathHelper.floor(z - diameter - 1.0D),
                MathHelper.floor(x + diameter + 1.0D), MathHelper.floor(y + diameter + 1.0D), MathHelper.floor(z + diameter + 1.0D));

        // 上游在爆炸区域外再扩大 200 格寻找可被 Formidibomb 触发的风暴。
        List<EntityWitherStormLegacy> storms = world.getEntitiesWithinAABB(
                EntityWitherStormLegacy.class, area.grow(200.0D));
        for (EntityWitherStormLegacy storm : storms) storm.onFormidibombExplosion();
        // 先发送客户端表现包，再执行大范围方块遍历，避免爆炸视觉被服务端计算阻塞。
        LegacyNetwork.sendFormidibombExplosion(world, bomb, x, y, z, radius, squish);
        LegacyNetwork.shakeDimension(world, 100.0F, 7.5F);
        LegacyNetwork.blindNear(world, x, y, z, 250.0D, 260, 40, 240);

        List<Drop> drops = new ArrayList<Drop>();
        for (int dx = -radius; dx < radius; dx++) {
            for (int dy = -radius; dy < radius; dy++) {
                for (int dz = -radius; dz < radius; dz++) {
                    if (MathHelper.sqrt(dx * dx + dy * dy * squish + dz * dz) >= radius) continue;
                    int thickness = world.rand.nextInt(2);
                    for (int offset = -thickness; offset <= thickness; offset++) {
                        BlockPos pos = new BlockPos(dx + x, dy + y - offset, dz + z);
                        IBlockState state = world.getBlockState(pos);
                        if (state.getBlock() == Blocks.AIR) continue;
                        Block block = state.getBlock();

                        if (block.canDropFromExplosion(explosion)) {
                            NonNullList<ItemStack> blockDrops = NonNullList.create();
                            block.getDrops(blockDrops, world, pos, state, 0);
                            for (ItemStack stack : blockDrops) mergeDrop(drops, stack, pos);
                        }

                        float remainingPower = radius * (0.7F + world.rand.nextFloat() * 0.6F);
                        float resistance = owner != null
                                ? owner.getExplosionResistance(explosion, world, pos, state)
                                : block.getExplosionResistance(world, pos, null, explosion);
                        remainingPower -= (resistance + 0.3F) * 0.01F;
                        if (remainingPower <= 0.0F
                                || owner != null && !owner.canExplosionDestroyBlock(explosion, world, pos, state, remainingPower)) continue;

                        block.onBlockExploded(world, pos, explosion);
                        if (world.rand.nextInt(3) == 0 && world.isAirBlock(pos) && world.getBlockState(pos.down()).isFullBlock()) {
                            world.setBlockState(pos, Blocks.FIRE.getDefaultState());
                        }
                    }
                }
            }
        }

        List<Entity> affected = world.getEntitiesWithinAABBExcludingEntity(null, area);
        ForgeEventFactory.onExplosionDetonate(world, explosion, affected, diameter);
        Vec3d center = new Vec3d(x, y, z);
        for (Entity entity : affected) {
            if (entity.isImmuneToExplosions()) continue;
            double distance = Math.sqrt(entity.getDistanceSq(center.x, center.y, center.z)) / diameter;
            if (distance > 1.0D) continue;

            double relativeX = entity.posX - x;
            double relativeY = (entity instanceof EntityTNTPrimed ? entity.posY : entity.posY + entity.getEyeHeight()) - y;
            double relativeZ = entity.posZ - z;
            double length = Math.sqrt(relativeX * relativeX + relativeY * relativeY + relativeZ * relativeZ);
            if (length == 0.0D) continue;
            relativeX /= length;
            relativeY /= length;
            relativeZ /= length;

            double visibility = world.getBlockDensity(center, entity.getEntityBoundingBox());
            double power = (1.0D - distance) * visibility;
            float damage = (float) ((int) ((power * power + power) * 0.5D * 7.0D * diameter + 1.0D));
            entity.attackEntityFrom(DamageSource.causeExplosionDamage(explosion), damage);
            double knockback = entity instanceof EntityLivingBase
                    ? EnchantmentProtection.getBlastDamageReduction((EntityLivingBase) entity, power)
                    : power;
            entity.motionX += relativeX * (knockback + radius);
            entity.motionY += relativeY * (knockback + radius);
            entity.motionZ += relativeZ * (knockback + radius);
            entity.velocityChanged = true;
        }

        for (Drop drop : drops) Block.spawnAsEntity(world, drop.pos, drop.stack);
    }

    private static void mergeDrop(List<Drop> drops, ItemStack stack, BlockPos pos) {
        if (stack.isEmpty()) return;
        for (Drop drop : drops) {
            if (!ItemStack.areItemsEqual(drop.stack, stack) || !ItemStack.areItemStackTagsEqual(drop.stack, stack)) continue;
            int amount = Math.min(16 - drop.stack.getCount(), stack.getCount());
            if (amount <= 0) continue;
            drop.stack.grow(amount);
            stack.shrink(amount);
            if (stack.isEmpty()) return;
        }
        drops.add(new Drop(stack.copy(), pos.toImmutable()));
    }

    private static final class Drop {
        private final ItemStack stack;
        private final BlockPos pos;

        private Drop(ItemStack stack, BlockPos pos) {
            this.stack = stack;
            this.pos = pos;
        }
    }
}
