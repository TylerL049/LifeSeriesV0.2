package net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.mat0u5.lifeseries.seasons.other.LivesManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.mat0u5.lifeseries.Main.livesManager;

public class HotPotato extends Wildcard {

    private ServerPlayerEntity potatoHolder;
    private ServerPlayerEntity lastHolder;
    private int ticksUntilExplode;
    private boolean active;

    public HotPotato() {
        this.active = false;
    }

    /**
     * Starts the Hot Potato for a given player.
     * @param starter the player who initially holds the potato
     * @param fuseTicks how many ticks until the potato explodes
     */
    public void start(ServerPlayerEntity starter, int fuseTicks) {
        if (starter == null) return;

        this.potatoHolder = starter;
        this.lastHolder = null;
        this.ticksUntilExplode = fuseTicks;
        this.active = true;

        notifyHolder();
    }

    /**
     * Call this every tick to update the fuse.
     */
    public void tick() {
        if (!active) return;

        ticksUntilExplode--;
        if (ticksUntilExplode <= 0) {
            explode();
        }
    }

    /**
     * Pass the potato to another player.
     * @param nextPlayer the player who receives the potato
     */
    public void passTo(ServerPlayerEntity nextPlayer) {
        if (!active || nextPlayer == null) return;

        lastHolder = potatoHolder;
        potatoHolder = nextPlayer;

        notifyHolder();
    }

    /**
     * Explode the potato, reducing the holder's lives by 1.
     */
    private void explode() {
        if (potatoHolder != null) {
            int currentLives = livesManager.getPlayerLives(potatoHolder);
            livesManager.setPlayerLives(potatoHolder, Math.max(currentLives - 1, 0));

            PlayerUtils.sendTitle(potatoHolder,
                    Text.literal("The Hot Potato exploded!").formatted(Formatting.RED),
                    20, 40, 20);
        }

        reset();
    }

    /**
     * Notify the current holder that they have the potato.
     */
    private void notifyHolder() {
        if (potatoHolder != null) {
            PlayerUtils.sendTitle(potatoHolder,
                    Text.literal("You have the Hot Potato!").formatted(Formatting.GOLD),
                    10, 40, 10);
        }
    }

    /**
     * Reset the Hot Potato state.
     */
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

    @Override
    public Wildcards getType() {
        return Wildcards.HOT_POTATO;
    }
}