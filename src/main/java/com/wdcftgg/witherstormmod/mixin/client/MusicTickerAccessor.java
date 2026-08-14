package com.wdcftgg.witherstormmod.mixin.client;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MusicTicker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MusicTicker.class)
public interface MusicTickerAccessor {
    @Accessor("currentMusic")
    ISound witherstormmod$getCurrentMusic();

    @Accessor("currentMusic")
    void witherstormmod$setCurrentMusic(ISound sound);

    @Accessor("timeUntilNextMusic")
    void witherstormmod$setTimeUntilNextMusic(int ticks);
}
