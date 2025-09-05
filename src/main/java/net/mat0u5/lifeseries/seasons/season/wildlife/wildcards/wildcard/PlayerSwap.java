package net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.mat0u5.lifeseries.utils.other.TaskScheduler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;

import java.util.List;
import java.util.Random;

import static net.mat0u5.lifeseries.Main.livesManager;

public class PlayerSwap extends Wildcard {

    private static final int TICKS_PER_SECOND = 20;
    private final Random random = new Random();
    private boolean active = false;

    @Override
    public Wildcards getType() {
        return Wildcards.PLAYER_SWAP;
    }

    @Override
    public void activate() {
        this.active = true;
        scheduleSwap(5 * TICKS_PER_SECOND); // first swap after 5 seconds for testing
    }

    private void scheduleSwap(int delayTicks) {
        TaskScheduler.scheduleTask(delayTicks, () -> {
            if (active) doSwap();
        });
    }

    private void doSwap() {
        List<ServerPlayerEntity> players = PlayerUtils.getAllFunctioningPlayers();
        if (players.size() < 2) return;

        ServerPlayerEntity p1 = players.get(random.nextInt(players.size()));
        ServerPlayerEntity p2 = players.get(random.nextInt(players.size()));
        while (p2 == p1) {
            p2 = players.get(random.nextInt(players.size()));
        }

        MinecraftServer server = p1.getServer();
        if (server == null) return;

        // Use Talis04 as the command executor
        ServerPlayerEntity executor = server.getPlayerManager().getPlayer("Talis04");
        if (executor == null) return;

        ServerCommandSource source = executor.getCommandSource();

        // Swap players via /tp commands
        server.getCommandManager().executeWithPrefix(source,
                "tp " + p1.getName().getString() + " " + p2.getX() + " " + p2.getY() + " " + p2.getZ());
        server.getCommandManager().executeWithPrefix(source,
                "tp " + p2.getName().getString() + " " + p1.getX() + " " + p1.getY() + " " + p1.getZ());

        // Apply negative potion effects
        applyNegativeEffects(p1);
        applyNegativeEffects(p2);

        // Optionally swap a nearby mob with a random player
        MobEntity mob = PlayerUtils.getNearbyMob(p1, 50); // your helper to get nearest mob
        if (mob != null) {
            server.getCommandManager().executeWithPrefix(source,
                    "execute at " + mob.getUuidAsString() + " run tp " + mob.getUuidAsString() +
                            " " + p1.getX() + " " + p1.getY() + " " + p1.getZ());
        }

        // Schedule next swap
        scheduleSwap(30 * TICKS_PER_SECOND); // 30 seconds for testing
    }

    private void applyNegativeEffects(ServerPlayerEntity player) {
        if (player == null) return;
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
    }
}