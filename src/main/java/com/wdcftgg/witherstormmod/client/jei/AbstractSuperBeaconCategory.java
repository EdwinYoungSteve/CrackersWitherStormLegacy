package com.wdcftgg.witherstormmod.client.jei;

import com.wdcftgg.witherstormmod.Tags;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.List;

/** 两个超级信标分类共用的 3x3 输入 + 输出布局。 */
abstract class AbstractSuperBeaconCategory<T extends SuperBeaconRecipeWrapper>
        implements IRecipeCategory<T> {

    static final int INPUT_START_X = 1;
    static final int INPUT_START_Y = 1;
    static final int OUTPUT_X = 63;
    static final int OUTPUT_Y = 19;
    private static final int SLOT_SIZE = 18;
    private static final ResourceLocation SLOT_TEXTURE =
            new ResourceLocation(Tags.MOD_ID, "textures/gui/jei/slot.png");

    private final IDrawable background;
    private final IDrawable icon;

    AbstractSuperBeaconCategory(IGuiHelper guiHelper, String iconPath) {
        this.background = guiHelper.createBlankDrawable(84, 58);
        this.icon = guiHelper.createDrawable(
                new ResourceLocation(Tags.MOD_ID, iconPath), 0, 0, 16, 16);
    }

    @Override
    public String getModName() {
        return Tags.MOD_NAME;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, T recipeWrapper, IIngredients ingredients) {
        IGuiItemStackGroup stacks = recipeLayout.getItemStacks();
        List<List<ItemStack>> inputs = ingredients.getInputs(ItemStack.class);
        List<List<ItemStack>> outputs = ingredients.getOutputs(ItemStack.class);
        for (int index = 0; index < Math.min(inputs.size(), 9); index++) {
            int x = INPUT_START_X + (index % 3) * SLOT_SIZE;
            int y = INPUT_START_Y + (index / 3) * SLOT_SIZE;
            stacks.init(index, true, x, y);
            stacks.set(index, inputs.get(index));
        }
        stacks.init(9, false, OUTPUT_X, OUTPUT_Y);
        if (!outputs.isEmpty() && !outputs.get(0).isEmpty()) {
            stacks.set(9, outputs.get(0));
        }
    }
}
