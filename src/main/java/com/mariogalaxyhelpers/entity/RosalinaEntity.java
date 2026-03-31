package com.mariogalaxyhelpers.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.particles.ParticleTypes;

public class RosalinaEntity extends NPCEntity {

    public RosalinaEntity(EntityType<? extends NPCEntity> type, Level level) {
        super(type, level);
        this.setModelName("rosalina");
    }

    @Override
    public String getNPCId() {
        return "rosalina";
    }

    @Override
    public int getHireCost() {
        return 1000;
    }

    @Override
    public int getAbilityCooldown() {
        return 40;
    }

    @Override
    public boolean isAutoAbility() {
        return true;
    }

    @Override
    public void performAbility(Player owner) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockPos center = owner.blockPosition();
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        for (int dx = -8; dx <= 8; dx++) {
            for (int dy = -4; dy <= 4; dy++) {
                for (int dz = -8; dz <= 8; dz++) {
                    mut.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    BlockState st = serverLevel.getBlockState(mut);
                    if (isOre(st)) {
                        serverLevel.sendParticles(
                                ParticleTypes.ENCHANT,
                                mut.getX() + 0.5,
                                mut.getY() + 0.5,
                                mut.getZ() + 0.5,
                                2,
                                0.2,
                                0.2,
                                0.2,
                                0.02);
                    }
                }
            }
        }
    }

    private static boolean isOre(BlockState state) {
        return state.is(BlockTags.COAL_ORES)
                || state.is(BlockTags.IRON_ORES)
                || state.is(BlockTags.COPPER_ORES)
                || state.is(BlockTags.GOLD_ORES)
                || state.is(BlockTags.REDSTONE_ORES)
                || state.is(BlockTags.LAPIS_ORES)
                || state.is(BlockTags.DIAMOND_ORES)
                || state.is(BlockTags.EMERALD_ORES);
    }
}
