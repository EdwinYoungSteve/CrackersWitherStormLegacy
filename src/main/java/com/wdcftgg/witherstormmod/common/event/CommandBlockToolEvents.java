package com.wdcftgg.witherstormmod.common.event;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.common.recipe.AnvilRecipes;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class CommandBlockToolEvents {

    private CommandBlockToolEvents() {
    }

    @SubscribeEvent
    public static void updateAnvil(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        AnvilRecipes.Recipe recipe = AnvilRecipes.find(left, right);
        if (recipe == null) return;

        event.setOutput(recipe.createOutput(left, right));
        event.setCost(recipe.getCost());
    }
}
