package net.sam.ping_system.sound;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class PingSound extends AbstractSoundInstance {


    public PingSound(SoundEvent event, SoundSource source,
                              float volume, float pitch,
                              double x, double y, double z) {
        super(event, source, RandomSource.create());
        this.volume = volume;
        this.pitch = pitch;
        this.x = x;
        this.y = y;
        this.z = z;
        this.attenuation = Attenuation.NONE;
    }


}

