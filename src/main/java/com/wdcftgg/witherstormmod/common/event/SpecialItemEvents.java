package com.wdcftgg.witherstormmod.common.event;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.common.init.ModBlocks;
import com.wdcftgg.witherstormmod.common.taint.LegacyWitherSicknessCure;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class SpecialItemEvents {
    private SpecialItemEvents() { }

    @SubscribeEvent
    public static void protectPumpkinWearers(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityEnderman)) return;
        EntityEnderman enderman = (EntityEnderman) event.getEntityLiving();
        EntityLivingBase target = enderman.getAttackTarget();
        if (!(target instanceof EntityPlayer)) return;
        ItemStack helmet = target.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        if (helmet.getItem() == Item.getItemFromBlock(ModBlocks.get("tainted_carved_pumpkin"))) {
            enderman.setAttackTarget(null);
        }
    }

    @SubscribeEvent
    public static void updateWitherSicknessCure(LivingEvent.LivingUpdateEvent event) {
        LegacyWitherSicknessCure.tick(event.getEntityLiving());
    }
}
