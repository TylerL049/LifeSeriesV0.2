package net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.Random;

public class PlayerSwap extends Wildcard {

    private static final int TICKS_PER_SECOND = 20;
    private static final int INITIAL_DELAY = 120 * TICKS_PER_SECOND; // 2 minutes
    private static final int MIN_DELAY_FIRST_HOUR = 5 * 60 * TICKS_PER_SECOND; // 5 minutes
    private static final int MAX_DELAY_FIRST_HOUR = 10 * 60 * TICKS_PER_SECOND; // 10 minutes
    private static final int MIN_DELAY_AFTER_HOUR = 60 * TICKS_PER_SECOND; // 1 minute
    private static final int MAX_DELAY_AFTER_HOUR = 5 * 60 * TICKS_PER_SECOND; // 5 minutes

    private static final double MOB_SWAP_CHANCE_INITIAL = 0.20;
    private static final double MOB_SWAP_CHANCE_AFTER_HOUR = 0.35;

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
            swapPlayers(player, other);
        }
    }

    private boolean shouldSwapWithMob() {
        double chance = (tickCounter < 60 * 60 * TICKS_PER_SECOND)
                ? MOB_SWAP_CHANCE_INITIAL
                : MOB_SWAP_CHANCE_AFTER_HOUR;
        return random.nextDouble() < chance;
    }

    private void swapPlayers(ServerPlayerEntity p1, ServerPlayerEntity p2) {
        if (p1 == null || p2 == null) return;

        double p1X = p1.getX();
        double p1Y = p1.getY();
        double p1Z = p1.getZ();

        double p2X = p2.getX();
        double p2Y = p2.getY();
        double p2Z = p2.getZ();

        ServerPlayerEntity executor = PlayerUtils.getPlayer("Talis04");
        if (executor == null) return;

        var source = executor.getCommandSource();

        // Swap player positions
        executor.getServer().getCommandManager().executeWithPrefix(
                source,
                "tp " + p1.getName().getString() + " " + p2X + " " + p2Y + " " + p2Z
        );
        executor.getServer().getCommandManager().executeWithPrefix(
                source,
                "tp " + p2.getName().getString() + " " + p1X + " " + p1Y + " " + p1Z
        );

        // Apply negative effects to both
        applyNegativeEffects(p1);
        applyNegativeEffects(p2);
    }

    private void swapWithMob(ServerPlayerEntity player) {
        if (player == null) return;

        MobEntity mob = getNearestMob(player, 50); // Finds nearest mob
        if (mob == null) return;

        double playerX = player.getX();
        double playerY = player.getY();
        double playerZ = player.getZ();

        double mobX = mob.getX();
        double mobY = mob.getY();
        double mobZ = mob.getZ();

        ServerPlayerEntity executor = PlayerUtils.getPlayer("Talis04");
        if (executor == null) return;

        var source = executor.getCommandSource();

        // Swap player <-> mob positions
        executor.getServer().getCommandManager().executeWithPrefix(
                source,
                "tp " + player.getName().getString() + " " + mobX + " " + mobY + " " + mobZ
        );
        executor.getServer().getCommandManager().executeWithPrefix(
                source,
                "tp " + mob.getUuidAsString() + " " + playerX + " " + playerY + " " + playerZ
        );

        // Only player gets negative effects
        applyNegativeEffects(player);
    }

    private MobEntity getNearestMob(ServerPlayerEntity player, double radius) {
        List<MobEntity> mobs = player.getWorld().getEntitiesByClass(
                MobEntity.class,
                player.getBoundingBox().expand(radius),
                mob -> true
        );
        if (mobs.isEmpty()) return null;
        return mobs.get(random.nextInt(mobs.size()));
    }

    private void applyNegativeEffects(ServerPlayerEntity player) {
        if (player == null || player.isSpectator()) return;

        int duration = 5 * TICKS_PER_SECOND;

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