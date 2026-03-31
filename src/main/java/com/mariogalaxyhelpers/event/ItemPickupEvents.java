package com.mariogalaxyhelpers.event;

import com.mariogalaxyhelpers.MarioGalaxyHelpers;
import com.mariogalaxyhelpers.capability.ModCapabilities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = MarioGalaxyHelpers.MODID)
public final class ItemPickupEvents {

    private ItemPickupEvents() {}

    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItem().getItem();
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId == null) {
            return;
        }

        player.getCapability(ModCapabilities.PLAYER_DATA)
                .ifPresent(
                        data -> {
                            String id = itemId.toString();

                            if (id.equals("super_block_world:coin")) {
                                data.addCoins(stack.getCount());
                                ModCapabilities.syncToClient(player);
                                player.displayClientMessage(
                                        Component.literal("+" + stack.getCount() + " coins")
                                                .withStyle(ChatFormatting.GOLD),
                                        true);
                            } else if (id.startsWith("super_mario:yoshi_egg")) {
                                data.addCoins(10 * stack.getCount());
                                data.incrementEggsCollected(stack.getCount());
                                ModCapabilities.syncToClient(player);
                                player.displayClientMessage(
                                        Component.literal(
                                                        "+"
                                                                + (10 * stack.getCount())
                                                                + " coins (Yoshi Egg)")
                                                .withStyle(ChatFormatting.GOLD),
                                        true);
                            } else if (id.contains("star_bit")) {
                                int value = id.contains("rainbow") ? 100 : 50;
                                int total = value * stack.getCount();
                                data.addCoins(total);
                                data.incrementStarBitsCollected(stack.getCount());
                                ModCapabilities.syncToClient(player);
                                player.displayClientMessage(
                                        Component.literal(
                                                        "+" + total + " coins (Star Bit)")
                                                .withStyle(ChatFormatting.GOLD),
                                        true);
                            } else if (id.equals("super_mario:power_star")) {
                                data.addCoins(333 * stack.getCount());
                                data.incrementPowerStarsCollected(stack.getCount());
                                ModCapabilities.syncToClient(player);
                                player.displayClientMessage(
                                        Component.literal(
                                                        "\u2b50 POWER STAR \u2b50 +333 coins")
                                                .withStyle(
                                                        ChatFormatting.YELLOW,
                                                        ChatFormatting.BOLD),
                                        false);
                            }
                        });
    }

    @SubscribeEvent
    public static void onEntityKilled(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }
        player.getCapability(ModCapabilities.PLAYER_DATA)
                .ifPresent(
                        data -> {
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
                                data.addCoins(reward);
                                ModCapabilities.syncToClient(player);
                                player.displayClientMessage(
                                        Component.literal("+" + reward + " coins")
                                                .withStyle(ChatFormatting.GOLD),
                                        true);
                            } else if (!data.isHired("toad")) {
                                data.addCoins(1);
                                data.incrementKills();
                                ModCapabilities.syncToClient(player);
                                player.displayClientMessage(
                                        Component.literal("+1 coin")
                                                .withStyle(ChatFormatting.GOLD),
                                        true);
                            }
                        });
    }
}
