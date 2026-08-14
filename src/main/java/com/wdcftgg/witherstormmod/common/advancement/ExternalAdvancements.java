package com.wdcftgg.witherstormmod.common.advancement;

import com.wdcftgg.witherstormmod.Tags;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wdcftgg.witherstormmod.WitherStormMod;
import com.wdcftgg.witherstormmod.common.resource.AdvancementResourceConverter;
import com.wdcftgg.witherstormmod.common.resource.UpstreamResourceArchive;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ExternalAdvancements {

    private static final int EXPECTED_ADVANCEMENT_COUNT = 66;

    private ExternalAdvancements() {
    }

    public static int install(MinecraftServer server) throws IOException {
        File advancementDirectory = server.getActiveAnvilConverter().getFile(
                server.getFolderName(), "data/advancements");
        return install(advancementDirectory);
    }

    public static int install(File advancementDirectory) throws IOException {
        Map<File, GeneratedAdvancement> generated = new LinkedHashMap<File, GeneratedAdvancement>();
        for (String relativeName : AdvancementResourceConverter.MAIN_CHAIN) {
            String entryName = AdvancementResourceConverter.sourceEntry(relativeName);
            JsonObject converted;
            try (InputStream source = UpstreamResourceArchive.open(entryName)) {
                converted = AdvancementResourceConverter.convert(entryName, source);
            }
            File target = new File(new File(advancementDirectory, Tags.MOD_ID), relativeName);
            generated.put(target, new GeneratedAdvancement(entryName,
                    AdvancementResourceConverter.serialize(converted)));
        }
        for (String entryName : UpstreamResourceArchive.listEntries(
                AdvancementResourceConverter.ADVANCEMENT_PREFIX, ".json")) {
            String relativeName = entryName.substring(
                    AdvancementResourceConverter.ADVANCEMENT_PREFIX.length());
            if (!relativeName.startsWith("recipes/")) continue;
            JsonObject converted;
            try (InputStream source = UpstreamResourceArchive.open(entryName)) {
                converted = AdvancementResourceConverter.convertRecipeAdvancement(entryName, source);
            }
            if (converted == null) continue;
            File target = new File(new File(advancementDirectory, Tags.MOD_ID), relativeName);
            generated.put(target, new GeneratedAdvancement(entryName,
                    AdvancementResourceConverter.serialize(converted)));
        }
        if (generated.size() != EXPECTED_ADVANCEMENT_COUNT) {
            throw new IOException("Expected " + EXPECTED_ADVANCEMENT_COUNT
                    + " external advancements but prepared " + generated.size());
        }

        for (Map.Entry<File, GeneratedAdvancement> entry : generated.entrySet()) {
            verifyWritable(entry.getKey(), entry.getValue().sourceEntry);
        }
        for (Map.Entry<File, GeneratedAdvancement> entry : generated.entrySet()) {
            writeAtomically(entry.getKey(), entry.getValue().json);
        }
        WitherStormMod.LOGGER.info("Prepared {} advancements from the external Wither Storm archive",
                generated.size());
        return generated.size();
    }

    private static void verifyWritable(File target, String sourceEntry) throws IOException {
        if (!target.exists()) return;
        if (!target.isFile()) {
            throw new IOException("Advancement target is not a file: " + target.getAbsolutePath());
        }
        JsonObject existing;
        try {
            String json = Files.readString(target.toPath(), StandardCharsets.UTF_8);
            existing = JsonParser.parseString(json).getAsJsonObject();
        } catch (RuntimeException exception) {
            throw new IOException("Refusing to replace an unreadable custom advancement: "
                    + target.getAbsolutePath(), exception);
        }
        if (!AdvancementResourceConverter.isGeneratedFile(existing, sourceEntry)) {
            throw new IOException("Refusing to replace a custom advancement: "
                    + target.getAbsolutePath());
        }
    }

    private static void writeAtomically(File target, String json) throws IOException {
        File parent = target.getParentFile();
        if (!parent.isDirectory() && !parent.mkdirs() && !parent.isDirectory()) {
            throw new IOException("Unable to create advancement directory: " + parent.getAbsolutePath());
        }
        Path temporary = Files.createTempFile(parent.toPath(), target.getName(), ".tmp");
        try {
            Files.writeString(temporary, json, StandardCharsets.UTF_8);
            try {
                Files.move(temporary, target.toPath(), StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static final class GeneratedAdvancement {
        private final String sourceEntry;
        private final String json;

        private GeneratedAdvancement(String sourceEntry, String json) {
            this.sourceEntry = sourceEntry;
            this.json = json;
        }
    }
}
