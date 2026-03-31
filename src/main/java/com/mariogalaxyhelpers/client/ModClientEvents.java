package com.mariogalaxyhelpers.client;

import com.invictor.customcpmodel.client.CPModelEntityRenderer;
import com.mariogalaxyhelpers.MarioGalaxyHelpers;
import com.mariogalaxyhelpers.entity.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MarioGalaxyHelpers.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModClientEvents {

    private ModClientEvents() {}

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.TOAD.get(), CPModelEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.YOSHI_PLACEHOLDER.get(), CPModelEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.LUIGI.get(), CPModelEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.CAPTAIN_TOAD.get(), CPModelEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.ROSALINA.get(), CPModelEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.LUMA.get(), CPModelEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.PEACH.get(), CPModelEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.BOWSER_JR.get(), CPModelEntityRenderer::new);
    }
}
