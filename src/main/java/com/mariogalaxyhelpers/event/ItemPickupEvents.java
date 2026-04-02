package com.mariogalaxyhelpers.event;

import com.mariogalaxyhelpers.MarioGalaxyHelpers;
import com.mariogalaxyhelpers.capability.ModCapabilities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = MarioGalaxyHelpers.MODID)
public final class ItemPickupEvents {

    private static final ResourceLocation COIN_ID =
            new ResourceLocation("super_block_world", "coin");
    private static final ResourceLocation STAR_COIN_ID =
            new ResourceLocation("super_block_world", "star_coin");

    private ItemPickupEvents() {}

    private static void playCoinSound(Player player, String soundId) {
        SoundEvent sound =
                ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(soundId));
        if (sound != null) {
            player.level()
                    .playSound(
                            null,
                            player.blockPosition(),
                            sound,
                            SoundSource.PLAYERS,
                            1.0f,
                            1.0f);
        }
    }

    // --- Utilidad: spawnear coins fisicas ---

    public static void spawnCoins(Level level, double x, double y, double z, int amount) {
        Item coinItem = ForgeRegistries.ITEMS.getValue(COIN_ID);
        if (coinItem == null || coinItem == Items.AIR) return;

        // 1 star_coin = 50 coins
        Item starCoinItem = ForgeRegistries.ITEMS.getValue(STAR_COIN_ID);
        int starCoins = 0;
        int normalCoins = amount;
        if (starCoinItem != null && starCoinItem != Items.AIR) {
            starCoins = amount / 50;
            normalCoins = amount % 50;
        }

        for (int i = 0; i < starCoins; i++) {
            ItemEntity entity =
                    new ItemEntity(level, x, y + 0.5, z, new ItemStack(starCoinItem, 1));
            entity.setDeltaMovement(
                    (level.random.nextDouble() - 0.5) * 0.3,
                    0.3 + level.random.nextDouble() * 0.2,
                    (level.random.nextDouble() - 0.5) * 0.3);
            level.addFreshEntity(entity);
        }

        while (normalCoins > 0) {
            int stack = Math.min(normalCoins, 64);
            ItemEntity entity =
                    new ItemEntity(level, x, y + 0.5, z, new ItemStack(coinItem, stack));
            entity.setDeltaMovement(
                    (level.random.nextDouble() - 0.5) * 0.3,
                    0.3 + level.random.nextDouble() * 0.2,
                    (level.random.nextDouble() - 0.5) * 0.3);
            level.addFreshEntity(entity);
            normalCoins -= stack;
        }
    }

    // --- Fall damage ---

    @SubscribeEvent
    public static void onFallDamage(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (player.getVehicle() != null) {
                ResourceLocation vehicleId =
                        ForgeRegistries.ENTITY_TYPES.getKey(
                                player.getVehicle().getType());
                if (vehicleId != null
                        && vehicleId.toString().equals("super_mario:yoshi")) {
                    event.setCanceled(true);
                }
            }
            if (player.hasEffect(net.minecraft.world.effect.MobEffects.SLOW_FALLING)) {
                event.setCanceled(true);
            }
        }
    }

    // --- Pickup: coin/star_coin suman puntos y desaparecen (no entran al inventario) ---

    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItem().getItem();
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId == null) return;

        String id = itemId.toString();

        // Coins: sumar y destruir (no va al inventario) + sonido original
        if (id.equals("super_block_world:coin")) {
            player.getCapability(ModCapabilities.PLAYER_DATA)
                    .ifPresent(data -> {
                        data.addCoins(stack.getCount());
                        ModCapabilities.syncToClient(player);
                        player.displayClientMessage(
                                Component.literal("+" + stack.getCount() + " coins")
                                        .withStyle(ChatFormatting.GOLD),
                                true);
                    });
            playCoinSound(player, "super_block_world:item.coin.pickup");
            event.getItem().discard();
            event.setCanceled(true);
            return;
        }

        if (id.equals("super_block_world:star_coin")) {
            player.getCapability(ModCapabilities.PLAYER_DATA)
                    .ifPresent(data -> {
                        int total = 50 * stack.getCount();
                        data.addCoins(total);
                        ModCapabilities.syncToClient(player);
                        player.displayClientMessage(
                                Component.literal("+" + total + " coins (Star Coin)")
                                        .withStyle(ChatFormatting.GOLD),
                                true);
                    });
            playCoinSound(player, "super_block_world:item.star_coin.pickup");
            event.getItem().discard();
            event.setCanceled(true);
            return;
        }

        // Otros items coleccionables: spawnean coins delante del player
        int coinReward = 0;
        boolean tracked = false;

        if (id.startsWith("super_mario:yoshi_egg")) {
            coinReward = 10 * stack.getCount();
            tracked = true;
            player.getCapability(ModCapabilities.PLAYER_DATA)
                    .ifPresent(data -> {
                        data.incrementEggsCollected(stack.getCount());
                        ModCapabilities.syncToClient(player);
                    });
        } else if (id.contains("star_bit")) {
            coinReward = (id.contains("rainbow") ? 100 : 50) * stack.getCount();
            tracked = true;
            player.getCapability(ModCapabilities.PLAYER_DATA)
                    .ifPresent(data -> {
                        data.incrementStarBitsCollected(stack.getCount());
                        ModCapabilities.syncToClient(player);
                    });
        } else if (id.equals("super_mario:power_star")) {
            coinReward = 333 * stack.getCount();
            tracked = true;
            player.getCapability(ModCapabilities.PLAYER_DATA)
                    .ifPresent(data -> {
                        data.incrementPowerStarsCollected(stack.getCount());
                        ModCapabilities.syncToClient(player);
                    });
        }

        if (tracked && coinReward > 0) {
            net.minecraft.world.phys.Vec3 look =
                    player.getLookAngle().multiply(1.5, 0, 1.5);
            spawnCoins(
                    player.level(),
                    player.getX() + look.x,
                    player.getY(),
                    player.getZ() + look.z,
                    coinReward);
        }
    }

    // --- Kill events: spawnean coins fisicas en vez de sumar directo ---

    @SubscribeEvent
    public static void onEntityKilled(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        player.getCapability(ModCapabilities.PLAYER_DATA)
                .ifPresent(data -> {
                    ResourceLocation mobId =
                            ForgeRegistries.ENTITY_TYPES.getKey(victim.getType());
                    int reward = 0;
                    if (mobId != null) {
                        String id = mobId.toString();
                        if (id.equals("super_block_world:octoomba")) {
                            reward = 5;
                        } else if (id.equals("super_block_world:shy_guy")) {
                            reward = 3;
                        } else if (id.equals("super_block_world:maw_ray")) {
                            reward = 10;
                        } else if (id.equals("super_block_world:boom_boom")) {
                            reward = 100;
                        }
                    }

                    if (reward > 0) {
                        spawnCoins(
                                victim.level(),
                                victim.getX(),
                                victim.getY(),
                                victim.getZ(),
                                reward);
                    } else if (!data.isHired("toad")) {
                        data.incrementKills();
                        spawnCoins(
                                victim.level(),
                                victim.getX(),
                                victim.getY(),
                                victim.getZ(),
                                1);
                    }
                });
    }
}
