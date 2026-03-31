package com.mariogalaxyhelpers;

import com.mariogalaxyhelpers.entity.ModEntities;
import com.mariogalaxyhelpers.network.ModMessages;
import com.mariogalaxyhelpers.registry.ModItems;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MarioGalaxyHelpers.MODID)
public class MarioGalaxyHelpers {

    public static final String MODID = "mariogalaxyhelpers";

    public MarioGalaxyHelpers() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITY_TYPES.register(modBus);
        ModItems.ITEMS.register(modBus);
        modBus.addListener(this::onEntityAttributes);
        modBus.addListener(this::onCommonSetup);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModMessages::register);
    }

    private void onEntityAttributes(EntityAttributeCreationEvent event) {
        ModEntities.registerAttributes(event);
    }
}
