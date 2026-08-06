package com.wdcftgg.witherstormmod.client.render;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.client.model.WitherStormPhaseModel;
import com.wdcftgg.witherstormmod.client.model.WitherStormPhaseModel.Form;
import com.wdcftgg.witherstormmod.common.entity.WitherStormEntity;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import java.util.EnumMap;
import java.util.Map;

public class WitherStormRenderer extends RenderLiving<WitherStormEntity> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MOD_ID, "textures/entity/wither_storm/wither_storm.png");
    private final Map<Form, WitherStormPhaseModel> models = new EnumMap<Form, WitherStormPhaseModel>(Form.class);

    public WitherStormRenderer(RenderManager renderManager) {
        super(renderManager, new WitherStormPhaseModel(Form.COMMAND_BLOCK), 3.0F);
        models.put(Form.COMMAND_BLOCK, (WitherStormPhaseModel) mainModel);
        for (Form form : Form.values()) {
            if (form != Form.COMMAND_BLOCK) models.put(form, new WitherStormPhaseModel(form));
        }
    }

    @Override
    public void doRender(WitherStormEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        mainModel = models.get(fetchForm(entity));
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(WitherStormEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void preRenderCallback(WitherStormEntity entity, float partialTickTime) {
        float scale = 2.0F;
        int shrinkingTicks = Math.max(0, entity.getInvulnerableTicks() - 750);
        if (shrinkingTicks > 0) scale -= (shrinkingTicks - partialTickTime) / 450.0F * 0.5F;
        GlStateManager.scale(scale, scale, scale);
    }

    @Override
    protected void applyRotations(WitherStormEntity entity, float ageInTicks,
                                  float rotationYaw, float partialTicks) {
        GlStateManager.rotate(180.0F - rotationYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(entity.getBodyXRotation(partialTicks), 1.0F, 0.0F, 0.0F);
    }

    private static Form fetchForm(WitherStormEntity entity) {
        int phase = entity.getPhase();
        int consumed = entity.getConsumedMass();
        if (phase == 1) {
            if (consumed >= entity.adjustAmountForEvolutionSpeed(250)) return Form.HUNCHBACK_1_2;
            if (consumed >= entity.adjustAmountForEvolutionSpeed(150)) return Form.HUNCHBACK_1_1;
            return Form.HUNCHBACK_1;
        }
        if (phase == 2) {
            return consumed >= entity.adjustAmountForEvolutionSpeed(800)
                    ? Form.HUNCHBACK_2_1 : Form.GROWING_HUNCHBACK;
        }
        if (phase == 3) {
            if (consumed >= entity.adjustAmountForEvolutionSpeed(3500)) return Form.HUNCHBACK_3_2;
            if (consumed >= entity.adjustAmountForEvolutionSpeed(2350)) return Form.HUNCHBACK_3_1;
            return Form.PREGNANT_HUNCHBACK;
        }
        if (phase == 4) {
            return consumed <= entity.getSubPhaseRequirement(phase) ? Form.DESTROYER : Form.INTERMEDIATE_EVOLVED_DESTROYER;
        }
        if (phase == 5) {
            if (consumed > entity.getPhaseRequirement()) return Form.DEVOURER;
            return consumed <= entity.getSubPhaseRequirement(phase) ? Form.EVOLVED_DESTROYER : Form.INTERMEDIATE_DEVOURER;
        }
        if (phase == 6) {
            return consumed <= entity.getSubPhaseRequirement(phase) ? Form.DISMANTLED : Form.INTERMEDIATE_EVOLVED_DEVOURER;
        }
        if (phase == 7) return entity.isBeingTornApart() ? Form.TORN_EVOLVED_DEVOURER : Form.EVOLVED_DEVOURER;
        return Form.COMMAND_BLOCK;
    }
}
