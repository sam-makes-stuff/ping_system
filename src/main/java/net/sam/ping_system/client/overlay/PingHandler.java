package net.sam.ping_system.client.overlay;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.sam.ping_system.PingSystem;
import net.sam.ping_system.networking.ModPackets;
import net.sam.ping_system.networking.packets.C2SRequestToPingPacket;
import net.sam.ping_system.render.CustomHudRenderer;
import net.sam.ping_system.util.PartialTickUtils;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = PingSystem.MOD_ID, value = Dist.CLIENT)
public class PingHandler {

    private static final ResourceLocation BASIC_PING = new ResourceLocation(PingSystem.MOD_ID, "textures/client/basic.png");
    private static final ResourceLocation MOVE_PING = new ResourceLocation(PingSystem.MOD_ID, "textures/client/move.png");
    private static final ResourceLocation ATTACK_PING = new ResourceLocation(PingSystem.MOD_ID, "textures/client/attack.png");
    private static final ResourceLocation DANGER_PING = new ResourceLocation(PingSystem.MOD_ID, "textures/client/danger.png");
    private static final ResourceLocation BREAK_PING = new ResourceLocation(PingSystem.MOD_ID, "textures/client/break.png");

    private static final ResourceLocation PING_0 = new ResourceLocation(PingSystem.MOD_ID, "textures/client/ping_0.png");
    private static final ResourceLocation PING_1 = new ResourceLocation(PingSystem.MOD_ID, "textures/client/ping_1.png");
    private static final ResourceLocation PING_2 = new ResourceLocation(PingSystem.MOD_ID, "textures/client/ping_2.png");
    private static final ResourceLocation PING_3 = new ResourceLocation(PingSystem.MOD_ID, "textures/client/ping_3.png");
    private static final ResourceLocation PING_4 = new ResourceLocation(PingSystem.MOD_ID, "textures/client/ping_4.png");

    private static final ResourceLocation ARROW_0 = new ResourceLocation(PingSystem.MOD_ID, "textures/client/arrow_0.png");
    private static final ResourceLocation ARROW_1 = new ResourceLocation(PingSystem.MOD_ID, "textures/client/arrow_1.png");
    private static final ResourceLocation ARROW_2 = new ResourceLocation(PingSystem.MOD_ID, "textures/client/arrow_2.png");
    private static final ResourceLocation ARROW_3 = new ResourceLocation(PingSystem.MOD_ID, "textures/client/arrow_3.png");
    private static final ResourceLocation ARROW_4 = new ResourceLocation(PingSystem.MOD_ID, "textures/client/arrow_4.png");

    private static final ResourceLocation PING_OUTLINE = new ResourceLocation(PingSystem.MOD_ID, "textures/client/ping_outline.png");
    private static final ResourceLocation ARROW_OUTLINE = new ResourceLocation(PingSystem.MOD_ID, "textures/client/arrow_outline.png");

    private static final double distance = 512.0;
    private static double timeSinceLastPing = 0.0;
    private static final double pingCooldown = 12.0; //in ticks
    private static final float fadeRadius = 0.4f;
    private static final float fadeMin = 0.04f;

    private static int centerX = 0;
    private static int centerY = 0;

    public static List<Ping> pingList = new ArrayList<>();

    private static float edgePixels = 128.0f;

    public static void newPing(int playerId, int type, double x, double y, double z, int r, int g, int b){
        Ping ping = new Ping(playerId,type,x,y,z,r,g,b);
        pingList.add(ping);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event){
        List<Ping> temp = new ArrayList<>();
        for(Ping f : pingList){
            if(!f.update()){
                temp.add(f);
            }
        }
        pingList = temp;
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Pre event) {

        centerX = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2;
        centerY = Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2;

        for(Ping p : pingList){
            p.tick();
            Vec3 pos = new Vec3(p.x, p.y, p.z);
            Vector4f pos4f = CustomHudRenderer.worldToScreenTransform(pos);
            Vec2 screenRenderPos = CustomHudRenderer.worldToScreenWithEdgeClip(pos4f, edgePixels);
            boolean isEdge = CustomHudRenderer.isOffScreen(pos4f, edgePixels);

            float x = screenRenderPos.x;
            float y = screenRenderPos.y;
            if(isEdge){

                float distLeft = x;
                float distRight  = Minecraft.getInstance().getWindow().getGuiScaledWidth() - x;
                float distTop = y;
                float distBottom = Minecraft.getInstance().getWindow().getGuiScaledHeight() - y;

                // Find nearest edge
                float min = Math.min(Math.min(distLeft, distRight), Math.min(distTop, distBottom));
                float arrowRotation = 0f;
                if (min == distTop) {
                    arrowRotation = 0f;   // Up
                } else if (min == distRight) {
                    arrowRotation = 90f;  // Right
                } else if (min == distBottom) {
                    arrowRotation = 180f; // Down
                } else {
                    arrowRotation = 270f; // Left
                }

                float offset = 16f;
                float offsetX = offset * -Mth.sin(arrowRotation * (Mth.PI/180));
                float offsetY = offset * Mth.cos(arrowRotation * (Mth.PI/180));

                if(p.type == 1){
                    CustomHudRenderer.renderCustomHudObject(ARROW_1,x, y, 12,12,1,arrowRotation,255,255,255,255);
                    CustomHudRenderer.renderCustomHudObject(MOVE_PING,x + offsetX, y + offsetY, 16,16,1,0,255,255,255,255);
                } else if (p.type == 2) {
                    CustomHudRenderer.renderCustomHudObject(ARROW_2,x, y, 12,12,1,arrowRotation,255,255,255,255);
                    CustomHudRenderer.renderCustomHudObject(ATTACK_PING,x+ offsetX, y + offsetY, 16,16,1,0,255,255,255,255);
                }else if (p.type == 3) {
                    CustomHudRenderer.renderCustomHudObject(ARROW_3,x, y, 12,12,1,arrowRotation,255,255,255,255);
                    CustomHudRenderer.renderCustomHudObject(DANGER_PING,x+ offsetX, y + offsetY, 16,16,1,0,255,255,255,255);
                }else if (p.type == 4) {
                    CustomHudRenderer.renderCustomHudObject(ARROW_4,x, y, 12,12,1,arrowRotation,255,255,255,255);
                    CustomHudRenderer.renderCustomHudObject(BREAK_PING,x+ offsetX, y + offsetY, 16,16,1,0,255,255,255,255);
                }else{
                    CustomHudRenderer.renderCustomHudObject(ARROW_0,x, y, 12,12,1,arrowRotation,255,255,255,255);
                    if(arrowRotation == 0){
                        CustomHudRenderer.renderCustomHudObject(BASIC_PING,x+ offsetX, y + offsetY, 16,16,1,180,255,255,255,255);
                    } else if (arrowRotation == 180) {
                        CustomHudRenderer.renderCustomHudObject(BASIC_PING,x+ offsetX, y + offsetY, 16,16,1,0,255,255,255,255);
                    }else {
                        CustomHudRenderer.renderCustomHudObject(BASIC_PING,x+ offsetX, y + offsetY, 16,16,1,-arrowRotation,255,255,255,255);
                    }

                }

                CustomHudRenderer.renderCustomHudObject(ARROW_OUTLINE,x, y, 12,12,1,arrowRotation,p.r,p.g,p.b,255);

            }else{

                float xRel = centerX - x;
                float yRel = centerY - y;
                float length = (Mth.sqrt(centerX * centerX + centerY * centerY));
                float dist = Mth.sqrt(xRel * xRel + yRel * yRel) / fadeRadius;
                float opacity = 1 - ((length - dist) / (length));
                opacity = Mth.clamp(opacity, fadeMin, 1);
                int alpha = (int)(opacity * 255);

                if(p.type == 1){
                    CustomHudRenderer.renderCustomHudObject(PING_1,x, y, 12,12,1,0,255,255,255,alpha);
                    CustomHudRenderer.renderCustomHudObject(MOVE_PING,x, y - 16f, 16,16,1,0,255,255,255,alpha);
                } else if (p.type == 2) {
                    CustomHudRenderer.renderCustomHudObject(PING_2,x, y, 12,12,1,0,255,255,255,alpha);
                    CustomHudRenderer.renderCustomHudObject(ATTACK_PING,x, y - 16f, 16,16,1,0,255,255,255,alpha);
                }else if (p.type == 3) {
                    CustomHudRenderer.renderCustomHudObject(PING_3,x, y, 12,12,1,0,255,255,255,alpha);
                    CustomHudRenderer.renderCustomHudObject(DANGER_PING,x, y - 16f, 16,16,1,0,255,255,255,alpha);
                }else if (p.type == 4) {
                    CustomHudRenderer.renderCustomHudObject(PING_4,x, y, 12,12,1,0,255,255,255,alpha);
                    CustomHudRenderer.renderCustomHudObject(BREAK_PING,x, y - 16f, 16,16,1,0,255,255,255,alpha);
                }else{
                    CustomHudRenderer.renderCustomHudObject(PING_0,x, y, 12,12,1,0,255,255,255,alpha);
                    CustomHudRenderer.renderCustomHudObject(BASIC_PING,x, y - 16f, 16,16,1,0,255,255,255,alpha);
                }
                CustomHudRenderer.renderCustomHudObject(PING_OUTLINE,x, y, 12,12,1,0,p.r,p.g,p.b,alpha);
            }
        }
    }
    @SubscribeEvent
    public static void updateCooldown(RenderGuiEvent.Pre event){
        if (timeSinceLastPing < pingCooldown){
            timeSinceLastPing += PartialTickUtils.timeDif;
        }
    }

    public static void sendPing(int type){

        if(!(timeSinceLastPing >= pingCooldown)){
            return;
        }
        timeSinceLastPing = 0.0;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Vec3 start = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = start.add(look.scale(distance));
        BlockHitResult hitResult = player.level().clip(new ClipContext(
                start,
                end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        ));

        if(hitResult.getType() == HitResult.Type.MISS){return;}
        else{
            BlockPos blockPos = hitResult.getBlockPos();
            Vec3 pos = hitResult.getLocation();
            double x = pos.x();
            double y = pos.y();
            double z = pos.z();
            ModPackets.sendToServer(new C2SRequestToPingPacket(mc.player.getId(), type, x, y, z, blockPos));
        }
    }
}
