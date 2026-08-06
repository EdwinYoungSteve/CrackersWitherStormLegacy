package com.wdcftgg.witherstormmod.common.advancement;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

public final class CuredSickenedMobTrigger
        extends CriterionTrigger<CuredSickenedMobTrigger.Instance> {

    public CuredSickenedMobTrigger(ResourceLocation id) {
        super(id);
    }

    @Override
    public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        JsonElement conversion = json.has("converison")
                ? json.get("converison") : json.get("conversion");
        return new Instance(getId(), EntityPredicate.deserialize(json.get("sickened")),
                EntityPredicate.deserialize(conversion));
    }

    public void trigger(EntityPlayerMP player, EntityLivingBase sickened,
                        EntityLivingBase conversion) {
        triggerMatching(player, instance -> instance.test(player, sickened, conversion));
    }

    public static final class Instance extends AbstractCriterionInstance {
        private final EntityPredicate sickened;
        private final EntityPredicate conversion;

        private Instance(ResourceLocation id, EntityPredicate sickened,
                         EntityPredicate conversion) {
            super(id);
            this.sickened = sickened;
            this.conversion = conversion;
        }

        private boolean test(EntityPlayerMP player, EntityLivingBase sickenedEntity,
                             EntityLivingBase convertedEntity) {
            return sickened.test(player, sickenedEntity)
                    && conversion.test(player, convertedEntity);
        }
    }
}
