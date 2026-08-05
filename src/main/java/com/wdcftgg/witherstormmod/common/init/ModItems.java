package com.wdcftgg.witherstormmod.common.init;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.common.item.LegacyAxeItem;
import com.wdcftgg.witherstormmod.common.item.AmuletItem;
import com.wdcftgg.witherstormmod.common.item.FormidiBladeItem;
import com.wdcftgg.witherstormmod.common.item.EyeOfTheStormItem;
import com.wdcftgg.witherstormmod.common.item.GoldenAppleStewItem;
import com.wdcftgg.witherstormmod.common.item.LegacyFoodItem;
import com.wdcftgg.witherstormmod.common.item.LegacyFoiledItem;
import com.wdcftgg.witherstormmod.common.item.LegacyHoeItem;
import com.wdcftgg.witherstormmod.common.item.LegacyItem;
import com.wdcftgg.witherstormmod.common.item.LegacyPickaxeItem;
import com.wdcftgg.witherstormmod.common.item.LegacyShovelItem;
import com.wdcftgg.witherstormmod.common.item.LegacySpawnEggItem;
import com.wdcftgg.witherstormmod.common.item.LegacySwordItem;
import com.wdcftgg.witherstormmod.common.item.LegacyToolMaterials;
import com.wdcftgg.witherstormmod.common.item.WitheredNetherStarItem;
import com.wdcftgg.witherstormmod.common.item.PhasometerItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.LinkedHashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class ModItems {

    private static final Map<String, Item> ITEMS = new LinkedHashMap<String, Item>();
    private static final Map<String, int[]> SPAWN_EGG_COLORS = new LinkedHashMap<String, int[]>();
    private static final String[] ITEM_NAMES = LegacyRegistryNames.itemNames();

    static {
        SPAWN_EGG_COLORS.put("sickened_creeper", new int[] {9851315, 3278099});
        SPAWN_EGG_COLORS.put("sickened_skeleton", new int[] {13606575, 3612758});
        SPAWN_EGG_COLORS.put("sickened_spider", new int[] {2827051, 16056399});
        SPAWN_EGG_COLORS.put("sickened_villager", new int[] {8551284, 11305627});
        SPAWN_EGG_COLORS.put("sickened_zombie", new int[] {4808027, 10648470});
        SPAWN_EGG_COLORS.put("sickened_phantom", new int[] {6967167, 16713046});
        SPAWN_EGG_COLORS.put("sickened_chicken", new int[] {5977232, 5570648});
        SPAWN_EGG_COLORS.put("sickened_parrot", new int[] {7032441, 5911693});
        SPAWN_EGG_COLORS.put("sickened_wolf", new int[] {4866401, 7960207});
        SPAWN_EGG_COLORS.put("sickened_cat", new int[] {1775149, 9595267});
        SPAWN_EGG_COLORS.put("sickened_cow", new int[] {3613496, 10066329});
        SPAWN_EGG_COLORS.put("sickened_pig", new int[] {6706811, 5786734});
        SPAWN_EGG_COLORS.put("sickened_mushroom_cow", new int[] {8200599, 11887564});
        SPAWN_EGG_COLORS.put("sickened_bee", new int[] {10711155, 3023140});
        SPAWN_EGG_COLORS.put("sickened_pillager", new int[] {4403259, 10190758});
        SPAWN_EGG_COLORS.put("sickened_vindicator", new int[] {10190758, 3422273});
        SPAWN_EGG_COLORS.put("sickened_iron_golem", new int[] {13541842, 15270143});
        SPAWN_EGG_COLORS.put("sickened_snow_golem", new int[] {15589887, 12754175});
        SPAWN_EGG_COLORS.put("tentacle", new int[] {722193, 1379103});
        SPAWN_EGG_COLORS.put("withered_symbiont", new int[] {2233397, 16056568});
        for (String name : ITEM_NAMES) {
            if (!ModBlocksContains.blockExists(name)) {
                ITEMS.put(name, createItem(name));
            }
        }
    }

    private ModItems() {
    }

    public static void bootstrap() {
    }

    public static Item get(String name) {
        Item item = ITEMS.get(name);
        if (item != null) {
            return item;
        }
        return ModBlocks.getItem(name);
    }

    public static String[] getRegisteredNames() {
        return ITEM_NAMES.clone();
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(ITEMS.values().toArray(new Item[0]));
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels() {
        for (Item item : ITEMS.values()) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }

    private static final class ModBlocksContains {
        private static boolean blockExists(String name) {
            return ModBlocks.get(name) != null;
        }
    }

    private static Item createItem(String name) {
        if ("amulet".equals(name)) {
            return new AmuletItem(name);
        }
        if ("phasometer".equals(name)) {
            return new PhasometerItem(name);
        }
        if ("withered_bone".equals(name)) {
            return new LegacyItem(name, EnumRarity.UNCOMMON);
        }
        if ("command_block_book".equals(name)) {
            return new LegacyFoiledItem(name, EnumRarity.RARE);
        }
        if ("formidi_blade".equals(name)) {
            return new FormidiBladeItem(name);
        }
        if ("eye_of_the_storm".equals(name)) {
            return new EyeOfTheStormItem(name);
        }
        if (name.endsWith("_spawn_egg")) {
            String entityName = name.substring(0, name.length() - "_spawn_egg".length());
            int[] colors = SPAWN_EGG_COLORS.get(entityName);
            if (colors == null) {
                throw new IllegalStateException("Missing upstream spawn egg colors for " + entityName);
            }
            return new LegacySpawnEggItem(name, entityName, colors[0], colors[1]);
        }
        if ("golden_apple_stew".equals(name)) {
            return new GoldenAppleStewItem(name);
        }
        if ("withered_flesh".equals(name)) {
            return new LegacyFoodItem(name, 4, 0.1F, true, EnumRarity.UNCOMMON,
                    LegacyFoodItem.effect(MobEffects.HUNGER, 800, 0, 0.8F),
                    LegacyFoodItem.effect(MobEffects.WITHER, 400, 0, 1.0F));
        }
        if ("withered_spider_eye".equals(name)) {
            return new LegacyFoodItem(name, 2, 0.8F, false, EnumRarity.UNCOMMON,
                    LegacyFoodItem.effect(MobEffects.POISON, 200, 0, 1.0F),
                    LegacyFoodItem.effect(MobEffects.WITHER, 400, 0, 1.0F));
        }
        if ("withered_nether_star".equals(name)) {
            return new WitheredNetherStarItem(name);
        }
        if (name.endsWith("_sword") || "formidi_blade".equals(name)) {
            return new LegacySwordItem(name, materialFor(name), swordDamage(name), swordSpeed(name));
        }
        if (name.endsWith("_pickaxe")) {
            return new LegacyPickaxeItem(name, materialFor(name), pickaxeDamage(name), pickaxeSpeed(name));
        }
        if (name.endsWith("_axe")) {
            return new LegacyAxeItem(name, materialFor(name), axeDamage(name), axeSpeed(name));
        }
        if (name.endsWith("_shovel")) {
            return new LegacyShovelItem(name, materialFor(name), shovelDamage(name), shovelSpeed(name));
        }
        if (name.endsWith("_hoe")) {
            return new LegacyHoeItem(name, materialFor(name), hoeDamage(name), hoeSpeed(name));
        }
        return new LegacyItem(name);
    }

    private static Item.ToolMaterial materialFor(String name) {
        if (name.startsWith("wooden_")) {
            return LegacyToolMaterials.WOOD_COMMAND_BLOCK;
        }
        if (name.startsWith("stone_")) {
            return LegacyToolMaterials.STONE_COMMAND_BLOCK;
        }
        if (name.startsWith("iron_")) {
            return LegacyToolMaterials.IRON_COMMAND_BLOCK;
        }
        if (name.startsWith("gold_")) {
            return LegacyToolMaterials.GOLD_COMMAND_BLOCK;
        }
        return LegacyToolMaterials.COMMAND_BLOCK;
    }

    private static float swordDamage(String name) {
        if (name.startsWith("iron_")) return 4.0F;
        if (name.startsWith("gold_")) return -1.0F;
        return 3.0F;
    }

    private static float swordSpeed(String name) {
        if (name.startsWith("iron_")) return -2.8F;
        if (name.startsWith("gold_")) return -1.2F;
        return -2.4F;
    }

    private static float pickaxeDamage(String name) { return name.startsWith("iron_") ? 3.0F : 1.0F; }
    private static float pickaxeSpeed(String name) { return name.startsWith("iron_") ? -3.2F : -2.8F; }
    private static float axeDamage(String name) { return name.startsWith("command_block_") ? 5.0F : 6.0F; }
    private static float axeSpeed(String name) {
        if (name.startsWith("wooden_") || name.startsWith("stone_")) return -3.2F;
        if (name.startsWith("iron_")) return -3.1F;
        return -3.0F;
    }
    private static float shovelDamage(String name) { return name.startsWith("iron_") ? 2.5F : 1.5F; }
    private static float shovelSpeed(String name) {
        if (name.startsWith("command_block_")) return -3.4F;
        if (name.startsWith("stone_")) return -2.0F;
        return -3.0F;
    }
    private static float hoeDamage(String name) {
        if (name.startsWith("wooden_") || name.startsWith("command_block_")) return -4.0F;
        if (name.startsWith("stone_")) return -3.0F;
        if (name.startsWith("iron_")) return 9.0F;
        return 0.0F;
    }
    private static float hoeSpeed(String name) {
        if (name.startsWith("wooden_")) return 3.0F;
        if (name.startsWith("command_block_")) return 0.0F;
        if (name.startsWith("iron_")) return -3.5F;
        return -3.0F;
    }
}
