package com.mariogalaxyhelpers.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class BowserJrEntity extends NPCEntity {

    private boolean inNether;
    private int netherTimer;

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
    public void performAbility(Player owner) {
        if (!inNether) {
            inNether = true;
            netherTimer = 600;
            this.setInvisible(true);

            ServerLevel serverLevel = (ServerLevel) this.level();

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

            this.level()
                    .playSound(
                            null,
                            this.blockPosition(),
                            SoundEvents.PORTAL_TRAVEL,
                            SoundSource.NEUTRAL,
                            0.5f,
                            1.0f);

            owner.displayClientMessage(
                    Component.literal("Bowser Jr: \u00a1Voy al Nether! \u00a1Dame 30 segundos!")
                            .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),
                    false);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && inNether) {
            netherTimer--;
            if (netherTimer <= 0) {
                returnFromNether();
            }
        }
    }

    private void returnFromNether() {
        inNether = false;
        this.setInvisible(false);

        Player owner = getOwnerPlayer();
        if (owner != null) {
            ServerLevel serverLevel = (ServerLevel) this.level();

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
        if (!this.level().isClientSide && isHired() && inNether) {
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }
}
