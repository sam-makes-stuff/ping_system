package net.sam.ping_system.networking.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.sam.ping_system.networking.ClientPacketHandler;
import net.sam.ping_system.networking.ServerPacketHandler;

import java.util.function.Supplier;

public class S2CSendPingPacket {
    private final int senderId;
    private final int type;
    private final double x;
    private final double y;
    private final double z;
    private final BlockPos blockPos;


    public S2CSendPingPacket(int senderId, int type, double x, double y, double z, BlockPos blockPos) {
        this.senderId = senderId;
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockPos = blockPos;
    }

    public static void encode(S2CSendPingPacket pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.senderId);
        buf.writeInt(pkt.type);
        buf.writeDouble(pkt.x);
        buf.writeDouble(pkt.y);
        buf.writeDouble(pkt.z);
        buf.writeBlockPos(pkt.blockPos);

    }

    public static S2CSendPingPacket decode(FriendlyByteBuf buf) {
        return new S2CSendPingPacket(buf.readInt(), buf.readInt(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readBlockPos());
    }

    public static void handle(S2CSendPingPacket pkt, Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(() -> {
                    ClientPacketHandler.handleS2CPingPacket(pkt.senderId, pkt.type, pkt.x, pkt.y, pkt.z, pkt.blockPos);
        });
        ctx.get().setPacketHandled(true);
    }
}