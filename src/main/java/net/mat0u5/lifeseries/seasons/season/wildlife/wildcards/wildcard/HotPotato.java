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
    private int fuseTicks;
    private boolean active;
    private boolean potatoAssigned;

    private static final int DELAY_TICKS = 600;       // 30 seconds before assigning potato
    private static final int FUSE_DURATION = 600;     // 30 seconds countdown after assignment

    public HotPotato() {
        this.active = false;
        this.potatoAssigned = false;
    }

    @Override
    public Wildcards getType() {
        return Wildcards.HOT_POTATO;
    }

    /** Called by WildcardManager */
    @Override
    public void activate() {
        this.active = true;
        this.potatoAssigned = false;

        // Mystery delay before potato is given
        TaskScheduler.scheduleTask(DELAY_TICKS, this::assignPotatoToRandomPlayer);
    }

    private void assignPotatoToRandomPlayer() {
        List<ServerPlayerEntity> candidates = livesManager.getAlivePlayers();
        candidates.removeIf(WatcherManager::isWatcher);

        if (candidates.isEmpty()) {
            reset();
            return;
        }

        potatoHolder = candidates.get(new Random().nextInt(candidates.size()));
        givePotato(potatoHolder);
        potatoAssigned = true;
        fuseTicks = FUSE_DURATION;

        PlayerUtils.sendTitle(
                potatoHolder,
                Text.literal("You have the Hot Potato!").formatted(Formatting.RED),
                20, 40, 20
        );
    }

    @Override
    public void tick() {
        if (!active) return;

        if (potatoAssigned) {
            fuseTicks--;
            if (fuseTicks <= 0) {
                explode();
            }

            // Ensure player still has potato
            if (potatoHolder != null && !playerHasPotato(potatoHolder)) {
                givePotato(potatoHolder);
            }
        }
    }

    public void passTo(ServerPlayerEntity nextPlayer) {
        if (!active || !potatoAssigned || nextPlayer == null || nextPlayer == potatoHolder) return;

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

    private void explode() {
        if (potatoHolder != null) {
            removePotato(potatoHolder);

            int currentLives = livesManager.getPlayerLives(potatoHolder);
            livesManager.setPlayerLives(potatoHolder, currentLives - 1);

            // Console logging
            System.out.println("[HotPotato] Exploded! Current holder: " + potatoHolder.getEntityName());
            if (lastHolder != null) {
                System.out.println("[HotPotato] Last holder: " + lastHolder.getEntityName());
            } else {
                System.out.println("[HotPotato] No previous holder.");
            }

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

    private void givePotato(ServerPlayerEntity player) {
        if (player == null) return;
        ItemStack potato = new ItemStack(Items.POTATO);
        if (!player.getInventory().insertStack(potato)) {
            player.dropItem(potato, false);
        }
    }

    private void removePotato(ServerPlayerEntity player) {
        if (player == null) return;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == Items.POTATO) {
                player.getInventory().removeStack(i);
                i--;
            }
        }
    }

    private boolean playerHasPotato(ServerPlayerEntity player) {
        if (player == null) return false;
        for (int i = 0; i < player.getInventory().size(); i++) {
            if (player.getInventory().getStack(i).getItem() == Items.POTATO) return true;
        }
        return false;
    }

    private void reset() {
        potatoHolder = null;
        lastHolder = null;
        fuseTicks = 0;
        active = false;
        potatoAssigned = false;
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