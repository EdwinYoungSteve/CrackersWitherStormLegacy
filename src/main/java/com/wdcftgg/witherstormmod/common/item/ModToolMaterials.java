package com.wdcftgg.witherstormmod.common.item;

import net.minecraft.item.Item;
import net.minecraftforge.common.util.EnumHelper;

public final class ModToolMaterials {
    public static final Item.ToolMaterial WOOD_COMMAND_BLOCK = material("WOOD_CMD", 5, 0, 3.5F, 3.75F, 32);
    public static final Item.ToolMaterial STONE_COMMAND_BLOCK = material("STONE_CMD", 3, 0, 16.0F, 5.25F, 5);
    public static final Item.ToolMaterial IRON_COMMAND_BLOCK = material("IRON_CMD", 5, 0, 8.0F, 6.5F, 10);
    public static final Item.ToolMaterial GOLD_COMMAND_BLOCK = material("GOLD_CMD", 1, 0, 32.0F, 2.5F, 64);
    public static final Item.ToolMaterial COMMAND_BLOCK = material("COMMAND_BLOCK", 5, 0, 14.0F, 6.0F, 15);
    public static final Item.ToolMaterial EYE_OF_THE_STORM = material("EYE_OF_THE_STORM", 5, 0, 12.0F, 7.5F, 25);
    public static final Item.ToolMaterial FORMIDI_BLADE = material("FORMIDI_BLADE", 5, 0, 12.0F, 6.0F, 15);

    private ModToolMaterials() { }

    private static Item.ToolMaterial material(String name, int level, int uses, float speed, float damage, int enchantability) {
        return EnumHelper.addToolMaterial(name, level, uses, speed, damage, enchantability);
    }
}
