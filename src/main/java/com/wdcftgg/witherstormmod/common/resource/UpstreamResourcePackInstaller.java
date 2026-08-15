package com.wdcftgg.witherstormmod.common.resource;

import com.wdcftgg.witherstormmod.WitherStormMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;

import java.io.File;
import java.util.List;

public final class UpstreamResourcePackInstaller {

    private UpstreamResourcePackInstaller() {
    }

    @SuppressWarnings("deprecation")
    public static void install() {
        Minecraft minecraft = Minecraft.getMinecraft();
        File source = UpstreamResourceArchive.getArchiveFile();
        addDefaultResourcePack(minecraft, source);
        // Forge 1.12 在模组 preInit 前已经完成第一次资源重载，挂载后必须立即重载。
        minecraft.refreshResources();
        WitherStormMod.LOGGER.info("Mounted external Wither Storm resource pack: {}", source.getAbsolutePath());
    }

    private static void addDefaultResourcePack(Minecraft minecraft, File file) {
        List<IResourcePack> defaultPacks = minecraft.defaultResourcePacks;
        String expectedName = file.getName();
        defaultPacks.removeIf(pack -> expectedName.equals(pack.getPackName()));
        // 原版 1.20 资源包只提供媒体资源，端口内置的 1.12 兼容定义必须保持更高优先级。
        defaultPacks.add(0, new UpstreamResourcePack(file));
    }
}
