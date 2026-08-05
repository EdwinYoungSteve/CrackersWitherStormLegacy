package com.wdcftgg.witherstormmod.client.model.witherstorm;

import com.wdcftgg.witherstormmod.client.model.witherstorm.LegacyModelBuilders.CubeDeformation;
import com.wdcftgg.witherstormmod.client.model.witherstorm.LegacyModelBuilders.CubeListBuilder;
import com.wdcftgg.witherstormmod.client.model.witherstorm.LegacyModelBuilders.PartDefinition;
import com.wdcftgg.witherstormmod.client.model.witherstorm.LegacyModelBuilders.PartPose;
import net.minecraft.util.math.MathHelper;

public final class LegacyCommandBlockGeometry {
    private LegacyCommandBlockGeometry() { }

public static PartDefinition populateBase(PartDefinition root, CubeDeformation def, boolean hasCenterHead, boolean hasRibcageExtension, boolean hasTail) {
        CubeListBuilder builder0 = CubeListBuilder.m_171558_();
        PartDefinition base = root.m_171599_("witherBase", builder0, PartPose.f_171404_);
        CubeListBuilder builder1 = CubeListBuilder.m_171558_();
        builder1.m_171514_(0, 16);
        builder1.m_171488_(-10.0f, 3.9f, -0.5f, 20.0f, 3.0f, 3.0f, def);
        base.m_171599_("shoulders", builder1, PartPose.f_171404_);
        CubeListBuilder builder2 = CubeListBuilder.m_171558_();
        builder2.m_171514_(0, 22);
        builder2.m_171488_(0.0f, 0.0f, 0.0f, 3.0f, 10.0f, 3.0f, def);
        builder2.m_171514_(24, 22);
        builder2.m_171488_(-4.0f, 1.5f, 0.5f, 11.0f, 2.0f, 2.0f, def);
        builder2.m_171514_(24, 22);
        builder2.m_171488_(-4.0f, 4.5f, 0.5f, 11.0f, 2.0f, 2.0f, def);
        builder2.m_171514_(24, 22);
        builder2.m_171488_(-4.0f, 7.5f, 0.5f, 11.0f, 2.0f, 2.0f, def);
        PartDefinition ribcage = base.m_171599_("ribcage", builder2, PartPose.m_171423_((float)-2.0f, (float)6.9f, (float)-0.5f, (float)0.20420352f, (float)0.0f, (float)0.0f));
        if (hasRibcageExtension) {
            CubeListBuilder builder3 = CubeListBuilder.m_171558_();
            builder3.m_171514_(128, 40);
            builder3.m_171488_(-5.5f, -2.0f, -4.0f, 2.0f, 2.0f, 8.0f, def);
            builder3.m_171514_(128, 40);
            builder3.m_171488_(-5.5f, -5.0f, -4.0f, 2.0f, 2.0f, 8.0f, def);
            builder3.m_171514_(128, 40);
            builder3.m_171488_(-5.5f, -8.0f, -4.0f, 2.0f, 2.0f, 8.0f, def);
            builder3.m_171514_(128, 40);
            builder3.m_171488_(3.5f, -8.0f, -4.0f, 2.0f, 2.0f, 8.0f, def);
            builder3.m_171514_(128, 40);
            builder3.m_171488_(3.5f, -5.0f, -4.0f, 2.0f, 2.0f, 8.0f, def);
            builder3.m_171514_(128, 40);
            builder3.m_171488_(3.5f, -2.0f, -4.0f, 2.0f, 2.0f, 8.0f, def);
            builder3.m_171514_(140, 44);
            builder3.m_171488_(2.5f, -2.0f, -4.0f, 1.0f, 2.0f, 2.0f, def);
            builder3.m_171514_(140, 44);
            builder3.m_171488_(2.5f, -5.0f, -4.0f, 1.0f, 2.0f, 2.0f, def);
            builder3.m_171514_(140, 44);
            builder3.m_171488_(2.5f, -8.0f, -4.0f, 1.0f, 2.0f, 2.0f, def);
            builder3.m_171514_(140, 44);
            builder3.m_171488_(-3.5f, -8.0f, -4.0f, 1.0f, 2.0f, 2.0f, def);
            builder3.m_171514_(140, 44);
            builder3.m_171488_(-3.5f, -5.0f, -4.0f, 1.0f, 2.0f, 2.0f, def);
            builder3.m_171514_(140, 44);
            builder3.m_171488_(-3.5f, -2.0f, -4.0f, 1.0f, 2.0f, 2.0f, def);
            PartDefinition ribcageExtension = ribcage.m_171599_("ribcageExtension", builder3, PartPose.m_171419_((float)1.5f, (float)9.5f, (float)-3.5f));
            CubeListBuilder builder4 = CubeListBuilder.m_171558_();
            builder4.m_171514_(48, 0);
            builder4.m_171496_(-4.0f, -8.0f, -3.0f, 8.0f, 8.0f, 8.0f, def.m_171469_(0.001f), 0.5f, 0.5f);
            ribcageExtension.m_171599_("block", builder4, PartPose.f_171404_);
        }
        if (hasTail) {
            CubeListBuilder builder5 = CubeListBuilder.m_171558_();
            builder5.m_171514_(12, 22);
            builder5.m_171488_(0.0f, 0.0f, 0.0f, 3.0f, 6.0f, 3.0f, def);
            base.m_171599_("tail", builder5, PartPose.m_171423_((float)-2.0f, (float)(6.9f + MathHelper.cos((float)0.20420352f) * 10.0f), (float)(-0.5f + MathHelper.sin((float)0.20420352f) * 10.0f), (float)0.83252203f, (float)0.0f, (float)0.0f));
        }
        if (hasCenterHead) {
            CubeListBuilder builder6 = CubeListBuilder.m_171558_();
            builder6.m_171514_(0, 0);
            builder6.m_171488_(-4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f, def);
            base.m_171599_("center_head", builder6, PartPose.f_171404_);
        }
        CubeListBuilder builder7 = CubeListBuilder.m_171558_();
        builder7.m_171514_(32, 0);
        builder7.m_171488_(-4.0f, -4.0f, -4.0f, 6.0f, 6.0f, 6.0f, def);
        CubeListBuilder sideHeads = builder7;
        base.m_171599_("right_head", sideHeads, PartPose.m_171419_((float)-8.0f, (float)4.0f, (float)0.0f));
        base.m_171599_("left_head", sideHeads, PartPose.m_171419_((float)10.0f, (float)4.0f, (float)0.0f));
        return base;
    }
}
