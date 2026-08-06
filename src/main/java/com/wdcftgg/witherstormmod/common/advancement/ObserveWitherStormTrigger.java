package com.wdcftgg.witherstormmod.common.advancement;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public final class ObserveWitherStormTrigger
        extends CriterionTrigger<ObserveWitherStormTrigger.Instance> {

    public ObserveWitherStormTrigger(ResourceLocation id) {
        super(id);
    }

    @Override
    public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        return new Instance(getId(), ItemPredicate.deserialize(json.get("item")),
                EntityPredicate.deserialize(json.get("entity")));
    }

    public void trigger(EntityPlayerMP player, ItemStack item, Entity observed) {
        triggerMatching(player, instance -> instance.test(player, item, observed));
    }

    public static final class Instance extends AbstractCriterionInstance {
        private final ItemPredicate item;
        private final EntityPredicate entity;

        private Instance(ResourceLocation id, ItemPredicate item, EntityPredicate entity) {
            super(id);
            this.item = item;
            this.entity = entity;
        }

        private boolean test(EntityPlayerMP player, ItemStack testedItem, Entity observed) {
            return item.test(testedItem) && entity.test(player, observed);
        }
    }
}
