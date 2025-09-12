package net.sam.ping_system.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.sam.ping_system.PingSystem;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, PingSystem.MOD_ID);

    public static final RegistryObject<SoundEvent> DANGER = SOUND_EVENTS.register("danger",
            () ->  SoundEvent.createVariableRangeEvent(new ResourceLocation(PingSystem.MOD_ID, "danger")));

    public static final RegistryObject<SoundEvent> BREAK = SOUND_EVENTS.register("break",
            () ->  SoundEvent.createVariableRangeEvent(new ResourceLocation(PingSystem.MOD_ID, "break")));

    public static final RegistryObject<SoundEvent> GO = SOUND_EVENTS.register("go",
            () ->  SoundEvent.createVariableRangeEvent(new ResourceLocation(PingSystem.MOD_ID, "go")));

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
