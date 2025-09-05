package net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.List;
import java.util.Random;

public class PlayerSwap extends Wildcard {

    private static final int TICKS_PER_SECOND = 20;
    private static final int INITIAL_DELAY = 120 * TICKS_PER_SECOND; // 2 min
    private static final int MIN_DELAY_FIRST_HOUR = 5 * 60 * TICKS_PER_SECOND; // 5 min
    private static final int MAX_DELAY_FIRST_HOUR = 10 * 60 * TICKS_PER_SECOND; // 10 min
    private static final int MIN_DELAY_AFTER_HOUR = 60 * TICKS_PER_SECOND; // 1 min
    private static final int MAX_DELAY_AFTER_HOUR = 5 * 60 * TICKS_PER_SECOND; // up to 5 min

    private static final double MOB_SWAP_CHANCE_INITIAL = 0.20; // 20%
    private static final double MOB_SWAP_CHANCE_AFTER_HOUR = 0.35; // 35%

    private final Random random = new Random();
    private int tickCounter = 0;
    private int nextSwapTick = -1;
    private boolean active = false;

    @Override
    public Wildcards getType() {
        return Wildcards.PLAYER_SWAP;
    }

    @Override
    public void activate() {
        this.active = true;
        this.tickCounter = 0;
        this.nextSwapTick = INITIAL_DELAY;
    }

    @Override
    public void tick() {
        if (!active) return;
        tickCounter++;

        if (nextSwapTick > 0 && tickCounter >= nextSwapTick) {
            doSwap();

            int elapsed = tickCounter;
            if (elapsed < 60 * 60 * TICKS_PER_SECOND) {
                nextSwapTick = tickCounter + MIN_DELAY_FIRST_HOUR +
                        random.nextInt(MAX_DELAY_FIRST_HOUR - MIN_DELAY_FIRST_HOUR + 1);
            } else {
                nextSwapTick = tickCounter + MIN_DELAY_AFTER_HOUR +
                        random.nextInt(MAX_DELAY_AFTER_HOUR - MIN_DELAY_AFTER_HOUR + 1);
            }
        }
    }

    private void doSwap() {
        List<ServerPlayerEntity> players = PlayerUtils.getAllFunctioningPlayers();
        if (players.size() < 1) return;

        ServerPlayerEntity player = players.get(random.nextInt(players.size()));

        boolean useMob = shouldSwapWithMob();
        if (useMob) {
            swapWithMob(player);
        } else if (players.size() > 1) {
            ServerPlayerEntity other = players.get(random.nextInt(players.size()));
            while (other == player && players.size() > 1) {
                other = players.get(random.nextInt(players.size()));
            }
            swapEntities(player, other);
            applyNegativeEffects(player);
            applyNegativeEffects(other);
        }
    }

    private boolean shouldSwapWithMob() {
        double chance = (tickCounter < 60 * 60 * TICKS_PER_SECOND)
                ? MOB_SWAP_CHANCE_INITIAL
                : MOB_SWAP_CHANCE_AFTER_HOUR;
        return random.nextDouble() < chance;
    }

    private void swapWithMob(ServerPlayerEntity player) {
        ServerWorld world = player.getWorld();
        List<MobEntity> mobs = world.getEntitiesByClass(MobEntity.class,
                player.getBoundingBox().expand(100), mob -> true);
        if (mobs.isEmpty()) return;

        MobEntity mob = mobs.get(random.nextInt(mobs.size()));
        swapEntities(player, mob);
        applyNegativeEffects(player);
    }

    private void swapEntities(Entity a, Entity b) {
        if (a == null || b == null) return;

        var posA = a.getPos();
        var yawA = a.getYaw();
        var pitchA = a.getPitch();

        // Use the Fabric 1.21.6 teleport method
        a.teleport(b.getWorld(), b.getX(), b.getY(), b.getZ(), b.getYaw(), b.getPitch());
        b.teleport(a.getWorld(), posA.x, posA.y, posA.z, yawA, pitchA);
        //adding this to commit
    }

    private void applyNegativeEffects(ServerPlayerEntity player) {
        if (player == null || player.isSpectator()) return;

        int duration = 5 * TICKS_PER_SECOND; // 5 seconds

        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, duration, 0, false, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, duration, 1, false, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, duration, 0, false, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, duration, 0, false, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, duration, 0, false, false, false));
    }

    @Override
    public void deactivate() {
        this.active = false;
        this.tickCounter = 0;
        this.nextSwapTick = -1;
    }
}