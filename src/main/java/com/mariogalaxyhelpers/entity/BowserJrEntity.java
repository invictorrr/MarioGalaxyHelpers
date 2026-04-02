package com.mariogalaxyhelpers.entity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class BowserJrEntity extends NPCEntity {

    private boolean building;
    private boolean inNether;
    private int buildTimer;
    private int buildIndex;
    private int netherTimer;
    private List<BlockPos> framePlan;
    private List<BlockPos> interiorPlan;
    private Direction.Axis portalAxis;

    public BowserJrEntity(EntityType<? extends NPCEntity> type, Level level) {
        super(type, level);
        this.setModelName("bowserjr");
    }

    @Override
    public String getNPCId() {
        return "bowser_jr";
    }

    @Override
    public int getHireCost() {
        return 10000;
    }

    @Override
    public int getAbilityCooldown() {
        return 6000;
    }

    @Override
    public boolean isAutoAbility() {
        return false;
    }

    @Override
    protected boolean shouldFollowOwner() {
        return !building && !inNether;
    }

    @Override
    public void performAbility(Player owner) {
        if (building || inNether) return;

        building = true;
        buildTimer = 0;
        buildIndex = 0;
        createPortalPlan(owner);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.PORTAL,
                    this.getX(),
                    this.getY() + 1,
                    this.getZ(),
                    60,
                    0.5,
                    1,
                    0.5,
                    0.3);
        }

        owner.displayClientMessage(
                Component.literal(
                                "Bowser Jr: \u00a1Voy a construir un portal al Nether!")
                        .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),
                false);
    }

    private void createPortalPlan(Player owner) {
        BlockPos base = this.blockPosition().relative(owner.getDirection(), 2);
        Direction facing = owner.getDirection();

        int dx;
        int dz;
        if (facing == Direction.NORTH || facing == Direction.SOUTH) {
            portalAxis = Direction.Axis.X;
            dx = 1;
            dz = 0;
        } else {
            portalAxis = Direction.Axis.Z;
            dx = 0;
            dz = 1;
        }

        framePlan = new ArrayList<>();
        interiorPlan = new ArrayList<>();

        // Base: 2 bloques
        framePlan.add(base);
        framePlan.add(base.offset(dx, 0, dz));
        // Columna izquierda: 3 bloques
        framePlan.add(base.offset(-dx, 1, -dz));
        framePlan.add(base.offset(-dx, 2, -dz));
        framePlan.add(base.offset(-dx, 3, -dz));
        // Columna derecha: 3 bloques
        framePlan.add(base.offset(2 * dx, 1, 2 * dz));
        framePlan.add(base.offset(2 * dx, 2, 2 * dz));
        framePlan.add(base.offset(2 * dx, 3, 2 * dz));
        // Techo: 2 bloques
        framePlan.add(base.offset(0, 4, 0));
        framePlan.add(base.offset(dx, 4, dz));

        // Interior: 6 bloques de portal (2x3)
        for (int y = 1; y <= 3; y++) {
            interiorPlan.add(base.offset(0, y, 0));
            interiorPlan.add(base.offset(dx, y, dz));
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            if (building && framePlan != null) {
                tickBuilding();
            }

            if (inNether) {
                tickNether();
            }
        }
    }

    private void tickBuilding() {
        buildTimer++;

        if (buildTimer % 8 == 0) {
            if (buildIndex < framePlan.size()) {
                // Colocar un bloque de obsidiana
                BlockPos pos = framePlan.get(buildIndex);
                this.level()
                        .setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), 3);
                this.level()
                        .playSound(
                                null,
                                pos,
                                SoundEvents.STONE_PLACE,
                                SoundSource.BLOCKS,
                                1.0f,
                                0.8f);

                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ParticleTypes.PORTAL,
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5,
                            10,
                            0.3,
                            0.3,
                            0.3,
                            0.2);
                }

                buildIndex++;
            } else if (buildIndex == framePlan.size()) {
                // Encender el portal
                for (BlockPos pos : interiorPlan) {
                    this.level()
                            .setBlock(
                                    pos,
                                    Blocks.NETHER_PORTAL
                                            .defaultBlockState()
                                            .setValue(
                                                    BlockStateProperties.HORIZONTAL_AXIS,
                                                    portalAxis),
                                    3);
                }

                this.level()
                        .playSound(
                                null,
                                this.blockPosition(),
                                SoundEvents.PORTAL_TRIGGER,
                                SoundSource.BLOCKS,
                                0.5f,
                                1.0f);

                buildIndex++;
                enterPortal();
            }
        }
    }

    private void enterPortal() {
        building = false;
        inNether = true;
        netherTimer = 200;

        // Teletransportar al centro del portal y desaparecer
        if (!interiorPlan.isEmpty()) {
            BlockPos center = interiorPlan.get(interiorPlan.size() / 2);
            this.teleportTo(
                    center.getX() + 0.5, center.getY(), center.getZ() + 0.5);
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.PORTAL,
                    this.getX(),
                    this.getY() + 1,
                    this.getZ(),
                    100,
                    0.5,
                    1,
                    0.5,
                    0.5);
        }

        this.level()
                .playSound(
                        null,
                        this.blockPosition(),
                        SoundEvents.PORTAL_TRAVEL,
                        SoundSource.NEUTRAL,
                        0.5f,
                        1.0f);

        this.setInvisible(true);

        Player owner = getOwnerPlayer();
        if (owner != null) {
            owner.displayClientMessage(
                    Component.literal(
                                    "Bowser Jr: \u00a1Entro al Nether! \u00a1Dame 10 segundos!")
                            .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),
                    false);
        }
    }

    private void tickNether() {
        netherTimer--;
        if (netherTimer <= 0) {
            returnFromNether();
        }
    }

    private void returnFromNether() {
        inNether = false;
        this.setInvisible(false);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.REVERSE_PORTAL,
                    this.getX(),
                    this.getY() + 1,
                    this.getZ(),
                    50,
                    0.5,
                    1,
                    0.5,
                    0.3);
        }

        Player owner = getOwnerPlayer();
        if (owner != null) {
            owner.getInventory().add(new ItemStack(Items.BLAZE_ROD, 12));
            owner.getInventory().add(new ItemStack(Items.ENDER_PEARL, 16));

            owner.displayClientMessage(
                    Component.literal(
                                    "Bowser Jr: \u00a1He vuelto! Toma todo lo que necesitas para el End.")
                            .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),
                    false);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && isHired() && (building || inNether)) {
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }
}
