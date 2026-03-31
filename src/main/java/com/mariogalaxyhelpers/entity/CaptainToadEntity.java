package com.mariogalaxyhelpers.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

public class CaptainToadEntity extends NPCEntity {

    public CaptainToadEntity(EntityType<? extends NPCEntity> type, Level level) {
        super(type, level);
        this.setModelName("captaintoad");
    }

    @Override
    public String getNPCId() {
        return "captain_toad";
    }

    @Override
    public int getHireCost() {
        return 500;
    }

    @Override
    public int getAbilityCooldown() {
        return 1200;
    }

    @Override
    public boolean isAutoAbility() {
        return false;
    }

    @Override
    public void performAbility(Player owner) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockPos dest =
                serverLevel.findNearestMapStructure(
                        StructureTags.VILLAGE, this.blockPosition(), 160, false);
        if (dest == null) {
            owner.displayClientMessage(
                    Component.literal("Captain Toad: sigue explorando...")
                            .withStyle(ChatFormatting.GRAY),
                    false);
            return;
        }

        Vec3 start = this.position().add(0, 1, 0);
        Vec3 end = Vec3.atCenterOf(dest);
        Vec3 dir = end.subtract(start).normalize();

        for (int i = 0; i < 32; i++) {
            Vec3 p = start.add(dir.scale(i * 0.75));
            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    p.x,
                    p.y,
                    p.z,
                    2,
                    0.05,
                    0.05,
                    0.05,
                    0.01);
        }

        owner.displayClientMessage(
                Component.literal("Captain Toad: \u00a1Por all\u00ed hay una aldea!")
                        .withStyle(ChatFormatting.GOLD),
                false);
    }
}
