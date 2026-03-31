package com.mariogalaxyhelpers.capability;

import com.mariogalaxyhelpers.MarioGalaxyHelpers;
import com.mariogalaxyhelpers.network.ModMessages;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MarioGalaxyHelpers.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ModCapabilityEvents {

    private ModCapabilityEvents() {}

    @SubscribeEvent
    public static void onAttach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(
                    new ResourceLocation(MarioGalaxyHelpers.MODID, "player_data"),
                    new PlayerDataProvider());
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getOriginal()
                .getCapability(ModCapabilities.PLAYER_DATA)
                .ifPresent(
                        oldData ->
                                event.getEntity()
                                        .getCapability(ModCapabilities.PLAYER_DATA)
                                        .ifPresent(
                                                newData ->
                                                        newData.deserializeNBT(
                                                                oldData.serializeNBT())));
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer) {
            ModMessages.sendPlayerData(event.getEntity());
        }
    }
}
