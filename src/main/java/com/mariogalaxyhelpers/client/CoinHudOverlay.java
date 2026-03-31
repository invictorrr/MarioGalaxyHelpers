package com.mariogalaxyhelpers.client;

import com.mariogalaxyhelpers.MarioGalaxyHelpers;
import com.mariogalaxyhelpers.capability.ModCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MarioGalaxyHelpers.MODID, value = Dist.CLIENT)
public final class CoinHudOverlay {

    private CoinHudOverlay() {}

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        mc.player
                .getCapability(ModCapabilities.PLAYER_DATA)
                .ifPresent(
                        data -> {
                            int coins = data.getCoins();
                            GuiGraphics g = event.getGuiGraphics();
                            g.drawString(
                                    mc.font,
                                    "\u00a7e\u2726 " + coins + " coins",
                                    6,
                                    6,
                                    0xffffff,
                                    false);
                        });
    }
}
