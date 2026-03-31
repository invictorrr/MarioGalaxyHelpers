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

public class LuigiEntity extends NPCEntity {

    private boolean isMining;
    private int miningTimer;

    public LuigiEntity(EntityType<? extends NPCEntity> type, Level level) {
        super(type, level);
        this.setModelName("luigi");
    }

    @Override
    public String getNPCId() {
        return "luigi";
    }

    @Override
    public int getHireCost() {
        return 200;
    }

    @Override
    public int getAbilityCooldown() {
        return 600;
    }

    @Override
    public boolean isAutoAbility() {
        return false;
    }

    @Override
    public void performAbility(Player owner) {
        if (!isMining) {
            isMining = true;
            miningTimer = 300;
            this.setInvisible(true);

            ((ServerLevel) this.level())
                    .sendParticles(
                            ParticleTypes.POOF,
                            this.getX(),
                            this.getY() + 1,
                            this.getZ(),
                            20,
                            0.5,
                            0.5,
                            0.5,
                            0.1);

            owner.displayClientMessage(
                    Component.literal("Luigi: \u00a1Voy a buscar hierro!")
                            .withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC),
                    false);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && isMining) {
            miningTimer--;
            if (miningTimer <= 0) {
                returnFromMining();
            }
        }
    }

    private void returnFromMining() {
        isMining = false;
        this.setInvisible(false);

        Player owner = getOwnerPlayer();
        if (owner != null) {
            int amount = 5 + (this.random.nextBoolean() ? 1 : 0);
            owner.getInventory().add(new ItemStack(Items.IRON_INGOT, amount));

            ((ServerLevel) this.level())
                    .sendParticles(
                            ParticleTypes.HAPPY_VILLAGER,
                            this.getX(),
                            this.getY() + 1,
                            this.getZ(),
                            10,
                            0.5,
                            0.5,
                            0.5,
                            0.1);

            owner.displayClientMessage(
                    Component.literal("Luigi: \u00a1Encontr\u00e9 " + amount + " hierro!")
                            .withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC),
                    false);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && isHired() && isMining) {
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }
}
