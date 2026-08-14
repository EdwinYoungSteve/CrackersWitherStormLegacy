package com.wdcftgg.witherstormmod.client.jei;

import mezz.jei.api.IGuiHelper;
import net.minecraft.util.text.TextComponentTranslation;

/** 超级信标合成物品分类。 */
public final class SuperBeaconItemCraftingCategory
        extends AbstractSuperBeaconCategory<SuperBeaconItemCraftingRecipeWrapper> {

    public SuperBeaconItemCraftingCategory(IGuiHelper guiHelper) {
        super(guiHelper, "textures/gui/jei/crafting_icon.png");
    }

    @Override
    public String getUid() {
        return SuperBeaconJeiPlugin.ITEM_CRAFTING_UID;
    }

    @Override
    public String getTitle() {
        return new TextComponentTranslation(
                "witherstormmod.jei.item_craft_super_beacon.title").getFormattedText();
    }
}
