package net.sam.ping_system.networking;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Team;
import net.minecraftforge.network.NetworkEvent;
import net.sam.ping_system.networking.packets.S2CSendPingPacket;

import java.util.List;
import java.util.function.Supplier;

public class ServerPacketHandler {


    public static void handleC2SRequestToPingPacket(int senderId, int type, double x, double y, double z, BlockPos blockPos,Supplier<NetworkEvent.Context> ctx, boolean isRemove) {
        ServerPlayer sender = ctx.get().getSender();
        List<ServerPlayer> players = sender.serverLevel().players();
        Team senderTeam = sender.getTeam();
        int r,g,b;
        if(senderTeam == null){
            r = 0;
            g = 0;
            b = 0;
        }else{
            ChatFormatting color = senderTeam.getColor();
            int colorInt = color.getColor().intValue();
            r = (colorInt >> 16) & 0xFF;
            g = (colorInt >> 8)  & 0xFF;
            b = (colorInt)       & 0xFF;
        }
        S2CSendPingPacket sendPingPacket = new S2CSendPingPacket(senderId, type, x, y, z, blockPos, r, g, b, isRemove);
        for(ServerPlayer p: players){
            if(p.getTeam() == senderTeam || (p.getTeam() == null && senderTeam == null)){
                ModPackets.sendToPlayer(p, sendPingPacket);
            }

        }
    }
}
