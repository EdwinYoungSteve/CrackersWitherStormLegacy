package com.wdcftgg.witherstormmod.common.init;

import com.google.gson.JsonObject;
import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.WitherStormMod;
import com.wdcftgg.witherstormmod.common.recipe.AnvilRecipes;
import com.wdcftgg.witherstormmod.common.recipe.LockAmuletRecipe;
import com.wdcftgg.witherstormmod.common.resource.RecipeResourceConverter;
import com.wdcftgg.witherstormmod.common.resource.UpstreamResourceArchive;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.potion.PotionHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import thedarkcolour.futuremc.recipe.stonecutter.StonecutterRecipes;

import java.io.InputStream;
import java.util.List;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class ModRecipes {

    private static final int EXPECTED_CRAFTING_RECIPE_COUNT = 45;
    private static final int EXPECTED_LOCK_AMULET_RECIPE_COUNT = 1;
    private static final int EXPECTED_STONECUTTING_RECIPE_COUNT = 13;
    private static final int BREWING_COMBINATION_COUNT = 15;
    private static final String LOCK_AMULET_ENTRY = RecipeResourceConverter.RECIPE_PREFIX + "amulet_lock.json";
    private static boolean brewingRegistered;
    private static final ResourceLocation FUTURE_MC_CROSSBOW =
            new ResourceLocation("futuremc", "crossbow");

    private ModRecipes() {
    }

    public static void registerSmelting() {
        GameRegistry.addSmelting(ModBlocks.get("tainted_cobblestone"), new ItemStack(ModBlocks.get("tainted_stone")), 0.1F);
        GameRegistry.addSmelting(ModBlocks.get("tainted_sandstone"), new ItemStack(ModBlocks.get("tainted_smooth_sandstone")), 0.1F);
        GameRegistry.addSmelting(ModBlocks.get("tainted_sand"), new ItemStack(ModBlocks.get("tainted_glass")), 0.1F);
    }

    public static void registerAnvil() {
        AnvilRecipes.initialize();
    }

    public static void registerStonecutting() {
        int registered = 0;
        try {
            List<String> entries = UpstreamResourceArchive.listEntries(
                    RecipeResourceConverter.RECIPE_PREFIX, ".json");
            JsonContext context = new JsonContext(Tags.MOD_ID);
            for (String entryName : entries) {
                JsonObject converted;
                try (InputStream stream = UpstreamResourceArchive.open(entryName)) {
                    converted = RecipeResourceConverter.convertStonecutting(entryName, stream);
                }
                if (converted == null) continue;

                Ingredient ingredient = CraftingHelper.getIngredient(converted.get("ingredient"), context);
                ItemStack result = CraftingHelper.getItemStack(converted.getAsJsonObject("result"), context);
                StonecutterRecipes.INSTANCE.addRecipe(ingredient, result);
                registered++;
            }
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to register stonecutting recipes from the external Wither Storm archive", exception);
        }
        if (registered != EXPECTED_STONECUTTING_RECIPE_COUNT) {
            throw new IllegalStateException("Expected " + EXPECTED_STONECUTTING_RECIPE_COUNT
                    + " external stonecutting recipes but registered " + registered);
        }
        WitherStormMod.LOGGER.info(
                "Registered {} stonecutting recipes from the external Wither Storm archive through Future MC",
                registered);
    }

    public static void registerBrewing() {
        if (brewingRegistered) {
            return;
        }

        PotionHelper.addMix(PotionTypes.POISON, ModItems.get("withered_spider_eye"), ModPotionTypes.WITHER);
        PotionHelper.addMix(PotionTypes.LONG_POISON, ModItems.get("withered_spider_eye"), ModPotionTypes.LONG_WITHER);
        PotionHelper.addMix(PotionTypes.STRONG_POISON, ModItems.get("withered_spider_eye"), ModPotionTypes.STRONG_WITHER);
        PotionHelper.addMix(ModPotionTypes.WITHER, Items.REDSTONE, ModPotionTypes.LONG_WITHER);
        PotionHelper.addMix(ModPotionTypes.WITHER, Items.GLOWSTONE_DUST, ModPotionTypes.STRONG_WITHER);
        brewingRegistered = true;

        WitherStormMod.LOGGER.info(
                "Registered {} brewing combinations through five potion-type conversions",
                BREWING_COMBINATION_COUNT);
    }

    @SubscribeEvent
    public static void registerCraftingRecipes(RegistryEvent.Register<IRecipe> event) {
        net.minecraft.item.Item crossbow = ForgeRegistries.ITEMS.getValue(FUTURE_MC_CROSSBOW);
        if (crossbow == null) {
            throw new IllegalStateException("Future MC crossbow was not registered");
        }
        ShapedOreRecipe crossbowRecipe = new ShapedOreRecipe(null, new ItemStack(crossbow),
                "SIS", "HTH", " S ",
                'S', Items.STICK,
                'I', Items.IRON_INGOT,
                'H', Items.STRING,
                'T', Blocks.TRIPWIRE_HOOK);
        crossbowRecipe.setRegistryName(new ResourceLocation(Tags.MOD_ID, "crossbow"));
        event.getRegistry().register(crossbowRecipe);

        int registered = 0;
        int lockAmuletRegistered = 0;
        try {
            List<String> entries = UpstreamResourceArchive.listEntries(
                    RecipeResourceConverter.RECIPE_PREFIX, ".json");
            JsonContext context = new JsonContext(Tags.MOD_ID);
            for (String entryName : entries) {
                if (LOCK_AMULET_ENTRY.equals(entryName)) {
                    boolean lockAmulet;
                    try (InputStream stream = UpstreamResourceArchive.open(entryName)) {
                        lockAmulet = RecipeResourceConverter.isLockAmulet(entryName, stream);
                    }
                    if (!lockAmulet) {
                        throw new IllegalStateException("Expected the external amulet lock recipe at " + entryName);
                    }
                    LockAmuletRecipe recipe = new LockAmuletRecipe();
                    recipe.setRegistryName(new ResourceLocation(Tags.MOD_ID, "amulet_lock"));
                    event.getRegistry().register(recipe);
                    lockAmuletRegistered++;
                    continue;
                }

                JsonObject converted;
                try (InputStream stream = UpstreamResourceArchive.open(entryName)) {
                    converted = RecipeResourceConverter.convert(entryName, stream);
                }
                if (converted == null) continue;

                String recipePath = entryName.substring(RecipeResourceConverter.RECIPE_PREFIX.length(),
                        entryName.length() - ".json".length());
                ResourceLocation registryName = new ResourceLocation(Tags.MOD_ID, recipePath);
                IRecipe recipe = CraftingHelper.getRecipe(converted, context).setRegistryName(registryName);
                event.getRegistry().register(recipe);
                registered++;
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to register recipes from the external Wither Storm archive", exception);
        }
        if (registered != EXPECTED_CRAFTING_RECIPE_COUNT) {
            throw new IllegalStateException("Expected " + EXPECTED_CRAFTING_RECIPE_COUNT
                    + " external crafting recipes but registered " + registered);
        }
        if (lockAmuletRegistered != EXPECTED_LOCK_AMULET_RECIPE_COUNT) {
            throw new IllegalStateException("Expected " + EXPECTED_LOCK_AMULET_RECIPE_COUNT
                    + " external amulet lock recipe but registered " + lockAmuletRegistered);
        }
        WitherStormMod.LOGGER.info("Registered {} crafting recipes from the external Wither Storm archive", registered);
        WitherStormMod.LOGGER.info("Registered the external amulet lock recipe");
        WitherStormMod.LOGGER.info("Registered the standard crafting recipe for the restored Future MC crossbow");
    }
}
