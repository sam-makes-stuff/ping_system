package net.sam.ping_system.networking;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.sam.ping_system.networking.packets.S2CSendPingPacket;

import java.util.List;
import java.util.function.Supplier;

public class ServerPacketHandler {


    public static void handleC2SRequestToPingPacket(int senderId, int type, double x, double y, double z, BlockPos blockPos, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        S2CSendPingPacket sendPingPacket = new S2CSendPingPacket(senderId, type, x, y, z, blockPos);
        List<ServerPlayer> players = sender.serverLevel().players();
        System.out.println("Received c2s packet");
        for(ServerPlayer p: players){
            ModPackets.sendToPlayer(p, sendPingPacket);
        }
    }
}
