package com.wdcftgg.witherstormmod.client.jei;

import mezz.jei.api.IGuiHelper;
import net.minecraft.util.text.TextComponentTranslation;

/** 超级信标召唤实体分类。 */
public final class SuperBeaconSummoningCategory
        extends AbstractSuperBeaconCategory<SuperBeaconSummoningRecipeWrapper> {

    public SuperBeaconSummoningCategory(IGuiHelper guiHelper) {
        super(guiHelper, "textures/gui/jei/summoning_icon.png");
    }

    @Override
    public String getUid() {
        return SuperBeaconJeiPlugin.SUMMONING_UID;
    }

    @Override
    public String getTitle() {
        return new TextComponentTranslation(
                "witherstormmod.jei.resummoning_super_beacon.title").getFormattedText();
    }
}
