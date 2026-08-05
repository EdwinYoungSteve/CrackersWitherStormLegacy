package com.wdcftgg.witherstormmod.common.item;

import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LegacyFoodItem extends ItemFood {

    private final EnumRarity rarity;
    private final List<FoodEffect> effects;

    public LegacyFoodItem(String name, int amount, float saturation, boolean wolfFood,
                          EnumRarity rarity, FoodEffect... effects) {
        super(amount, saturation, wolfFood);
        this.rarity = rarity;
        this.effects = Collections.unmodifiableList(Arrays.asList(effects.clone()));
        setRegistryName(name);
        setTranslationKey(name);
        setCreativeTab(ModCreativeTabs.MAIN);
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
        if (world.isRemote) {
            return;
        }
        for (FoodEffect effect : effects) {
            if (world.rand.nextFloat() < effect.probability) {
                player.addPotionEffect(effect.createPotionEffect());
            }
        }
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return rarity;
    }

    List<FoodEffect> getFoodEffects() {
        return effects;
    }

    public static FoodEffect effect(Potion potion, int duration, int amplifier, float probability) {
        return new FoodEffect(potion, duration, amplifier, probability);
    }

    public static final class FoodEffect {
        private final Potion potion;
        private final int duration;
        private final int amplifier;
        private final float probability;

        private FoodEffect(Potion potion, int duration, int amplifier, float probability) {
            if (potion == null) {
                throw new IllegalArgumentException("potion cannot be null");
            }
            if (duration <= 0) {
                throw new IllegalArgumentException("duration must be positive");
            }
            if (amplifier < 0) {
                throw new IllegalArgumentException("amplifier cannot be negative");
            }
            if (probability < 0.0F || probability > 1.0F) {
                throw new IllegalArgumentException("probability must be between zero and one");
            }
            this.potion = potion;
            this.duration = duration;
            this.amplifier = amplifier;
            this.probability = probability;
        }

        public Potion getPotion() {
            return potion;
        }

        public int getDuration() {
            return duration;
        }

        public int getAmplifier() {
            return amplifier;
        }

        public float getProbability() {
            return probability;
        }

        private PotionEffect createPotionEffect() {
            return new PotionEffect(potion, duration, amplifier);
        }
    }
}
