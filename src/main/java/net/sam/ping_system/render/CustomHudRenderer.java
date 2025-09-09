package net.sam.ping_system.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.sam.ping_system.PingSystem;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

@Mod.EventBusSubscriber(modid = PingSystem.MOD_ID, value = Dist.CLIENT)
public class CustomHudRenderer {

    public static double currentFov = 0.0;

    public static void renderCustomHudObject(ResourceLocation texture, float x, float y, float width, float height, float scale, float rotationDeg, int r, int g, int b, int a) {

        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(x, y, 0);
        poseStack.mulPose(Axis.ZP.rotation(rotationDeg * (float)(Math.PI/180)));
        poseStack.translate(-(width) / 2, -(height) / 2, 0);
        poseStack.scale(scale, scale, 1);


        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderTexture(0, texture);
        float r_f = r/255.0f;
        float g_f = g/255.0f;
        float b_f = b/255.0f;
        float a_f = a/255.0f;

        RenderSystem.setShaderColor(r_f,g_f,b_f,a_f);

        Matrix4f matrix = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(matrix, 0,0, 0).uv(0, 0).endVertex();
        buffer.vertex(matrix, width,0,        0).uv(1, 0).endVertex();
        buffer.vertex(matrix, width,   height,   0).uv(1, 1).endVertex();
        buffer.vertex(matrix, 0,       height,   0).uv(0, 1).endVertex();
        tesselator.end();
        poseStack.popPose();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.setShaderColor(1.0f,1.0f,1.0f,1.0f);
    }

    public static void renderText(GuiGraphics guiGraphics, String text, float x, float y, int r, int g, int b, int a, float size, float rotationDeg){
        int color = rgba(r,g,b,a);
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        double guiScale = mc.getWindow().getGuiScale();
        double scaledX = (x / guiScale);
        double scaledY = (y / guiScale);

        int textWidth = font.width(text);
        int textHeight = font.lineHeight;

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        poseStack.translate(scaledX, scaledY, 0);
        poseStack.mulPose(Axis.ZP.rotation(rotationDeg * (float)(Math.PI/180)));
        poseStack.scale(size, size, 1.0f);
        guiGraphics.drawString(font, text, -textWidth / 2, -textHeight / 2, color, true);
        poseStack.popPose();
    }

    public static void renderItemSprite(ItemStack stack, float x, float y, float scale, float rotationDeg, int alpha) {
        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        GuiGraphics gg = new GuiGraphics(mc, buffers);

        PoseStack poseStack = gg.pose();

        // Apply transformations

        poseStack.translate(x, y, 0);
        poseStack.translate(-8, -8, 0);

        if (rotationDeg != 0) {
            poseStack.translate(8, 8, 0);
            poseStack.mulPose(Axis.ZP.rotationDegrees(rotationDeg));
            poseStack.translate(-8, -8, 0);
        }

        if (scale != 1.0f) {
            poseStack.scale(scale, scale, 1.0f);
        }


        gg.renderItem(stack, 0,0);
        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    public static Vec2 worldToScreen(Vector4f pos) {

        Minecraft mc = Minecraft.getInstance();
        // Check if behind camera
        if (pos.w() <= 0.0f) {
            return null;
        }

        // Perspective divide
        pos.div(pos.w());

        // Convert normalized device coordinates to screen coordinates
        int screenWidth = mc.getWindow().getScreenWidth();
        int screenHeight = mc.getWindow().getScreenHeight();

        float guiInverseScale = 1.0f/(float) Minecraft.getInstance().getWindow().getGuiScale(); // e.g. 2.0

        float screenX = (pos.x * 0.5f + 0.5f) * screenWidth * guiInverseScale;
        float screenY = (1.0f - (pos.y * 0.5f + 0.5f)) * screenHeight * guiInverseScale;


        return new Vec2(screenX, screenY);
    }

    public static boolean isOffScreen(Vector4f pos, float edgePixels){
        if (pos.w() <= 0.0f) {
            return true;
        }
        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getScreenWidth();
        int screenHeight = mc.getWindow().getScreenHeight();
        float edgeFracX = (screenWidth - edgePixels) / screenWidth;
        float edgeFracY = (screenHeight - edgePixels) / screenHeight;

        float ndcX, ndcY;
        ndcX = pos.x / pos.w;
        ndcY = pos.y / pos.w;

        // Check if point is outside the view frustum
        if (Math.abs(ndcX) > edgeFracX || Math.abs(ndcY) > edgeFracY) {
            return true;
        }
        return false;
    }

    public static Vector4f worldToScreenTransform(Vec3 worldPos){
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();

        // Get camera position
        Vec3 camPos = camera.getPosition();

        // Calculate relative position
        float relX = (float)(worldPos.x - camPos.x);
        float relY = (float)(worldPos.y - camPos.y);
        float relZ = (float)(worldPos.z - camPos.z);

        // Create view matrix using lookAt approach
        Matrix4f viewMatrix = new Matrix4f();

        // Get camera's forward, up, and right vectors from rotation
        Quaternionf cameraRotation = camera.rotation();
        Vector3f forward = new Vector3f(0, 0, 1);
        Vector3f up = new Vector3f(0, 1, 0);
        Vector3f right = new Vector3f(1, 0, 0);

        // Apply camera rotation to get actual directions
        cameraRotation.transform(forward);
        cameraRotation.transform(up);
        cameraRotation.transform(right);

        // Create view matrix manually
        viewMatrix.setLookAt(
                0, 0, 0,
                forward.x, forward.y, forward.z,
                up.x, up.y, up.z
        );

        // Get projection matrix
        Matrix4f projectionMatrix = new Matrix4f();


        float aspectRatio = (float) mc.getWindow().getScreenWidth() / (float) mc.getWindow().getScreenHeight();
        float nearPlane = 10.0f;
        float farPlane = mc.gameRenderer.getRenderDistance() * 16.0f;
        projectionMatrix.perspective((float) Math.toRadians(currentFov), aspectRatio, nearPlane, farPlane);

        // Transform the relative position
        Vector4f pos = new Vector4f(relX, relY, relZ, 1.0f);

        // Apply view matrix
        pos.mul(viewMatrix);

        // Apply projection matrix
        pos.mul(projectionMatrix);
        return pos;
    }

    public static Vec2 worldToScreenWithEdgeClip(Vector4f pos, float edgePixels) {
        Minecraft mc = Minecraft.getInstance();

        int screenWidth = mc.getWindow().getScreenWidth();
        int screenHeight = mc.getWindow().getScreenHeight();

        float edgeFracX = (screenWidth - edgePixels) / screenWidth;
        float edgeFracY = (screenHeight - edgePixels) / screenHeight;

        float ndcX, ndcY;

        // Normal perspective divide
        ndcX = pos.x / pos.w;
        ndcY = pos.y / pos.w;

        float scale = 1.0f / Math.max(Math.abs(ndcX) / edgeFracX, Math.abs(ndcY) / edgeFracY);
        if(pos.w <= 0){
            ndcX *= -scale;
            ndcY *= -scale;
        }

        // Check if point is outside the view frustum and clamp to edge
        else if (Math.abs(ndcX) > edgeFracX || Math.abs(ndcY) > edgeFracY) {
            ndcX *= scale;
            ndcY *= scale;
        }

        ndcX = Mth.clamp(ndcX, -edgeFracX, edgeFracX);
        ndcY = Mth.clamp(ndcY, -edgeFracY, edgeFracY);


        // Convert normalized device coordinates to screen coordinates
        float guiInverseScale = 1.0f/(float) Minecraft.getInstance().getWindow().getGuiScale(); // e.g. 2.0

        float screenX = (ndcX * 0.5f + 0.5f) * screenWidth * guiInverseScale;
        float screenY = (1.0f - (ndcY * 0.5f + 0.5f)) * screenHeight * guiInverseScale;

        return new Vec2(screenX, screenY);
    }

    @SubscribeEvent
    public static void updateFov(ViewportEvent.ComputeFov event){
        if (!event.usedConfiguredFov()) return;
        currentFov = event.getFOV();
    }

    public static int rgba(int r, int g, int b, int a) {
        return ((a) << 24) |
                ((r) << 16) |
                ((g) << 8)  |
                ((b));
    }
}
