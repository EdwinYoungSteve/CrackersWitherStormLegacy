package com.wdcftgg.witherstormmod;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BowelsBossfightSafetyTest {

    @Test
    void symbiontSummoningDoesNotDependOnAnAttackTarget() throws IOException {
        String source = readSource("common", "entity", "SickenedEntities.java");

        assertTrue(source.contains(
                "return entity.getStage() == BossfightStage.SUMMONING;"));
        assertTrue(source.contains("return entity.getStageTicks() > 240;"));
        assertTrue(source.contains(
                "if (entity.getStage() == BossfightStage.SUMMONING) entity.nextStage();"));
    }

    @Test
    void independentBowelsCoreStateIsSentToTheClient() throws IOException {
        String source = readSource("common", "entity", "SupplementalEntities.java");

        assertTrue(source.contains("buffer.writeBoolean(isIndependentBowelsPart());"));
        assertTrue(source.contains(
                "if (buffer.readBoolean()) setIndependentBowelsPart();"));
        assertTrue(source.contains("synchronizePodiumAndCoreHeight(double expectedY)"));
    }

    @Test
    void rushSymbiontSpawnIsTrackedAndRetried() throws IOException {
        String controller = readSource("common", "world",
                "BowelsBossfightController.java");
        String data = readSource("common", "world", "BowelsInstanceData.java");

        assertTrue(controller.contains("ensureRushSymbiont(world, core, instance)"));
        assertTrue(controller.contains("randomNearbyArenaFloorPosition"));
        assertTrue(controller.contains(
                "phase == 10 && instance.rushSymbiontUuid == null"));
        assertTrue(data.contains("tag.setUniqueId(\"RushSymbiont\""));
        assertTrue(data.contains("tag.getUniqueId(\"RushSymbiont\")"));
    }

    private static String readSource(String... relativePath) throws IOException {
        Path path = Path.of("src", "main", "java", "com", "wdcftgg",
                "witherstormmod");
        for (String part : relativePath) path = path.resolve(part);
        return Files.readString(path);
    }
}
