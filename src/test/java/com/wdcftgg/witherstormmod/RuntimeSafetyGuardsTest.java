package com.wdcftgg.witherstormmod;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RuntimeSafetyGuardsTest {

    @Test
    void unresolvedPositionedSoundKeepsTheOriginalResult() throws IOException {
        String source = readSource("client", "WitherStormClientEvents.java");

        assertTrue(source.contains("catch (NullPointerException ignored)"));
        assertTrue(source.contains("Keeping the original result"));
    }

    @Test
    void orphanedWitherSkullDoesNotActAsTheExplosionSource() throws IOException {
        String source = readSource("common", "entity", "SupplementalEntities.java");

        assertTrue(source.contains(
                "Entity explosionSource = shootingEntity == null ? null : this;"));
        assertTrue(source.contains("world.newExplosion(explosionSource"));
    }

    private static String readSource(String... relativePath) throws IOException {
        Path path = Path.of("src", "main", "java", "com", "wdcftgg",
                "witherstormmod");
        for (String part : relativePath) path = path.resolve(part);
        return Files.readString(path);
    }
}
