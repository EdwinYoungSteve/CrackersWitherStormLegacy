package com.wdcftgg.witherstormmod.common.resource;

import net.minecraft.client.resources.FileResourcePack;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class LegacyUpstreamResourcePack extends FileResourcePack {

    private static final byte[] PACK_METADATA = ("{\"pack\":{\"pack_format\":3,\"description\":\"Cracker's Wither Storm Legacy upstream resources\"}}")
            .getBytes(StandardCharsets.UTF_8);
    private static final String[] PADDED_TEXTURES = {"flesh_skele", "flesh_skull_e", "flesh_zomb", "flesh_zomb_e"};

    public LegacyUpstreamResourcePack(File resourcePackFile) {
        super(resourcePackFile);
    }

    @Override
    protected InputStream getInputStreamByName(String name) throws IOException {
        if ("pack.mcmeta".equals(name)) {
            return new ByteArrayInputStream(PACK_METADATA);
        }
        if (LegacyModelResourceConverter.handles(name)) {
            String sourceName = LegacyModelResourceConverter.sourceName(name);
            try (InputStream source = super.getInputStreamByName(sourceName)) {
                return new ByteArrayInputStream(
                        LegacyModelResourceConverter.convert(name, source.readAllBytes()));
            }
        }
        if (LegacyLanguageResourceConverter.handles(name)) {
            byte[] localized = readResource(LegacyLanguageResourceConverter.sourceName(name));
            byte[] english = readResource(LegacyLanguageResourceConverter.englishSourceName());
            return new ByteArrayInputStream(LegacyLanguageResourceConverter.convert(localized, english));
        }
        if (isModernDefinition(name)) {
            throw new FileNotFoundException(name);
        }
        String mappedName = mapLegacyTexturePath(name);
        if (isBlockTexture(mappedName)) {
            InputStream normalized = normalizedBlockTexture(mappedName);
            if (normalized != null) {
                return normalized;
            }
        }
        return super.getInputStreamByName(mappedName);
    }

    @Override
    public boolean hasResourceName(String name) {
        if ("pack.mcmeta".equals(name)) {
            return true;
        }
        if (LegacyModelResourceConverter.handles(name)) {
            return super.hasResourceName(LegacyModelResourceConverter.sourceName(name));
        }
        if (LegacyLanguageResourceConverter.handles(name)) {
            return super.hasResourceName(LegacyLanguageResourceConverter.sourceName(name))
                    && super.hasResourceName(LegacyLanguageResourceConverter.englishSourceName());
        }
        if (isModernDefinition(name)) {
            return false;
        }
        String mappedName = mapLegacyTexturePath(name);
        if (isBlockTexture(mappedName) && hasNormalizedBlockTexture(mappedName)) {
            return true;
        }
        return super.hasResourceName(mappedName);
    }

    private InputStream normalizedBlockTexture(String name) throws IOException {
        if (name.endsWith(".mcmeta") || !hasResourceNameDirect(name)) {
            return null;
        }
        String metadataName = name + ".mcmeta";
        if (hasResourceNameDirect(metadataName)) {
            return null;
        }
        BufferedImage image;
        try (InputStream source = super.getInputStreamByName(name)) {
            image = ImageIO.read(source);
        }
        if (image == null) {
            return null;
        }
        int largestDimension = Math.max(image.getWidth(), image.getHeight());
        int targetSize = 1;
        while (targetSize < largestDimension) {
            targetSize <<= 1;
        }
        if (image.getWidth() == targetSize && image.getHeight() == targetSize) {
            image.flush();
            return super.getInputStreamByName(name);
        }
        BufferedImage normalized = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = normalized.createGraphics();
        try {
            graphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            if (isPaddedStaticTexture(name)) {
                graphics.drawImage(image, 0, 0, null);
            } else {
                graphics.drawImage(image, 0, 0, targetSize, targetSize, null);
            }
        } finally {
            graphics.dispose();
            image.flush();
        }
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(normalized, "png", output);
            return new ByteArrayInputStream(output.toByteArray());
        } finally {
            normalized.flush();
        }
    }

    private boolean hasNormalizedBlockTexture(String name) {
        if (!hasResourceNameDirect(name)) {
            return false;
        }
        return !hasResourceNameDirect(name + ".mcmeta");
    }

    private boolean hasResourceNameDirect(String name) {
        return super.hasResourceName(name);
    }

    private static boolean isBlockTexture(String name) {
        return name.startsWith("assets/")
                && (name.contains("/textures/blocks/") || name.contains("/textures/block/"))
                && name.endsWith(".png");
    }

    private static String mapLegacyTexturePath(String name) {
        return name.replace("/textures/blocks/", "/textures/block/")
                .replace("/textures/items/", "/textures/item/");
    }

    private static boolean isPaddedStaticTexture(String name) {
        int separator = name.lastIndexOf('/');
        String base = separator < 0 ? name : name.substring(separator + 1);
        if (base.endsWith(".png")) {
            base = base.substring(0, base.length() - 4);
        }
        for (String padded : PADDED_TEXTURES) {
            if (padded.equals(base)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isModernDefinition(String name) {
        return name.startsWith("assets/witherstormmod/lang/")
                || name.startsWith("assets/minecraft/models/")
                || name.startsWith("assets/minecraft/blockstates/");
    }

    private byte[] readResource(String name) throws IOException {
        try (InputStream input = super.getInputStreamByName(name)) {
            return input.readAllBytes();
        }
    }
}
