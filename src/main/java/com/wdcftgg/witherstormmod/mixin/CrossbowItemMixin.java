package com.wdcftgg.witherstormmod.mixin;

import com.wdcftgg.witherstormmod.common.config.WitherStormConfig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 为 Future MC 十字弩补回上游的手持末影珍珠装填与发射行为。 */
@Mixin(targets = "thedarkcolour.futuremc.item.CrossbowItem", remap = false)
public abstract class CrossbowItemMixin {
    private static final ResourceLocation FUTURE_MC_CROSSBOW_SHOOT =
            new ResourceLocation("futuremc", "crossbow_shoot");

    @Inject(
            method = "findAmmo(Lnet/minecraft/entity/EntityLivingBase;)Lnet/minecraft/item/ItemStack;",
            at = @At("HEAD"),
            cancellable = true)
    private void witherstormmod$findHeldEnderPearl(
            EntityLivingBase entity, CallbackInfoReturnable<ItemStack> callback) {
        if (!WitherStormConfig.crossbowsSupportEnderPearls
                || !(entity instanceof EntityPlayer || entity instanceof EntityMob)) return;

        ItemStack mainHand = entity.getHeldItemMainhand();
        if (isFutureMcHeldAmmo(mainHand)) return;
        if (mainHand.getItem() == Items.ENDER_PEARL) {
            callback.setReturnValue(mainHand);
            return;
        }

        ItemStack offHand = entity.getHeldItemOffhand();
        if (isFutureMcHeldAmmo(offHand)) return;
        if (offHand.getItem() == Items.ENDER_PEARL) {
            callback.setReturnValue(offHand);
        }
    }

    @Inject(
            method = "fireProjectile(Lnet/minecraft/world/World;Lnet/minecraft/entity/EntityLivingBase;"
                    + "Lnet/minecraft/util/EnumHand;Lnet/minecraft/item/ItemStack;"
                    + "Lnet/minecraft/item/ItemStack;FZFFF)V",
            at = @At("HEAD"),
            cancellable = true)
    private void witherstormmod$fireEnderPearl(
            World world, EntityLivingBase shooter, EnumHand hand, ItemStack crossbow,
            ItemStack ammunition, float soundPitch, boolean creative, float velocity,
            float inaccuracy, float offset, CallbackInfo callback) {
        if (world.isRemote || !WitherStormConfig.crossbowsSupportEnderPearls
                || ammunition.getItem() != Items.ENDER_PEARL) return;

        EntityEnderPearl enderPearl = new EntityEnderPearl(world, shooter);
        Vec3d direction = rotateAroundAxis(
                shooter.getLook(1.0F), calculateUpVector(shooter), Math.toRadians(offset));
        enderPearl.shoot(direction.x, direction.y, direction.z, velocity, inaccuracy);

        crossbow.damageItem(3, shooter);
        world.spawnEntity(enderPearl);
        SoundEvent shootSound = ForgeRegistries.SOUND_EVENTS.getValue(FUTURE_MC_CROSSBOW_SHOOT);
        if (shootSound != null) {
            world.playSound(null, shooter.posX, shooter.posY, shooter.posZ,
                    shootSound, SoundCategory.PLAYERS, 1.0F, soundPitch);
        }
        callback.cancel();
    }

    private static boolean isFutureMcHeldAmmo(ItemStack stack) {
        return stack.getItem() instanceof ItemArrow || stack.getItem() == Items.FIREWORKS;
    }

    private static Vec3d calculateUpVector(EntityLivingBase entity) {
        float pitch = entity.rotationPitch - 90.0F;
        float yaw = entity.rotationYaw;
        float yawRadians = -yaw * 0.017453292F - (float) Math.PI;
        float pitchRadians = -pitch * 0.017453292F;
        float yawCosine = MathHelper.cos(yawRadians);
        float yawSine = MathHelper.sin(yawRadians);
        float pitchCosine = -MathHelper.cos(pitchRadians);
        float pitchSine = MathHelper.sin(pitchRadians);
        return new Vec3d(yawSine * pitchCosine, pitchSine, yawCosine * pitchCosine);
    }

    private static Vec3d rotateAroundAxis(Vec3d vector, Vec3d axis, double angle) {
        Vec3d normalizedAxis = axis.normalize();
        double cosine = Math.cos(angle);
        double sine = Math.sin(angle);
        return vector.scale(cosine)
                .add(normalizedAxis.crossProduct(vector).scale(sine))
                .add(normalizedAxis.scale(normalizedAxis.dotProduct(vector) * (1.0D - cosine)));
    }
}
