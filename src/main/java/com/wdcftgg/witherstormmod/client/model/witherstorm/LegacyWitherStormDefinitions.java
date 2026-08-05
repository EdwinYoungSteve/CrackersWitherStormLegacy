package com.wdcftgg.witherstormmod.client.model.witherstorm;

import com.wdcftgg.witherstormmod.client.model.witherstorm.LegacyModelBuilders.CubeDeformation;
import com.wdcftgg.witherstormmod.client.model.witherstorm.LegacyModelBuilders.CubeListBuilder;
import com.wdcftgg.witherstormmod.client.model.witherstorm.LegacyModelBuilders.PartDefinition;
import com.wdcftgg.witherstormmod.client.model.witherstorm.LegacyModelBuilders.PartPose;
import com.wdcftgg.witherstormmod.client.model.witherstorm.mass.*;

public final class LegacyWitherStormDefinitions {
    private LegacyWitherStormDefinitions() { }

    private static void createBaseRoot(PartDefinition root) {
        CubeListBuilder builder0 = CubeListBuilder.m_171558_();
        root.m_171599_("tentacles", builder0, PartPose.f_171404_);
        CubeListBuilder builder1 = CubeListBuilder.m_171558_();
        root.m_171599_("heads", builder1, PartPose.f_171404_);
    }

    private static void createSingleHead(PartDefinition root, PartPose pose) {
        PartDefinition heads = root.m_171597_("heads");
        CubeListBuilder builder2 = CubeListBuilder.m_171558_();
        LegacyHeadGeometry.populateDefinition(heads.m_171599_("head0", builder2, pose));
    }

    private static void createThreeHeads(PartDefinition root, PartPose[] poses) {
        PartDefinition heads = root.m_171597_("heads");
        for (int i = 0; i < 3; i++) {
            CubeListBuilder builder3 = CubeListBuilder.m_171558_();
            LegacyHeadGeometry.populateDefinition(heads.m_171599_("head" + i, builder3, poses[i]));
        }
    }

    public static void initializeRoot(PartDefinition root) {
        createBaseRoot(root);
    }

    public static void buildDestroyer(PartDefinition root) {
        createThreeHeads(root, new PartPose[]{PartPose.m_171419_((float)-22.0f, (float)-65.0f, (float)-40.0f), PartPose.m_171419_((float)0.0f, (float)-32.0f, (float)-23.0f), PartPose.m_171419_((float)32.0f, (float)-60.0f, (float)-24.0f)});
        DestroyerBodyModel.createBodyModel(root, 0.2f);
        LowResDestroyerBodyModel.createBodyModel(root, 0.3f);
        PartDefinition tentacles = root.m_171597_("tentacles");
        CubeListBuilder builder4 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle0", builder4, PartPose.f_171404_), new int[]{23, 28, 28, 28, 32, 32}, PartPose.m_171419_((float)-10.0f, (float)-30.0f, (float)0.0f));
        CubeListBuilder builder5 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle1", builder5, PartPose.f_171404_), new int[]{23, 28, 28, 28, 32, 32}, PartPose.m_171419_((float)30.0f, (float)-115.0f, (float)-10.0f));
        CubeListBuilder builder6 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle2", builder6, PartPose.f_171404_), new int[]{23, 28, 28, 28, 32, 32}, PartPose.m_171419_((float)10.0f, (float)-40.0f, (float)10.0f));
        CubeListBuilder builder7 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle3", builder7, PartPose.f_171404_), new int[]{23, 28, 28, 28, 32, 32}, PartPose.m_171419_((float)-50.0f, (float)-100.0f, (float)0.0f));
        CubeListBuilder builder8 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle4", builder8, PartPose.f_171404_), new int[]{23, 28, 28, 28, 32, 32}, PartPose.m_171419_((float)-10.0f, (float)-95.0f, (float)15.0f));
    }

    public static void buildDevourer(PartDefinition root) {
        createThreeHeads(root, new PartPose[]{PartPose.m_171419_((float)-22.0f, (float)-65.0f, (float)-40.0f), PartPose.m_171419_((float)0.0f, (float)-32.0f, (float)-23.0f), PartPose.m_171419_((float)32.0f, (float)-60.0f, (float)-24.0f)});
        DevourerBodyModel.createBodyModel(root, 0.2f);
        LowResDevourerBodyModel.createBodyModel(root, 0.3f);
        PartDefinition tentacles = root.m_171597_("tentacles");
        CubeListBuilder builder9 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle0", builder9, PartPose.f_171404_), new int[]{18, 24, 24, 28, 28, 32}, PartPose.m_171419_((float)-20.0f, (float)-25.0f, (float)5.0f));
        CubeListBuilder builder10 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle1", builder10, PartPose.f_171404_), new int[]{18, 24, 24, 24, 28, 38}, PartPose.m_171419_((float)20.0f, (float)-27.5f, (float)7.0f));
        CubeListBuilder builder11 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle2", builder11, PartPose.f_171404_), new int[]{18, 24, 24, 28, 32, 28}, PartPose.m_171419_((float)-10.0f, (float)-30.0f, (float)-10.0f));
        CubeListBuilder builder12 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle3", builder12, PartPose.f_171404_), new int[]{18, 18, 24, 24, 24, 28}, PartPose.m_171419_((float)8.0f, (float)-34.0f, (float)-6.0f));
        CubeListBuilder builder13 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle4", builder13, PartPose.f_171404_), new int[]{18, 18, 24, 28, 32, 32}, PartPose.m_171419_((float)-8.0f, (float)-25.0f, (float)16.0f));
        CubeListBuilder builder14 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle5", builder14, PartPose.f_171404_), new int[]{18, 20, 26, 28, 32, 28}, PartPose.m_171419_((float)10.0f, (float)-23.0f, (float)19.0f));
        CubeListBuilder builder15 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle6", builder15, PartPose.f_171404_), new int[]{18, 20, 26, 28, 28, 24}, PartPose.m_171419_((float)-2.0f, (float)0.0f, (float)0.0f));
        CubeListBuilder builder16 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacleLarge0", builder16, PartPose.f_171404_), new int[]{16, 20, 24, 28, 32, 28}, PartPose.m_171419_((float)-24.0f, (float)-28.0f, (float)0.0f));
        CubeListBuilder builder17 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacleLarge1", builder17, PartPose.f_171404_), new int[]{20, 20, 24, 24, 28, 32}, PartPose.m_171419_((float)28.0f, (float)-28.0f, (float)2.0f));
    }

    public static void buildDismantled(PartDefinition root) {
        createThreeHeads(root, new PartPose[]{PartPose.m_171419_((float)-22.0f, (float)-65.0f, (float)-40.0f), PartPose.m_171419_((float)0.0f, (float)-32.0f, (float)-23.0f), PartPose.m_171419_((float)32.0f, (float)-60.0f, (float)-24.0f)});
        DismantledBodyModel.createBodyModel(root, 0.2f);
        LowResDismantledBodyModel.createBodyModel(root, 0.3f);
        PartDefinition tentacles = root.m_171597_("tentacles");
        CubeListBuilder builder18 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle0", builder18, PartPose.f_171404_), new int[]{18, 24, 24, 28, 28, 32}, PartPose.m_171419_((float)-20.0f, (float)-25.0f, (float)5.0f));
        CubeListBuilder builder19 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle1", builder19, PartPose.f_171404_), new int[]{18, 24, 24, 24, 28, 38}, PartPose.m_171419_((float)20.0f, (float)-27.5f, (float)7.0f));
        CubeListBuilder builder20 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle2", builder20, PartPose.f_171404_), new int[]{18, 24, 24, 28, 32, 28}, PartPose.m_171419_((float)-10.0f, (float)-30.0f, (float)-10.0f));
        CubeListBuilder builder21 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle3", builder21, PartPose.f_171404_), new int[]{18, 18, 24, 24, 24, 28}, PartPose.m_171419_((float)8.0f, (float)-34.0f, (float)-6.0f));
        CubeListBuilder builder22 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle4", builder22, PartPose.f_171404_), new int[]{18, 18, 24, 28, 32, 32}, PartPose.m_171419_((float)-8.0f, (float)-25.0f, (float)16.0f));
        CubeListBuilder builder23 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle5", builder23, PartPose.f_171404_), new int[]{18, 20, 26, 28, 32, 28}, PartPose.m_171419_((float)10.0f, (float)-23.0f, (float)19.0f));
        CubeListBuilder builder24 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle6", builder24, PartPose.f_171404_), new int[]{18, 20, 26, 28, 28, 24}, PartPose.m_171419_((float)-2.0f, (float)0.0f, (float)0.0f));
        CubeListBuilder builder25 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacleLarge0", builder25, PartPose.f_171404_), new int[]{16, 20, 24, 28, 32, 28}, PartPose.m_171419_((float)-24.0f, (float)-28.0f, (float)0.0f));
        CubeListBuilder builder26 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacleLarge1", builder26, PartPose.f_171404_), new int[]{20, 20, 24, 24, 28, 32}, PartPose.m_171419_((float)28.0f, (float)-28.0f, (float)2.0f));
    }

    public static void buildEvolvedDestroyer(PartDefinition root) {
        createThreeHeads(root, new PartPose[]{PartPose.m_171419_((float)-22.0f, (float)-65.0f, (float)-40.0f), PartPose.m_171419_((float)0.0f, (float)-32.0f, (float)-23.0f), PartPose.m_171419_((float)32.0f, (float)-60.0f, (float)-24.0f)});
        EvolvedDestroyerBodyModel.createBodyModel(root, 0.2f);
        LowResEvolvedDestroyerBodyModel.createBodyModel(root, 0.3f);
        PartDefinition tentacles = root.m_171597_("tentacles");
        CubeListBuilder builder27 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle0", builder27, PartPose.f_171404_), new int[]{18, 24, 24, 28, 28, 32}, PartPose.m_171419_((float)-20.0f, (float)-50.0f, (float)0.0f));
        CubeListBuilder builder28 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle1", builder28, PartPose.f_171404_), new int[]{18, 24, 24, 24, 28, 38}, PartPose.m_171419_((float)20.0f, (float)-55.0f, (float)0.0f));
        CubeListBuilder builder29 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle2", builder29, PartPose.f_171404_), new int[]{18, 24, 24, 28, 32, 28}, PartPose.m_171419_((float)-10.0f, (float)-40.0f, (float)-10.0f));
        CubeListBuilder builder30 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle3", builder30, PartPose.f_171404_), new int[]{18, 18, 24, 24, 24, 28}, PartPose.m_171419_((float)8.0f, (float)-45.0f, (float)-10.0f));
        CubeListBuilder builder31 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle4", builder31, PartPose.f_171404_), new int[]{18, 18, 24, 28, 32, 32}, PartPose.m_171419_((float)-8.0f, (float)-50.0f, (float)12.0f));
        CubeListBuilder builder32 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle5", builder32, PartPose.f_171404_), new int[]{18, 20, 26, 28, 32, 28}, PartPose.m_171419_((float)10.0f, (float)-45.0f, (float)12.0f));
        CubeListBuilder builder33 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle6", builder33, PartPose.f_171404_), new int[]{18, 20, 26, 28, 28, 24}, PartPose.m_171419_((float)-2.0f, (float)0.0f, (float)0.0f));
    }

    public static void buildEvolvedDevourer(PartDefinition root) {
        createThreeHeads(root, new PartPose[]{PartPose.m_171419_((float)-22.0f, (float)-65.0f, (float)-40.0f), PartPose.m_171419_((float)0.0f, (float)-32.0f, (float)-23.0f), PartPose.m_171419_((float)32.0f, (float)-60.0f, (float)-24.0f)});
        EvolvedDevourerBodyModel.createBodyModel(root, 0.2f);
        LowResEvolvedDevourerBodyModel.createBodyModel(root, 0.3f);
        PartDefinition tentacles = root.m_171597_("tentacles");
        CubeListBuilder builder34 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle0", builder34, PartPose.f_171404_), new int[]{18, 24, 24, 28, 28, 32}, PartPose.m_171419_((float)-20.0f, (float)-25.0f, (float)5.0f));
        CubeListBuilder builder35 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle1", builder35, PartPose.f_171404_), new int[]{18, 24, 24, 24, 28, 38}, PartPose.m_171419_((float)20.0f, (float)-27.5f, (float)7.0f));
        CubeListBuilder builder36 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle2", builder36, PartPose.f_171404_), new int[]{18, 24, 24, 28, 32, 28}, PartPose.m_171419_((float)-10.0f, (float)-30.0f, (float)-10.0f));
        CubeListBuilder builder37 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle3", builder37, PartPose.f_171404_), new int[]{18, 18, 24, 24, 24, 28}, PartPose.m_171419_((float)8.0f, (float)-34.0f, (float)-6.0f));
        CubeListBuilder builder38 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle4", builder38, PartPose.f_171404_), new int[]{18, 18, 24, 28, 32, 32}, PartPose.m_171419_((float)-8.0f, (float)-25.0f, (float)16.0f));
        CubeListBuilder builder39 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle5", builder39, PartPose.f_171404_), new int[]{18, 20, 26, 28, 32, 28}, PartPose.m_171419_((float)10.0f, (float)-23.0f, (float)19.0f));
        CubeListBuilder builder40 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle6", builder40, PartPose.f_171404_), new int[]{18, 20, 26, 28, 28, 24}, PartPose.m_171419_((float)-2.0f, (float)0.0f, (float)0.0f));
        CubeListBuilder builder41 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacleLarge0", builder41, PartPose.f_171404_), new int[]{16, 20, 24, 28, 32, 28}, PartPose.m_171419_((float)-24.0f, (float)-28.0f, (float)0.0f));
        CubeListBuilder builder42 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacleLarge1", builder42, PartPose.f_171404_), new int[]{20, 20, 24, 24, 28, 32}, PartPose.m_171419_((float)28.0f, (float)-28.0f, (float)2.0f));
    }

    public static void buildGrowingHunchback(PartDefinition root, CubeDeformation def) {
        createSingleHead(root, PartPose.f_171404_);
        LegacyCommandBlockGeometry.populateBase(root, def, false, true, true);
        GrowingHunchbackMassModel.createMassModel(root, 1.0f);
    }

    public static void buildHunchback1_1(PartDefinition root, CubeDeformation def) {
        LegacyCommandBlockGeometry.populateBase(root, def, true, true, true);
        WSHunchback1_1.createBodyModel(root, 1.0f);
    }

    public static void buildHunchback1_2(PartDefinition root, CubeDeformation def) {
        LegacyCommandBlockGeometry.populateBase(root, def, true, true, true);
        WSHunchback1_2.createBodyModel(root, 1.0f);
    }

    public static void buildHunchback2_1(PartDefinition root, CubeDeformation def) {
        createSingleHead(root, PartPose.f_171404_);
        LegacyCommandBlockGeometry.populateBase(root, def, false, true, false);
        WSHunchback2_1.createBodyModel(root, 1.0f);
    }

    public static void buildHunchback3_1(PartDefinition root, CubeDeformation def) {
        createSingleHead(root, PartPose.f_171404_);
        LegacyCommandBlockGeometry.populateBase(root, def, false, false, false);
        WSHunchback3_1.createBodyModel(root, 1.0f);
        PartDefinition tentacles = root.m_171597_("tentacles");
        CubeListBuilder builder43 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle0", builder43, PartPose.f_171404_), new int[]{18, 24, 24, 24, 24, 32}, PartPose.m_171419_((float)0.0f, (float)0.0f, (float)20.0f));
        CubeListBuilder builder44 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle1", builder44, PartPose.f_171404_), new int[]{18, 24, 24, 24, 24, 32}, PartPose.m_171419_((float)0.0f, (float)0.0f, (float)20.0f));
        CubeListBuilder builder45 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle2", builder45, PartPose.f_171404_), new int[]{18, 24, 24, 24, 24, 32}, PartPose.m_171419_((float)0.0f, (float)20.0f, (float)20.0f));
    }

    public static void buildHunchback3_2(PartDefinition root, CubeDeformation def) {
        createSingleHead(root, PartPose.f_171404_);
        LegacyCommandBlockGeometry.populateBase(root, def, false, false, false);
        WSHunchback3_2.createBodyModel(root, 1.0f);
        PartDefinition tentacles = root.m_171597_("tentacles");
        CubeListBuilder builder46 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle0", builder46, PartPose.f_171404_), new int[]{18, 24, 24, 24, 24, 32}, PartPose.m_171419_((float)0.0f, (float)0.0f, (float)20.0f));
        CubeListBuilder builder47 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle1", builder47, PartPose.f_171404_), new int[]{18, 24, 24, 24, 24, 32}, PartPose.m_171419_((float)0.0f, (float)0.0f, (float)20.0f));
        CubeListBuilder builder48 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle2", builder48, PartPose.f_171404_), new int[]{18, 24, 24, 24, 24, 32}, PartPose.m_171419_((float)0.0f, (float)20.0f, (float)20.0f));
    }

    public static void buildHunchback(PartDefinition root, CubeDeformation def) {
        LegacyCommandBlockGeometry.populateBase(root, def, true, true, true);
        HunchbackBodyModel.createBodyModel(root, 1.0f);
    }

    public static void buildIntermediateDevourer(PartDefinition root) {
        createThreeHeads(root, new PartPose[]{PartPose.m_171419_((float)-22.0f, (float)-65.0f, (float)-40.0f), PartPose.m_171419_((float)0.0f, (float)-32.0f, (float)-23.0f), PartPose.m_171419_((float)32.0f, (float)-60.0f, (float)-24.0f)});
        IntermediateDevourerBodyModel.createBodyModel(root, 0.2f);
        LowResIntermediateDevourerBodyModel.createBodyModel(root, 0.3f);
        PartDefinition tentacles = root.m_171597_("tentacles");
        CubeListBuilder builder49 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle0", builder49, PartPose.f_171404_), new int[]{18, 24, 24, 28, 28, 32}, PartPose.m_171419_((float)-20.0f, (float)-50.0f, (float)0.0f));
        CubeListBuilder builder50 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle1", builder50, PartPose.f_171404_), new int[]{18, 24, 24, 24, 28, 38}, PartPose.m_171419_((float)20.0f, (float)-55.0f, (float)0.0f));
        CubeListBuilder builder51 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle2", builder51, PartPose.f_171404_), new int[]{18, 24, 24, 28, 32, 28}, PartPose.m_171419_((float)-10.0f, (float)-40.0f, (float)-10.0f));
        CubeListBuilder builder52 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle3", builder52, PartPose.f_171404_), new int[]{18, 18, 24, 24, 24, 28}, PartPose.m_171419_((float)8.0f, (float)-45.0f, (float)-10.0f));
        CubeListBuilder builder53 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle4", builder53, PartPose.f_171404_), new int[]{18, 18, 24, 28, 32, 32}, PartPose.m_171419_((float)-8.0f, (float)-50.0f, (float)12.0f));
        CubeListBuilder builder54 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle5", builder54, PartPose.f_171404_), new int[]{18, 20, 26, 28, 32, 28}, PartPose.m_171419_((float)10.0f, (float)-45.0f, (float)12.0f));
        CubeListBuilder builder55 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle6", builder55, PartPose.f_171404_), new int[]{18, 20, 26, 28, 28, 24}, PartPose.m_171419_((float)-2.0f, (float)0.0f, (float)0.0f));
        CubeListBuilder builder56 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacleLarge0", builder56, PartPose.f_171404_), new int[]{16, 20, 24, 28, 32, 28}, PartPose.m_171419_((float)-34.0f, (float)-43.0f, (float)0.0f));
        CubeListBuilder builder57 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacleLarge1", builder57, PartPose.f_171404_), new int[]{20, 20, 24, 24, 28, 32}, PartPose.m_171419_((float)38.0f, (float)-48.0f, (float)2.0f));
    }

    public static void buildIntermediateEvolvedDestroyer(PartDefinition root) {
        createThreeHeads(root, new PartPose[]{PartPose.m_171419_((float)-22.0f, (float)-65.0f, (float)-40.0f), PartPose.m_171419_((float)0.0f, (float)-32.0f, (float)-23.0f), PartPose.m_171419_((float)32.0f, (float)-60.0f, (float)-24.0f)});
        IntermediateEvolvedDestroyerBodyModel.createBodyModel(root, 0.2f);
        LowResIntermediateEvolvedDestroyerBodyModel.createBodyModel(root, 0.3f);
        PartDefinition tentacles = root.m_171597_("tentacles");
        CubeListBuilder builder58 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle0", builder58, PartPose.f_171404_), new int[]{12, 18, 20, 23, 22, 25}, PartPose.m_171419_((float)-20.0f, (float)-50.0f, (float)0.0f));
        CubeListBuilder builder59 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle1", builder59, PartPose.f_171404_), new int[]{12, 19, 22, 24, 28, 22}, PartPose.m_171419_((float)20.0f, (float)-55.0f, (float)0.0f));
        CubeListBuilder builder60 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle2", builder60, PartPose.f_171404_), new int[]{12, 17, 23, 22, 28, 24}, PartPose.m_171419_((float)-10.0f, (float)-40.0f, (float)-10.0f));
        CubeListBuilder builder61 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle3", builder61, PartPose.f_171404_), new int[]{12, 14, 19, 20, 20, 22}, PartPose.m_171419_((float)8.0f, (float)-45.0f, (float)-10.0f));
        CubeListBuilder builder62 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle4", builder62, PartPose.f_171404_), new int[]{12, 14, 20, 21, 23, 24}, PartPose.m_171419_((float)-8.0f, (float)-50.0f, (float)12.0f));
        CubeListBuilder builder63 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle5", builder63, PartPose.f_171404_), new int[]{12, 18, 22, 23, 24, 22}, PartPose.m_171419_((float)10.0f, (float)-45.0f, (float)12.0f));
        CubeListBuilder builder64 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle6", builder64, PartPose.f_171404_), new int[]{23, 28, 28, 28, 32, 32}, PartPose.m_171419_((float)30.0f, (float)-155.0f, (float)-10.0f));
        CubeListBuilder builder65 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle7", builder65, PartPose.f_171404_), new int[]{23, 28, 28, 28, 32, 32}, PartPose.m_171419_((float)-60.0f, (float)-120.0f, (float)0.0f));
        CubeListBuilder builder66 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle8", builder66, PartPose.f_171404_), new int[]{23, 28, 28, 28, 32, 32}, PartPose.m_171419_((float)-30.0f, (float)-155.0f, (float)5.0f));
    }

    public static void buildIntermediateEvolvedDevourer(PartDefinition root) {
        createThreeHeads(root, new PartPose[]{PartPose.m_171419_((float)-22.0f, (float)-65.0f, (float)-40.0f), PartPose.m_171419_((float)0.0f, (float)-32.0f, (float)-23.0f), PartPose.m_171419_((float)32.0f, (float)-60.0f, (float)-24.0f)});
        IntermediateEvolvedDevourerBodyModel.createBodyModel(root, 0.2f);
        LowResIntermediateEvolvedDevourerBodyModel.createBodyModel(root, 0.3f);
        PartDefinition tentacles = root.m_171597_("tentacles");
        CubeListBuilder builder67 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle0", builder67, PartPose.f_171404_), new int[]{18, 24, 24, 28, 28, 32}, PartPose.m_171419_((float)-20.0f, (float)-25.0f, (float)5.0f));
        CubeListBuilder builder68 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle1", builder68, PartPose.f_171404_), new int[]{18, 24, 24, 24, 28, 38}, PartPose.m_171419_((float)20.0f, (float)-27.5f, (float)7.0f));
        CubeListBuilder builder69 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle2", builder69, PartPose.f_171404_), new int[]{18, 24, 24, 28, 32, 28}, PartPose.m_171419_((float)-10.0f, (float)-30.0f, (float)-10.0f));
        CubeListBuilder builder70 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle3", builder70, PartPose.f_171404_), new int[]{18, 18, 24, 24, 24, 28}, PartPose.m_171419_((float)8.0f, (float)-34.0f, (float)-6.0f));
        CubeListBuilder builder71 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle4", builder71, PartPose.f_171404_), new int[]{18, 18, 24, 28, 32, 32}, PartPose.m_171419_((float)-8.0f, (float)-25.0f, (float)16.0f));
        CubeListBuilder builder72 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle5", builder72, PartPose.f_171404_), new int[]{18, 20, 26, 28, 32, 28}, PartPose.m_171419_((float)10.0f, (float)-23.0f, (float)19.0f));
        CubeListBuilder builder73 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle6", builder73, PartPose.f_171404_), new int[]{18, 20, 26, 28, 28, 24}, PartPose.m_171419_((float)-2.0f, (float)0.0f, (float)0.0f));
        CubeListBuilder builder74 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacleLarge0", builder74, PartPose.f_171404_), new int[]{16, 20, 24, 28, 32, 28}, PartPose.m_171419_((float)-24.0f, (float)-28.0f, (float)0.0f));
        CubeListBuilder builder75 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacleLarge1", builder75, PartPose.f_171404_), new int[]{20, 20, 24, 24, 28, 32}, PartPose.m_171419_((float)28.0f, (float)-28.0f, (float)2.0f));
    }

    public static void buildPregnantHunchback(PartDefinition root, CubeDeformation def) {
        createSingleHead(root, PartPose.f_171404_);
        LegacyCommandBlockGeometry.populateBase(root, def, false, false, false);
        PregnantHunchbackBodyModel.createBodyModel(root, 1.0f);
        PartDefinition tentacles = root.m_171597_("tentacles");
        CubeListBuilder builder76 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle0", builder76, PartPose.f_171404_), new int[]{18, 24, 24, 24, 24, 32}, PartPose.m_171419_((float)0.0f, (float)0.0f, (float)20.0f));
        CubeListBuilder builder77 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle1", builder77, PartPose.f_171404_), new int[]{18, 24, 24, 24, 24, 32}, PartPose.m_171419_((float)0.0f, (float)0.0f, (float)20.0f));
        CubeListBuilder builder78 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle2", builder78, PartPose.f_171404_), new int[]{18, 24, 24, 24, 24, 32}, PartPose.m_171419_((float)0.0f, (float)20.0f, (float)20.0f));
    }

    public static void buildSegment(PartDefinition root) {
        createThreeHeads(root, new PartPose[]{PartPose.m_171419_((float)16.0f, (float)-20.0f, (float)-30.0f), PartPose.m_171419_((float)0.0f, (float)-23.0f, (float)-35.0f), PartPose.m_171419_((float)-16.0f, (float)-20.0f, (float)-30.0f)});
        SegmentBodyModel.createBodyModel(root, 0.2f);
        LowResSegmentBodyModel.createBodyModel(root, 0.3f);
        PartDefinition tentacles = root.m_171597_("tentacles");
        CubeListBuilder builder79 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle0", builder79, PartPose.f_171404_), new int[]{18, 24, 24, 28, 28, 32}, PartPose.m_171419_((float)20.0f, (float)5.0f, (float)0.0f));
        CubeListBuilder builder80 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle1", builder80, PartPose.f_171404_), new int[]{24, 28, 28, 28, 32, 32}, PartPose.m_171419_((float)5.0f, (float)0.0f, (float)5.0f));
        CubeListBuilder builder81 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle2", builder81, PartPose.f_171404_), new int[]{24, 24, 24, 28, 28, 32}, PartPose.m_171419_((float)-27.5f, (float)-15.0f, (float)-15.0f));
        CubeListBuilder builder82 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle3", builder82, PartPose.f_171404_), new int[]{20, 24, 24, 28, 28, 28}, PartPose.m_171419_((float)-10.0f, (float)-20.0f, (float)-5.0f));
        CubeListBuilder builder83 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle4", builder83, PartPose.f_171404_), new int[]{24, 24, 28, 28, 32, 32}, PartPose.m_171419_((float)35.0f, (float)-20.0f, (float)40.0f));
    }

    public static void buildTornEvolvedDevourer(PartDefinition root) {
        createThreeHeads(root, new PartPose[]{PartPose.m_171419_((float)-22.0f, (float)-65.0f, (float)-40.0f), PartPose.m_171419_((float)0.0f, (float)-32.0f, (float)-23.0f), PartPose.m_171419_((float)32.0f, (float)-60.0f, (float)-24.0f)});
        TornEvolvedDevourerBodyModel.createBodyModel(root, 0.2f);
        LowResTornEvolvedDevourerBodyModel.createBodyModel(root, 0.3f);
        PartDefinition tentacles = root.m_171597_("tentacles");
        CubeListBuilder builder84 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle0", builder84, PartPose.f_171404_), new int[]{18, 24, 24, 28, 28, 32}, PartPose.m_171419_((float)-20.0f, (float)-25.0f, (float)5.0f));
        CubeListBuilder builder85 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle1", builder85, PartPose.f_171404_), new int[]{18, 24, 24, 24, 28, 38}, PartPose.m_171419_((float)20.0f, (float)-27.5f, (float)7.0f));
        CubeListBuilder builder86 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle2", builder86, PartPose.f_171404_), new int[]{18, 24, 24, 28, 32, 28}, PartPose.m_171419_((float)-10.0f, (float)-30.0f, (float)-10.0f));
        CubeListBuilder builder87 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle3", builder87, PartPose.f_171404_), new int[]{18, 18, 24, 24, 24, 28}, PartPose.m_171419_((float)8.0f, (float)-34.0f, (float)-6.0f));
        CubeListBuilder builder88 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle4", builder88, PartPose.f_171404_), new int[]{18, 18, 24, 28, 32, 32}, PartPose.m_171419_((float)-8.0f, (float)-25.0f, (float)16.0f));
        CubeListBuilder builder89 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle5", builder89, PartPose.f_171404_), new int[]{18, 20, 26, 28, 32, 28}, PartPose.m_171419_((float)10.0f, (float)-23.0f, (float)19.0f));
        CubeListBuilder builder90 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacle6", builder90, PartPose.f_171404_), new int[]{18, 20, 26, 28, 28, 24}, PartPose.m_171419_((float)-2.0f, (float)0.0f, (float)0.0f));
        CubeListBuilder builder91 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacleLarge0", builder91, PartPose.f_171404_), new int[]{16, 20, 24, 28, 32, 28}, PartPose.m_171419_((float)-24.0f, (float)-28.0f, (float)0.0f));
        CubeListBuilder builder92 = CubeListBuilder.m_171558_();
        LegacyTentacleGeometry.populateDefinition(tentacles.m_171599_("tentacleLarge1", builder92, PartPose.f_171404_), new int[]{20, 20, 24, 24, 28, 32}, PartPose.m_171419_((float)28.0f, (float)-28.0f, (float)2.0f));
    }
}
