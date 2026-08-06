package com.wdcftgg.witherstormmod.common.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class FireResistantItemEntity extends EntityItem {

    public FireResistantItemEntity(World world, Entity location, ItemStack stack) {
        super(world, location.posX, location.posY, location.posZ, stack.copy());
        motionX = location.motionX;
        motionY = location.motionY;
        motionZ = location.motionZ;
        isImmuneToFire = true;
    }

    public static boolean isFireResistant(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !isLegacyItemImplementation(stack.getItem())) {
            return false;
        }
        Item item = stack.getItem();
        ResourceLocation registryName = item.getRegistryName();
        if (registryName == null && item instanceof ItemBlock) {
            registryName = ((ItemBlock) item).getBlock().getRegistryName();
        }
        if (registryName == null) return false;
        String name = registryName.getPath();
        return "amulet".equals(name)
                || "command_block_book".equals(name)
                || "withered_nether_star".equals(name)
                || "formidibomb".equals(name)
                || "super_beacon".equals(name)
                || "super_support_beacon".equals(name)
                || name.contains("command_block_");
    }

    private static boolean isLegacyItemImplementation(Item item) {
        return item instanceof AmuletItem
                || item instanceof SimpleItem
                || item instanceof RarityBlockItem
                || item instanceof CommandBlockSwordItem
                || item instanceof CommandBlockPickaxeItem
                || item instanceof CommandBlockAxeItem
                || item instanceof CommandBlockShovelItem
                || item instanceof CommandBlockHoeItem;
    }

    public static Entity create(World world, Entity location, ItemStack stack) {
        return new FireResistantItemEntity(world, location, stack);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return source.isFireDamage() ? false : super.attackEntityFrom(source, amount);
    }
}
