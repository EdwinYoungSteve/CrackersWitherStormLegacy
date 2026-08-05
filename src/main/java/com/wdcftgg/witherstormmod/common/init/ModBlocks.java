package com.wdcftgg.witherstormmod.common.init;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.common.block.LegacyButtonBlock;
import com.wdcftgg.witherstormmod.common.block.LegacyAxisBlock;
import com.wdcftgg.witherstormmod.common.block.BlockSuperBeacon;
import com.wdcftgg.witherstormmod.common.block.BlockSuperSupportBeacon;
import com.wdcftgg.witherstormmod.common.block.BlockFireworkBundle;
import com.wdcftgg.witherstormmod.common.block.BlockTaintedTorch;
import com.wdcftgg.witherstormmod.common.block.BlockTaintedStandingSign;
import com.wdcftgg.witherstormmod.common.block.BlockTaintedWallSign;
import com.wdcftgg.witherstormmod.common.item.TaintedSignItem;
import com.wdcftgg.witherstormmod.common.item.TaintedTorchItem;
import com.wdcftgg.witherstormmod.common.item.LegacySlabItem;
import com.wdcftgg.witherstormmod.common.item.LegacyRarityBlockItem;
import com.wdcftgg.witherstormmod.common.block.LegacyDoorBlock;
import com.wdcftgg.witherstormmod.common.block.LegacyFallingBlock;
import com.wdcftgg.witherstormmod.common.block.LegacyFenceBlock;
import com.wdcftgg.witherstormmod.common.block.LegacyFenceGateBlock;
import com.wdcftgg.witherstormmod.common.block.LegacyFleshVeinsBlock;
import com.wdcftgg.witherstormmod.common.block.LegacyBlock;
import com.wdcftgg.witherstormmod.common.block.LegacyExplosiveBlock;
import com.wdcftgg.witherstormmod.common.block.LegacyPressurePlateBlock;
import com.wdcftgg.witherstormmod.common.block.LegacyHorizontalBlock;
import com.wdcftgg.witherstormmod.common.block.LegacyPaneBlock;
import com.wdcftgg.witherstormmod.common.block.LegacySlabBlock;
import com.wdcftgg.witherstormmod.common.block.LegacyStairsBlock;
import com.wdcftgg.witherstormmod.common.block.LegacyStrippableAxisBlock;
import com.wdcftgg.witherstormmod.common.block.LegacyTrapDoorBlock;
import com.wdcftgg.witherstormmod.common.block.LegacyTaintedDustBlock;
import com.wdcftgg.witherstormmod.common.block.LegacyTaintedDustLampBlock;
import com.wdcftgg.witherstormmod.common.block.LegacyTaintedCarvedPumpkinBlock;
import com.wdcftgg.witherstormmod.common.block.LegacyTaintedPumpkinBlock;
import com.wdcftgg.witherstormmod.common.block.LegacyTaintedStatueBlock;
import com.wdcftgg.witherstormmod.common.block.LegacyWallBlock;
import com.wdcftgg.witherstormmod.common.block.LegacyTaintedMushroomBlock;
import com.wdcftgg.witherstormmod.common.block.LegacyPottedTaintedMushroomBlock;
import com.wdcftgg.witherstormmod.common.block.BlockWitheredPhlegm;
import net.minecraft.block.BlockPressurePlate;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import com.wdcftgg.witherstormmod.common.item.FormidibombItem;
import com.wdcftgg.witherstormmod.common.item.TaintedCarvedPumpkinItem;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.EnumRarity;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.LinkedHashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class ModBlocks {

    private static final Map<String, Block> BLOCKS = new LinkedHashMap<String, Block>();
    private static final Map<String, Item> BLOCK_ITEMS = new LinkedHashMap<String, Item>();
    private static final String[] BLOCK_NAMES = LegacyRegistryNames.blockNames();

    static {
        for (String name : BLOCK_NAMES) {
            BLOCKS.put(name, createBlock(name));
        }
    }

    private ModBlocks() {
    }

    public static void bootstrap() {
    }

    public static Block get(String name) {
        return BLOCKS.get(name);
    }

    public static String[] getRegisteredNames() {
        return BLOCK_NAMES.clone();
    }

    public static boolean isItemless(String name) {
        return LegacyRegistryNames.isItemlessBlock(name);
    }

    public static Item getItem(String name) {
        return BLOCK_ITEMS.get(name);
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(BLOCKS.values().toArray(new Block[0]));
    }

    @SubscribeEvent
    public static void registerItemBlocks(RegistryEvent.Register<Item> event) {
        for (Block block : BLOCKS.values()) {
            if (isItemless(block)) {
                continue;
            }
            Item item;
            String name = block.getRegistryName().getPath();
            if (block instanceof LegacyDoorBlock) item = new ItemDoor(block);
            else if (block instanceof LegacySlabBlock) item = new LegacySlabItem((LegacySlabBlock) block);
            else if ("tainted_sign".equals(name)) item = new TaintedSignItem(name);
            else if ("tainted_torch".equals(name)) item = new TaintedTorchItem(name);
            else if ("formidibomb".equals(name)) item = new FormidibombItem(block);
            else if (block instanceof LegacyTaintedCarvedPumpkinBlock) item = new TaintedCarvedPumpkinItem(block);
            else item = createRarityBlockItem(block, name);
            item.setRegistryName(block.getRegistryName());
            item.setTranslationKey(block.getTranslationKey().replace("tile.", ""));
            item.setCreativeTab(ModCreativeTabs.MAIN);
            BLOCK_ITEMS.put(name, item);
            event.getRegistry().register(item);
            if (block instanceof LegacyTaintedCarvedPumpkinBlock) {
                ((LegacyTaintedCarvedPumpkinBlock) block).registerDispenserBehavior(item);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels() {
        for (Block block : BLOCKS.values()) {
            if (isItemless(block)) {
                continue;
            }
            Item item = BLOCK_ITEMS.get(block.getRegistryName().getPath());
            if (item == null) {
                throw new IllegalStateException("Missing registered block item for " + block.getRegistryName());
            }
            ModelResourceLocation inventoryModel = new ModelResourceLocation(block.getRegistryName(), "inventory");
            ModelLoader.setCustomModelResourceLocation(item, 0, inventoryModel);
            if (block instanceof LegacyWallBlock) {
                ModelLoader.setCustomModelResourceLocation(item, 1, inventoryModel);
            }
        }
    }

    private static Block createBlock(String name) {
        if ("super_tnt".equals(name)) {
            return new LegacyExplosiveBlock(name, false);
        }
        if ("formidibomb".equals(name)) {
            return new LegacyExplosiveBlock(name, true);
        }
        if ("super_beacon".equals(name)) {
            return new BlockSuperBeacon(name);
        }
        if ("super_support_beacon".equals(name)) {
            return new BlockSuperSupportBeacon(name);
        }
        if ("firework_bundle".equals(name)) {
            return new BlockFireworkBundle(name);
        }
        if ("withered_phlegm_block".equals(name)) {
            return new BlockWitheredPhlegm(name);
        }
        if ("tainted_torch".equals(name)) {
            return new BlockTaintedTorch(name, false);
        }
        if ("tainted_wall_torch".equals(name)) {
            return new BlockTaintedTorch(name, true);
        }
        if ("tainted_sign".equals(name)) {
            return new BlockTaintedStandingSign(name);
        }
        if ("tainted_wall_sign".equals(name)) {
            return new BlockTaintedWallSign(name);
        }
        if ("tainted_dust".equals(name)) {
            return new LegacyTaintedDustBlock(name);
        }
        if ("tainted_dust_block".equals(name)) {
            return new LegacyTaintedDustLampBlock(name);
        }
        if ("tainted_flesh_veins".equals(name)) {
            return new LegacyFleshVeinsBlock(name);
        }
        if ("tainted_flesh_block".equals(name)) {
            return createSimpleBlock(name, Material.CLOTH, 0.6F, 0.6F, SoundType.SLIME, false);
        }
        if ("infected_flesh_block".equals(name)) {
            return createSimpleBlock(name, Material.CLOTH, 0.8F, 0.8F, SoundType.SLIME, false);
        }
        if ("hardened_flesh_block".equals(name)) {
            return createSimpleBlock(name, Material.CLOTH, -1.0F, 3600000.0F, SoundType.SLIME, false);
        }
        if ("tainted_mushroom".equals(name)) {
            return new LegacyTaintedMushroomBlock(name);
        }
        if ("potted_tainted_mushroom".equals(name)) {
            return new LegacyPottedTaintedMushroomBlock(name);
        }
        if ("tainted_pumpkin".equals(name)) {
            return new LegacyTaintedPumpkinBlock(name);
        }
        if ("tainted_carved_pumpkin".equals(name) || "tainted_jack_o_lantern".equals(name)) {
            return new LegacyTaintedCarvedPumpkinBlock(name, "tainted_jack_o_lantern".equals(name));
        }
        if ("tainted_stone".equals(name)) {
            return createSimpleBlock(name, Material.ROCK, 1.5F, 6.0F, SoundType.STONE, true);
        }
        if ("tainted_cobblestone".equals(name)) {
            return createSimpleBlock(name, Material.ROCK, 2.0F, 6.0F, SoundType.STONE, true);
        }
        if ("tainted_dirt".equals(name)) {
            return createSimpleBlock(name, Material.GROUND, 0.5F, 0.5F, SoundType.GROUND, false);
        }
        if ("tainted_sandstone".equals(name) || "tainted_cut_sandstone".equals(name)
                || "tainted_chiseled_sandstone".equals(name) || "tainted_smooth_sandstone".equals(name)) {
            return createSimpleBlock(name, Material.ROCK, 0.8F, 0.8F, SoundType.STONE, true);
        }
        if ("tainted_glass".equals(name)) {
            return createSimpleBlock(name, Material.GLASS, 0.6F, 1200.0F, SoundType.GLASS, false);
        }
        if ("tainted_planks".equals(name)) {
            return createSimpleBlock(name, Material.WOOD, 2.0F, 3.0F, SoundType.WOOD, false);
        }
        if ("tainted_leaves".equals(name)) {
            return createSimpleBlock(name, Material.LEAVES, 0.2F, 0.2F, SoundType.PLANT, false);
        }
        if ("tainted_zombie_sitting".equals(name) || "tainted_zombie_wall".equals(name)
                || "tainted_zombie_lying".equals(name)) {
            return new LegacyTaintedStatueBlock(name,
                    LegacyTaintedStatueBlock.StatueMaterial.TAINTED_ZOMBIE);
        }
        if ("tainted_bone_pile".equals(name) || "tainted_skeleton_wall".equals(name)
                || "tainted_skull_ceiling".equals(name)) {
            return new LegacyTaintedStatueBlock(name,
                    LegacyTaintedStatueBlock.StatueMaterial.TAINTED_BONE);
        }
        if ("tainted_log".equals(name)) {
            return new LegacyStrippableAxisBlock(name, "stripped_tainted_log");
        }
        if ("tainted_wood".equals(name)) {
            return new LegacyStrippableAxisBlock(name, "stripped_tainted_wood");
        }
        if (name.endsWith("_log") || name.endsWith("_wood")) {
            return new LegacyAxisBlock(name);
        }
        if (name.endsWith("_slab")) {
            return new LegacySlabBlock(name, name.equals("tainted_slab") ? Material.WOOD : Material.ROCK);
        }
        if (name.endsWith("_wall")) {
            return new LegacyWallBlock(name, modelSource(name.replace("_wall", "")));
        }
        if ("tainted_glass_pane".equals(name)) {
            return new LegacyPaneBlock(name);
        }
        if (name.endsWith("_stairs")) {
            boolean heavyStone = "tainted_stone_stairs".equals(name) || "tainted_cobblestone_stairs".equals(name);
            return new LegacyStairsBlock(name, modelSource(name).getDefaultState(), heavyStone ? 3.0F : 2.0F,
                    heavyStone ? 6.0F : 3.0F);
        }
        if (name.endsWith("_fence")) {
            return new LegacyFenceBlock(name, Material.WOOD, MapColor.PURPLE);
        }
        if (name.endsWith("_fence_gate")) {
            return new LegacyFenceGateBlock(name);
        }
        if (name.endsWith("_door")) {
            return new LegacyDoorBlock(name);
        }
        if (name.endsWith("_trapdoor")) {
            return new LegacyTrapDoorBlock(name);
        }
        if (name.endsWith("_button")) {
            return new LegacyButtonBlock(name, !name.contains("stone"));
        }
        if (name.endsWith("_pressure_plate")) {
            return new LegacyPressurePlateBlock(name, name.contains("stone") ? Material.ROCK : Material.WOOD, BlockPressurePlate.Sensitivity.EVERYTHING);
        }
        if (name.endsWith("_sand")) {
            return new LegacyFallingBlock(name, Material.SAND);
        }

        Material material = materialFor(name);
        return createSimpleBlock(name, material, 1.5F, 6.0F, SoundType.STONE, false);
    }

    private static Block createSimpleBlock(String name, Material material, float hardness, float resistance,
                                           SoundType soundType, boolean requiresPickaxe) {
        LegacyBlock block = new LegacyBlock(name, material, hardness, resistance, soundType);
        if (material == Material.GLASS) {
            block.setLightOpacity(0);
        }
        if (requiresPickaxe) {
            block.setHarvestLevel("pickaxe", 0);
        }
        return block;
    }

    private static Item createRarityBlockItem(Block block, String name) {
        if ("super_tnt".equals(name)) return new LegacyRarityBlockItem(block, EnumRarity.RARE);
        if ("super_beacon".equals(name)) return new LegacyRarityBlockItem(block, EnumRarity.EPIC);
        if ("super_support_beacon".equals(name)) return new LegacyRarityBlockItem(block, EnumRarity.RARE);
        if ("tainted_dust".equals(name)) return new LegacyRarityBlockItem(block, EnumRarity.UNCOMMON);
        if ("withered_phlegm_block".equals(name)) return new LegacyRarityBlockItem(block, EnumRarity.UNCOMMON);
        if (name.startsWith("tainted_zombie_") || name.startsWith("tainted_skeleton_")
                || "tainted_bone_pile".equals(name) || "tainted_skull_ceiling".equals(name)) {
            return new LegacyRarityBlockItem(block, EnumRarity.UNCOMMON);
        }
        return new ItemBlock(block);
    }

    private static Material materialFor(String name) {
        if (name.contains("glass")) {
            return Material.GLASS;
        }
        if (name.contains("leaves")) {
            return Material.LEAVES;
        }
        if (name.contains("log") || name.contains("wood") || name.contains("planks") || name.contains("sign") || name.contains("torch") || name.contains("mushroom")) {
            return Material.WOOD;
        }
        if (name.contains("flesh") || name.contains("bone") || name.contains("zombie") || name.contains("skeleton") || name.contains("skull") || name.contains("phlegm")) {
            return Material.CLOTH;
        }
        return Material.ROCK;
    }

    private static Block modelSource(String name) {
        String sourceName = switch (name) {
            case "tainted_stairs" -> "tainted_planks";
            default -> name.endsWith("_stairs")
                    ? name.substring(0, name.length() - "_stairs".length())
                    : name;
        };
        Block source = BLOCKS.get(sourceName);
        if (source == null) {
            throw new IllegalStateException("Missing model source " + sourceName + " for " + name);
        }
        return source;
    }

    private static boolean isItemless(Block block) {
        if (block.getRegistryName() == null) {
            return false;
        }
        return LegacyRegistryNames.isItemlessBlock(block.getRegistryName().getPath());
    }
}
