package com.mariogalaxyhelpers.capability;

import com.mariogalaxyhelpers.MarioGalaxyHelpers;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MarioGalaxyHelpers.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModCapabilities {

    private ModCapabilities() {}

    public static final Capability<PlayerData> PLAYER_DATA = CapabilityManager.get(new CapabilityToken<>() {});

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(PlayerData.class);
    }

    public static void syncToClient(Player player) {
        com.mariogalaxyhelpers.network.ModMessages.sendPlayerData(player);
    }
}
