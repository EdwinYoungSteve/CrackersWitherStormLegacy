package com.wdcftgg.witherstormmod.common.item;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LegacySpawnEggItem extends Item {

    private final ResourceLocation entityName;
    private final int primaryColor;
    private final int secondaryColor;

    public LegacySpawnEggItem(String itemName, String entityName, int primaryColor, int secondaryColor) {
        this.entityName = new ResourceLocation(Tags.MOD_ID, entityName);
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        setRegistryName(itemName);
        setTranslationKey(itemName);
        setCreativeTab(ModCreativeTabs.MAIN);
    }

    public int getColor(int tintIndex) {
        return tintIndex == 0 ? primaryColor : tintIndex == 1 ? secondaryColor : 0xFFFFFF;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos position, EnumHand hand,
                                      EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        }
        BlockPos spawnPosition = position.offset(facing);
        Entity entity = EntityList.createEntityByIDFromName(entityName, world);
        if (entity == null) {
            return EnumActionResult.FAIL;
        }
        entity.setLocationAndAngles(spawnPosition.getX() + 0.5D, spawnPosition.getY(), spawnPosition.getZ() + 0.5D,
                world.rand.nextFloat() * 360.0F, 0.0F);
        if (entity instanceof EntityLiving) {
            ((EntityLiving) entity).onInitialSpawn(world.getDifficultyForLocation(spawnPosition), null);
        }
        world.spawnEntity(entity);
        ItemStack stack = player.getHeldItem(hand);
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
        return EnumActionResult.SUCCESS;
    }
}
