package com.mariogalaxyhelpers.network;

import com.mariogalaxyhelpers.MarioGalaxyHelpers;
import com.mariogalaxyhelpers.capability.ModCapabilities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModMessages {

    private static final String PROTOCOL = "1";
    private static int packetId;

    public static final SimpleChannel CHANNEL =
            NetworkRegistry.newSimpleChannel(
                    new ResourceLocation(MarioGalaxyHelpers.MODID, "main"),
                    () -> PROTOCOL,
                    PROTOCOL::equals,
                    PROTOCOL::equals);

    private ModMessages() {}

    public static void register() {
        packetId = 0;
        CHANNEL.registerMessage(
                packetId++,
                SyncPlayerDataPacket.class,
                SyncPlayerDataPacket::encode,
                SyncPlayerDataPacket::decode,
                SyncPlayerDataPacket::handle);
    }

    public static void sendPlayerData(Player player) {
        if (!(player instanceof ServerPlayer sp)) {
            return;
        }
        player.getCapability(ModCapabilities.PLAYER_DATA)
                .ifPresent(
                        data ->
                                CHANNEL.send(
                                        PacketDistributor.PLAYER.with(() -> sp),
                                        new SyncPlayerDataPacket(data.serializeNBT())));
    }
}
