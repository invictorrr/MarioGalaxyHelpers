package com.mariogalaxyhelpers.entity;

import com.mariogalaxyhelpers.capability.ModCapabilities;
import com.invictor.customcpmodel.entity.CPModelEntity;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public abstract class NPCEntity extends CPModelEntity {

    protected static final EntityDataAccessor<Boolean> HIRED =
            SynchedEntityData.defineId(NPCEntity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Optional<UUID>> OWNER =
            SynchedEntityData.defineId(NPCEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    protected int abilityCooldown;

    protected NPCEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(HIRED, false);
        this.entityData.define(OWNER, Optional.empty());
    }

    public abstract int getHireCost();

    public abstract String getNPCId();

    public abstract void performAbility(Player owner);

    public abstract int getAbilityCooldown();

    public abstract boolean isAutoAbility();

    /** Si es false, no se llama a {@link #followOwner} este tick (p. ej. Toad persiguiendo presa). */
    protected boolean shouldFollowOwner() {
        return true;
    }

    /** Auto-habilidad: si es false, no se ejecuta {@link #performAbility} (p. ej. Toad ya cazando). */
    protected boolean canPerformAutoAbility() {
        return true;
    }

    /** Tras {@link #performAbility}, si es false no se reinicia el cooldown (hasta que la subclase lo aplique). */
    protected boolean shouldApplyAbilityCooldown() {
        return true;
    }

    public boolean isHired() {
        return this.entityData.get(HIRED);
    }

    public void setHired(boolean hired) {
        this.entityData.set(HIRED, hired);
    }

    public Optional<UUID> getOwnerUUID() {
        return this.entityData.get(OWNER);
    }

    public void setOwner(Player player) {
        this.entityData.set(OWNER, Optional.of(player.getUUID()));
        this.setHired(true);
    }

    @Nullable
    public Player getOwnerPlayer() {
        return getOwnerUUID().map(uuid -> this.level().getPlayerByUUID(uuid)).orElse(null);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && isHired()) {
            Player owner = getOwnerPlayer();
            if (owner != null) {
                if (shouldFollowOwner()) {
                    followOwner(owner);
                }

                if (isAutoAbility() && abilityCooldown <= 0 && canPerformAutoAbility()) {
                    performAbility(owner);
                    if (shouldApplyAbilityCooldown()) {
                        abilityCooldown = getAbilityCooldown();
                    }
                }

                if (abilityCooldown > 0) {
                    abilityCooldown--;
                }
            }
        }
    }

    protected void followOwner(Player owner) {
        double distance = this.distanceTo(owner);
        if (distance > 4 && distance < 30) {
            this.getNavigation().moveTo(owner, 1.2);
        } else if (distance >= 30) {
            this.teleportTo(owner.getX(), owner.getY(), owner.getZ());
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide) {
            if (!isHired()) {
                return tryHire(player);
            } else if (!isAutoAbility() && abilityCooldown <= 0) {
                performAbility(player);
                if (shouldApplyAbilityCooldown()) {
                    abilityCooldown = getAbilityCooldown();
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    protected InteractionResult tryHire(Player player) {
        return player.getCapability(ModCapabilities.PLAYER_DATA)
                .map(
                        data -> {
                            if (data.spendCoins(getHireCost())) {
                                setOwner(player);
                                data.hire(getNPCId());
                                spawnQuestionBlock();
                                ModCapabilities.syncToClient(player);

                                this.level()
                                        .playSound(
                                                null,
                                                this.blockPosition(),
                                                SoundEvents.PLAYER_LEVELUP,
                                                SoundSource.PLAYERS,
                                                1.0f,
                                                1.0f);
                                player.displayClientMessage(
                                        Component.literal(
                                                        "\u00a1"
                                                                + this.getName().getString()
                                                                + " contratado!")
                                                .withStyle(ChatFormatting.GREEN),
                                        false);

                                return InteractionResult.SUCCESS;
                            } else {
                                player.displayClientMessage(
                                        Component.literal(
                                                        "Necesitas "
                                                                + getHireCost()
                                                                + " coins")
                                                .withStyle(ChatFormatting.RED),
                                        true);
                                return InteractionResult.FAIL;
                            }
                        })
                .orElse(InteractionResult.FAIL);
    }

    protected void spawnQuestionBlock() {
        BlockPos pos = this.blockPosition().above(2);
        Block questionBlock =
                ForgeRegistries.BLOCKS.getValue(
                        new ResourceLocation("super_mario", "question_mark_block"));
        if (questionBlock != null) {
            this.level().setBlock(pos, questionBlock.defaultBlockState(), 3);
        }
    }
}
