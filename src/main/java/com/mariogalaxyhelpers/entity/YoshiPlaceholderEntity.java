package com.mariogalaxyhelpers.entity;

import com.mariogalaxyhelpers.capability.ModCapabilities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class YoshiPlaceholderEntity extends NPCEntity {

    private Entity realYoshi;

    public YoshiPlaceholderEntity(EntityType<? extends NPCEntity> type, Level level) {
        super(type, level);
        this.setModelName("yoshi");
    }

    @Override
    public String getNPCId() {
        return "yoshi";
    }

    @Override
    public int getHireCost() {
        return 50;
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
        // no-op: contratacion especial
    }

    @Override
    public boolean isPickable() {
        return !isHired();
    }

    @Override
    public boolean isPushable() {
        return !isHired();
    }

    @Override
    protected void followOwner(Player owner) {
        // El placeholder invisible solo se teletransporta para mantenerse en chunks cargados
        if (this.distanceTo(owner) > 5) {
            this.teleportTo(owner.getX(), owner.getY(), owner.getZ());
        }
    }

    @Override
    protected InteractionResult tryHire(Player player) {
        return player.getCapability(ModCapabilities.PLAYER_DATA)
                .map(
                        data -> {
                            if (data.spendCoins(getHireCost())) {
                                data.hire(getNPCId());
                                setOwner(player);
                                ModCapabilities.syncToClient(player);

                                spawnRealYoshi(player);
                                this.setInvisible(true);

                                spawnQuestionBlock();
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
                                                        "\u00a1Yoshi contratado! Click derecho para montarlo")
                                                .withStyle(ChatFormatting.GREEN),
                                        false);
                                return InteractionResult.SUCCESS;
                            }
                            player.displayClientMessage(
                                    Component.literal(
                                                    "Necesitas " + getHireCost() + " coins")
                                            .withStyle(ChatFormatting.RED),
                                    true);
                            return InteractionResult.FAIL;
                        })
                .orElse(InteractionResult.FAIL);
    }

    private void spawnRealYoshi(Player player) {
        EntityType<?> yoshiType =
                ForgeRegistries.ENTITY_TYPES.getValue(
                        new ResourceLocation("super_mario", "yoshi"));
        if (yoshiType != null) {
            Entity yoshi = yoshiType.create(this.level());
            if (yoshi != null) {
                yoshi.setPos(this.getX(), this.getY(), this.getZ());
                if (yoshi instanceof TamableAnimal tamable) {
                    tamable.tame(player);
                }
                this.level().addFreshEntity(yoshi);
                realYoshi = yoshi;
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && isHired()) {
            if (realYoshi == null || !realYoshi.isAlive()) {
                realYoshi = null;
                return;
            }

            Player owner = getOwnerPlayer();
            if (owner == null) return;

            // No mover a Yoshi si el jugador lo esta montando
            if (!realYoshi.getPassengers().isEmpty()) return;

            double distance = realYoshi.distanceTo(owner);
            if (distance > 4 && distance < 30) {
                if (realYoshi instanceof PathfinderMob mob) {
                    mob.getNavigation().moveTo(owner, 1.2);
                }
            } else if (distance >= 30) {
                realYoshi.teleportTo(owner.getX(), owner.getY(), owner.getZ());
            }
        }
    }
}
