package com.wdcftgg.witherstormmod.common.advancement;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

public final class LegacyEntityTrigger extends LegacyCriterionTrigger<LegacyEntityTrigger.Instance> {

    private final String conditionKey;

    public LegacyEntityTrigger(ResourceLocation id, String conditionKey) {
        super(id);
        this.conditionKey = conditionKey;
    }

    @Override
    public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        return new Instance(getId(), EntityPredicate.deserialize(json.get(conditionKey)));
    }

    public void trigger(EntityPlayerMP player, Entity entity) {
        triggerMatching(player, instance -> instance.test(player, entity));
    }

    public static final class Instance extends AbstractCriterionInstance {
        private final EntityPredicate entity;

        private Instance(ResourceLocation id, EntityPredicate entity) {
            super(id);
            this.entity = entity;
        }

        private boolean test(EntityPlayerMP player, Entity testedEntity) {
            return entity.test(player, testedEntity);
        }
    }
}
