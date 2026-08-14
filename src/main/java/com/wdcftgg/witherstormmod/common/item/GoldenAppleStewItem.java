package com.wdcftgg.witherstormmod.common.item;

import com.wdcftgg.witherstormmod.common.entity.SickenedMobEntity;
import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import com.wdcftgg.witherstormmod.common.taint.WitherSicknessCure;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class GoldenAppleStewItem extends ItemSoup {

    static final int REGENERATION_DURATION = 200;
    static final int ABSORPTION_DURATION = 2600;

    public GoldenAppleStewItem(String name) {
        super(5);
        setRegistryName(name);
        setTranslationKey(name);
        setCreativeTab(ModCreativeTabs.MAIN);
        setAlwaysEdible();
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
        if (world.isRemote) {
            return;
        }
        player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, REGENERATION_DURATION, 0));
        player.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, ABSORPTION_DURATION, 0));
        if (WitherSicknessCure.beginCure(player)) {
            playCureSound(player);
        }
    }

    @Override
    public float getSaturationModifier(ItemStack stack) {
        return 1.0F;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.RARE;
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase target, EnumHand hand) {
        if (target instanceof SickenedMobEntity) {
            SickenedMobEntity sickened = (SickenedMobEntity) target;
            if (sickened.isConverting() || sickened.getOriginalType() == null) {
                return true;
            }
            if (!target.world.isRemote) {
                sickened.startConverting(player.getUniqueID(), 3600 + player.getRNG().nextInt(2401));
                if (!player.capabilities.isCreativeMode) stack.shrink(1);
            }
            return true;
        }

        if (!WitherSicknessCure.isInfected(target)
                || WitherSicknessCure.isBeingCured(target)
                || WitherSicknessCure.isActuallyImmune(target)) {
            return false;
        }
        if (!target.world.isRemote && WitherSicknessCure.beginCure(target)) {
            playCureSound(target);
            if (!player.capabilities.isCreativeMode) stack.shrink(1);
        }
        return true;
    }

    private static void playCureSound(EntityLivingBase entity) {
        entity.world.playSound(null, entity.posX, entity.posY, entity.posZ,
                SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, SoundCategory.NEUTRAL, 1.0F, 1.0F);
    }
}
