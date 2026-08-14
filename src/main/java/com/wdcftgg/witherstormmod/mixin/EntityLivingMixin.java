package com.wdcftgg.witherstormmod.mixin;

import com.wdcftgg.witherstormmod.common.item.AmuletItem;
import com.wdcftgg.witherstormmod.common.item.GoldenAppleStewItem;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 让护符与金苹果炖汤先于 1.12 生物自身的骑乘、交易等右键行为处理。 */
@Mixin(EntityLiving.class)
public abstract class EntityLivingMixin {

    @Inject(
            method = "processInitialInteract(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/EnumHand;)Z",
            at = @At("HEAD"),
            cancellable = true)
    private void witherstormmod$handleImportantItemInteractionFirst(
            EntityPlayer player, EnumHand hand, CallbackInfoReturnable<Boolean> callback) {
        ItemStack stack = player.getHeldItem(hand);
        if (!(stack.getItem() instanceof AmuletItem)
                && !(stack.getItem() instanceof GoldenAppleStewItem)) return;
        if (stack.interactWithEntity(player, (EntityLiving) (Object) this, hand)) {
            callback.setReturnValue(true);
        }
    }
}
