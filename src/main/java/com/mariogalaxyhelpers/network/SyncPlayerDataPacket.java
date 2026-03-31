package com.mariogalaxyhelpers.network;

import com.mariogalaxyhelpers.capability.ModCapabilities;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class SyncPlayerDataPacket {

    private final CompoundTag tag;

    public SyncPlayerDataPacket(CompoundTag tag) {
        this.tag = tag;
    }

    public static void encode(SyncPlayerDataPacket msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.tag);
    }

    public static SyncPlayerDataPacket decode(FriendlyByteBuf buf) {
        CompoundTag t = buf.readNbt();
        return new SyncPlayerDataPacket(t != null ? t : new CompoundTag());
    }

    public static void handle(SyncPlayerDataPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get()
                .enqueueWork(
                        () -> {
                            Minecraft mc = Minecraft.getInstance();
                            if (mc.player != null) {
                                mc.player
                                        .getCapability(ModCapabilities.PLAYER_DATA)
                                        .ifPresent(data -> data.deserializeNBT(msg.tag));
                            }
                        });
        ctx.get().setPacketHandled(true);
    }
}
