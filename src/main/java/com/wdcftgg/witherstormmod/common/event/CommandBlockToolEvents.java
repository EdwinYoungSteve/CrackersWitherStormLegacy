package com.wdcftgg.witherstormmod.common.event;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.common.entity.EntityWitherStormLegacy;
import com.wdcftgg.witherstormmod.common.entity.SickenedEntities;
import com.wdcftgg.witherstormmod.common.entity.SupplementalEntities;
import com.wdcftgg.witherstormmod.common.recipe.LegacyAnvilRecipes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class CommandBlockToolEvents {

    private static final String[] TOOL_TYPES = {"sword", "pickaxe", "axe", "shovel", "hoe"};

    private CommandBlockToolEvents() {
    }

    @SubscribeEvent
    public static void updateAnvil(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        LegacyAnvilRecipes.Recipe recipe = LegacyAnvilRecipes.find(left, right);
        if (recipe == null) return;

        event.setOutput(recipe.createOutput(left, right));
        event.setCost(recipe.getCost());
    }

    @SubscribeEvent
    public static void hurtStormEntity(LivingHurtEvent event) {
        Entity source = event.getSource().getTrueSource();
        if (!(source instanceof EntityPlayer) || !isStormTarget(event.getEntityLiving())) {
            return;
        }

        ItemStack weapon = ((EntityPlayer) source).getHeldItemMainhand();
        ResourceLocation itemName = weapon.getItem().getRegistryName();
        if (itemName == null || !Tags.MOD_ID.equals(itemName.getNamespace()) || !isCommandBlockTool(itemName.getPath())) {
            return;
        }

        float bonus = specialDamage(itemName.getPath());
        event.setAmount(event.getAmount() + bonus);
        if (!source.world.isRemote && source.world instanceof WorldServer) {
            WorldServer world = (WorldServer) source.world;
            world.spawnParticle(EnumParticleTypes.PORTAL, event.getEntityLiving().posX,
                    event.getEntityLiving().posY + event.getEntityLiving().height * 0.6D,
                    event.getEntityLiving().posZ, 18, 0.45D, 0.45D, 0.45D, 0.12D);
        }
    }

    private static boolean isCommandBlockTool(String name) {
        if (!name.contains("command_block_")) {
            return false;
        }
        for (String type : TOOL_TYPES) {
            if (name.endsWith("_" + type)) {
                return true;
            }
        }
        return false;
    }

    private static float specialDamage(String name) {
        if (name.startsWith("wooden_")) return 4.0F;
        if (name.startsWith("stone_")) return 6.0F;
        if (name.startsWith("iron_")) return 8.0F;
        if (name.startsWith("gold_")) return 5.0F;
        return 12.0F;
    }

    private static boolean isStormTarget(Entity entity) {
        return entity instanceof EntityWitherStormLegacy
                || entity instanceof SickenedEntities.Tentacle
                || entity instanceof SickenedEntities.WitheredSymbiont
                || entity instanceof SupplementalEntities.CommandBlockCore
                || entity instanceof SupplementalEntities.WitherStormHead
                || entity instanceof SupplementalEntities.WitherStormSegment;
    }
}
