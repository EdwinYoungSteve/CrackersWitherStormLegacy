package com.wdcftgg.witherstormmod.common.advancement;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.wdcftgg.witherstormmod.Tags;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

public final class LinkAmuletTrigger
        extends CriterionTrigger<LinkAmuletTrigger.Instance> {

    public static final ResourceLocation ID = new ResourceLocation(Tags.MOD_ID, "link_amulet");

    public LinkAmuletTrigger() {
        super(ID);
    }

    @Override
    public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        return new Instance(EntityPredicate.deserialize(json.get("linked")),
                MinMaxBounds.deserialize(json.get("total_linked")));
    }

    public void trigger(EntityPlayerMP player, Entity linked, int totalLinked) {
        triggerMatching(player, instance -> instance.test(player, linked, totalLinked));
    }

    public static final class Instance extends AbstractCriterionInstance {
        private final EntityPredicate linked;
        private final MinMaxBounds totalLinked;

        private Instance(EntityPredicate linked, MinMaxBounds totalLinked) {
            super(ID);
            this.linked = linked;
            this.totalLinked = totalLinked;
        }

        private boolean test(EntityPlayerMP player, Entity linkedEntity, int linkedCount) {
            return linked.test(player, linkedEntity) && totalLinked.test(linkedCount);
        }
    }
}
