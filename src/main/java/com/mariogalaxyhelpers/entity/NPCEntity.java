package com.mariogalaxyhelpers.entity;

import com.mariogalaxyhelpers.capability.ModCapabilities;
import com.invictor.customcpmodel.entity.CPModelEntity;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.registries.ForgeRegistries;

public abstract class NPCEntity extends CPModelEntity {

    protected static final EntityDataAccessor<Boolean> HIRED =
            SynchedEntityData.defineId(NPCEntity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Optional<UUID>> OWNER =
            SynchedEntityData.defineId(NPCEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    protected static final EntityDataAccessor<Boolean> WAITING =
            SynchedEntityData.defineId(NPCEntity.class, EntityDataSerializers.BOOLEAN);

    protected int abilityCooldown;
    private LivingEntity attackTarget;
    private static final double ATTACK_RANGE = 2.0;
    private static final double PROTECT_RANGE = 12.0;
    private static final int ATTACK_COOLDOWN_TICKS = 15;

    protected NPCEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setInvulnerable(true);
        this.setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(HIRED, false);
        this.entityData.define(OWNER, Optional.empty());
        this.entityData.define(WAITING, false);
    }

    public abstract int getHireCost();

    public abstract String getNPCId();

    public abstract void performAbility(Player owner);

    public abstract int getAbilityCooldown();

    public abstract boolean isAutoAbility();

    protected boolean shouldFollowOwner() {
        return true;
    }

    protected boolean canPerformAutoAbility() {
        return true;
    }

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

    public boolean isWaiting() {
        return this.entityData.get(WAITING);
    }

    public void setWaiting(boolean waiting) {
        this.entityData.set(WAITING, waiting);
    }

    @Nullable
    public Player getOwnerPlayer() {
        return getOwnerUUID().map(uuid -> this.level().getPlayerByUUID(uuid)).orElse(null);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Hired", isHired());
        tag.putBoolean("Waiting", isWaiting());
        getOwnerUUID().ifPresent(uuid -> tag.putUUID("Owner", uuid));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setHired(tag.getBoolean("Hired"));
        setWaiting(tag.getBoolean("Waiting"));
        if (tag.hasUUID("Owner")) {
            this.entityData.set(OWNER, Optional.of(tag.getUUID("Owner")));
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void checkDespawn() {
        // Never despawn hired NPCs
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && isHired()) {
            Player owner = getOwnerPlayer();
            if (owner == null && this.level() instanceof ServerLevel serverLevel) {
                // Owner might be in a different dimension — search all levels
                getOwnerUUID().ifPresent(uuid -> {
                    ServerPlayer sp = serverLevel.getServer().getPlayerList().getPlayer(uuid);
                    if (sp != null && sp.level() != this.level()) {
                        teleportToDimension(sp);
                    }
                });
                owner = getOwnerPlayer();
            }
            if (owner != null && !isWaiting()) {
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

                protectOwner(owner);
            }
        }
    }

    private void protectOwner(Player owner) {
        // Buscar nuevo objetivo si no tenemos uno vivo
        if (attackTarget == null || !attackTarget.isAlive()
                || attackTarget.distanceTo(owner) > PROTECT_RANGE) {
            attackTarget = null;
            java.util.List<Monster> threats =
                    this.level()
                            .getEntitiesOfClass(
                                    Monster.class,
                                    owner.getBoundingBox().inflate(PROTECT_RANGE),
                                    mob -> {
                                        ResourceLocation id =
                                                ForgeRegistries.ENTITY_TYPES.getKey(
                                                        mob.getType());
                                        return id == null
                                                || !id.toString()
                                                        .equals("super_mario:yoshi");
                                    });
            if (!threats.isEmpty()) {
                threats.sort(java.util.Comparator.comparingDouble(this::distanceTo));
                attackTarget = threats.get(0);
            }
        }

        if (attackTarget == null || !attackTarget.isAlive()) return;

        double dist = this.distanceTo(attackTarget);
        if (dist <= ATTACK_RANGE) {
            // Golpear
            if (this.tickCount % ATTACK_COOLDOWN_TICKS == 0) {
                this.swing(InteractionHand.MAIN_HAND);
                attackTarget.hurt(this.damageSources().mobAttack(this), 4.0f);
            }
        } else if (shouldFollowOwner()) {
            // Ir hacia el enemigo solo si no estamos ocupados
            this.getNavigation().moveTo(attackTarget, 1.3);
        }
    }

    protected void followOwner(Player owner) {
        // If owner is in a different dimension, teleport there first
        if (owner.level() != this.level() && owner instanceof ServerPlayer sp) {
            teleportToDimension(sp);
            return;
        }

        double distance = this.distanceTo(owner);
        if (distance <= 3) {
            this.getNavigation().stop();
        } else if (distance < 30) {
            this.getNavigation().moveTo(owner, 1.2);
        } else {
            this.teleportTo(owner.getX(), owner.getY(), owner.getZ());
        }
    }

    private void teleportToDimension(ServerPlayer target) {
        if (!(this.level() instanceof ServerLevel)) return;
        ServerLevel targetLevel = target.serverLevel();

        NPCEntity copy = (NPCEntity) this.getType().create(targetLevel);
        if (copy == null) return;

        // Transfer all data (hired state, owner, cooldowns, etc.)
        CompoundTag tag = new CompoundTag();
        this.saveWithoutId(tag);
        copy.load(tag);
        copy.setPos(target.getX(), target.getY(), target.getZ());

        targetLevel.addFreshEntity(copy);
        this.discard();
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
        BlockPos pos = this.blockPosition().above(3);
        Block questionBlock =
                ForgeRegistries.BLOCKS.getValue(
                        new ResourceLocation("super_mario", "question_mark_block"));
        if (questionBlock != null) {
            this.level().setBlock(pos, questionBlock.defaultBlockState(), 3);

            // Poner recompensa dentro del bloque ? via NBT (como el mod espera)
            BlockEntity be = this.level().getBlockEntity(pos);
            if (be != null) {
                ItemStack reward = generateReward();
                CompoundTag beTag = be.saveWithoutMetadata();

                ListTag items = new ListTag();
                CompoundTag slotTag = reward.save(new CompoundTag());
                slotTag.putByte("Slot", (byte) 0);
                items.add(slotTag);
                beTag.put("Items", items);

                be.load(beTag);
                be.setChanged();
            }
        }

        this.level()
                .playSound(
                        null,
                        pos,
                        SoundEvents.EXPERIENCE_ORB_PICKUP,
                        SoundSource.BLOCKS,
                        1.0f,
                        1.5f);
    }

    private ItemStack generateReward() {
        int roll = this.random.nextInt(10);
        return switch (roll) {
            case 0 -> new ItemStack(Items.DIAMOND, 1 + this.random.nextInt(2));
            case 1 -> new ItemStack(Items.GOLDEN_APPLE, 1);
            case 2, 3 -> new ItemStack(Items.IRON_INGOT, 2 + this.random.nextInt(4));
            case 4, 5 -> new ItemStack(Items.GOLD_INGOT, 1 + this.random.nextInt(3));
            case 6 -> new ItemStack(Items.EMERALD, 1 + this.random.nextInt(3));
            case 7, 8 -> new ItemStack(Items.EXPERIENCE_BOTTLE, 2 + this.random.nextInt(4));
            case 9 -> new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 1);
            default -> new ItemStack(Items.GOLD_INGOT, 1);
        };
    }
}
