package net.sam.ping_system.networking;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.sam.ping_system.PingSystem;
import net.sam.ping_system.networking.packets.C2SRequestToPingPacket;
import net.sam.ping_system.networking.packets.S2CSendPingPacket;


public class ModPackets {

    private static int id = 0;

    private static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(PingSystem.MOD_ID, "main"))
            .networkProtocolVersion(() -> "1.0")
            .clientAcceptedVersions(s -> true)
            .serverAcceptedVersions(s -> true)
            .simpleChannel();

    public static void registerPackets(){

        // Server → Client
        INSTANCE.messageBuilder(S2CSendPingPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CSendPingPacket::encode)
                .decoder(S2CSendPingPacket::decode)
                .consumerMainThread(S2CSendPingPacket::handle)
                .add();

        // Client → Server
        INSTANCE.messageBuilder(C2SRequestToPingPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SRequestToPingPacket::encode)
                .decoder(C2SRequestToPingPacket::decode)
                .consumerMainThread(C2SRequestToPingPacket::handle)
                .add();
    }

    public static void sendToServer(Object msg){
        INSTANCE.send(PacketDistributor.SERVER.noArg(), msg);
    }

    public static void sendToAllClients(Object msg){
        INSTANCE.send(PacketDistributor.ALL.noArg(), msg);
    }

    public static void sendToTracking(Entity entity, Object packet) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            for (ServerPlayer player : serverLevel.getChunkSource().chunkMap.getPlayers(entity.chunkPosition(), false)) {
                INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
            }
        }
    }

    public static void sendToPlayer(ServerPlayer player, Object packet){
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}


