package com.wdcftgg.witherstormmod.common.tile;

import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;

public class TileEntityFireworkBundle extends TileEntity implements ITickable {

    private int fuse;
    private int launchDuration;

    public void beginFuse() {
        if (fuse == 0 && launchDuration == 0) {
            fuse = 100;
            if (world != null) world.playSound(null, pos, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
            markDirty();
        }
    }

    @Override
    public void update() {
        if (world == null || world.isRemote) return;
        if (fuse > 0) {
            ((WorldServer) world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
                    pos.getX() + 0.5D, pos.getY() + 0.8D, pos.getZ() + 0.5D,
                    1, 0.05D, 0.05D, 0.05D, 0.0D);
            fuse--;
            if (fuse == 0) launchDuration = 500;
        }
        if (launchDuration > 0) {
            launchDuration--;
            if (launchDuration == 0) {
                world.setBlockToAir(pos);
            } else if (world.rand.nextInt(3) == 0) {
                ItemStack firework = createRandomFirework();
                EntityFireworkRocket rocket = new EntityFireworkRocket(world,
                        pos.getX() + world.rand.nextDouble(), pos.getY() + 0.6D,
                        pos.getZ() + world.rand.nextDouble(), firework);
                rocket.motionX += (world.rand.nextDouble() - 0.5D) * 0.05D;
                rocket.motionZ += (world.rand.nextDouble() - 0.5D) * 0.05D;
                world.spawnEntity(rocket);
            }
        }
        if (fuse > 0 || launchDuration > 0) markDirty();
    }

    private ItemStack createRandomFirework() {
        ItemStack stack = new ItemStack(Items.FIREWORKS);
        NBTTagCompound explosion = new NBTTagCompound();
        explosion.setBoolean("Flicker", world.rand.nextBoolean());
        explosion.setBoolean("Trail", world.rand.nextBoolean());
        int[] colors = new int[world.rand.nextInt(5) + 1];
        int[] palette = {1973019, 11743532, 3887386, 5320730, 2437522, 8073150, 2651799, 11250603,
                4408131, 14188952, 4312372, 14602026, 6719955, 12801229, 15435844, 15790320};
        for (int i = 0; i < colors.length; i++) colors[i] = palette[world.rand.nextInt(palette.length)];
        explosion.setIntArray("Colors", colors);
        explosion.setByte("Type", (byte) MathHelper.clamp(world.rand.nextInt(5), 0, 4));
        NBTTagList explosions = new NBTTagList();
        explosions.appendTag(explosion);
        NBTTagCompound fireworks = new NBTTagCompound();
        fireworks.setByte("Flight", (byte) 2);
        fireworks.setTag("Explosions", explosions);
        stack.setTagInfo("Fireworks", fireworks);
        return stack;
    }

    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag) { super.writeToNBT(tag); tag.setInteger("Fuse", fuse); tag.setInteger("LaunchDuration", launchDuration); return tag; }
    @Override public void readFromNBT(NBTTagCompound tag) { super.readFromNBT(tag); fuse = tag.getInteger("Fuse"); launchDuration = tag.getInteger("LaunchDuration"); }
}
