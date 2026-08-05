package com.wdcftgg.witherstormmod.common.advancement;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

public final class LegacyCountTrigger extends LegacyCriterionTrigger<LegacyCountTrigger.Instance> {

    private final String conditionKey;

    public LegacyCountTrigger(ResourceLocation id, String conditionKey) {
        super(id);
        this.conditionKey = conditionKey;
    }

    @Override
    public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        return new Instance(getId(), MinMaxBounds.deserialize(json.get(conditionKey)));
    }

    public void trigger(EntityPlayerMP player, int count) {
        triggerMatching(player, instance -> instance.test(count));
    }

    public static final class Instance extends AbstractCriterionInstance {
        private final MinMaxBounds count;

        private Instance(ResourceLocation id, MinMaxBounds count) {
            super(id);
            this.count = count;
        }

        private boolean test(int testedCount) {
            return count.test(testedCount);
        }
    }
}
