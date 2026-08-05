package com.wdcftgg.witherstormmod.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {

    @Accessor("defaultResourcePacks")
    List<IResourcePack> witherstormmod$getDefaultResourcePacks();
}
