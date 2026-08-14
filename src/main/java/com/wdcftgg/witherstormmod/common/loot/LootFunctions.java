package com.wdcftgg.witherstormmod.common.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.wdcftgg.witherstormmod.Tags;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;

import java.util.Collections;
import java.util.Random;

public final class LootFunctions {

    private static boolean registered;

    private LootFunctions() {
    }

    public static void register() {
        if (registered) return;
        LootFunctionManager.registerFunction(new SetStewEffect.Serializer());
        registered = true;
    }

    public static final class SetStewEffect extends LootFunction {
        private static final EffectRange[] EFFECTS = {
                new EffectRange(MobEffects.RESISTANCE, 4, 10),
                new EffectRange(MobEffects.LUCK, 4, 16),
                new EffectRange(MobEffects.REGENERATION, 5, 15),
                new EffectRange(MobEffects.STRENGTH, 12, 24),
                new EffectRange(MobEffects.WITHER, 5, 12),
                new EffectRange(MobEffects.SATURATION, 12, 24)
        };

        private SetStewEffect(LootCondition[] conditions) {
            super(conditions);
        }

        @Override
        public ItemStack apply(ItemStack stack, Random random, LootContext context) {
            EffectRange selected = EFFECTS[random.nextInt(EFFECTS.length)];
            int seconds = selected.minimumSeconds
                    + random.nextInt(selected.maximumSeconds - selected.minimumSeconds + 1);
            PotionUtils.appendEffects(stack, Collections.singletonList(
                    new PotionEffect(selected.potion, seconds * 20)));
            return stack;
        }

        private static final class EffectRange {
            private final Potion potion;
            private final int minimumSeconds;
            private final int maximumSeconds;

            private EffectRange(Potion potion, int minimumSeconds, int maximumSeconds) {
                this.potion = potion;
                this.minimumSeconds = minimumSeconds;
                this.maximumSeconds = maximumSeconds;
            }
        }

        public static final class Serializer extends LootFunction.Serializer<SetStewEffect> {
            private Serializer() {
                super(new ResourceLocation(Tags.MOD_ID, "set_stew_effect"), SetStewEffect.class);
            }

            @Override
            public void serialize(JsonObject json, SetStewEffect value,
                                  JsonSerializationContext context) {
            }

            @Override
            public SetStewEffect deserialize(JsonObject json, JsonDeserializationContext context,
                                              LootCondition[] conditions) {
                return new SetStewEffect(conditions);
            }
        }
    }
}
