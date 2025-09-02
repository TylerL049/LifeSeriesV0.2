package net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.seasons.other.WatcherManager;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.mat0u5.lifeseries.utils.other.TaskScheduler;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.entity.damage.DamageSource;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static net.mat0u5.lifeseries.Main.livesManager;

public class HotPotato extends Wildcard {

    private static HotPotato instance;

    public static HotPotato getInstance() {
        if (instance == null) instance = new HotPotato();
        return instance;
    }

    private ServerPlayerEntity potatoHolder;
    private ServerPlayerEntity lastHolder;
    private boolean active;
    private boolean potatoAssigned;
    private UUID potatoUuid;

    private static final int DELAY_TICKS = 200;
    private static final int FUSE_DURATION = 13000;
    private static final String NBT_KEY = "HotPotatoUUID";

    private HotPotato() {
        this.active = false;
        this.potatoAssigned = false;
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

            // Kill the player
            potatoHolder.damage(DamageSource.GENERIC, Float.MAX_VALUE);

            PlayerUtils.broadcastMessage(
                    Text.literal(potatoHolder.getName().getString() + " didn't want to get rid of the Potato")
                            .formatted(Formatting.RED)
            );

            PlayerUtils.sendTitle(
                    potatoHolder,
                    Text.literal("The Hot Potato exploded!").formatted(Formatting.RED),
                    20, 40, 20
            );
        }

        reset();
    }

    private void givePotato(ServerPlayerEntity player) {
        if (player == null) return;

        ItemStack potato = new ItemStack(Items.POTATO);
        potato.setCustomName(Text.literal("Hot Potato").formatted(Formatting.RED, Formatting.BOLD));

        NbtCompound tag = new NbtCompound();
        tag.putString(NBT_KEY, potatoUuid.toString());
        potato.getOrCreateNbt().put(NBT_KEY, potatoUuid.toString());

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

    private boolean isHotPotato(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (!stack.getOrCreateNbt().contains(NBT_KEY)) return false;
        return potatoUuid != null && potatoUuid.toString().equals(stack.getOrCreateNbt().getString(NBT_KEY));
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
}