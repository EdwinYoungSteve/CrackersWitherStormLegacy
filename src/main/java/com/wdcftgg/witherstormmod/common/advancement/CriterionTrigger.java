package com.wdcftgg.witherstormmod.common.advancement;

import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

abstract class CriterionTrigger<T extends AbstractCriterionInstance>
        implements ICriterionTrigger<T> {

    private final ResourceLocation id;
    private final Map<PlayerAdvancements, Listeners<T>> listeners =
            new HashMap<PlayerAdvancements, Listeners<T>>();

    CriterionTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public final ResourceLocation getId() {
        return id;
    }

    @Override
    public final void addListener(PlayerAdvancements advancements,
                                  ICriterionTrigger.Listener<T> listener) {
        Listeners<T> playerListeners = listeners.get(advancements);
        if (playerListeners == null) {
            playerListeners = new Listeners<T>(advancements);
            listeners.put(advancements, playerListeners);
        }
        playerListeners.add(listener);
    }

    @Override
    public final void removeListener(PlayerAdvancements advancements,
                                     ICriterionTrigger.Listener<T> listener) {
        Listeners<T> playerListeners = listeners.get(advancements);
        if (playerListeners == null) return;
        playerListeners.remove(listener);
        if (playerListeners.isEmpty()) listeners.remove(advancements);
    }

    @Override
    public final void removeAllListeners(PlayerAdvancements advancements) {
        listeners.remove(advancements);
    }

    protected final void triggerMatching(EntityPlayerMP player, Predicate<T> predicate) {
        Listeners<T> playerListeners = listeners.get(player.getAdvancements());
        if (playerListeners != null) playerListeners.trigger(predicate);
    }

    private static final class Listeners<T extends AbstractCriterionInstance> {
        private final PlayerAdvancements advancements;
        private final Set<ICriterionTrigger.Listener<T>> listeners =
                new HashSet<ICriterionTrigger.Listener<T>>();

        private Listeners(PlayerAdvancements advancements) {
            this.advancements = advancements;
        }

        private boolean isEmpty() {
            return listeners.isEmpty();
        }

        private void add(ICriterionTrigger.Listener<T> listener) {
            listeners.add(listener);
        }

        private void remove(ICriterionTrigger.Listener<T> listener) {
            listeners.remove(listener);
        }

        private void trigger(Predicate<T> predicate) {
            List<ICriterionTrigger.Listener<T>> matched = null;
            for (ICriterionTrigger.Listener<T> listener : listeners) {
                if (!predicate.test(listener.getCriterionInstance())) continue;
                if (matched == null) matched = new ArrayList<ICriterionTrigger.Listener<T>>();
                matched.add(listener);
            }
            if (matched == null) return;
            for (ICriterionTrigger.Listener<T> listener : matched) {
                listener.grantCriterion(advancements);
            }
        }
    }
}
