package com.wdcftgg.witherstormmod.common.item;

import com.wdcftgg.witherstormmod.common.entity.WitherStormEntity;
import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class FormidiBladeItem extends ItemSword {

    public FormidiBladeItem(String name) {
        super(ModToolMaterials.FORMIDI_BLADE);
        setRegistryName(name);
        setTranslationKey(name);
        setCreativeTab(ModCreativeTabs.MAIN);
        setMaxDamage(3122);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.EPIC;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        player.setActiveHand(hand);
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 40;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase entity, int timeLeft) {
        int chargeTime = getMaxItemUseDuration(stack) - timeLeft;
        if (chargeTime >= 30) {
            stack.getOrCreateSubCompound("WitherStormMod").setBoolean("Charged", true);
        }
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        if (stack.hasTagCompound() && stack.getSubCompound("WitherStormMod") != null
                && stack.getSubCompound("WitherStormMod").getBoolean("Charged")) {
            if (!attacker.world.isRemote) {
                float explosionStrength = target instanceof WitherStormEntity ? 8.0F : 4.0F;
                attacker.world.newExplosion(attacker, target.posX, target.posY + target.height * 0.5D, target.posZ,
                        explosionStrength, false, false);
            }
            stack.getSubCompound("WitherStormMod").setBoolean("Charged", false);
            stack.damageItem(16, attacker);
        }
        return super.hitEntity(stack, target, attacker);
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return stack.hasTagCompound() && stack.getSubCompound("WitherStormMod") != null
                && stack.getSubCompound("WitherStormMod").getBoolean("Charged");
    }
}
