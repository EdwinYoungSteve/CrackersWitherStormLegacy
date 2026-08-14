package com.wdcftgg.witherstormmod.mixin;

import com.wdcftgg.witherstormmod.common.init.ModBlocks;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityEnderman.class)
public abstract class EntityEndermanMixin {

    @Inject(method = "shouldAttackPlayer", at = @At("HEAD"), cancellable = true)
    private void witherstormmod$taintedPumpkinIsEndermanMask(EntityPlayer player,
                                                             CallbackInfoReturnable<Boolean> callback) {
        ItemStack helmet = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        if (helmet.getItem() == Item.getItemFromBlock(ModBlocks.get("tainted_carved_pumpkin"))) {
            callback.setReturnValue(false);
        }
    }
}
