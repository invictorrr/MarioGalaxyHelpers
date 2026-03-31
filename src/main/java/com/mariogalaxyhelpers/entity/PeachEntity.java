package com.mariogalaxyhelpers.entity;

import com.mariogalaxyhelpers.capability.ModCapabilities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class PeachEntity extends NPCEntity {

    public PeachEntity(EntityType<? extends NPCEntity> type, Level level) {
        super(type, level);
        this.setModelName("peach");
    }

    @Override
    public String getNPCId() {
        return "peach";
    }

    @Override
    public int getHireCost() {
        return 5000;
    }

    @Override
    public int getAbilityCooldown() {
        return 40;
    }

    @Override
    public boolean isAutoAbility() {
        return true;
    }

    @Override
    protected InteractionResult tryHire(Player player) {
        return player.getCapability(ModCapabilities.PLAYER_DATA)
                .map(
                        data -> {
                            if (!data.isHired("captain_toad")) {
                                player.displayClientMessage(
                                        Component.literal(
                                                        "Peach: ¡Primero salva a Captain Toad! Usa la pintura mágica...")
                                                .withStyle(
                                                        ChatFormatting.LIGHT_PURPLE,
                                                        ChatFormatting.ITALIC),
                                        false);
                                return InteractionResult.FAIL;
                            }
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

    @Override
    public void performAbility(Player owner) {
        owner.heal(0.5f);
    }
}
