package com.mariogalaxyhelpers.registry;

import com.mariogalaxyhelpers.MarioGalaxyHelpers;
import com.mariogalaxyhelpers.entity.ModEntities;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {

    private ModItems() {}

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MarioGalaxyHelpers.MODID);

    public static final RegistryObject<Item> TOAD_SPAWN_EGG =
            ITEMS.register(
                    "toad_spawn_egg",
                    () ->
                            new ForgeSpawnEggItem(
                                    ModEntities.TOAD, 0xff4040, 0xffffff, new Item.Properties()));

    public static final RegistryObject<Item> YOSHI_SPAWN_EGG =
            ITEMS.register(
                    "yoshi_placeholder_spawn_egg",
                    () ->
                            new ForgeSpawnEggItem(
                                    ModEntities.YOSHI_PLACEHOLDER,
                                    0x55ff55,
                                    0xffffff,
                                    new Item.Properties()));

    public static final RegistryObject<Item> LUIGI_SPAWN_EGG =
            ITEMS.register(
                    "luigi_spawn_egg",
                    () ->
                            new ForgeSpawnEggItem(
                                    ModEntities.LUIGI, 0x00aa00, 0x0000ff, new Item.Properties()));

    public static final RegistryObject<Item> CAPTAIN_TOAD_SPAWN_EGG =
            ITEMS.register(
                    "captain_toad_spawn_egg",
                    () ->
                            new ForgeSpawnEggItem(
                                    ModEntities.CAPTAIN_TOAD,
                                    0xffcc00,
                                    0x663300,
                                    new Item.Properties()));

    public static final RegistryObject<Item> ROSALINA_SPAWN_EGG =
            ITEMS.register(
                    "rosalina_spawn_egg",
                    () ->
                            new ForgeSpawnEggItem(
                                    ModEntities.ROSALINA, 0xaaaaff, 0xffffaa, new Item.Properties()));

    public static final RegistryObject<Item> LUMA_SPAWN_EGG =
            ITEMS.register(
                    "luma_spawn_egg",
                    () ->
                            new ForgeSpawnEggItem(
                                    ModEntities.LUMA, 0xffffaa, 0xffffff, new Item.Properties()));

    public static final RegistryObject<Item> PEACH_SPAWN_EGG =
            ITEMS.register(
                    "peach_spawn_egg",
                    () ->
                            new ForgeSpawnEggItem(
                                    ModEntities.PEACH, 0xffb0c8, 0xffe0e8, new Item.Properties()));

    public static final RegistryObject<Item> BOWSER_JR_SPAWN_EGG =
            ITEMS.register(
                    "bowser_jr_spawn_egg",
                    () ->
                            new ForgeSpawnEggItem(
                                    ModEntities.BOWSER_JR, 0xff8800, 0x00aa00, new Item.Properties()));
}
