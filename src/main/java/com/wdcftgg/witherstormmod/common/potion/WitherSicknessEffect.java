package com.wdcftgg.witherstormmod.common.potion;

import com.wdcftgg.witherstormmod.common.capability.WitherSicknessCapability;
import com.wdcftgg.witherstormmod.common.capability.WitherSicknessTracker;
import com.wdcftgg.witherstormmod.common.init.ModDamageSources;
import com.wdcftgg.witherstormmod.common.resource.UpstreamEntityTags;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class WitherSicknessEffect extends Potion {
    private static final UUID MAX_HEALTH_MODIFIER =
            UUID.fromString("08BA7AB9-0056-4B4F-AA13-7103B4B9D127");
    private static final String PENDING_MAX_HEALTH_AMOUNT =
            "WitherSicknessPendingMaxHealthAmount";

    public static final DamageSource DAMAGE_SOURCE = ModDamageSources.witherSickness();

    public WitherSicknessEffect() {
        super(true, 0x582E67);
        setRegistryName("wither_sickness");
        setPotionName("effect.witherstormmod.wither_sickness");
        registerPotionAttributeModifier(SharedMonsterAttributes.MAX_HEALTH,
                MAX_HEALTH_MODIFIER.toString(), 0.0D, 0);
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        int interval = 7200 >> amplifier;
        return interval > 0 ? duration % interval == 0 : true;
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        if (entity.world.isRemote) return;
        WitherSicknessTracker tracker = WitherSicknessCapability.get(entity);
        if (tracker != null && tracker.isBeingCured()) return;
        float damage = UpstreamEntityTags.contains(UpstreamEntityTags.HIGH_IMMUNITY, entity)
                ? 1.0F : 2.0F;
        int protection = EnchantmentHelper.getEnchantmentModifierDamage(
                entity.getArmorInventoryList(), DAMAGE_SOURCE);
        if (protection > 0) damage = CombatRules.getDamageAfterMagicAbsorb(damage, protection);
        entity.attackEntityFrom(DAMAGE_SOURCE, damage);
        reduceMaximumHealth(entity, amplifier);
    }

    private void reduceMaximumHealth(EntityLivingBase entity, int amplifier) {
        IAttributeInstance maximumHealth = entity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        AttributeModifier current = maximumHealth.getModifier(MAX_HEALTH_MODIFIER);
        double currentAmount = current == null ? 0.0D : current.getAmount();
        double minimumAmount = -maximumHealth.getBaseValue() + 1.0D;
        double nextAmount = Math.max(minimumAmount, currentAmount - 1.0D);
        if (current != null) maximumHealth.removeModifier(current);
        maximumHealth.applyModifier(new AttributeModifier(MAX_HEALTH_MODIFIER,
                getName() + " " + amplifier, nextAmount, 0));
        if (entity.getHealth() > entity.getMaxHealth()) entity.setHealth(entity.getMaxHealth());
    }

    @Override
    public void removeAttributesModifiersFromEntity(EntityLivingBase entity,
                                                     AbstractAttributeMap attributes, int amplifier) {
        IAttributeInstance maximumHealth = attributes.getAttributeInstance(
                SharedMonsterAttributes.MAX_HEALTH);
        AttributeModifier current = maximumHealth == null
                ? null : maximumHealth.getModifier(MAX_HEALTH_MODIFIER);
        if (entity.isPotionActive(this) && current != null) {
            entity.getEntityData().setDouble(PENDING_MAX_HEALTH_AMOUNT, current.getAmount());
        } else {
            entity.getEntityData().removeTag(PENDING_MAX_HEALTH_AMOUNT);
        }
        super.removeAttributesModifiersFromEntity(entity, attributes, amplifier);
    }

    @Override
    public void applyAttributesModifiersToEntity(EntityLivingBase entity,
                                                  AbstractAttributeMap attributes, int amplifier) {
        boolean restoreAmount = entity.getEntityData().hasKey(PENDING_MAX_HEALTH_AMOUNT);
        double amount = entity.getEntityData().getDouble(PENDING_MAX_HEALTH_AMOUNT);
        super.applyAttributesModifiersToEntity(entity, attributes, amplifier);
        if (restoreAmount) {
            IAttributeInstance maximumHealth = attributes.getAttributeInstance(
                    SharedMonsterAttributes.MAX_HEALTH);
            AttributeModifier current = maximumHealth == null
                    ? null : maximumHealth.getModifier(MAX_HEALTH_MODIFIER);
            if (maximumHealth != null) {
                if (current != null) maximumHealth.removeModifier(current);
                maximumHealth.applyModifier(new AttributeModifier(MAX_HEALTH_MODIFIER,
                        getName() + " " + amplifier, amount, 0));
            }
        }
        entity.getEntityData().removeTag(PENDING_MAX_HEALTH_AMOUNT);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return Collections.emptyList();
    }
}
