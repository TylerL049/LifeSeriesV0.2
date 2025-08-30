package net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.mat0u5.lifeseries.seasons.other.LivesManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.UUID;

import static net.mat0u5.lifeseries.Main.livesManager;

public class HotPotato extends Wildcard {

    private ServerPlayerEntity potatoHolder;
    private ServerPlayerEntity lastHolder;
    private int ticksUntilExplode;
    private boolean active;

    public HotPotato() {
        this.active = false;
    }

    public void start(ServerPlayerEntity starter, int fuseTicks) {
        this.potatoHolder = starter;
        this.lastHolder = null;
        this.ticksUntilExplode = fuseTicks;
        this.active = true;
        broadcastPotatoHolder();
    }

    public void tick() {
        if (!active) return;

        ticksUntilExplode--;
        if (ticksUntilExplode <= 0) {
            explode();
        }
    }

    public void passTo(ServerPlayerEntity nextPlayer) {
        if (!active) return;
        if (nextPlayer == null) return;
        lastHolder = potatoHolder;
        potatoHolder = nextPlayer;
        broadcastPotatoHolder();
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

    private void broadcastPotatoHolder() {
        // Optional: implement broadcast logic if needed
    }

    public void reset() {
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