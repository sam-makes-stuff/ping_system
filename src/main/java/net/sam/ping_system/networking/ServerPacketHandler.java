package net.sam.ping_system.networking;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Team;
import net.minecraftforge.network.NetworkEvent;
import net.sam.ping_system.config.ClientConfig;
import net.sam.ping_system.config.ServerConfig;
import net.sam.ping_system.networking.packets.S2CSendPingPacket;
import net.sam.ping_system.util.ConfigUtils;

import java.util.List;
import java.util.function.Supplier;

public class ServerPacketHandler {

    public static double maxPlayerDistance;

    public static void handleC2SRequestToPingPacket(int senderId, int type, double x, double y, double z, BlockPos blockPos,Supplier<NetworkEvent.Context> ctx, boolean isAlternative, int acknowledgerId, int selectedId) {
        ServerPlayer sender = ctx.get().getSender();
        List<ServerPlayer> players = sender.serverLevel().players();
        Team senderTeam = sender.getTeam();
        int r,g,b;
        if(senderTeam == null){
            r = 255;
            g = 255;
            b = 255;
        }else{
            ChatFormatting color = senderTeam.getColor();
            int colorInt = color.getColor().intValue();
            r = (colorInt >> 16) & 0xFF;
            g = (colorInt >> 8)  & 0xFF;
            b = (colorInt)       & 0xFF;
        }
        S2CSendPingPacket sendPingPacket = new S2CSendPingPacket(senderId, type, x, y, z, blockPos, r, g, b, isAlternative, acknowledgerId, selectedId);
        for(ServerPlayer p: players){
            if(p.getTeam() == senderTeam || (p.getTeam() == null && senderTeam == null)){
                double dist = p.position().distanceTo(sender.position());
                //System.out.println(dist);
                if(dist <= 9999999){
                    ModPackets.sendToPlayer(p, sendPingPacket);
                }
            }
        }
    }

    public static void initFromConfig() {


    }

}
