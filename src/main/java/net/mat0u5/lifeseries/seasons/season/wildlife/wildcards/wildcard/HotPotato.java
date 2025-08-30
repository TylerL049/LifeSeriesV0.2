package net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.mat0u5.lifeseries.seasons.other.LivesManager;
import net.mat0u5.lifeseries.seasons.other.WatcherManager;
import net.mat0u5.lifeseries.utils.other.TaskScheduler;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
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

    /** Activates the Hot Potato wildcard silently, waits 30 seconds before assigning a holder */
    public void activate(int fuseTicks) {
        this.active = true;

        // Schedule assignment after 30 seconds (600 ticks)
        TaskScheduler.scheduleTask(600, () -> {
            List<ServerPlayerEntity> candidates = livesManager.getAlivePlayers();
            candidates.removeIf(WatcherManager::isWatcher);

            if (candidates.isEmpty()) {
                reset();
                return;
            }

            ServerPlayerEntity chosen = candidates.get(new Random().nextInt(candidates.size()));
            start(chosen, fuseTicks);
        });
    }

    /** Starts the Hot Potato for a specific player */
    private void start(ServerPlayerEntity starter, int fuseTicks) {
        if (starter == null) {
            reset();
            return;
        }

        this.potatoHolder = starter;
        this.lastHolder = null;
        this.ticksUntilExplode = fuseTicks;

        givePotato(potatoHolder);

        PlayerUtils.sendTitle(
                potatoHolder,
                Text.literal("You have the Hot Potato!").formatted(Formatting.RED),
                20, 40, 20
        );
    }

    /** Tick logic, should be called every server tick */
    public void tick() {
        if (!active || potatoHolder == null) return;

        // Reinsert the potato if the player dropped it accidentally
        if (!potatoHolder.getInventory().contains(new ItemStack(Items.POTATO))) {
            givePotato(potatoHolder);
        }

        ticksUntilExplode--;
        if (ticksUntilExplode <= 0) {
            explode();
        }
    }

    /** Passes the potato to another player */
    public void passTo(ServerPlayerEntity nextPlayer) {
        if (!active || nextPlayer == null || nextPlayer == potatoHolder) return;

        lastHolder = potatoHolder;
        potatoHolder = nextPlayer;

        removePotato(lastHolder);
        givePotato(potatoHolder);

        PlayerUtils.sendTitle(
                potatoHolder,
                Text.literal("You have the Hot Potato!").formatted(Formatting.RED),
                20, 40, 20
        );
    }

    /** Handles explosion of the potato */
    private void explode() {
        if (potatoHolder != null) {
            removePotato(potatoHolder);

            int currentLives = livesManager.getPlayerLives(potatoHolder);
            livesManager.setPlayerLives(potatoHolder, currentLives - 1);

            PlayerUtils.sendTitle(
                    potatoHolder,
                    Text.literal("The Hot Potato exploded!").formatted(Formatting.RED),
                    20, 40, 20
            );
        }

        if (lastHolder != null) {
            PlayerUtils.sendTitle(
                    lastHolder,
                    Text.literal("You just passed the Hot Potato.").formatted(Formatting.GRAY),
                    20, 40, 20
            );
        }

        reset();
    }

    /** Safely gives a potato item to a player, drops it if inventory full */
    private void givePotato(ServerPlayerEntity player) {
        if (player == null) return;
        ItemStack potato = new ItemStack(Items.POTATO);

        if (!player.getInventory().insertStack(potato)) {
            player.dropItem(potato, false);
        }
    }

    /** Removes all potato items from a player */
    private void removePotato(ServerPlayerEntity player) {
        if (player == null) return;
        player.getInventory().removeStack(Items.POTATO);
    }

    /** Resets the wildcard state */
    private void reset() {
        potatoHolder = null;
        lastHolder = null;
        ticksUntilExplode = 0;
        active = false;
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