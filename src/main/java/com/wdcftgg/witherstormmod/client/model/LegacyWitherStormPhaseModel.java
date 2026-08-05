package com.wdcftgg.witherstormmod.client.model;

import com.wdcftgg.witherstormmod.client.model.witherstorm.LegacyCommandBlockGeometry;
import com.wdcftgg.witherstormmod.client.model.witherstorm.LegacyModelBuilders.CubeDeformation;
import com.wdcftgg.witherstormmod.client.model.witherstorm.LegacyModelBuilders.PartDefinition;
import com.wdcftgg.witherstormmod.client.model.witherstorm.LegacyWitherStormDefinitions;
import com.wdcftgg.witherstormmod.common.entity.EntityWitherStormLegacy;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LegacyWitherStormPhaseModel extends ModelBase {
    public enum Form {
        COMMAND_BLOCK,
        HUNCHBACK_1,
        HUNCHBACK_1_1,
        HUNCHBACK_1_2,
        GROWING_HUNCHBACK,
        HUNCHBACK_2_1,
        PREGNANT_HUNCHBACK,
        HUNCHBACK_3_1,
        HUNCHBACK_3_2,
        DESTROYER,
        INTERMEDIATE_EVOLVED_DESTROYER,
        EVOLVED_DESTROYER,
        INTERMEDIATE_DEVOURER,
        DEVOURER,
        DISMANTLED,
        INTERMEDIATE_EVOLVED_DEVOURER,
        EVOLVED_DEVOURER,
        TORN_EVOLVED_DEVOURER
    }

    private final Form form;
    private final PartDefinition rootDefinition;
    private final ModelRenderer mass;
    private final ModelRenderer lowResMass;
    private final ModelRenderer commandBlockBase;
    private final List<ModelRenderer> heads = new ArrayList<ModelRenderer>();
    private final List<TentacleParts> tentacles = new ArrayList<TentacleParts>();

    public LegacyWitherStormPhaseModel(Form form) {
        this.form = form;
        textureWidth = 160;
        textureHeight = 160;
        ModelRenderer root = new ModelRenderer(this);
        rootDefinition = new PartDefinition(this, root);
        LegacyWitherStormDefinitions.initializeRoot(rootDefinition);
        buildDefinition(form);
        mass = renderer(rootDefinition.child("mass"));
        lowResMass = renderer(rootDefinition.child("lowResMass"));
        commandBlockBase = renderer(rootDefinition.child("witherBase"));
        collectHeads();
        collectTentacles();
        LegacyWitherStormTentacleConfig.apply(form, tentacles);
    }

    private void buildDefinition(Form selected) {
        CubeDeformation def = CubeDeformation.f_171458_;
        switch (selected) {
            case COMMAND_BLOCK:
                LegacyCommandBlockGeometry.populateBase(rootDefinition, def, true, true, true);
                break;
            case HUNCHBACK_1:
                LegacyWitherStormDefinitions.buildHunchback(rootDefinition, def);
                break;
            case HUNCHBACK_1_1:
                LegacyWitherStormDefinitions.buildHunchback1_1(rootDefinition, def);
                break;
            case HUNCHBACK_1_2:
                LegacyWitherStormDefinitions.buildHunchback1_2(rootDefinition, def);
                break;
            case GROWING_HUNCHBACK:
                LegacyWitherStormDefinitions.buildGrowingHunchback(rootDefinition, def);
                break;
            case HUNCHBACK_2_1:
                LegacyWitherStormDefinitions.buildHunchback2_1(rootDefinition, def);
                break;
            case PREGNANT_HUNCHBACK:
                LegacyWitherStormDefinitions.buildPregnantHunchback(rootDefinition, def);
                break;
            case HUNCHBACK_3_1:
                LegacyWitherStormDefinitions.buildHunchback3_1(rootDefinition, def);
                break;
            case HUNCHBACK_3_2:
                LegacyWitherStormDefinitions.buildHunchback3_2(rootDefinition, def);
                break;
            case DESTROYER:
                LegacyWitherStormDefinitions.buildDestroyer(rootDefinition);
                break;
            case INTERMEDIATE_EVOLVED_DESTROYER:
                LegacyWitherStormDefinitions.buildIntermediateEvolvedDestroyer(rootDefinition);
                break;
            case EVOLVED_DESTROYER:
                LegacyWitherStormDefinitions.buildEvolvedDestroyer(rootDefinition);
                break;
            case INTERMEDIATE_DEVOURER:
                LegacyWitherStormDefinitions.buildIntermediateDevourer(rootDefinition);
                break;
            case DEVOURER:
                LegacyWitherStormDefinitions.buildDevourer(rootDefinition);
                break;
            case DISMANTLED:
                LegacyWitherStormDefinitions.buildDismantled(rootDefinition);
                break;
            case INTERMEDIATE_EVOLVED_DEVOURER:
                LegacyWitherStormDefinitions.buildIntermediateEvolvedDevourer(rootDefinition);
                break;
            case EVOLVED_DEVOURER:
                LegacyWitherStormDefinitions.buildEvolvedDevourer(rootDefinition);
                break;
            case TORN_EVOLVED_DEVOURER:
                LegacyWitherStormDefinitions.buildTornEvolvedDevourer(rootDefinition);
                break;
            default:
                throw new IllegalArgumentException(selected.name());
        }
    }

    private void collectHeads() {
        PartDefinition root = rootDefinition.child("heads");
        if (root == null) return;
        for (PartDefinition definition : root.children().values()) {
            heads.add(definition.renderer());
        }
    }

    private void collectTentacles() {
        PartDefinition root = rootDefinition.child("tentacles");
        if (root == null) return;
        for (Map.Entry<String, PartDefinition> entry : root.children().entrySet()) {
            PartDefinition base = entry.getValue().child("base");
            if (base != null) tentacles.add(new TentacleParts(base));
        }
    }

    private static ModelRenderer renderer(PartDefinition definition) {
        return definition == null ? null : definition.renderer();
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch, float scale) {
        EntityWitherStormLegacy storm = (EntityWitherStormLegacy) entity;
        animate(storm, age, yaw, pitch);
        if (commandBlockBase != null) commandBlockBase.render(scale);

        if (mass != null) {
            GlStateManager.pushMatrix();
            applyMassTransform();
            mass.render(scale);
            GlStateManager.popMatrix();
        }

        float headScale = headScale();
        for (int i = 0; i < heads.size(); i++) {
            if (storm.areOtherHeadsDisabled() && i != 1 && heads.size() == 3) continue;
            GlStateManager.pushMatrix();
            GlStateManager.scale(headScale, headScale, headScale);
            heads.get(i).render(scale);
            GlStateManager.popMatrix();
        }

        for (TentacleParts tentacle : tentacles) {
            GlStateManager.pushMatrix();
            float tentacleScale = tentacle.scale;
            GlStateManager.scale(tentacleScale, tentacleScale, tentacleScale);
            if (rotatesEarlyMass()) GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
            tentacle.base.render(scale);
            GlStateManager.popMatrix();
        }
    }

    private void animate(EntityWitherStormLegacy storm, float age, float yaw, float pitch) {
        if (commandBlockBase != null) {
            PartDefinition base = rootDefinition.child("witherBase");
            ModelRenderer ribcage = renderer(base.child("ribcage"));
            ModelRenderer tail = renderer(base.child("tail"));
            float wave = MathHelper.sin(age * 0.1F);
            if (ribcage != null) ribcage.rotateAngleX = (0.065F + 0.05F * wave) * (float) Math.PI;
            if (tail != null && ribcage != null) {
                tail.setRotationPoint(-2.0F, 6.9F + MathHelper.cos(ribcage.rotateAngleX) * 10.0F,
                        -0.5F + MathHelper.sin(ribcage.rotateAngleX) * 10.0F);
                tail.rotateAngleX = (0.265F + 0.1F * wave) * (float) Math.PI;
            }
            ModelRenderer center = renderer(base.child("center_head"));
            if (center != null) {
                center.rotateAngleY = yaw * ((float) Math.PI / 180.0F);
                center.rotateAngleX = pitch * ((float) Math.PI / 180.0F);
            }
            animateVanillaSideHead(storm, renderer(base.child("left_head")), 1);
            animateVanillaSideHead(storm, renderer(base.child("right_head")), 0);
        }

        for (int i = 0; i < heads.size(); i++) {
            ModelRenderer head = heads.get(i);
            int headIndex = heads.size() == 3 ? new int[]{2, 0, 1}[i] : 0;
            if (headIndex == 0) {
                head.rotateAngleY = (float) Math.PI + yaw * ((float) Math.PI / 180.0F);
                head.rotateAngleX = -pitch * ((float) Math.PI / 180.0F);
            } else {
                animateCommandSideHead(storm, head, headIndex);
            }
            PartDefinition headDefinition = rootDefinition.child("heads").child("head" + i);
            ModelRenderer lower = renderer(headDefinition == null ? null : headDefinition.child("lowerJaw"));
            if (lower != null) {
                float partialTicks = MathHelper.clamp(age - storm.ticksExisted, 0.0F, 1.0F);
                float hinge = storm.getMouthAnimation(headIndex, partialTicks) * 0.3F;
                lower.rotateAngleX = MathHelper.sin(hinge) * 10.0F - 10.0F
                        + (0.065F + 0.02F * MathHelper.sin((age + i * 75.0F) * 0.1F)) * (float) Math.PI - 0.5F;
                lower.rotateAngleZ = storm.getBrokenJawAnimation(headIndex, partialTicks);
                head.rotateAngleZ = storm.getHeadShakeAnimation(headIndex, partialTicks);
            }
        }

        for (int i = 0; i < tentacles.size(); i++) {
            tentacles.get(i).animate(age);
        }
    }

    private static void animateCommandSideHead(EntityWitherStormLegacy storm, ModelRenderer head, int index) {
        if (head == null) return;
        head.rotateAngleY = (storm.getHeadYRotation(index - 1) - storm.renderYawOffset) * ((float) Math.PI / 180.0F) + (float) Math.PI;
        head.rotateAngleX = -storm.getHeadXRotation(index - 1) * ((float) Math.PI / 180.0F);
    }

    private static void animateVanillaSideHead(EntityWitherStormLegacy storm, ModelRenderer head, int sideIndex) {
        if (head == null) return;
        head.rotateAngleY = (storm.getHeadYRotation(sideIndex) - storm.renderYawOffset) * ((float) Math.PI / 180.0F);
        head.rotateAngleX = storm.getHeadXRotation(sideIndex) * ((float) Math.PI / 180.0F);
    }

    private void applyMassTransform() {
        if (isLateForm()) {
            GlStateManager.scale(10.0F, 10.0F, 10.0F);
        } else if (form == Form.GROWING_HUNCHBACK) {
            GlStateManager.scale(1.001F, 1.001F, 1.001F);
        }
        if (rotatesEarlyMass()) GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
    }

    private boolean rotatesEarlyMass() {
        return form == Form.PREGNANT_HUNCHBACK || form == Form.HUNCHBACK_3_1 || form == Form.HUNCHBACK_3_2;
    }

    private boolean isLateForm() {
        return form.ordinal() >= Form.DESTROYER.ordinal();
    }

    private float headScale() {
        if (isLateForm()) return 3.0F;
        if (form == Form.GROWING_HUNCHBACK || form == Form.HUNCHBACK_2_1 ||
                form == Form.PREGNANT_HUNCHBACK || form == Form.HUNCHBACK_3_1 || form == Form.HUNCHBACK_3_2) return 0.7F;
        return 1.0F;
    }

    public ModelRenderer getLowResMass() {
        return lowResMass;
    }

    static final class TentacleParts {
        final ModelRenderer base;
        final ModelRenderer[] segments = new ModelRenderer[6];
        float scale = 0.7F;
        float xRotationalOffset;
        float yRotationalOffset;
        float animationSpeed = 1.0F;
        float yAngularOffset;
        float xAngularOffset;
        float animationOffset;
        float reach = 1.0F;

        TentacleParts(PartDefinition definition) {
            base = definition.renderer();
            PartDefinition current = definition;
            for (int i = 0; i < segments.length; i++) {
                current = current.child("segment" + (i + 1));
                if (current == null) break;
                segments[i] = current.renderer();
            }
        }

        void animate(float age) {
            float f = MathHelper.sin((age + animationOffset * 10.0F) * animationSpeed * 0.1F) * reach;
            float s = MathHelper.cos((age + animationOffset * 10.0F) * animationSpeed * 0.05F) * reach;
            base.rotateAngleY = s * f * 0.05F + yRotationalOffset;
            base.rotateAngleX = f * s * 0.05F + xRotationalOffset;
            if (segments[0] != null) segments[0].rotateAngleX = f * -0.1F;
            if (segments[1] != null) segments[1].rotateAngleX = f * 0.1F + xAngularOffset;
            if (segments[2] != null) segments[2].rotateAngleX = f * 0.075F + xAngularOffset;
            if (segments[3] != null) segments[3].rotateAngleX = f * 0.05F + xAngularOffset;
            if (segments[4] != null) segments[4].rotateAngleX = f * 0.1F + xAngularOffset;
            if (segments[5] != null) segments[5].rotateAngleX = f * 0.1F + xAngularOffset;
            for (int i = 1; i < segments.length; i++) {
                if (segments[i] != null) segments[i].rotateAngleY = yAngularOffset;
            }
        }
    }
}
