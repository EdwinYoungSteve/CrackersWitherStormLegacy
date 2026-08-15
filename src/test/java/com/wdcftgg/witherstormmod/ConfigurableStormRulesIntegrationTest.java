package com.wdcftgg.witherstormmod;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurableStormRulesIntegrationTest {

    @Test
    void configuredRequirementsDriveEntityEvolution() throws IOException {
        String source = readSource("common", "entity", "WitherStormEntity.java");

        assertTrue(source.contains(
                "WitherStormConfig.getConfiguredPhaseRequirement(phase)"));
        assertTrue(!source.contains("PHASE_REQUIREMENTS"));
    }

    @Test
    void consumableRulesCoverClusterAndTractorBeamPaths() throws IOException {
        String apiSource = readSource("api", "common", "ai", "witherstorm",
                "clustersource", "BlockClusterSource.java");
        String entitySource = readSource("common", "entity", "WitherStormEntity.java");
        String managerSource = readSource("common", "entity",
                "WitherStormClusterManager.java");

        assertTrue(apiSource.contains("WitherStormBlockRules.canConsume(state)"));
        assertTrue(entitySource.contains(
                "WitherStormBlockRules.canConsume(candidateState)"));
        assertTrue(managerSource.contains("!WitherStormBlockRules.canConsume(state)"));
    }

    @Test
    void dimensionRuleCoversAllSummoningEntrypoints() throws IOException {
        String ritual = readSource("common", "event", "WitherStormSummoningEvents.java");
        String automatic = readSource("common", "world", "WitherStormSpawnManager.java");
        String beacon = readSource("common", "tile", "SuperBeaconTileEntity.java");

        assertTrue(ritual.contains("WitherStormConfig.canSummonInDimension"));
        assertTrue(ritual.contains("wasRestoredFromPersistentData"));
        assertTrue(automatic.contains("WitherStormConfig.canSummonInDimension"));
        assertTrue(beacon.contains("WitherStormConfig.canSummonInDimension"));
    }

    private static String readSource(String... relativePath) throws IOException {
        Path path = Path.of("src", "main", "java", "com", "wdcftgg",
                "witherstormmod");
        for (String part : relativePath) path = path.resolve(part);
        return Files.readString(path);
    }
}
