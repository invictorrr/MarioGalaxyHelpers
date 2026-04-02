package com.mariogalaxyhelpers.entity;

import com.mariogalaxyhelpers.capability.ModCapabilities;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public class LumaEntity extends NPCEntity {

    private Entity visualEntity;
    private boolean hadPassenger;

    public LumaEntity(EntityType<? extends NPCEntity> type, Level level) {
        super(type, level);
        this.setInvisible(true);
    }

    @Override
    public String getNPCId() {
        return "luma";
    }

    @Override
    public int getHireCost() {
        return 2500;
    }

    @Override
    public int getAbilityCooldown() {
        return 200;
    }

    @Override
    public boolean isAutoAbility() {
        return true;
    }

    @Override
    public boolean isPickable() {
        // Siempre pickable: antes de hire para contratar, despues para montar
        return true;
    }

    @Override
    public boolean isInvisibleTo(Player player) {
        return true;
    }

    @Override
    public float getScale() {
        return 0.0f;
    }

    @Override
    protected boolean shouldFollowOwner() {
        return this.getPassengers().isEmpty();
    }

    // --- Montable ---

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && isHired()) {
            player.startRiding(this);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        return passenger.position();
    }

    // --- Contratacion ---

    @Override
    protected InteractionResult tryHire(Player player) {
        return player.getCapability(ModCapabilities.PLAYER_DATA)
                .map(
                        data -> {
                            if (data.spendCoins(getHireCost())) {
                                setOwner(player);
                                data.hire(getNPCId());
                                ModCapabilities.syncToClient(player);

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
                                        Component.literal("\u00a1Luma contratado!")
                                                .withStyle(ChatFormatting.LIGHT_PURPLE),
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

    // --- Visual ---

    private void spawnVisualEntity() {
        EntityType<?> lumaType =
                ForgeRegistries.ENTITY_TYPES.getValue(
                        new ResourceLocation("super_block_world", "luma"));
        if (lumaType != null) {
            Entity luma = lumaType.create(this.level());
            if (luma != null) {
                luma.setPos(this.getX(), this.getY(), this.getZ());
                luma.setInvulnerable(true);
                luma.setNoGravity(true);
                if (luma instanceof Mob mob) {
                    mob.setNoAi(true);
                }
                this.level().addFreshEntity(luma);
                visualEntity = luma;
            }
        }
    }

    // --- Tick ---

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            boolean hasPassenger = !this.getPassengers().isEmpty();

            // Levitacion cuando alguien monta
            if (hasPassenger) {
                this.setNoGravity(true);
                this.setDeltaMovement(0, 0.15, 0);
            } else {
                this.setNoGravity(false);
            }

            // Al desmontarse: dar slow falling para no morir
            if (hadPassenger && !hasPassenger) {
                Player owner = getOwnerPlayer();
                if (owner != null) {
                    owner.addEffect(
                            new MobEffectInstance(
                                    MobEffects.SLOW_FALLING, 600, 0, false, true));
                    owner.displayClientMessage(
                            Component.literal("\u2726 Luma te protege al caer \u2726")
                                    .withStyle(ChatFormatting.LIGHT_PURPLE),
                            true);
                }
            }
            hadPassenger = hasPassenger;

            // Mantener entidad visual
            if (visualEntity == null || !visualEntity.isAlive()) {
                spawnVisualEntity();
            }
            if (visualEntity != null && visualEntity.isAlive()) {
                visualEntity.setPos(this.getX(), this.getY(), this.getZ());
                visualEntity.setYRot(this.getYRot());
                visualEntity.setXRot(this.getXRot());
            }
        }
    }

    // --- Habilidad fuego ---

    @Override
    public void performAbility(Player owner) {
        ServerLevel serverLevel = (ServerLevel) this.level();

        serverLevel.sendParticles(
                ParticleTypes.FLAME,
                this.getX(),
                this.getY() + 0.5,
                this.getZ(),
                100,
                3,
                2,
                3,
                0.05);

        this.level()
                .playSound(
                        null,
                        this.blockPosition(),
                        SoundEvents.FIRECHARGE_USE,
                        SoundSource.NEUTRAL,
                        1.0f,
                        0.7f);

        List<LivingEntity> entities =
                this.level()
                        .getEntitiesOfClass(
                                LivingEntity.class, this.getBoundingBox().inflate(12));

        for (LivingEntity entity : entities) {
            if (entity instanceof Player) continue;
            if (entity instanceof NPCEntity) continue;
            if (entity == visualEntity) continue;
            if (entity.isInvulnerable()) continue;
            if (entity instanceof TamableAnimal tamable && tamable.isOwnedBy(owner))
                continue;
            ResourceLocation entityId =
                    ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
            if (entityId != null && entityId.toString().equals("super_mario:yoshi"))
                continue;
            entity.setSecondsOnFire(5);
            entity.hurt(this.damageSources().onFire(), 6.0f);
        }

        owner.displayClientMessage(
                Component.literal("\u2726 Luma irradia energ\u00eda estelar \u2726")
                        .withStyle(ChatFormatting.LIGHT_PURPLE),
                false);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (visualEntity != null && visualEntity.isAlive()) {
            visualEntity.discard();
        }
        super.remove(reason);
    }
}
