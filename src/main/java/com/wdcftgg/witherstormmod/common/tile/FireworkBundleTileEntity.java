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

import java.util.Random;

public class FireworkBundleTileEntity extends TileEntity implements ITickable {

    private final Random random = new Random();
    private int fuse;
    private int launchDuration;

    public void beginFuse() {
        if (fuse == 0 && launchDuration == 0) {
            fuse = 100;
            if (world != null) world.playSound(null, pos, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
            if (world != null) world.checkLight(pos);
            markDirty();
        }
    }

    public boolean isActivated() {
        return fuse > 0 || launchDuration > 0;
    }

    @Override
    public void update() {
        if (world == null || world.isRemote) return;
        if (fuse > 0) {
            ((WorldServer) world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
                    pos.getX(), pos.getY(), pos.getZ(),
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
            fuse--;
            if (fuse == 0) launchDuration = 500;
        }
        if (launchDuration > 0) {
            launchDuration--;
            if (launchDuration == 0) {
                world.setBlockToAir(pos);
            } else if (random.nextInt(3) == 0) {
                ItemStack firework = createRandomFirework();
                EntityFireworkRocket rocket = new EntityFireworkRocket(world,
                        pos.getX() + random.nextDouble() - 0.5D, pos.getY() + 0.6D,
                        pos.getZ() + random.nextDouble() - 0.5D, firework);
                rocket.motionX += (random.nextDouble() - 0.5D) * 0.05D;
                rocket.motionZ += (random.nextDouble() - 0.5D) * 0.05D;
                world.spawnEntity(rocket);
            }
        }
        if (fuse > 0 || launchDuration > 0) markDirty();
    }

    private ItemStack createRandomFirework() {
        ItemStack stack = new ItemStack(Items.FIREWORKS);
        NBTTagCompound explosion = new NBTTagCompound();
        explosion.setBoolean("Flicker", random.nextBoolean());
        explosion.setBoolean("Trail", random.nextBoolean());
        int[] colors = new int[random.nextInt(5) + 1];
        int[] palette = {1973019, 11743532, 3887386, 5320730, 2437522, 8073150, 2651799, 11250603,
                4408131, 14188952, 4312372, 14602026, 6719955, 12801229, 15435844, 15790320};
        for (int i = 0; i < colors.length; i++) colors[i] = palette[random.nextInt(palette.length)];
        explosion.setIntArray("Colors", colors);
        explosion.setByte("Type", (byte) MathHelper.clamp(random.nextInt(5), 0, 4));
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
