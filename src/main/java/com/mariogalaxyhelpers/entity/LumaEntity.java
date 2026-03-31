package com.mariogalaxyhelpers.entity;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;

public class LumaEntity extends NPCEntity {

    public LumaEntity(EntityType<? extends NPCEntity> type, Level level) {
        super(type, level);
        // CPM (customcpmodel) resuelve assets/customcpmodel/cpmmodels/{nombre}.cpmmodel
        // El modelo de Luma de super_block_world debe existir en ese path (p. ej. el mod lo aporta
        // como customcpmodel/cpmmodels/luma.cpmmodel para fusionarse con el resource pack).
        this.setModelName("luma");
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
        return 600;
    }

    @Override
    public boolean isAutoAbility() {
        return true;
    }

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

        List<Monster> monsters =
                this.level()
                        .getEntitiesOfClass(
                                Monster.class, this.getBoundingBox().inflate(12));

        for (Monster monster : monsters) {
            monster.setSecondsOnFire(5);
            monster.hurt(this.damageSources().onFire(), 6.0f);
        }

        owner.displayClientMessage(
                Component.literal("\u2726 Luma irradia energ\u00eda estelar \u2726")
                        .withStyle(ChatFormatting.LIGHT_PURPLE),
                false);
    }
}
