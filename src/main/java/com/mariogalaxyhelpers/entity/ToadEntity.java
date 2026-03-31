package com.mariogalaxyhelpers.entity;

import java.util.Comparator;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;

public class ToadEntity extends NPCEntity {

    private static final double MELEE_RANGE = 2.9;
    private static final double HUNT_SEARCH_RANGE = 10.0;
    private static final double HUNT_ABORT_DISTANCE = 40.0;
    private static final int HUNT_TIMEOUT_TICKS = 200;

    private Animal huntTarget;
    private long huntStartGameTime = -1L;

    public ToadEntity(EntityType<? extends NPCEntity> type, Level level) {
        super(type, level);
        this.setModelName("toad");
    }

    @Override
    public String getNPCId() {
        return "toad";
    }

    @Override
    public int getHireCost() {
        return 10;
    }

    @Override
    public int getAbilityCooldown() {
        return 100;
    }

    @Override
    public boolean isAutoAbility() {
        return true;
    }

    @Override
    protected boolean shouldFollowOwner() {
        return huntTarget == null;
    }

    @Override
    protected boolean canPerformAutoAbility() {
        return huntTarget == null;
    }

    @Override
    protected boolean shouldApplyAbilityCooldown() {
        return huntTarget == null;
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide && isHired()) {
            Player owner = getOwnerPlayer();
            if (owner != null && huntTarget != null) {
                processHunt(owner);
            }
        }
        super.tick();
    }

    private void processHunt(Player owner) {
        Animal target = huntTarget;
        if (!target.isAlive()) {
            endHuntAborted();
            return;
        }
        if (target.level() != this.level()) {
            endHuntAborted();
            return;
        }
        if (this.distanceTo(target) > HUNT_ABORT_DISTANCE) {
            endHuntAborted();
            return;
        }

        Level level = this.level();
        if (huntStartGameTime >= 0 && level.getGameTime() - huntStartGameTime > HUNT_TIMEOUT_TICKS) {
            endHuntAborted();
            return;
        }

        if (this.distanceTo(target) <= MELEE_RANGE) {
            killAndReward(owner, target);
            huntTarget = null;
            huntStartGameTime = -1L;
            abilityCooldown = getAbilityCooldown();
            return;
        }

        this.getNavigation().moveTo(target, 1.35);
    }

    private void endHuntAborted() {
        huntTarget = null;
        huntStartGameTime = -1L;
        abilityCooldown = getAbilityCooldown();
    }

    @Override
    public void performAbility(Player owner) {
        List<Animal> animals =
                this.level()
                        .getEntitiesOfClass(
                                Animal.class,
                                this.getBoundingBox().inflate(HUNT_SEARCH_RANGE),
                                a ->
                                        a instanceof Cow
                                                || a instanceof Pig
                                                || a instanceof Sheep
                                                || a instanceof Chicken);

        if (animals.isEmpty()) {
            return;
        }

        animals.sort(Comparator.comparingDouble(this::distanceTo));
        Animal target = animals.get(0);

        this.huntTarget = target;
        this.huntStartGameTime = this.level().getGameTime();
        this.getNavigation().moveTo(target, 1.35);
    }

    private void killAndReward(Player owner, Animal target) {
        ItemStack food = cookedFoodFor(target);

        ((ServerLevel) this.level())
                .sendParticles(
                        ParticleTypes.SWEEP_ATTACK,
                        target.getX(),
                        target.getY() + 0.5,
                        target.getZ(),
                        1,
                        0,
                        0,
                        0,
                        0);

        target.kill();
        owner.getInventory().add(food);

        this.level()
                .playSound(
                        null,
                        this.blockPosition(),
                        SoundEvents.PLAYER_ATTACK_SWEEP,
                        SoundSource.NEUTRAL,
                        1.0f,
                        1.0f);
    }

    private static ItemStack cookedFoodFor(Animal animal) {
        if (animal instanceof Cow) {
            return new ItemStack(Items.COOKED_BEEF, 2);
        }
        if (animal instanceof Pig) {
            return new ItemStack(Items.COOKED_PORKCHOP, 2);
        }
        if (animal instanceof Sheep) {
            return new ItemStack(Items.COOKED_MUTTON, 2);
        }
        if (animal instanceof Chicken) {
            return new ItemStack(Items.COOKED_CHICKEN, 1);
        }
        return new ItemStack(Items.COOKED_BEEF, 1);
    }
}
