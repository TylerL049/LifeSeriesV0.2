package net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.mat0u5.lifeseries.seasons.other.WatcherManager;
import net.mat0u5.lifeseries.utils.other.TaskScheduler;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.entity.damage.DamageSource;
import net.mat0u5.lifeseries.Main;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static net.mat0u5.lifeseries.Main.livesManager;

public class HotPotato extends Wildcard {

    private static HotPotato instance;

    private ServerPlayerEntity potatoHolder;
    private ServerPlayerEntity lastHolder;
    private boolean active;
    private boolean potatoAssigned;
    private UUID potatoUuid;

    private static final int DELAY_TICKS = 200;
    private static final int FUSE_DURATION = 12000;
    private static final String NBT_KEY = "HotPotatoUUID";

    private HotPotato() {
        this.active = false;
        this.potatoAssigned = false;
    }

    public static HotPotato getInstance() {
        if (instance == null) instance = new HotPotato();
        return instance;
    }

    @Override
    public Wildcards getType() {
        return Wildcards.HOT_POTATO;
    }

    @Override
    public void activate() {
        this.active = true;
        this.potatoAssigned = false;
        this.potatoUuid = UUID.randomUUID();
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
        PlayerUtils.sendTitle(
                potatoHolder,
                Text.literal("You have the Hot Potato!").formatted(Formatting.RED),
                20, 40, 20
        );
        TaskScheduler.scheduleTask(FUSE_DURATION, this::explode);
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
        if (!active || !potatoAssigned) return;
        if (potatoHolder != null) {
            removePotato(potatoHolder);
            PlayerUtils.broadcastMessage(
                    Text.literal(potatoHolder.getName().getString() + " didn't want to get rid of the Potato")
                            .formatted(Formatting.RED)
            );
            PlayerUtils.sendTitle(
                    potatoHolder,
                    Text.literal("The Hot Potato exploded!").formatted(Formatting.RED),
                    20, 40, 20
            );
            // Kill the player
            potatoHolder.damage(DamageSource.generic(), Float.MAX_VALUE);
        }
        reset();
    }

    private void givePotato(ServerPlayerEntity player) {
        if (player == null) return;
        ItemStack potato = new ItemStack(Items.POTATO);
        potato.setCustomName(Text.literal("Hot Potato").formatted(Formatting.RED, Formatting.BOLD));

        // Add lore
        List<Text> lore = List.of(
                Text.literal("You have been given the Hot Potato").formatted(Formatting.GRAY),
                Text.literal("It will explode during this session").formatted(Formatting.DARK_RED),
                Text.literal("Don't be the last player holding it").formatted(Formatting.RED)
        );
        NbtCompound display = new NbtCompound();
        for (int i = 0; i < lore.size(); i++) {
            display.putString("Lore" + i, lore.get(i).getString());
        }
        potato.getOrCreateNbt().put("display", display);

        // Set HotPotato UUID
        potato.getOrCreateNbt().putString(NBT_KEY, potatoUuid.toString());

        if (!player.getInventory().insertStack(potato)) {
            player.dropItem(potato, false);
        }
    }

    private void removePotato(ServerPlayerEntity player) {
        if (player == null) return;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (isHotPotato(stack)) {
                player.getInventory().removeStack(i);
                i--;
            }
        }
    }

    public boolean isHotPotato(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        NbtCompound nbt = stack.getOrCreateNbt();
        return nbt.contains(NBT_KEY) && potatoUuid != null && potatoUuid.toString().equals(nbt.getString(NBT_KEY));
    }

    private void reset() {
        potatoHolder = null;
        lastHolder = null;
        active = false;
        potatoAssigned = false;
        potatoUuid = null;
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

    public void onPlayerPickupPotato(ServerPlayerEntity player, ItemStack stack) {
        passTo(player);
    }
}
