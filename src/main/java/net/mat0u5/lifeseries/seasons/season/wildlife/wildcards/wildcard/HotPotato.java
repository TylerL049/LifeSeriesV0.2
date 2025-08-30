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

    private static final int DEFAULT_FUSE_TICKS = 600; // 30 seconds
    private static final int DEFAULT_DURATION_TICKS = 600; // 30 seconds until explosion

    public HotPotato() {
        this.active = false;
    }

    @Override
    public Wildcards getType() {
        return Wildcards.HOT_POTATO;
    }

    /** Called by WildcardManager */
    @Override
    public void activate() {
        this.active = true;

        // Schedule holder assignment after 30 seconds
        TaskScheduler.scheduleTask(DEFAULT_FUSE_TICKS, () -> {
            List<ServerPlayerEntity> candidates = livesManager.getAlivePlayers();
            candidates.removeIf(WatcherManager::isWatcher);

            if (candidates.isEmpty()) {
                reset();
                return;
            }

            ServerPlayerEntity chosen = candidates.get(new Random().nextInt(candidates.size()));
            start(chosen, DEFAULT_DURATION_TICKS);
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

        // Notify only the holder
        PlayerUtils.sendTitle(
                potatoHolder,
                Text.literal("You have the Hot Potato!").formatted(Formatting.RED),
                20, 40, 20
        );
    }

    /** Called every server tick by WildcardManager.tick() */
    @Override
    public void tick() {
        if (!active || potatoHolder == null) return;

        // Re-insert potato if player dropped it
        if (!playerHasPotato(potatoHolder)) {
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

    /** Gives a potato item to the player, drops if inventory full */
    private void givePotato(ServerPlayerEntity player) {
        if (player == null) return;
        ItemStack potato = new ItemStack(Items.POTATO);

        if (!player.getInventory().insertStack(potato)) {
            player.dropItem(potato, false);
        }
    }

    /** Removes all potatoes from a player */
    private void removePotato(ServerPlayerEntity player) {
        if (player == null) return;

        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == Items.POTATO) {
                player.getInventory().removeStack(i);
                i--; // adjust index after removal
            }
        }
    }

    /** Checks if player currently has a potato */
    private boolean playerHasPotato(ServerPlayerEntity player) {
        if (player == null) return false;

        for (int i = 0; i < player.getInventory().size(); i++) {
            if (player.getInventory().getStack(i).getItem() == Items.POTATO) return true;
        }
        return false;
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