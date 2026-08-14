package com.wdcftgg.witherstormmod.client;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.WitherStormMod;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Collections;

/** 将上游末影珍珠装填外观接入 Future MC 的十字弩模型。 */
final class CrossbowCompatibility {
    private static final ResourceLocation CROSSBOW_ID = new ResourceLocation("futuremc", "crossbow");
    private static final ResourceLocation ENDER_PEARL_PROPERTY =
            new ResourceLocation(Tags.MOD_ID, "ender_pearl");
    private static final ResourceLocation ENDER_PEARL_MODEL =
            new ResourceLocation(Tags.MOD_ID, "crossbow_ender_pearl");
    private static final ModelResourceLocation CROSSBOW_MODEL =
            new ModelResourceLocation(CROSSBOW_ID, "inventory");
    private static final ModelResourceLocation BAKED_ENDER_PEARL_MODEL =
            new ModelResourceLocation(ENDER_PEARL_MODEL, "inventory");

    private CrossbowCompatibility() {
    }

    static void registerModels() {
        Item crossbow = ForgeRegistries.ITEMS.getValue(CROSSBOW_ID);
        if (crossbow == null) {
            // 允许附属环境关闭 Future MC 弩时仍保持模型注册阶段可容错
            WitherStormMod.LOGGER.info(
                    "Future MC crossbow is unavailable; ender pearl crossbow model registration skipped");
            return;
        }
        crossbow.addPropertyOverride(ENDER_PEARL_PROPERTY,
                (stack, world, entity) -> hasChargedEnderPearl(stack) ? 1.0F : 0.0F);
        ModelBakery.registerItemVariants(crossbow, ENDER_PEARL_MODEL);
    }

    static void bakeModels(ModelBakeEvent event) {
        IBakedModel crossbow = event.getModelRegistry().getObject(CROSSBOW_MODEL);
        IBakedModel enderPearl = event.getModelRegistry().getObject(BAKED_ENDER_PEARL_MODEL);
        IBakedModel missing = event.getModelManager().getMissingModel();
        if (crossbow == null || crossbow == missing || enderPearl == null || enderPearl == missing) {
            WitherStormMod.LOGGER.info(
                    "Ender pearl crossbow model attachment skipped (crossbow={}, enderPearl={})",
                    crossbow, enderPearl);
            return;
        }
        event.getModelRegistry().putObject(
                CROSSBOW_MODEL, new CrossbowBakedModel(crossbow, enderPearl));
    }

    private static boolean hasChargedEnderPearl(ItemStack crossbow) {
        if (crossbow.isEmpty()) return false;
        NBTTagCompound tag = crossbow.getTagCompound();
        if (tag == null || !tag.getBoolean("Charged")
                || !tag.hasKey("ChargedProjectiles", Constants.NBT.TAG_LIST)) return false;

        NBTTagList projectiles = tag.getTagList("ChargedProjectiles", Constants.NBT.TAG_COMPOUND);
        for (int index = 0; index < projectiles.tagCount(); index++) {
            if (new ItemStack(projectiles.getCompoundTagAt(index)).getItem() == Items.ENDER_PEARL) {
                return true;
            }
        }
        return false;
    }

    private static final class CrossbowBakedModel extends BakedModelWrapper<IBakedModel> {
        private final ItemOverrideList overrides;

        private CrossbowBakedModel(IBakedModel originalModel, IBakedModel enderPearlModel) {
            super(originalModel);
            this.overrides = new CrossbowOverrideList(originalModel, enderPearlModel);
        }

        @Override
        public ItemOverrideList getOverrides() {
            return overrides;
        }
    }

    private static final class CrossbowOverrideList extends ItemOverrideList {
        private final IBakedModel originalModel;
        private final IBakedModel enderPearlModel;

        private CrossbowOverrideList(IBakedModel originalModel, IBakedModel enderPearlModel) {
            super(Collections.<ItemOverride>emptyList());
            this.originalModel = originalModel;
            this.enderPearlModel = enderPearlModel;
        }

        @Override
        public IBakedModel handleItemState(
                IBakedModel model, ItemStack stack, World world, EntityLivingBase entity) {
            if (hasChargedEnderPearl(stack)) return enderPearlModel;
            IBakedModel resolved = originalModel.getOverrides()
                    .handleItemState(originalModel, stack, world, entity);
            return resolved == null ? originalModel : resolved;
        }
    }
}
