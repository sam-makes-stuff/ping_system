package net.sam.ping_system.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.sam.ping_system.PingSystem;
import net.sam.ping_system.client.ClientInputHandler;
import net.sam.ping_system.networking.ModPackets;
import net.sam.ping_system.networking.packets.C2SRequestToPingPacket;
import net.sam.ping_system.render.CustomHudRenderer;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = PingSystem.MOD_ID, value = Dist.CLIENT)
public class PingWheelOverlay {
    private static final ResourceLocation PING_WHEEL_0 = new ResourceLocation(PingSystem.MOD_ID, "textures/client/ping_wheel_0.png");
    private static final ResourceLocation PING_WHEEL_1 = new ResourceLocation(PingSystem.MOD_ID, "textures/client/ping_wheel_1.png");
    private static final ResourceLocation PING_WHEEL_2 = new ResourceLocation(PingSystem.MOD_ID, "textures/client/ping_wheel_2.png");
    private static final ResourceLocation PING_WHEEL_3 = new ResourceLocation(PingSystem.MOD_ID, "textures/client/ping_wheel_3.png");
    private static final ResourceLocation PING_WHEEL_4 = new ResourceLocation(PingSystem.MOD_ID, "textures/client/ping_wheel_4.png");

    private static final double FULL_CIRCLE = Math.PI * 2;
    private static final int ROUNDNESS = 10;

    private static int centerX = 0;
    private static int centerY = 0;

    private static final int centerRadius = 8; //radius at which no selection is made

    private static int currentHovered = -1;

    public static final int slices = 4;

    public static boolean pingWheelShowing = false;

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (pingWheelShowing) {
            Minecraft mc = Minecraft.getInstance();
            double scaleX = (double) mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth();
            double scaleY = (double) mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight();
            int mouseX = (int)(mc.mouseHandler.xpos() * scaleX);
            int mouseY = (int)(mc.mouseHandler.ypos() * scaleY);
            render(event.getGuiGraphics(), mouseX, mouseY, event.getPartialTick());
        }
    }

    public static void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        centerX = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2;
        centerY = Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2;

        currentHovered = getSliceAtMouse(mouseX,mouseY);
        if(currentHovered == 1){
            CustomHudRenderer.renderCustomHudObject(PING_WHEEL_1, centerX, centerY, 128, 128, 1.0f,0.0f, 255,255,255,2);
        } else if (currentHovered == 2) {
            CustomHudRenderer.renderCustomHudObject(PING_WHEEL_2, centerX, centerY, 128, 128, 1.0f,0.0f, 255,255,255,2);
        } else if (currentHovered == 3) {
            CustomHudRenderer.renderCustomHudObject(PING_WHEEL_3, centerX, centerY, 128, 128, 1.0f,0.0f, 255,255,255,2);
        } else if (currentHovered == 4) {
            CustomHudRenderer.renderCustomHudObject(PING_WHEEL_4, centerX, centerY, 128, 128, 1.0f,0.0f, 255,255,255,2);
        } else {
            CustomHudRenderer.renderCustomHudObject(PING_WHEEL_0, centerX, centerY, 128, 128, 1.0f,0.0f, 255,255,255,2);
        }

    }

    private static int getSliceAtMouse(int mouseX, int mouseY) {
        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        if(dx * dx + dy * dy <= centerRadius * centerRadius){return -1;} // mouse in center

        double angle = Math.atan2(dy, dx) + (Math.PI) - ((FULL_CIRCLE / slices)/2);
        if (angle < 0) angle += FULL_CIRCLE;

        int sector = (int) (angle / (FULL_CIRCLE / slices));
        return sector + 1; //sprites use 1 index not 0 index
    }

    public static void closeWheel(){
        pingWheelShowing = false;
        Minecraft mc = Minecraft.getInstance();
        mc.mouseHandler.grabMouse();
        PingHandler.sendPing(currentHovered);

    }

    public static void openWheel(){
        pingWheelShowing = true;
        Minecraft mc = Minecraft.getInstance();
        mc.mouseHandler.releaseMouse();

    }
}
