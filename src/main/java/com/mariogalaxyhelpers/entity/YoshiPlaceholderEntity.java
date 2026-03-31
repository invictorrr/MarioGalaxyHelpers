package com.mariogalaxyhelpers.entity;

import com.mariogalaxyhelpers.capability.ModCapabilities;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class YoshiPlaceholderEntity extends NPCEntity {

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
    protected InteractionResult tryHire(Player player) {
        return player.getCapability(ModCapabilities.PLAYER_DATA)
                .map(
                        data -> {
                            if (data.spendCoins(getHireCost())) {
                                data.hire(getNPCId());
                                ModCapabilities.syncToClient(player);

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
                                    }
                                }

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
                                        net.minecraft.network.chat.Component.literal(
                                                        "\u00a1Yoshi contratado! Click derecho para montarlo")
                                                .withStyle(ChatFormatting.GREEN),
                                        false);

                                this.discard();
                                return InteractionResult.SUCCESS;
                            }
                            player.displayClientMessage(
                                    net.minecraft.network.chat.Component.literal(
                                                    "Necesitas " + getHireCost() + " coins")
                                            .withStyle(ChatFormatting.RED),
                                    true);
                            return InteractionResult.FAIL;
                        })
                .orElse(InteractionResult.FAIL);
    }
}
