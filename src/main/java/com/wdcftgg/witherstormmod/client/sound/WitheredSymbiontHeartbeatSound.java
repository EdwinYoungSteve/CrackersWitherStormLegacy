package com.wdcftgg.witherstormmod.client.sound;

import com.wdcftgg.witherstormmod.common.entity.SickenedEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;

public final class WitheredSymbiontHeartbeatSound extends MovingSound {
    private static final float FADE_TICKS = 20.0F;
    private static final float MAXIMUM_VOLUME = 7.0F;

    private final SickenedEntities.WitheredSymbiontEntity symbiont;
    private float fade;
    private boolean stopping;

    public WitheredSymbiontHeartbeatSound(SickenedEntities.WitheredSymbiontEntity symbiont,
                                           SoundEvent sound) {
        super(sound, SoundCategory.AMBIENT);
        this.symbiont = symbiont;
        repeat = true;
        repeatDelay = 0;
        volume = 0.0F;
        pitch = 1.0F;
    }

    @Override
    public void update() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (symbiont.isDead || symbiont.world != minecraft.world || !symbiont.isEntityAlive()) {
            donePlaying = true;
            return;
        }
        xPosF = (float) symbiont.posX;
        yPosF = (float) symbiont.posY;
        zPosF = (float) symbiont.posZ;

        if (!symbiont.isVulnerable()) stopping = true;
        if (stopping) {
            fade -= 1.0F;
            if (fade <= 0.0F) {
                donePlaying = true;
                volume = 0.0F;
                return;
            }
        } else if (fade < FADE_TICKS) {
            fade += 1.0F;
        }
        volume = MathHelper.clamp(fade / FADE_TICKS, 0.0F, 1.0F) * MAXIMUM_VOLUME;
    }

    public void stop() {
        if (stopping) return;
        stopping = true;
        fade = Math.max(fade, FADE_TICKS);
    }

    public void stopImmediately() {
        donePlaying = true;
    }
}
