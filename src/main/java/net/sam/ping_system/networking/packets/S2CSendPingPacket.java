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
    private final int r;
    private final int g;
    private final int b;
    private final boolean isAlternative;
    private final int acknowledgerId;

    public S2CSendPingPacket(int senderId, int type, double x, double y, double z, BlockPos blockPos,int r, int g, int b, boolean isAlternative, int acknowledgerId) {
        this.senderId = senderId;
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockPos = blockPos;
        this.r = r;
        this.g = g;
        this.b = b;
        this.isAlternative = isAlternative;
        this.acknowledgerId = acknowledgerId;
    }

    public static void encode(S2CSendPingPacket pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.senderId);
        buf.writeInt(pkt.type);
        buf.writeDouble(pkt.x);
        buf.writeDouble(pkt.y);
        buf.writeDouble(pkt.z);
        buf.writeBlockPos(pkt.blockPos);
        buf.writeInt(pkt.r);
        buf.writeInt(pkt.g);
        buf.writeInt(pkt.b);
        buf.writeBoolean(pkt.isAlternative);
        buf.writeInt(pkt.acknowledgerId);
    }

    public static S2CSendPingPacket decode(FriendlyByteBuf buf) {
        return new S2CSendPingPacket(buf.readInt(), buf.readInt(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readBlockPos(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readBoolean(), buf.readInt());
    }

    public static void handle(S2CSendPingPacket pkt, Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(() -> {
                    ClientPacketHandler.handleS2CPingPacket(pkt.senderId, pkt.type, pkt.x, pkt.y, pkt.z, pkt.blockPos, pkt.r, pkt.g, pkt.b, pkt.isAlternative, pkt.acknowledgerId);
        });
        ctx.get().setPacketHandled(true);
    }
}