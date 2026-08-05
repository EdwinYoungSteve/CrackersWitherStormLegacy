package com.wdcftgg.witherstormmod.common.item;

import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import net.minecraft.item.ItemSword;
import net.minecraft.item.EnumRarity;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import com.google.common.collect.Multimap;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;

public class LegacySwordItem extends ItemSword {
    private final float portAttackDamage;
    private final float portAttackSpeed;

    public LegacySwordItem(String name, ToolMaterial material) {
        this(name, material, 3.0F, -2.4F);
    }

    public LegacySwordItem(String name, ToolMaterial material, float attackDamage, float attackSpeed) {
        super(material);
        this.portAttackDamage = attackDamage + material.getAttackDamage();
        this.portAttackSpeed = attackSpeed;
        setRegistryName(name);
        setTranslationKey(name);
        setCreativeTab(ModCreativeTabs.MAIN);
    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {
        Multimap<String, AttributeModifier> modifiers = super.getItemAttributeModifiers(slot);
        if (slot == EntityEquipmentSlot.MAINHAND) {
            modifiers.removeAll(SharedMonsterAttributes.ATTACK_DAMAGE.getName());
            modifiers.removeAll(SharedMonsterAttributes.ATTACK_SPEED.getName());
            modifiers.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", portAttackDamage, 0));
            modifiers.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", portAttackSpeed, 0));
        }
        return modifiers;
    }

    @Override
    public EnumRarity getRarity(net.minecraft.item.ItemStack stack) {
        return EnumRarity.EPIC;
    }

    @Override
    public boolean hasCustomEntity(net.minecraft.item.ItemStack stack) {
        return LegacyFireResistantItemEntity.isFireResistant(stack);
    }

    @Override
    public Entity createEntity(World world, Entity location, net.minecraft.item.ItemStack stack) {
        return hasCustomEntity(stack) ? LegacyFireResistantItemEntity.create(world, location, stack) : null;
    }
}
