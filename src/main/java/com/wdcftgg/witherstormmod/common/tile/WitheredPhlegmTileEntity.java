package com.wdcftgg.witherstormmod.common.tile;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.common.inventory.WitheredPhlegmContainer;
import com.wdcftgg.witherstormmod.common.block.WitheredPhlegmBlock;
import com.wdcftgg.witherstormmod.common.init.ModSounds;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.List;

public class WitheredPhlegmTileEntity extends TileEntityLockableLoot implements ITickable {
    public static final int CONTAINER_SIZE = 25;

    private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    private int storedExperience;
    private int playersUsing;
    private int openerCheckTicks;

    @Override
    public int getSizeInventory() {
        return CONTAINER_SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    public NonNullList<ItemStack> getVisibleItems() {
        return items;
    }

    @Override
    public String getName() {
        return hasCustomName() ? customName : "container.witherstormmod.phlegm_block";
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return id == 0 ? storedExperience : 0;
    }

    @Override
    public void setField(int id, int value) {
        if (id == 0) storedExperience = value;
    }

    @Override
    public int getFieldCount() {
        return 1;
    }

    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer player) {
        return new WitheredPhlegmContainer(playerInventory, this);
    }

    @Override
    public String getGuiID() {
        return Tags.MOD_ID + ":withered_phlegm";
    }

    @Override
    public void update() {
        if (world == null || world.isRemote) return;
        if (++openerCheckTicks >= 5) {
            openerCheckTicks = 0;
            if (playersUsing > 0) recheckOpen();
        }

        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof WitheredPhlegmBlock && state.getValue(WitheredPhlegmBlock.POWERED)) return;
        if (allSlotsOccupied()) return;

        AxisAlignedBB blockBounds = new AxisAlignedBB(pos, pos.add(1, 1, 1));
        List<EntityItem> entities = world.getEntitiesWithinAABB(EntityItem.class, blockBounds.grow(8.0D));
        Vec3d target = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
        for (EntityItem entity : entities) {
            double distance = entity.getPositionVector().distanceTo(target);
            Vec3d pull = target.subtract(entity.getPositionVector()).normalize()
                    .scale(Math.max(1.0D - distance / 8.0D, 0.1D));
            Vec3d motion = new Vec3d(entity.motionX * 0.8D, entity.motionY, entity.motionZ * 0.8D)
                    .add(pull.x, pull.y * 0.2D, pull.z);
            entity.motionX = motion.x;
            entity.motionY = motion.y;
            entity.motionZ = motion.z;
            entity.velocityChanged = true;

            if (entity.getEntityBoundingBox().intersects(blockBounds.grow(0.5D)) && insertEntityCompletely(entity)) {
                world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
        }
    }

    private boolean allSlotsOccupied() {
        for (ItemStack stack : items) {
            if (stack.isEmpty()) return false;
        }
        return true;
    }

    private boolean insertEntityCompletely(EntityItem entity) {
        ItemStack original = entity.getItem();
        if (original.isEmpty()) return false;
        ItemStack remainder = insertStack(original.copy());
        if (remainder.isEmpty()) {
            entity.setDead();
            return true;
        }
        if (remainder.getCount() != original.getCount()) entity.setItem(remainder);
        return false;
    }

    private ItemStack insertStack(ItemStack stack) {
        boolean changed = false;
        for (int slot = 0; slot < items.size() && !stack.isEmpty(); slot++) {
            ItemStack existing = items.get(slot);
            int limit = Math.min(getInventoryStackLimit(), stack.getMaxStackSize());
            if (existing.isEmpty()) {
                int moved = Math.min(limit, stack.getCount());
                ItemStack inserted = stack.copy();
                inserted.setCount(moved);
                items.set(slot, inserted);
                stack.shrink(moved);
                changed = true;
            } else if (canMerge(existing, stack)) {
                int available = Math.min(getInventoryStackLimit(), existing.getMaxStackSize()) - existing.getCount();
                if (available > 0) {
                    int moved = Math.min(available, stack.getCount());
                    existing.grow(moved);
                    stack.shrink(moved);
                    changed = true;
                }
            }
        }
        if (changed) markDirty();
        return stack;
    }

    private static boolean canMerge(ItemStack first, ItemStack second) {
        return ItemStack.areItemsEqual(first, second) && ItemStack.areItemStackTagsEqual(first, second);
    }

    @Override
    public void openInventory(EntityPlayer player) {
        if (world == null || world.isRemote || player.isSpectator()) return;
        int previous = playersUsing++;
        if (previous == 0) playSound("withered_phlegm_block_open");
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        if (world == null || world.isRemote || player.isSpectator()) return;
        int previous = playersUsing;
        playersUsing = Math.max(0, playersUsing - 1);
        if (previous > 0 && playersUsing == 0) playSound("withered_phlegm_block_close");
    }

    public void recheckOpen() {
        if (world == null || world.isRemote) return;
        int actual = 0;
        for (EntityPlayer player : world.playerEntities) {
            if (!player.isSpectator() && player.openContainer instanceof WitheredPhlegmContainer
                    && ((WitheredPhlegmContainer) player.openContainer).getPhlegmInventory() == this) {
                actual++;
            }
        }
        int previous = playersUsing;
        playersUsing = actual;
        if (previous == 0 && actual > 0) playSound("withered_phlegm_block_open");
        else if (previous > 0 && actual == 0) playSound("withered_phlegm_block_close");
    }

    private void playSound(String name) {
        if (world != null && ModSounds.get(name) != null) {
            world.playSound(null, pos, ModSounds.get(name), SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
        if (!checkLootAndRead(compound)) ItemStackHelper.loadAllItems(compound, items);
        if (compound.hasKey("CustomName", 8)) customName = compound.getString("CustomName");
        storedExperience = compound.getInteger("StoredXp");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (!checkLootAndWrite(compound)) ItemStackHelper.saveAllItems(compound, items);
        if (hasCustomName()) compound.setString("CustomName", customName);
        compound.setInteger("StoredXp", storedExperience);
        return compound;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound compound = new NBTTagCompound();
        ItemStackHelper.saveAllItems(compound, items);
        if (hasCustomName()) compound.setString("CustomName", customName);
        return compound;
    }

    @Override
    public void onDataPacket(NetworkManager network, SPacketUpdateTileEntity packet) {
        readClientData(packet.getNbtCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        readClientData(tag);
    }

    private void readClientData(NBTTagCompound tag) {
        items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(tag, items);
        customName = tag.hasKey("CustomName", 8) ? tag.getString("CustomName") : null;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (world != null) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    public int getStoredExperience() {
        return storedExperience;
    }

    public void setStoredExperience(int storedExperience) {
        this.storedExperience = Math.max(0, storedExperience);
        markDirty();
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return 32.0D * 32.0D;
    }
}
