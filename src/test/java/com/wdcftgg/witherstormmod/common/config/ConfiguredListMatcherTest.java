package com.wdcftgg.witherstormmod.common.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfiguredListMatcherTest {

    @Test
    void matchesExactIdsNamespacesAndGlobalWildcard() {
        ConfiguredListMatcher.Matcher matcher = ConfiguredListMatcher.compile(new String[] {
                " minecraft:bedrock ", "ExampleMod:*"
        });

        assertTrue(matcher.matches("MINECRAFT:BEDROCK"));
        assertTrue(matcher.matches("examplemod:any_block"));
        assertFalse(matcher.matches("minecraft:stone"));
        assertTrue(ConfiguredListMatcher.compile(new String[] {"*"})
                .matches("anything:anything"));
    }

    @Test
    void appliesEmptyAndPopulatedBlackAndWhiteLists() {
        assertTrue(ConfiguredListMatcher.allows("0", new String[0], false));
        assertFalse(ConfiguredListMatcher.allows("0", new String[0], true));
        assertFalse(ConfiguredListMatcher.allows("-1", new String[] {"-1"}, false));
        assertTrue(ConfiguredListMatcher.allows("-1", new String[] {"-1"}, true));
    }

    @Test
    void phaseRequirementsCannotMoveBackwards() {
        int oldPhase0 = WitherStormConfig.phase0Requirement;
        int oldPhase1 = WitherStormConfig.phase1Requirement;
        try {
            WitherStormConfig.phase0Requirement = 500;
            WitherStormConfig.phase1Requirement = 100;
            assertTrue(WitherStormConfig.getConfiguredPhaseRequirement(1) == 500);
        } finally {
            WitherStormConfig.phase0Requirement = oldPhase0;
            WitherStormConfig.phase1Requirement = oldPhase1;
        }
    }
}
