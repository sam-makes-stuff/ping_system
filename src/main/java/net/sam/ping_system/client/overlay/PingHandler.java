package net.sam.ping_system.client.overlay;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.scores.Team;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.sam.ping_system.PingSystem;
import net.sam.ping_system.config.ClientConfig;
import net.sam.ping_system.config.ServerConfig;
import net.sam.ping_system.networking.ModPackets;
import net.sam.ping_system.networking.packets.C2SRequestToPingPacket;
import net.sam.ping_system.render.CustomHudRenderer;
import net.sam.ping_system.util.ConfigUtils;
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

    private static final ResourceLocation BASIC_OUTLINE = new ResourceLocation(PingSystem.MOD_ID, "textures/client/basic_outline.png");
    private static final ResourceLocation DANGER_OUTLINE = new ResourceLocation(PingSystem.MOD_ID, "textures/client/danger_outline.png");
    private static final ResourceLocation MOVE_OUTLINE = new ResourceLocation(PingSystem.MOD_ID, "textures/client/move_outline.png");
    private static final ResourceLocation BREAK_OUTLINE = new ResourceLocation(PingSystem.MOD_ID, "textures/client/break_outline.png");
    private static final ResourceLocation ATTACK_OUTLINE = new ResourceLocation(PingSystem.MOD_ID, "textures/client/attack_outline.png");

    private static final int PING_SIZE_PIXELS = 7;
    private static final double distance = 512.0;
    private static double timeSinceLastPing = 0.0;

    private static final float selectRadius = 25;

    private static int centerX = 0;
    private static int centerY = 0;

    private static int pingGhostAlpha = 50;

    public static List<Ping> pingList = new ArrayList<>();
    public static List<PingGhost> pingGhostList = new ArrayList<>();

    private static float edgePixels;
    private static float sizeMult;
    private static int maxPings;
    private static double pingCooldown;

    public static void newPing(int playerId, int type, double x, double y, double z, int r, int g, int b, BlockPos blockPos, Team team, int attachedId) {
        Ping ping = new Ping(playerId, type, x, y, z, r, g, b, blockPos, team, attachedId);
        pingList.add(ping);
        if (pingList.size() > maxPings) {
            pingList.remove(0);
        }
    }

    public static void newPingGhost(int type, double x, double y, double z, int r, int g, int b, Team team) {
        PingGhost pingGhost = new PingGhost(type, x, y, z, r, g, b, team);
        pingGhostList.add(pingGhost);
        if (pingList.size() > maxPings) {
            pingList.remove(0);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        List<Ping> tempPing = new ArrayList<>();
        List<PingGhost> tempPingGhost = new ArrayList<>();

        int selectedPing = -1;

        for (Ping p : pingList) {
            if (!p.update() && !p.toRemove) {
                tempPing.add(p);
            }
        }

        for (PingGhost p : pingGhostList) {
            if (!p.toRemove) {
                tempPingGhost.add(p);
            }
        }


        pingList = tempPing;
        pingGhostList = tempPingGhost;
    }


    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Pre event) {
        centerX = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2;
        centerY = Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2;


        for (PingGhost p : pingGhostList) {
            p.tick();
            Vec3 pos = new Vec3(p.x, p.y, p.z);
            Vector4f pos4f = CustomHudRenderer.worldToScreenTransform(pos);
            Vec2 screenRenderPos = CustomHudRenderer.worldToScreenWithEdgeClip(pos4f, edgePixels);
            boolean isEdge = CustomHudRenderer.isOffScreen(pos4f, edgePixels);

            float x = screenRenderPos.x;
            float y = screenRenderPos.y;

            if (p.type == 1) {
                CustomHudRenderer.renderCustomHudObject(PING_1, x, y, PingGhost.baseSize, PingGhost.baseSize, p.sizeMult, 0, 255, 255, 255, pingGhostAlpha);
            } else if (p.type == 2) {
                CustomHudRenderer.renderCustomHudObject(PING_2, x, y, PingGhost.baseSize, PingGhost.baseSize, p.sizeMult, 0, 255, 255, 255, pingGhostAlpha);
            } else if (p.type == 3) {
                CustomHudRenderer.renderCustomHudObject(PING_3, x, y, PingGhost.baseSize, PingGhost.baseSize, p.sizeMult, 0, 255, 255, 255, pingGhostAlpha);
            } else if (p.type == 4) {
                CustomHudRenderer.renderCustomHudObject(PING_4, x, y, PingGhost.baseSize, PingGhost.baseSize, p.sizeMult, 0, 255, 255, 255, pingGhostAlpha);
            } else {
                CustomHudRenderer.renderCustomHudObject(PING_0, x, y, PingGhost.baseSize, PingGhost.baseSize, p.sizeMult, 0, 255, 255, 255, pingGhostAlpha);
            }

        }

        int i = 0;
        int selected = -1;
        float minDist = Float.POSITIVE_INFINITY;

        for (Ping p : pingList) {
            p.tick(event.getPartialTick());
            Vec3 pos = new Vec3(p.x, p.y, p.z);
            Vector4f pos4f = CustomHudRenderer.worldToScreenTransform(pos);
            Vec2 screenRenderPos = CustomHudRenderer.worldToScreenWithEdgeClip(pos4f, edgePixels);
            boolean isEdge = CustomHudRenderer.isOffScreen(pos4f, edgePixels);

            float x = screenRenderPos.x;
            float y = screenRenderPos.y;

            float xRel = centerX - x;
            float yRel = centerY - y;
            float dist = Mth.sqrt(xRel * xRel + yRel * yRel) * (float) (Minecraft.getInstance().getWindow().getGuiScale());
            if (dist <= selectRadius) {
                if (dist < minDist) {
                    minDist = dist;
                    selected = i;
                }
            }
            i += 1;

            float worldDist = (float)((Minecraft.getInstance().player.position().subtract(new Vec3(p.x, p.y, p.z))).length());
            if (isEdge) { //if off-screen

                float distLeft = x;
                float distRight = Minecraft.getInstance().getWindow().getGuiScaledWidth() - x;
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
                float offsetX = offset * -Mth.sin(arrowRotation * (Mth.PI / 180));
                float offsetY = offset * Mth.cos(arrowRotation * (Mth.PI / 180));

                CustomHudRenderer.renderItemSprite(p.itemStack, x + (offsetX * sizeMult), y + (offsetY * sizeMult), sizeMult, 0.0f, 255);
                if (p.type == 1) {
                    CustomHudRenderer.renderCustomHudObject(ARROW_1, x, y, 12, 12, sizeMult, arrowRotation, 255, 255, 255, 255);
                    //CustomHudRenderer.renderCustomHudObject(MOVE_PING,x + offsetX, y + offsetY, 16,16,1,0,255,255,255,255);
                } else if (p.type == 2) {
                    CustomHudRenderer.renderCustomHudObject(ARROW_2, x, y, 12, 12, sizeMult, arrowRotation, 255, 255, 255, 255);
                    //CustomHudRenderer.renderCustomHudObject(ATTACK_PING,x+ offsetX, y + offsetY, 16,16,1,0,255,255,255,255);
                } else if (p.type == 3) {
                    CustomHudRenderer.renderCustomHudObject(ARROW_3, x, y, 12, 12, sizeMult, arrowRotation, 255, 255, 255, 255);
                    //CustomHudRenderer.renderCustomHudObject(DANGER_PING,x+ offsetX, y + offsetY, 16,16,1,0,255,255,255,255);
                } else if (p.type == 4) {
                    CustomHudRenderer.renderCustomHudObject(ARROW_4, x, y, 12, 12, sizeMult, arrowRotation, 255, 255, 255, 255);
                    //CustomHudRenderer.renderCustomHudObject(BREAK_PING,x+ offsetX, y + offsetY, 16,16,1,0,255,255,255,255);
                } else {
                    CustomHudRenderer.renderCustomHudObject(ARROW_0, x, y, 12, 12, sizeMult, arrowRotation, 255, 255, 255, 255);

                }
                if(p.team != null){
                    CustomHudRenderer.renderCustomHudObject(ARROW_OUTLINE, x, y, 14, 14, sizeMult, arrowRotation, p.r, p.g, p.b, 255);
                }



                CustomHudRenderer.renderText(event.getGuiGraphics(),String.format("%.1fm", worldDist), x, y + (16 * sizeMult), p.r,p.g,p.b,255,sizeMult,0.0f);

            } else { //if on screen

                int alpha = 255;

                CustomHudRenderer.renderItemSprite(p.itemStack, x + (16 * sizeMult), y, sizeMult, 0.0f, alpha);
                if (p.type == 1) {
                    CustomHudRenderer.renderCustomHudObject(PING_1, x, y, PING_SIZE_PIXELS, PING_SIZE_PIXELS, sizeMult, 0, 255, 255, 255, alpha);
                    CustomHudRenderer.renderCustomHudObject(MOVE_PING, x, y - (20f * sizeMult), 20, 20, sizeMult, 0, 255, 255, 255, alpha);
                    if (p.team != null) {
                        CustomHudRenderer.renderCustomHudObject(MOVE_OUTLINE, x, y - (20f * sizeMult), 22, 22, sizeMult, 0, p.r, p.g, p.b, alpha);
                    }
                } else if (p.type == 2) {
                    CustomHudRenderer.renderCustomHudObject(PING_2, x, y, PING_SIZE_PIXELS, PING_SIZE_PIXELS, sizeMult, 0, 255, 255, 255, alpha);
                    CustomHudRenderer.renderCustomHudObject(ATTACK_PING, x, y - (16f * sizeMult), 18, 16, sizeMult, 0, 255, 255, 255, alpha);
                    if (p.team != null) {
                        CustomHudRenderer.renderCustomHudObject(ATTACK_OUTLINE, x, y - (16f * sizeMult), 20, 18, sizeMult, 0, p.r, p.g, p.b, alpha);
                    }
                } else if (p.type == 3) {
                    CustomHudRenderer.renderCustomHudObject(PING_3, x, y, PING_SIZE_PIXELS, PING_SIZE_PIXELS, sizeMult, 0, 255, 255, 255, alpha);
                    CustomHudRenderer.renderCustomHudObject(DANGER_PING, x, y - (18f * sizeMult), 20, 18, sizeMult, 0, 255, 255, 255, alpha);
                    if (p.team != null) {
                        CustomHudRenderer.renderCustomHudObject(DANGER_OUTLINE, x, y - (18f * sizeMult), 22, 20, sizeMult, 0, p.r, p.g, p.b, alpha);
                    }
                } else if (p.type == 4) {
                    CustomHudRenderer.renderCustomHudObject(PING_4, x, y, PING_SIZE_PIXELS, PING_SIZE_PIXELS, sizeMult, 0, 255, 255, 255, alpha);
                    CustomHudRenderer.renderCustomHudObject(BREAK_PING, x, y - (13f * sizeMult), 13, 13, sizeMult, 0, 255, 255, 255, alpha);
                    if (p.team != null) {
                        CustomHudRenderer.renderCustomHudObject(BREAK_OUTLINE, x, y - (13f * sizeMult), 15, 15, sizeMult, 0, p.r, p.g, p.b, alpha);
                    }
                } else {
                    CustomHudRenderer.renderCustomHudObject(PING_0, x, y, PING_SIZE_PIXELS, PING_SIZE_PIXELS, sizeMult, 0, 255, 255, 255, alpha);
                    CustomHudRenderer.renderCustomHudObject(BASIC_PING, x, y - (16f * sizeMult), 16, 16, sizeMult, 0, 255, 255, 255, alpha);
                    if (p.team != null) {
                        CustomHudRenderer.renderCustomHudObject(BASIC_OUTLINE, x, y - (16f * sizeMult), 18, 18, sizeMult, 0, p.r, p.g, p.b, alpha);
                    }
                }
                if (p.team != null) {
                    CustomHudRenderer.renderCustomHudObject(PING_OUTLINE, x, y, PING_SIZE_PIXELS + 2, PING_SIZE_PIXELS + 2, sizeMult, 0, p.r, p.g, p.b, alpha);
                }

                if (p.isSelected) {
                    CustomHudRenderer.renderCustomHudObject(PING_OUTLINE, x, y, PING_SIZE_PIXELS + 2, PING_SIZE_PIXELS + 2, sizeMult, 0.0f, 255, 255, 255, 255);
                }
                CustomHudRenderer.renderText(event.getGuiGraphics(),String.format("%.1fm", worldDist), x, y + (16f * sizeMult), p.r,p.g,p.b,255,sizeMult,0.0f);
            }
        }
        pingList.forEach((p) -> p.isSelected = false);
        if (selected != -1) {
            pingList.get(selected).isSelected = true;
        }


    }

    @SubscribeEvent
    public static void updateCooldown(RenderGuiEvent.Pre event) {
        if (timeSinceLastPing < pingCooldown) {
            timeSinceLastPing += PartialTickUtils.timeDif;
        }
    }

    public static void removePing(int playerId, int type, double x, double y, double z) {
        BlockPos temp = new BlockPos(0, 0, 0);
        ModPackets.sendToServer(new C2SRequestToPingPacket(playerId, type, x, y, z, temp, true, -1, -1));
    }

    public static void acknowledgePing(int playerId, int type, double x, double y, double z) {

        if (!(timeSinceLastPing >= pingCooldown)) {
            return;
        }
        timeSinceLastPing = 0.0;

        BlockPos temp = new BlockPos(0, 0, 0);
        ModPackets.sendToServer(new C2SRequestToPingPacket(playerId, type, x, y, z, temp, true, Minecraft.getInstance().player.getId(), -1));
    }


    public static void sendPing(int type) {

        if (!(timeSinceLastPing >= pingCooldown)) {
            return;
        }
        timeSinceLastPing = 0.0;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Vec3 start = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = start.add(look.scale(distance));

        BlockHitResult hitResult;

        //raycast to hit block, if player is in a liquid, ignore liquids
        Vec3 eyePos = player.getEyePosition(1.0F);
        BlockPos eyeBlockPos = BlockPos.containing(eyePos);
        BlockState state = player.level().getBlockState(eyeBlockPos);
        if (state.liquid()) {
            hitResult = player.level().clip(new ClipContext(
                    start,
                    end,
                    ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE,
                    player
            ));
        } else {
            hitResult = player.level().clip(new ClipContext(
                    start,
                    end,
                    ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.ANY,
                    player
            ));
        }

        //do block raycast then entity raycast
        BlockPos hitPos = new BlockPos(0, 10000, 0);
        if (hitResult.getType() != HitResult.Type.MISS) {
            hitPos = hitResult.getBlockPos();
            end = hitResult.getLocation();
        }

        AABB searchBox = player.getBoundingBox()
                .expandTowards(look.scale(distance))
                .inflate(1.0); // extra padding

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                player,
                eyePos,
                end,
                searchBox,
                e -> !e.isSpectator() && e.isAlive(), // filter
                distance * distance
        );

        if (entityHit != null) {
            Entity target = entityHit.getEntity();
            hitPos = new BlockPos(0, 10000, 0);
            ModPackets.sendToServer(new C2SRequestToPingPacket(mc.player.getId(), type, target.getX(), target.getY(), target.getZ(), hitPos, false, -1, target.getId()));
        } else {
            if (hitPos.getY() != 10000) {
                ModPackets.sendToServer(new C2SRequestToPingPacket(mc.player.getId(), type, end.x, end.y, end.z, hitPos, false, -1, -1));
            }

        }
    }


    public static void initFromConfig() {

        //lifetime = ConfigUtils.getOrDefault(ClientConfig.NUMBER_DURATION);
        edgePixels = ConfigUtils.getOrDefault(ClientConfig.PING_EDGE_PIXELS);
        sizeMult = ConfigUtils.getOrDefault(ClientConfig.PING_SCALE_MULT);
        maxPings = ConfigUtils.getOrDefault(ServerConfig.MAX_PINGS);
        pingCooldown = ConfigUtils.getOrDefault(ServerConfig.PING_COOLDOWN);
    }


}
