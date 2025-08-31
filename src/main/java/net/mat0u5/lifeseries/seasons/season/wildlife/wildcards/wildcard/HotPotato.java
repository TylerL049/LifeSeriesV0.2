package net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.seasons.other.WatcherManager;
import net.mat0u5.lifeseries.utils.other.TaskScheduler;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static net.mat0u5.lifeseries.Main.livesManager;

public class HotPotato extends Wildcard {

    // --- State ---
    private ServerPlayerEntity potatoHolder;
    private ServerPlayerEntity lastHolder;
    private int fuseTicks;
    private boolean potatoAssigned;
    private UUID potatoUuid;

    // --- Config ---
    private static final int DELAY_TICKS = 600;          // 30s mystery before assigning potato
    private static final int FUSE_DURATION = 12000;      // 10 minutes after assignment
    private static final String NBT_KEY = "HotPotatoId"; // UUID key to track the item

    @Override
    public Wildcards getType() {
        return Wildcards.HOT_POTATO;
    }

    /** Called by WildcardManager */
    @Override
    public void activate() {
        // IMPORTANT: don't shadow the base field! We rely on Wildcard.active used by WildcardManager.tick()
        this.active = true;
        this.potatoAssigned = false;
        this.potatoUuid = UUID.randomUUID();

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
        giveTrackedPotato(potatoHolder);
        potatoAssigned = true;
        fuseTicks = FUSE_DURATION;

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
        if (!this.active) return;

        if (potatoAssigned) {
            fuseTicks--;
            if (fuseTicks <= 0) {
                explode();
                return;
            }

            // Ensure holder still has *our* tracked potato (by UUID)
            if (potatoHolder != null && !playerHasTrackedPotato(potatoHolder)) {
                giveTrackedPotato(potatoHolder);
            }
        }
    }

    /** Manual pass API if you hook it elsewhere */
    public void passTo(ServerPlayerEntity nextPlayer) {
        if (!this.active || !potatoAssigned || nextPlayer == null || nextPlayer == potatoHolder) return;
        if (WatcherManager.isWatcher(nextPlayer)) return;
        if (!livesManager.isAlive(nextPlayer)) return;

        lastHolder = potatoHolder;
        potatoHolder = nextPlayer;

        removeTrackedPotato(lastHolder);
        giveTrackedPotato(potatoHolder);

        PlayerUtils.sendTitle(
                potatoHolder,
                Text.literal("You have the Hot Potato!").formatted(Formatting.RED),
                20, 40, 20
        );
    }

    /** Handle the explosion: remove item, deduct life, log & display debugging info */
    private void explode() {
        if (potatoHolder != null) {
            removeTrackedPotato(potatoHolder);

            int currentLives = livesManager.getPlayerLives(potatoHolder);
            livesManager.setPlayerLives(potatoHolder, currentLives - 1);

            // Console logging for debugging
            System.out.println("[HotPotato] Exploded! Current holder: " + potatoHolder.getName().getString());
            if (lastHolder != null) {
                System.out.println("[HotPotato] Last holder: " + lastHolder.getName().getString());
            } else {
                System.out.println("[HotPotato] No previous holder.");
            }

            // Show the last holder to the victim
            Text subtitle = Text.literal("Last holder: " + (lastHolder != null ? lastHolder.getName().getString() : "None"))
                                 .formatted(Formatting.GRAY);
            PlayerUtils.sendTitleWithSubtitle(
                    potatoHolder,
                    Text.literal("The Hot Potato exploded!").formatted(Formatting.RED),
                    subtitle,
                    20, 60, 20
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

    // ---------- Item helpers (tracked via UUID in NBT) ----------

    private void giveTrackedPotato(ServerPlayerEntity player) {
        if (player == null) return;
        ItemStack potato = new ItemStack(Items.POTATO);

        // Custom name for visibility
        potato.setCustomName(Text.literal("Hot Potato").formatted(Formatting.RED, Formatting.BOLD));

        // Tag with UUID so we only track/remove our potato
        NbtCompound tag = potato.getOrCreateNbt();
        tag.putUuid(NBT_KEY, potatoUuid);

        if (!player.getInventory().insertStack(potato)) {
            player.dropItem(potato, false);
        }
    }

    private void removeTrackedPotato(ServerPlayerEntity player) {
        if (player == null) return;

        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (isOurPotato(stack)) {
                player.getInventory().removeStack(i);
                i--;
            }
        }
    }

    private boolean playerHasTrackedPotato(ServerPlayerEntity player) {
        if (player == null) return false;

        for (int i = 0; i < player.getInventory().size(); i++) {
            if (isOurPotato(player.getInventory().getStack(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean isOurPotato(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getItem() != Items.POTATO) return false;
        NbtCompound nbt = stack.getNbt();
        return nbt != null && nbt.containsUuid(NBT_KEY) && potatoUuid != null && potatoUuid.equals(nbt.getUuid(NBT_KEY));
    }

    // ---------- Reset ----------

    private void reset() {
        potatoHolder = null;
        lastHolder = null;
        fuseTicks = 0;
        potatoAssigned = false;
        this.active = false; // uses the base Wildcard.active field
        potatoUuid = null;
    }

    // Optional getters if you use them elsewhere
    public ServerPlayerEntity getPotatoHolder() { return potatoHolder; }
    public ServerPlayerEntity getLastHolder() { return lastHolder; }
}