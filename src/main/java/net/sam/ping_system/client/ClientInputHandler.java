package net.sam.ping_system.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.sam.ping_system.PingSystem;
import net.sam.ping_system.client.overlay.PingWheelOverlay;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = PingSystem.MOD_ID, value = Dist.CLIENT)
public class ClientInputHandler {


    @SubscribeEvent
    public static void onPingPress(InputEvent.Key event){

        Minecraft mc = Minecraft.getInstance();

        if(mc.screen != null) {return;}

        int key = event.getKey();
        int action = event.getAction();
        

        

        if(key == ModKeyMappings.PING_KEY.getKey().getValue()){
            if(action == GLFW.GLFW_PRESS){
                if(PingWheelOverlay.pingWheelShowing){
                    mc.mouseHandler.releaseMouse();
                }
                else{
                    PingWheelOverlay.openWheel();
                }

            }else if(action == GLFW.GLFW_RELEASE && PingWheelOverlay.pingWheelShowing){
                PingWheelOverlay.closeWheel();
            }

        }
    }

    @SubscribeEvent
    public static void onMouseClick(InputEvent.MouseButton.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (PingWheelOverlay.pingWheelShowing && event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && event.getAction() == GLFW.GLFW_PRESS) {
            PingWheelOverlay.closeWheel();
        }
    }

}
