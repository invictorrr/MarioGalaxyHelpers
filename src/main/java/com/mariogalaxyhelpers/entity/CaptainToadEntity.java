package com.mariogalaxyhelpers.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class CaptainToadEntity extends NPCEntity {

    private boolean tracking;
    private BlockPos targetPos;
    private int particleTimer;

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
        return 0;
    }

    @Override
    public boolean isAutoAbility() {
        return false;
    }

    @Override
    public void performAbility(Player owner) {
        if (tracking) {
            // Click para parar
            tracking = false;
            targetPos = null;
            owner.displayClientMessage(
                    Component.literal("Captain Toad: \u00a1Deja de rastrear!")
                            .withStyle(ChatFormatting.GRAY),
                    false);
            return;
        }

        // Click para buscar nueva estructura
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

        targetPos = dest;
        tracking = true;
        particleTimer = 0;

        owner.displayClientMessage(
                Component.literal("Captain Toad: \u00a1Por all\u00ed hay una aldea! \u00a1S\u00edgueme!")
                        .withStyle(ChatFormatting.GOLD),
                false);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && isHired() && tracking && targetPos != null) {
            particleTimer--;
            if (particleTimer <= 0) {
                particleTimer = 5;
                showTrailParticles();
            }
        }
    }

    private void showTrailParticles() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 start = this.position().add(0, 1.5, 0);
        Vec3 end = Vec3.atCenterOf(targetPos);
        Vec3 dir = end.subtract(start).normalize();

        double maxDist = Math.min(start.distanceTo(end), 40);
        int steps = (int) (maxDist / 0.75);

        for (int i = 0; i < steps; i++) {
            Vec3 p = start.add(dir.scale(i * 0.75));
            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    p.x,
                    p.y,
                    p.z,
                    1,
                    0.02,
                    0.02,
                    0.02,
                    0.005);
        }
    }
}
