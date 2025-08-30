package net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.mat0u5.lifeseries.seasons.other.LivesManager;
import net.mat0u5.lifeseries.utils.other.TaskScheduler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Random;

import static net.mat0u5.lifeseries.Main.livesManager;

public class HotPotato extends Wildcard {

    private ServerPlayerEntity potatoHolder;
    private ServerPlayerEntity lastHolder;
    private int ticksUntilExplode;
    private boolean active;

    public HotPotato() {
        this.active = false;
    }

    @Override
    public Wildcards getType() {
        return Wildcards.HOT_POTATO;
    }

    public void activate(int fuseTicks) {
        List<ServerPlayerEntity> alivePlayers = livesManager.getAlivePlayers();
        if (alivePlayers.isEmpty()) return;

        // Choose a random player
        Random rand = new Random();
        ServerPlayerEntity chosen = alivePlayers.get(rand.nextInt(alivePlayers.size()));

        start(chosen, fuseTicks);
    }

    private void start(ServerPlayerEntity starter, int fuseTicks) {
        this.potatoHolder = starter;
        this.lastHolder = null;
        this.ticksUntilExplode = fuseTicks;
        this.active = true;

        // Notify only the holder
        PlayerUtils.sendTitle(potatoHolder, Text.literal("You have the Hot Potato!").formatted(Formatting.RED), 20, 40, 20);
    }

    public void tick() {
        if (!active) return;

        ticksUntilExplode--;
        if (ticksUntilExplode <= 0) {
            explode();
        }
    }

    public void passTo(ServerPlayerEntity nextPlayer) {
        if (!active || nextPlayer == null) return;

        lastHolder = potatoHolder;
        potatoHolder = nextPlayer;

        PlayerUtils.sendTitle(potatoHolder, Text.literal("You have the Hot Potato!").formatted(Formatting.RED), 20, 40, 20);
    }

    private void explode() {
        if (potatoHolder != null) {
            int currentLives = livesManager.getPlayerLives(potatoHolder);
            livesManager.setPlayerLives(potatoHolder, currentLives - 1);
            PlayerUtils.sendTitle(potatoHolder, Text.literal("The Hot Potato exploded!").formatted(Formatting.RED), 20, 40, 20);
        }
        if (lastHolder != null) {
            PlayerUtils.sendTitle(lastHolder, Text.literal("You just passed the Hot Potato.").formatted(Formatting.GRAY), 20, 40, 20);
        }
        reset();
    }

    private void reset() {
        this.potatoHolder = null;
        this.lastHolder = null;
        this.ticksUntilExplode = 0;
        this.active = false;
    }

    public boolean isActive() {
        return active;
    }

    public ServerPlayerEntity getPotatoHolder() {
        return potatoHolder;
    }

    public ServerPlayerEntity getLastHolder() {
        return lastHolder;
    }
}