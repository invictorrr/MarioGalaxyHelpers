package com.mariogalaxyhelpers.entity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

public class RosalinaEntity extends NPCEntity {

    private static final DustParticleOptions COAL_DUST =
            new DustParticleOptions(new Vector3f(0.2f, 0.2f, 0.2f), 1.0f);
    private static final DustParticleOptions IRON_DUST =
            new DustParticleOptions(new Vector3f(0.85f, 0.6f, 0.45f), 1.0f);
    private static final DustParticleOptions COPPER_DUST =
            new DustParticleOptions(new Vector3f(0.9f, 0.5f, 0.2f), 1.0f);
    private static final DustParticleOptions GOLD_DUST =
            new DustParticleOptions(new Vector3f(1.0f, 0.85f, 0.0f), 1.0f);
    private static final DustParticleOptions REDSTONE_DUST =
            new DustParticleOptions(new Vector3f(0.9f, 0.0f, 0.0f), 1.0f);
    private static final DustParticleOptions LAPIS_DUST =
            new DustParticleOptions(new Vector3f(0.1f, 0.2f, 0.9f), 1.0f);
    private static final DustParticleOptions DIAMOND_DUST =
            new DustParticleOptions(new Vector3f(0.3f, 0.9f, 0.9f), 1.5f);
    private static final DustParticleOptions EMERALD_DUST =
            new DustParticleOptions(new Vector3f(0.0f, 0.9f, 0.2f), 1.2f);

    private boolean abilityEnabled = true;
    private boolean mining;
    private BlockPos miningOrePos;
    private int miningY;
    private int mineTickTimer;
    private int sneakTicks;
    private final List<BlockPos[]> mineablePositions = new ArrayList<>();

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
        return 20;
    }

    @Override
    public boolean isAutoAbility() {
        return true;
    }

    @Override
    protected boolean canPerformAutoAbility() {
        return abilityEnabled;
    }

    @Override
    protected boolean shouldFollowOwner() {
        return !mining;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && isHired()) {
            abilityEnabled = !abilityEnabled;
            player.displayClientMessage(
                    Component.literal(
                                    "Rosalina: Detecci\u00f3n "
                                            + (abilityEnabled ? "activada" : "desactivada"))
                            .withStyle(
                                    abilityEnabled
                                            ? ChatFormatting.GREEN
                                            : ChatFormatting.RED),
                    false);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void performAbility(Player owner) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        mineablePositions.clear();
        BlockPos center = owner.blockPosition();
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

        // Scan normal para todos los ores cercanos
        for (int dx = -12; dx <= 12; dx++) {
            for (int dy = -8; dy <= 8; dy++) {
                for (int dz = -12; dz <= 12; dz++) {
                    mut.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    BlockState st = serverLevel.getBlockState(mut);
                    if (isOre(st)) {
                        spawnOreParticles(serverLevel, mut, st);
                    }
                }
            }
        }

        // Scan profundo solo para diamantes
        int deepTop = center.getY() - 9;
        int minY = serverLevel.getMinBuildHeight();
        for (int dx = -12; dx <= 12; dx++) {
            for (int dz = -12; dz <= 12; dz++) {
                for (int y = minY; y <= deepTop; y++) {
                    mut.set(center.getX() + dx, y, center.getZ() + dz);
                    BlockState st = serverLevel.getBlockState(mut);
                    if (st.is(BlockTags.DIAMOND_ORES)) {
                        spawnOreParticles(serverLevel, mut, st);
                    }
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && isHired()) {
            // Deteccion de sneak para minado
            if (!mining) {
                Player owner = getOwnerPlayer();
                if (owner != null && owner.isShiftKeyDown()) {
                    sneakTicks++;
                    if (sneakTicks >= 80) {
                        BlockPos playerPos = owner.blockPosition();
                        for (BlockPos[] pair : mineablePositions) {
                            if (playerPos.closerThan(pair[0], 3)) {
                                startMining(pair[0], pair[1]);
                                break;
                            }
                        }
                        sneakTicks = 0;
                    }
                } else {
                    sneakTicks = 0;
                }
            }

            if (mining) {
                tickMining();
            }
        }
    }

    private void startMining(BlockPos airPos, BlockPos orePos) {
        mining = true;
        miningOrePos = orePos;
        miningY = airPos.getY() - 1;
        mineTickTimer = 0;
        this.teleportTo(airPos.getX() + 0.5, airPos.getY(), airPos.getZ() + 0.5);

        Player owner = getOwnerPlayer();
        if (owner != null) {
            owner.displayClientMessage(
                    Component.literal(
                                    "Rosalina: \u00a1Voy a cavar hasta los minerales!")
                            .withStyle(ChatFormatting.AQUA),
                    false);
        }
    }

    private void tickMining() {
        mineTickTimer++;
        if (mineTickTimer % 2 != 0) return;

        if (miningY > miningOrePos.getY()) {
            BlockPos pos =
                    new BlockPos(miningOrePos.getX(), miningY, miningOrePos.getZ());
            BlockState state = this.level().getBlockState(pos);
            if (!state.isAir() && state.getDestroySpeed(this.level(), pos) >= 0) {
                this.level().destroyBlock(pos, true);
                this.level()
                        .playSound(
                                null,
                                pos,
                                SoundEvents.STONE_BREAK,
                                SoundSource.BLOCKS,
                                0.7f,
                                1.0f);
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(
                            ParticleTypes.CLOUD,
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5,
                            5,
                            0.3,
                            0.3,
                            0.3,
                            0.02);
                }
            }
            miningY--;
        } else {
            mining = false;
            mineTickTimer = 0;
            Player owner = getOwnerPlayer();
            if (owner != null) {
                owner.displayClientMessage(
                        Component.literal(
                                        "Rosalina: \u00a1Listo! Los minerales est\u00e1n expuestos")
                                .withStyle(ChatFormatting.AQUA),
                        false);
            }
        }
    }

    private void spawnOreParticles(
            ServerLevel serverLevel, BlockPos.MutableBlockPos orePos, BlockState state) {
        DustParticleOptions particle = getOreParticle(state);
        int maxSearch = state.is(BlockTags.DIAMOND_ORES) ? 256 : 16;

        BlockPos.MutableBlockPos airPos =
                new BlockPos.MutableBlockPos(
                        orePos.getX(), orePos.getY(), orePos.getZ());
        for (int i = 0; i < maxSearch; i++) {
            airPos.move(0, 1, 0);
            if (serverLevel.getBlockState(airPos).isAir()) {
                serverLevel.sendParticles(
                        particle,
                        airPos.getX() + 0.5,
                        airPos.getY() + 0.1,
                        airPos.getZ() + 0.5,
                        3,
                        0.15,
                        0.0,
                        0.15,
                        0.01);
                mineablePositions.add(
                        new BlockPos[] {airPos.immutable(), orePos.immutable()});
                return;
            }
        }
        serverLevel.sendParticles(
                particle,
                orePos.getX() + 0.5,
                orePos.getY() + 0.5,
                orePos.getZ() + 0.5,
                4,
                0.6,
                0.6,
                0.6,
                0.01);
    }

    private static DustParticleOptions getOreParticle(BlockState state) {
        if (state.is(BlockTags.COAL_ORES)) return COAL_DUST;
        if (state.is(BlockTags.IRON_ORES)) return IRON_DUST;
        if (state.is(BlockTags.COPPER_ORES)) return COPPER_DUST;
        if (state.is(BlockTags.GOLD_ORES)) return GOLD_DUST;
        if (state.is(BlockTags.REDSTONE_ORES)) return REDSTONE_DUST;
        if (state.is(BlockTags.LAPIS_ORES)) return LAPIS_DUST;
        if (state.is(BlockTags.DIAMOND_ORES)) return DIAMOND_DUST;
        if (state.is(BlockTags.EMERALD_ORES)) return EMERALD_DUST;
        return COAL_DUST;
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
