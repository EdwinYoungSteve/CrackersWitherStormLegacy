package com.wdcftgg.witherstormmod.core;

import com.wdcftgg.witherstormmod.Tags;
import jakarta.annotation.Nullable;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@SuppressWarnings("unused")
@IFMLLoadingPlugin.Name(Tags.MOD_ID)
/**
 * Queues the mixin configuration before vanilla classes are defined.
 * Both targets in the configuration are vanilla classes, so a late loader
 * would discover them only after they have already been loaded by FML.
 */
public class WitherStormMixinLoader implements IFMLLoadingPlugin, IEarlyMixinLoader {

    @Override
    public String[] getASMTransformerClass() {
        return null;
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {}

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public List<String> getMixinConfigs() {
        return Collections.singletonList("witherstormmod.default.mixin.json");
    }
}
