package net.sam.ping_system.util;


import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.sam.ping_system.PingSystem;

@Mod.EventBusSubscriber(modid = PingSystem.MOD_ID, value = Dist.CLIENT)
public class PartialTickUtils {

    public static float currentPartialTick = 0.0f;
    public static float lastPartialTick = 0.0f;
    public static float timeDif = 0.0f;


    @SubscribeEvent
    public static void updateTimeDif(RenderGuiEvent.Post event){
        currentPartialTick = event.getPartialTick();
        if(lastPartialTick > currentPartialTick){
            timeDif = (1 - lastPartialTick + currentPartialTick); //ticks
        }else{
            timeDif = (currentPartialTick - lastPartialTick); //ticks
        }
        lastPartialTick = currentPartialTick;
    }
}
