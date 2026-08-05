package com.wdcftgg.witherstormmod.client.sound;

import com.wdcftgg.witherstormmod.common.world.BowelsDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

public class BowelsLoopSound extends MovingSound {

    public BowelsLoopSound(SoundEvent sound) {
        super(sound, SoundCategory.AMBIENT);
        repeat = true;
        repeatDelay = 0;
        volume = 0.7F;
        pitch = 1.0F;
        attenuationType = AttenuationType.NONE;
    }

    public void stop() {
        donePlaying = true;
    }

    @Override
    public void update() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.world == null || minecraft.player == null
                || minecraft.player.dimension != BowelsDimensions.DIMENSION_ID) {
            donePlaying = true;
            return;
        }
        xPosF = (float) minecraft.player.posX;
        yPosF = (float) minecraft.player.posY;
        zPosF = (float) minecraft.player.posZ;
    }
}
