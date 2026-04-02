package com.mariogalaxyhelpers.entity;

import com.mariogalaxyhelpers.capability.ModCapabilities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class PeachEntity extends NPCEntity {

    private static List<Item> cachedPowerups;
    private int powerupTimer = 600;

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
        return 60;
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
                                                        "Peach: \u00a1Primero salva a Captain Toad! Usa la pintura m\u00e1gica...")
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
        // Regeneracion II constante
        owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 1, false, true));
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && isHired()) {
            Player owner = getOwnerPlayer();
            if (owner != null) {
                powerupTimer--;
                if (powerupTimer <= 0) {
                    powerupTimer = 600;
                    givePowerup(owner);
                }
            }
        }
    }

    private void givePowerup(Player owner) {
        List<Item> powerups = getPowerups();
        if (powerups.isEmpty()) return;

        Item item = powerups.get(this.random.nextInt(powerups.size()));
        ItemStack stack = new ItemStack(item, 1);
        ItemEntity itemEntity =
                new ItemEntity(
                        this.level(),
                        this.getX(),
                        this.getY() + 0.5,
                        this.getZ(),
                        stack);
        this.level().addFreshEntity(itemEntity);

        this.level()
                .playSound(
                        null,
                        owner.blockPosition(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP,
                        SoundSource.PLAYERS,
                        1.0f,
                        1.2f);
        owner.displayClientMessage(
                Component.literal("Peach: \u00a1Toma un power-up!")
                        .withStyle(ChatFormatting.LIGHT_PURPLE),
                false);
    }

    private static List<Item> getPowerups() {
        if (cachedPowerups == null) {
            cachedPowerups = new ArrayList<>();
            String[] candidates = {
                "mario_power_ups:fire_flower_item",
                "mario_power_ups:ice_flower_item",
                "mario_power_ups:super_mushroom_item",
                "mario_power_ups:super_star_item",
                "mario_power_ups:one_up_item",
                "mario_power_ups:propeller_mushroom_item",
                "mario_power_ups:cloud_flower_item",
                "mario_power_ups:boomerang_flower_item",
                "mario_power_ups:gold_flower_item",
                "mario_power_ups:super_leaf",
                "mario_power_ups:p_wing"
            };
            for (String id : candidates) {
                Item item =
                        ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
                if (item != null && item != Items.AIR) {
                    cachedPowerups.add(item);
                }
            }
        }
        return cachedPowerups;
    }
}
