package com.wdcftgg.witherstormmod.client.sound;

import com.wdcftgg.witherstormmod.common.entity.WitherStormEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;

public class WitherStormLoopSound extends MovingSound {
    private final WitherStormEntity storm;

    public WitherStormLoopSound(WitherStormEntity storm, SoundEvent sound) {
        super(sound, SoundCategory.HOSTILE);
        this.storm = storm;
        repeat = true;
        repeatDelay = 0;
        attenuationType = AttenuationType.NONE;
    }

    public void stop() {
        donePlaying = true;
    }

    @Override
    public void update() {
        if (storm.isDead || storm.world != Minecraft.getMinecraft().world || storm.isPlayDeadAiDisabled()) {
            donePlaying = true;
            return;
        }
        xPosF = (float) storm.posX;
        yPosF = (float) storm.posY;
        zPosF = (float) storm.posZ;
        if (Minecraft.getMinecraft().player == null) {
            volume = 0.0F;
            return;
        }
        float distance = Minecraft.getMinecraft().player.getDistance(storm);
        volume = MathHelper.clamp(1.0F - distance / 1024.0F, 0.08F, 0.85F);
        pitch = 1.0F;
    }
}
