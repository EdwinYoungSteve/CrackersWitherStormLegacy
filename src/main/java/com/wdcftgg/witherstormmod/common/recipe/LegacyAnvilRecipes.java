package com.wdcftgg.witherstormmod.common.recipe;

import com.google.gson.JsonObject;
import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.WitherStormMod;
import com.wdcftgg.witherstormmod.common.resource.LegacyRecipeResourceConverter;
import com.wdcftgg.witherstormmod.common.resource.UpstreamResourceArchive;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class LegacyAnvilRecipes {

    private static final int EXPECTED_RECIPE_COUNT = 25;
    private static volatile List<Recipe> recipes = Collections.emptyList();
    private static volatile boolean initialized;

    private LegacyAnvilRecipes() {
    }

    public static synchronized void initialize() {
        if (initialized) return;

        List<Recipe> loaded = new ArrayList<Recipe>();
        JsonContext context = new JsonContext(Tags.MOD_ID);
        try {
            for (String entryName : UpstreamResourceArchive.listEntries(
                    LegacyRecipeResourceConverter.RECIPE_PREFIX, ".json")) {
                JsonObject converted;
                try (InputStream stream = UpstreamResourceArchive.open(entryName)) {
                    converted = LegacyRecipeResourceConverter.convertAnvil(entryName, stream);
                }
                if (converted == null) continue;

                String path = entryName.substring(LegacyRecipeResourceConverter.RECIPE_PREFIX.length(),
                        entryName.length() - ".json".length());
                loaded.add(new Recipe(
                        new ResourceLocation(Tags.MOD_ID, path),
                        CraftingHelper.getIngredient(converted.get("left"), context),
                        CraftingHelper.getIngredient(converted.get("right"), context),
                        CraftingHelper.getItemStack(converted.getAsJsonObject("result"), context),
                        converted.get("cost").getAsInt()));
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load anvil recipes from the external Wither Storm archive",
                    exception);
        }
        if (loaded.size() != EXPECTED_RECIPE_COUNT) {
            throw new IllegalStateException("Expected " + EXPECTED_RECIPE_COUNT
                    + " external anvil recipes but loaded " + loaded.size());
        }
        recipes = Collections.unmodifiableList(loaded);
        initialized = true;
        WitherStormMod.LOGGER.info("Loaded {} anvil recipes from the external Wither Storm archive", loaded.size());
    }

    public static Recipe find(ItemStack left, ItemStack right) {
        if (!initialized) initialize();
        for (Recipe recipe : recipes) {
            if (recipe.matches(left, right)) return recipe;
        }
        return null;
    }

    static List<Recipe> recipesForTesting() {
        if (!initialized) initialize();
        return recipes;
    }

    public static final class Recipe {
        private final ResourceLocation id;
        private final Ingredient left;
        private final Ingredient right;
        private final ItemStack result;
        private final int cost;

        Recipe(ResourceLocation id, Ingredient left, Ingredient right, ItemStack result, int cost) {
            this.id = id;
            this.left = left;
            this.right = right;
            this.result = result.copy();
            this.cost = cost;
        }

        public boolean matches(ItemStack leftStack, ItemStack rightStack) {
            return left.apply(leftStack) && right.apply(rightStack);
        }

        public ItemStack createOutput(ItemStack leftStack, ItemStack rightStack) {
            ItemStack output = result.copy();
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(leftStack);
            enchantments.putAll(EnchantmentHelper.getEnchantments(rightStack));
            EnchantmentHelper.setEnchantments(enchantments, output);
            return output;
        }

        public ResourceLocation getId() {
            return id;
        }

        public int getCost() {
            return cost;
        }
    }
}
