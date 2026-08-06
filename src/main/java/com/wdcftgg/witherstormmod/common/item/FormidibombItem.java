package com.wdcftgg.witherstormmod.common.item;

import com.wdcftgg.witherstormmod.common.entity.PowerfulExplosiveEntity;
import com.wdcftgg.witherstormmod.common.tile.FormidibombTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FormidibombItem extends RarityBlockItem {
    private static final int DEFAULT_FUSE = 12000;
    private static final int DROP_THRESHOLD = DEFAULT_FUSE / 4;

    public FormidibombItem(Block block) {
        super(block, EnumRarity.EPIC);
        setMaxStackSize(1);
    }

    @Override
    public void onCreated(ItemStack stack, World world, EntityPlayer player) {
        setFuse(stack, DEFAULT_FUSE);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        tickFuse(stack, world, entity, entity.getPosition());
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem entityItem) {
        tickFuse(entityItem.getItem(), entityItem.world, null, entityItem.getPosition());
        return false;
    }

    private void tickFuse(ItemStack stack, World world, Entity holder, BlockPos position) {
        if (world.isRemote) return;
        ensureFuse(stack);
        int fuse = getFuse(stack) - 1;
        stack.getOrCreateSubCompound("WitherStormMod").setInteger("Fuse", fuse);
        int startFuse = getStartFuse(stack);
        if (fuse <= 0) {
            spawnBomb(stack, world, holder, position, 1);
        } else if (holder instanceof EntityLivingBase && fuse <= Math.max(1, startFuse / 4)) {
            spawnBomb(stack, world, holder, position, fuse);
        }
    }

    private void spawnBomb(ItemStack stack, World world, Entity holder, BlockPos position, int fuse) {
        EntityLivingBase owner = holder instanceof EntityLivingBase ? (EntityLivingBase) holder : null;
        PowerfulExplosiveEntity.FormidibombEntity bomb = new PowerfulExplosiveEntity.FormidibombEntity(world,
                position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D, owner);
        bomb.setFuse(fuse);
        bomb.setStartFuse(getStartFuse(stack));
        if (world.spawnEntity(bomb)) {
            stack.shrink(1);
            world.playSound(null, bomb.posX, bomb.posY, bomb.posZ, SoundEvents.ENTITY_TNT_PRIMED,
                    SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos,
                                net.minecraft.util.EnumFacing side, float hitX, float hitY, float hitZ,
                                IBlockState newState) {
        if (!super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState)) return false;
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof FormidibombTileEntity) {
            ensureFuse(stack);
            ((FormidibombTileEntity) tile).setFuse(getFuse(stack), getStartFuse(stack), player);
        }
        return true;
    }

    private static void ensureFuse(ItemStack stack) {
        if (getStartFuse(stack) <= 0) setFuse(stack, DEFAULT_FUSE);
    }

    public static int getFuse(ItemStack stack) {
        return stack.getSubCompound("WitherStormMod") == null ? 0 : stack.getSubCompound("WitherStormMod").getInteger("Fuse");
    }

    public static int getStartFuse(ItemStack stack) {
        return stack.getSubCompound("WitherStormMod") == null ? 0 : stack.getSubCompound("WitherStormMod").getInteger("StartFuse");
    }

    public static void setFuse(ItemStack stack, int fuse) {
        stack.getOrCreateSubCompound("WitherStormMod").setInteger("Fuse", fuse);
        stack.getOrCreateSubCompound("WitherStormMod").setInteger("StartFuse", fuse);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return getFuse(stack) > 0 && getFuse(stack) < getStartFuse(stack);
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1.0D - getFuse(stack) / (double) Math.max(1, getStartFuse(stack));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }
}
