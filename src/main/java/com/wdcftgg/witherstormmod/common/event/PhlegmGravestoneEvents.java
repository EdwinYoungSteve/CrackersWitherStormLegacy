package com.wdcftgg.witherstormmod.common.event;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.common.util.PhlegmGravestoneHelper;
import com.wdcftgg.witherstormmod.common.access.EntityLivingBaseExperienceAccess;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class PhlegmGravestoneEvents {
    private PhlegmGravestoneEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void preserveDrops(LivingDropsEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.world.isRemote) return;
        Vec3d position = PhlegmGravestoneHelper.findPotentialClusterPosition(entity, event.getSource());
        if (position == null) return;

        List<ItemStack> drops = new ArrayList<ItemStack>();
        for (EntityItem item : event.getDrops()) {
            if (!item.getItem().isEmpty()) drops.add(item.getItem());
        }
        if (drops.isEmpty()) return;

        EntityLivingBaseExperienceAccess experienceAccess = (EntityLivingBaseExperienceAccess) entity;
        int experience = experienceAccess.witherstormmod$captureExperienceDrop();
        if (PhlegmGravestoneHelper.spawnForEntity(entity, position, drops, experience) == null) return;
        experienceAccess.witherstormmod$skipNextExperienceDrop();
        event.getDrops().clear();
        event.setCanceled(true);
    }
}
