package com.mariogalaxyhelpers.event;

import com.mariogalaxyhelpers.MarioGalaxyHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = MarioGalaxyHelpers.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CraftingRewardEvents {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final int SEARCH_RADIUS = 6;

    private CraftingRewardEvents() {}

    private static BlockPos findNearbyCraftingTable(Level level, BlockPos center) {
        double closest = Double.MAX_VALUE;
        BlockPos found = null;
        for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
            for (int y = -SEARCH_RADIUS; y <= SEARCH_RADIUS; y++) {
                for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (level.getBlockState(pos).is(Blocks.CRAFTING_TABLE)) {
                        double dist = center.distSqr(pos);
                        if (dist < closest) {
                            closest = dist;
                            found = pos;
                        }
                    }
                }
            }
        }
        return found;
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        Level level = player.level();
        if (level.isClientSide) return;

        ItemStack result = event.getCrafting();
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(result.getItem());
        if (itemId == null) return;

        String id = itemId.toString();
        LOGGER.debug("[MarioGalaxyHelpers] ItemCrafted event fired, result: {}", id);

        int coinReward;
        switch (id) {
            case "super_block_world:power_star" -> coinReward = 333;
            case "super_mario:super_gem" -> coinReward = 8000;
            default -> { return; }
        }

        BlockPos playerPos = player.blockPosition();
        BlockPos tablePos = findNearbyCraftingTable(level, playerPos);
        BlockPos spawnPos = (tablePos != null ? tablePos : playerPos).above(5);

        LOGGER.debug("[MarioGalaxyHelpers] Spawning {} coins at {}", coinReward, spawnPos);

        ItemPickupEvents.spawnCoins(
                level,
                spawnPos.getX() + 0.5,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5,
                coinReward);
    }
}
