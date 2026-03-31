package com.mariogalaxyhelpers.entity;

import com.invictor.customcpmodel.entity.CPModelEntity;
import com.mariogalaxyhelpers.MarioGalaxyHelpers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {

    private ModEntities() {}

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MarioGalaxyHelpers.MODID);

    public static final RegistryObject<EntityType<ToadEntity>> TOAD =
            ENTITY_TYPES.register(
                    "toad",
                    () ->
                            EntityType.Builder.of(ToadEntity::new, MobCategory.CREATURE)
                                    .sized(0.6f, 1.8f)
                                    .clientTrackingRange(10)
                                    .updateInterval(1)
                                    .build(MarioGalaxyHelpers.MODID + ":toad"));

    public static final RegistryObject<EntityType<YoshiPlaceholderEntity>> YOSHI_PLACEHOLDER =
            ENTITY_TYPES.register(
                    "yoshi_placeholder",
                    () ->
                            EntityType.Builder.of(YoshiPlaceholderEntity::new, MobCategory.CREATURE)
                                    .sized(0.6f, 1.8f)
                                    .clientTrackingRange(10)
                                    .updateInterval(1)
                                    .build(MarioGalaxyHelpers.MODID + ":yoshi_placeholder"));

    public static final RegistryObject<EntityType<LuigiEntity>> LUIGI =
            ENTITY_TYPES.register(
                    "luigi",
                    () ->
                            EntityType.Builder.of(LuigiEntity::new, MobCategory.CREATURE)
                                    .sized(0.6f, 1.8f)
                                    .clientTrackingRange(10)
                                    .updateInterval(1)
                                    .build(MarioGalaxyHelpers.MODID + ":luigi"));

    public static final RegistryObject<EntityType<CaptainToadEntity>> CAPTAIN_TOAD =
            ENTITY_TYPES.register(
                    "captain_toad",
                    () ->
                            EntityType.Builder.of(CaptainToadEntity::new, MobCategory.CREATURE)
                                    .sized(0.6f, 1.8f)
                                    .clientTrackingRange(10)
                                    .updateInterval(1)
                                    .build(MarioGalaxyHelpers.MODID + ":captain_toad"));

    public static final RegistryObject<EntityType<RosalinaEntity>> ROSALINA =
            ENTITY_TYPES.register(
                    "rosalina",
                    () ->
                            EntityType.Builder.of(RosalinaEntity::new, MobCategory.CREATURE)
                                    .sized(0.6f, 1.8f)
                                    .clientTrackingRange(10)
                                    .updateInterval(1)
                                    .build(MarioGalaxyHelpers.MODID + ":rosalina"));

    public static final RegistryObject<EntityType<LumaEntity>> LUMA =
            ENTITY_TYPES.register(
                    "luma",
                    () ->
                            EntityType.Builder.of(LumaEntity::new, MobCategory.CREATURE)
                                    .sized(0.6f, 1.8f)
                                    .clientTrackingRange(10)
                                    .updateInterval(1)
                                    .build(MarioGalaxyHelpers.MODID + ":luma"));

    public static final RegistryObject<EntityType<PeachEntity>> PEACH =
            ENTITY_TYPES.register(
                    "peach",
                    () ->
                            EntityType.Builder.of(PeachEntity::new, MobCategory.CREATURE)
                                    .sized(0.6f, 1.8f)
                                    .clientTrackingRange(10)
                                    .updateInterval(1)
                                    .build(MarioGalaxyHelpers.MODID + ":peach"));

    public static final RegistryObject<EntityType<BowserJrEntity>> BOWSER_JR =
            ENTITY_TYPES.register(
                    "bowser_jr",
                    () ->
                            EntityType.Builder.of(BowserJrEntity::new, MobCategory.CREATURE)
                                    .sized(0.6f, 1.8f)
                                    .clientTrackingRange(10)
                                    .updateInterval(1)
                                    .build(MarioGalaxyHelpers.MODID + ":bowser_jr"));

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(TOAD.get(), CPModelEntity.createAttributes().build());
        event.put(YOSHI_PLACEHOLDER.get(), CPModelEntity.createAttributes().build());
        event.put(LUIGI.get(), CPModelEntity.createAttributes().build());
        event.put(CAPTAIN_TOAD.get(), CPModelEntity.createAttributes().build());
        event.put(ROSALINA.get(), CPModelEntity.createAttributes().build());
        event.put(LUMA.get(), CPModelEntity.createAttributes().build());
        event.put(PEACH.get(), CPModelEntity.createAttributes().build());
        event.put(BOWSER_JR.get(), CPModelEntity.createAttributes().build());
    }
}
