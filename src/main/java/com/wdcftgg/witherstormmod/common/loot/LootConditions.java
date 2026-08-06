package com.wdcftgg.witherstormmod.common.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;

import java.util.Random;

public final class LootConditions {

    private static boolean registered;

    private LootConditions() {
    }

    public static void register() {
        if (!registered) {
            LootConditionManager.registerCondition(new SkeletonKiller.Serializer());
            registered = true;
        }
    }

    public static final class SkeletonKiller implements LootCondition {

        @Override
        public boolean testCondition(Random random, LootContext context) {
            Entity killer = context.getKiller();
            return killer instanceof AbstractSkeleton;
        }

        public static final class Serializer extends LootCondition.Serializer<SkeletonKiller> {

            public Serializer() {
                super(new ResourceLocation("witherstormmod", "skeleton_killer"), SkeletonKiller.class);
            }

            @Override
            public void serialize(JsonObject json, SkeletonKiller value, JsonSerializationContext context) {
            }

            @Override
            public SkeletonKiller deserialize(JsonObject json, JsonDeserializationContext context) {
                return new SkeletonKiller();
            }
        }
    }
}
