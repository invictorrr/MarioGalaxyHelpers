package com.mariogalaxyhelpers.command;

import com.mariogalaxyhelpers.MarioGalaxyHelpers;
import com.mariogalaxyhelpers.capability.ModCapabilities;
import com.mariogalaxyhelpers.entity.ModEntities;
import com.mariogalaxyhelpers.entity.NPCEntity;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = MarioGalaxyHelpers.MODID)
public final class ModCommands {

    private ModCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        var root =
                Commands.literal("mariogalaxy")
                        .requires(s -> s.hasPermission(2))
                        .then(
                                Commands.literal("coins")
                                        .then(
                                                Commands.literal("add")
                                                        .then(
                                                                Commands.argument(
                                                                                "amount",
                                                                                IntegerArgumentType.integer(
                                                                                        0))
                                                                        .executes(
                                                                                ctx -> {
                                                                                    int amt =
                                                                                            IntegerArgumentType
                                                                                                    .getInteger(
                                                                                                            ctx,
                                                                                                            "amount");
                                                                                    ServerPlayer p =
                                                                                            ctx.getSource()
                                                                                                    .getPlayerOrException();
                                                                                    p.getCapability(
                                                                                                    ModCapabilities
                                                                                                            .PLAYER_DATA)
                                                                                            .ifPresent(
                                                                                                    data -> {
                                                                                                        data.addCoins(
                                                                                                                amt);
                                                                                                        ModCapabilities
                                                                                                                .syncToClient(
                                                                                                                        p);
                                                                                                    });
                                                                                    ctx.getSource()
                                                                                            .sendSuccess(
                                                                                                    () ->
                                                                                                            Component.literal(
                                                                                                                    "+"
                                                                                                                            + amt
                                                                                                                            + " coins"),
                                                                                                    true);
                                                                                    return amt;
                                                                                })))
                                        .then(
                                                Commands.literal("set")
                                                        .then(
                                                                Commands.argument(
                                                                                "amount",
                                                                                IntegerArgumentType.integer(
                                                                                        0))
                                                                        .executes(
                                                                                ctx -> {
                                                                                    int amt =
                                                                                            IntegerArgumentType
                                                                                                    .getInteger(
                                                                                                            ctx,
                                                                                                            "amount");
                                                                                    ServerPlayer p =
                                                                                            ctx.getSource()
                                                                                                    .getPlayerOrException();
                                                                                    p.getCapability(
                                                                                                    ModCapabilities
                                                                                                            .PLAYER_DATA)
                                                                                            .ifPresent(
                                                                                                    data -> {
                                                                                                        data.setCoins(
                                                                                                                amt);
                                                                                                        ModCapabilities
                                                                                                                .syncToClient(
                                                                                                                        p);
                                                                                                    });
                                                                                    ctx.getSource()
                                                                                            .sendSuccess(
                                                                                                    () ->
                                                                                                            Component.literal(
                                                                                                                    "coins = "
                                                                                                                            + amt),
                                                                                                    true);
                                                                                    return amt;
                                                                                }))))
                        .then(
                                Commands.literal("spawn")
                                        .then(
                                                Commands.literal("all")
                                                        .executes(
                                                                ctx -> {
                                                                    ServerPlayer p =
                                                                            ctx.getSource()
                                                                                    .getPlayerOrException();
                                                                    spawnAll(p);
                                                                    ctx.getSource()
                                                                            .sendSuccess(
                                                                                    () ->
                                                                                            Component.literal(
                                                                                                    "NPCs generados"),
                                                                                    true);
                                                                    return 1;
                                                                }))
                                        .then(
                                                Commands.argument("npc", StringArgumentType.string())
                                                        .executes(
                                                                ctx -> {
                                                                    String name =
                                                                            StringArgumentType.getString(
                                                                                    ctx, "npc");
                                                                    ServerPlayer p =
                                                                            ctx.getSource()
                                                                                    .getPlayerOrException();
                                                                    if (!spawnOne(p, name)) {
                                                                        ctx.getSource()
                                                                                .sendFailure(
                                                                                        Component.literal(
                                                                                                "NPC desconocido: "
                                                                                                        + name));
                                                                        return 0;
                                                                    }
                                                                    ctx.getSource()
                                                                            .sendSuccess(
                                                                                    () ->
                                                                                            Component.literal(
                                                                                                    "Generado: "
                                                                                                            + name),
                                                                                    true);
                                                                    return 1;
                                                                })))
                        .then(
                                Commands.literal("reset")
                                        .executes(
                                                ctx -> {
                                                    ServerPlayer p =
                                                            ctx.getSource().getPlayerOrException();
                                                    p.getCapability(ModCapabilities.PLAYER_DATA)
                                                            .ifPresent(
                                                                    data -> {
                                                                        data.deserializeNBT(
                                                                                new CompoundTag());
                                                                        ModCapabilities.syncToClient(
                                                                                p);
                                                                    });
                                                    ctx.getSource()
                                                            .sendSuccess(
                                                                    () ->
                                                                            Component.literal(
                                                                                    "Datos reiniciados"),
                                                                    true);
                                                    return 1;
                                                }))
                        .then(
                                Commands.literal("state")
                                        .then(
                                                Commands.literal("set")
                                                        .then(
                                                                Commands.literal("wait")
                                                                        .then(
                                                                                Commands.literal(
                                                                                                "all")
                                                                                        .executes(
                                                                                                ctx -> {
                                                                                                    ServerPlayer
                                                                                                            p =
                                                                                                                    ctx.getSource()
                                                                                                                            .getPlayerOrException();
                                                                                                    int count =
                                                                                                            setWaitingForOwner(
                                                                                                                    p,
                                                                                                                    null,
                                                                                                                    true);
                                                                                                    ctx.getSource()
                                                                                                            .sendSuccess(
                                                                                                                    () ->
                                                                                                                            Component.literal(
                                                                                                                                    count
                                                                                                                                            + " NPCs esperando"),
                                                                                                                    true);
                                                                                                    return count;
                                                                                                }))
                                                                        .then(
                                                                                Commands.argument(
                                                                                                "npc",
                                                                                                StringArgumentType
                                                                                                        .string())
                                                                                        .executes(
                                                                                                ctx -> {
                                                                                                    ServerPlayer
                                                                                                            p =
                                                                                                                    ctx.getSource()
                                                                                                                            .getPlayerOrException();
                                                                                                    String
                                                                                                            name =
                                                                                                                    StringArgumentType
                                                                                                                            .getString(
                                                                                                                                    ctx,
                                                                                                                                    "npc");
                                                                                                    int count =
                                                                                                            setWaitingForOwner(
                                                                                                                    p,
                                                                                                                    name,
                                                                                                                    true);
                                                                                                    if (count
                                                                                                            == 0) {
                                                                                                        ctx.getSource()
                                                                                                                .sendFailure(
                                                                                                                        Component.literal(
                                                                                                                                "No se encontró NPC: "
                                                                                                                                        + name));
                                                                                                        return 0;
                                                                                                    }
                                                                                                    ctx.getSource()
                                                                                                            .sendSuccess(
                                                                                                                    () ->
                                                                                                                            Component.literal(
                                                                                                                                    name
                                                                                                                                            + " esperando"),
                                                                                                                    true);
                                                                                                    return count;
                                                                                                })))
                                                        .then(
                                                                Commands.literal("follow")
                                                                        .then(
                                                                                Commands.literal(
                                                                                                "all")
                                                                                        .executes(
                                                                                                ctx -> {
                                                                                                    ServerPlayer
                                                                                                            p =
                                                                                                                    ctx.getSource()
                                                                                                                            .getPlayerOrException();
                                                                                                    int count =
                                                                                                            setWaitingForOwner(
                                                                                                                    p,
                                                                                                                    null,
                                                                                                                    false);
                                                                                                    ctx.getSource()
                                                                                                            .sendSuccess(
                                                                                                                    () ->
                                                                                                                            Component.literal(
                                                                                                                                    count
                                                                                                                                            + " NPCs siguiendo"),
                                                                                                                    true);
                                                                                                    return count;
                                                                                                }))
                                                                        .then(
                                                                                Commands.argument(
                                                                                                "npc",
                                                                                                StringArgumentType
                                                                                                        .string())
                                                                                        .executes(
                                                                                                ctx -> {
                                                                                                    ServerPlayer
                                                                                                            p =
                                                                                                                    ctx.getSource()
                                                                                                                            .getPlayerOrException();
                                                                                                    String
                                                                                                            name =
                                                                                                                    StringArgumentType
                                                                                                                            .getString(
                                                                                                                                    ctx,
                                                                                                                                    "npc");
                                                                                                    int count =
                                                                                                            setWaitingForOwner(
                                                                                                                    p,
                                                                                                                    name,
                                                                                                                    false);
                                                                                                    if (count
                                                                                                            == 0) {
                                                                                                        ctx.getSource()
                                                                                                                .sendFailure(
                                                                                                                        Component.literal(
                                                                                                                                "No se encontró NPC: "
                                                                                                                                        + name));
                                                                                                        return 0;
                                                                                                    }
                                                                                                    ctx.getSource()
                                                                                                            .sendSuccess(
                                                                                                                    () ->
                                                                                                                            Component.literal(
                                                                                                                                    name
                                                                                                                                            + " siguiendo"),
                                                                                                                    true);
                                                                                                    return count;
                                                                                                })))))
                        .then(
                                Commands.literal("structure")
                                        .then(
                                                Commands.literal("place")
                                                        .then(
                                                                Commands.argument(
                                                                                "name",
                                                                                StringArgumentType
                                                                                        .string())
                                                                        .executes(
                                                                                ctx -> {
                                                                                    String name =
                                                                                            StringArgumentType
                                                                                                    .getString(
                                                                                                            ctx,
                                                                                                            "name");
                                                                                    ServerPlayer p =
                                                                                            ctx.getSource()
                                                                                                    .getPlayerOrException();
                                                                                    return placeStructure(
                                                                                            p, name,
                                                                                            ctx.getSource());
                                                                                })))
                                        .then(
                                                Commands.literal("list")
                                                        .executes(
                                                                ctx -> {
                                                                    ctx.getSource()
                                                                            .sendSuccess(
                                                                                    () ->
                                                                                            Component.literal(
                                                                                                    "Estructuras: "
                                                                                                            + String.join(
                                                                                                                    ", ",
                                                                                                                    STRUCTURE_NAMES)),
                                                                                    false);
                                                                    return 1;
                                                                })));

        event.getDispatcher().register(root);
    }

    private static final List<String> STRUCTURE_NAMES =
            List.of("str_kamek_ship", "str_moon_base");

    private static int placeStructure(
            ServerPlayer player,
            String name,
            net.minecraft.commands.CommandSourceStack source) {
        ServerLevel level = player.serverLevel();
        StructureTemplateManager manager = level.getStructureManager();

        ResourceLocation loc =
                new ResourceLocation(MarioGalaxyHelpers.MODID, name);
        Optional<StructureTemplate> opt = manager.get(loc);
        if (opt.isEmpty()) {
            source.sendFailure(
                    Component.literal("Estructura no encontrada: " + name));
            return 0;
        }

        StructureTemplate template = opt.get();
        BlockPos pos = player.blockPosition();

        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setMirror(Mirror.NONE)
                .setRotation(Rotation.NONE)
                .setIgnoreEntities(false);

        template.placeInWorld(level, pos, pos, settings, RandomSource.create(), 2);

        source.sendSuccess(
                () -> Component.literal(
                        "Estructura '" + name + "' colocada en "
                                + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()),
                true);
        return 1;
    }

    private static final List<RegistryObject<? extends EntityType<? extends NPCEntity>>> ALL_NPCS =
            List.of(
                    ModEntities.TOAD,
                    ModEntities.YOSHI_PLACEHOLDER,
                    ModEntities.LUIGI,
                    ModEntities.CAPTAIN_TOAD,
                    ModEntities.ROSALINA,
                    ModEntities.LUMA,
                    ModEntities.PEACH,
                    ModEntities.BOWSER_JR);

    private static void spawnAll(ServerPlayer player) {
        Vec3 base = player.position();
        int i = 0;
        for (RegistryObject<? extends EntityType<? extends NPCEntity>> ro : ALL_NPCS) {
            EntityType<? extends NPCEntity> type = ro.get();
            NPCEntity e = type.create(player.level());
            if (e != null) {
                e.setPos(base.x + (i % 4) * 2, base.y, base.z + (i / 4) * 2);
                player.level().addFreshEntity(e);
            }
            i++;
        }
    }

    private static boolean spawnOne(ServerPlayer player, String name) {
        String n = name.toLowerCase();
        return switch (n) {
            case "toad" -> spawn(player, ModEntities.TOAD.get());
            case "yoshi" -> spawn(player, ModEntities.YOSHI_PLACEHOLDER.get());
            case "luigi" -> spawn(player, ModEntities.LUIGI.get());
            case "captain_toad" -> spawn(player, ModEntities.CAPTAIN_TOAD.get());
            case "rosalina" -> spawn(player, ModEntities.ROSALINA.get());
            case "luma" -> spawn(player, ModEntities.LUMA.get());
            case "peach" -> spawn(player, ModEntities.PEACH.get());
            case "bowser_jr" -> spawn(player, ModEntities.BOWSER_JR.get());
            default -> false;
        };
    }

    /**
     * Sets the waiting state for NPCs owned by the player.
     * @param npcName null = all, otherwise match by NPC id (e.g. "yoshi", "toad")
     * @return number of NPCs affected
     */
    private static int setWaitingForOwner(ServerPlayer player, @Nullable String npcName, boolean waiting) {
        List<NPCEntity> npcs = player.serverLevel().getEntitiesOfClass(
                NPCEntity.class,
                AABB.ofSize(player.position(), 200, 200, 200),
                npc -> npc.isHired() && npc.getOwnerUUID().map(u -> u.equals(player.getUUID())).orElse(false));
        int count = 0;
        for (NPCEntity npc : npcs) {
            if (npcName == null || npc.getNPCId().equalsIgnoreCase(npcName)) {
                npc.setWaiting(waiting);
                if (!waiting) {
                    // Stop navigation so it recalculates next tick
                    npc.getNavigation().stop();
                }
                count++;
            }
        }
        return count;
    }

    private static boolean spawn(ServerPlayer player, EntityType<? extends NPCEntity> type) {
        NPCEntity e = type.create(player.level());
        if (e == null) {
            return false;
        }
        Vec3 p = player.position();
        e.setPos(p.x, p.y, p.z);
        player.level().addFreshEntity(e);
        return true;
    }
}
