package com.wdcftgg.witherstormmod.mixin;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MixinMappingCompatibilityTest {

    @Test
    void entityLivingBaseWrapOperationSupportsMcpAndSrgTargets() throws IOException {
        String source = readMixin("EntityLivingBaseMixin.java");

        assertTrue(source.contains(
                "method = {\"onDeathUpdate()V\", \"func_70609_aI()V\"}"));
        assertTrue(source.contains(
                "target = \"Lnet/minecraftforge/event/ForgeEventFactory;getExperienceDrop"));
        assertTrue(source.contains("remap = false"));
        assertTrue(source.contains("require = 1"));
    }

    @Test
    void entityFireballWrapOperationSupportsMcpAndSrgTargets() throws IOException {
        String source = readMixin("EntityFireballMixin.java");

        assertTrue(source.contains(
                "method = {\"onUpdate()V\", \"func_70071_h_()V\"}"));
        assertTrue(source.contains(";spawnParticle("));
        assertTrue(source.contains(";func_175688_a("));
        assertTrue(source.contains("remap = false"));
        assertTrue(source.contains("require = 1"));
    }

    private static String readMixin(String fileName) throws IOException {
        return Files.readString(Path.of("src", "main", "java", "com", "wdcftgg",
                "witherstormmod", "mixin", fileName));
    }
}
