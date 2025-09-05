// ClientPacketHandler.java
package net.sam.ping_system.networking;


import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.sam.ping_system.client.overlay.PingHandler;

public class ClientPacketHandler {

    public static void handleS2CPingPacket(int senderId, int type, double x, double y, double z, BlockPos blockPos, int r, int g, int b) {
        Minecraft mc = Minecraft.getInstance();
        PingHandler.newPing(senderId, type, x,y,z,r,g,b);
    }
}
