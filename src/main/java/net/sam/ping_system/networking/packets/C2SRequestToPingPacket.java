package net.sam.ping_system.networking.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.sam.ping_system.networking.ClientPacketHandler;
import net.sam.ping_system.networking.ServerPacketHandler;

import java.util.function.Supplier;

public class C2SRequestToPingPacket {
    private final int senderId;
    private final int type;
    private final double x;
    private final double y;
    private final double z;
    private final BlockPos blockPos;
    private final boolean isRemove;

    public C2SRequestToPingPacket(int senderId, int type, double x, double y, double z, BlockPos blockPos, boolean isRemove) {
        this.senderId = senderId;
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockPos = blockPos;
        this.isRemove = isRemove;
    }

    public static void encode(C2SRequestToPingPacket pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.senderId);
        buf.writeInt(pkt.type);
        buf.writeDouble(pkt.x);
        buf.writeDouble(pkt.y);
        buf.writeDouble(pkt.z);
        buf.writeBlockPos(pkt.blockPos);
        buf.writeBoolean(pkt.isRemove);
    }

    public static C2SRequestToPingPacket decode(FriendlyByteBuf buf) {
        return new C2SRequestToPingPacket(buf.readInt(), buf.readInt(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readBlockPos(), buf.readBoolean());
    }

    public static void handle(C2SRequestToPingPacket pkt, Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(() -> {
                    ServerPacketHandler.handleC2SRequestToPingPacket(pkt.senderId, pkt.type, pkt.x, pkt.y, pkt.z, pkt.blockPos, ctx, pkt.isRemove);
        });
        ctx.get().setPacketHandled(true);
    }
}