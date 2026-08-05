package com.wdcftgg.witherstormmod.client.model.witherstorm;

import com.wdcftgg.witherstormmod.client.model.witherstorm.LegacyModelBuilders.CubeDeformation;
import com.wdcftgg.witherstormmod.client.model.witherstorm.LegacyModelBuilders.CubeListBuilder;
import com.wdcftgg.witherstormmod.client.model.witherstorm.LegacyModelBuilders.PartDefinition;
import com.wdcftgg.witherstormmod.client.model.witherstorm.LegacyModelBuilders.PartPose;

public final class LegacyTentacleGeometry {
    private LegacyTentacleGeometry() { }

public static void populateDefinition(PartDefinition root, int[] length, PartPose offset) {
        CubeListBuilder builder0 = CubeListBuilder.m_171558_();
        PartDefinition tentacle = root.m_171599_("base", builder0, offset);
        CubeListBuilder builder1 = CubeListBuilder.m_171558_();
        builder1.m_171514_(36 + (24 - length[0]), 14 + (24 - length[0]));
        builder1.m_171506_(-6.0f, -6.0f, (float)(-length[0]), 12.0f, 12.0f, (float)length[0], false);
        PartDefinition segment1 = tentacle.m_171599_("segment1", builder1, PartPose.f_171404_);
        CubeListBuilder builder2 = CubeListBuilder.m_171558_();
        builder2.m_171514_(76 + (32 - length[1]), 32 + (32 - length[1]));
        builder2.m_171506_(-5.0f, -5.0f, (float)(-length[1]), 10.0f, 10.0f, (float)length[1], false);
        PartDefinition segment2 = segment1.m_171599_("segment2", builder2, PartPose.m_171419_((float)0.0f, (float)0.0f, (float)(-length[0])));
        CubeListBuilder builder3 = CubeListBuilder.m_171558_();
        builder3.m_171514_(76 + (32 - length[2]), 33 + (32 - length[2]));
        builder3.m_171506_(-4.0f, -4.0f, (float)(-length[2]), 8.0f, 8.0f, (float)length[2], false);
        PartDefinition segment3 = segment2.m_171599_("segment3", builder3, PartPose.m_171419_((float)0.0f, (float)0.0f, (float)(-length[1])));
        CubeListBuilder builder4 = CubeListBuilder.m_171558_();
        builder4.m_171514_(77 + (32 - length[3]), 34 + (32 - length[3]));
        builder4.m_171506_(-3.0f, -3.0f, (float)(-length[3]), 6.0f, 6.0f, (float)length[3], false);
        PartDefinition segment4 = segment3.m_171599_("segment4", builder4, PartPose.m_171419_((float)0.0f, (float)0.0f, (float)(-length[2])));
        CubeListBuilder builder5 = CubeListBuilder.m_171558_();
        builder5.m_171514_(76 + (34 - length[4]), 36 + (34 - length[4]));
        builder5.m_171506_(-2.0f, -2.0f, (float)(-length[4]), 4.0f, 4.0f, (float)length[4], false);
        PartDefinition segment5 = segment4.m_171599_("segment5", builder5, PartPose.m_171419_((float)0.0f, (float)0.0f, (float)(-length[3])));
        CubeListBuilder builder6 = CubeListBuilder.m_171558_();
        builder6.m_171514_(77 + (34 - length[5]), 38 + (34 - length[5]));
        builder6.m_171506_(-1.0f, -1.0f, (float)(-length[5]), 2.0f, 2.0f, (float)length[5], false);
        segment5.m_171599_("segment6", builder6, PartPose.m_171419_((float)0.0f, (float)0.0f, (float)(-length[4])));
    }
}
