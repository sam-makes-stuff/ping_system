package net.sam.ping_system.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.sam.ping_system.PingSystem;
import net.sam.ping_system.client.overlay.Ping;
import net.sam.ping_system.client.overlay.PingHandler;
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
                    int selectedPingInd = -1;
                    int i = 0;
                    for(Ping p : PingHandler.pingList){
                        if(p.isSelected){
                            selectedPingInd = i;
                            break;
                        }
                        i += 1;
                    }
                    if(selectedPingInd != -1){
                        Ping selectedPing = PingHandler.pingList.get(selectedPingInd);

                        //players can only remove their own pings
                        if(selectedPing.playerId == Minecraft.getInstance().player.getId()){
                            PingHandler.removePing(selectedPing.playerId, selectedPing.type, selectedPing.x, selectedPing.y, selectedPing.z);
                        }else{
                            return;
                        }
                    }else{
                        PingWheelOverlay.openWheel();
                    }
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
