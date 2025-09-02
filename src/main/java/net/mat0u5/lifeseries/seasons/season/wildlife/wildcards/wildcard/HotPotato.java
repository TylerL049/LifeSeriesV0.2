package net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.mat0u5.lifeseries.seasons.other.WatcherManager;
import net.mat0u5.lifeseries.utils.other.TaskScheduler;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtCompound;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static net.mat0u5.lifeseries.Main.livesManager;

public class HotPotato extends Wildcard {

    private ServerPlayerEntity potatoHolder;
    private ServerPlayerEntity lastHolder;
    private boolean active;
    private boolean potatoAssigned;
    private UUID potatoUuid;

    private static final int DELAY_TICKS = 200;
    private static final int FUSE_DURATION = 12000;
    private static final String NBT_KEY = "HotPotatoUUID";

    public HotPotato() {
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
                Text.literal("You have the Hot Potato!\nHover over it for info").formatted(Formatting.RED),
                20, 40, 20
        );
        potatoHolder.sendMessage(Text.of("You have the Hot Potato! Hover over it for info"));

        // Schedule fuse countdown
        scheduleClickCountdown(potatoHolder, 5, 10); // 5 clicks, 10 ticks apart
        TaskScheduler.scheduleTask(FUSE_DURATION, this::explode);
    }

    public void passTo(ServerPlayerEntity nextPlayer) {
        if (!active || !potatoAssigned || nextPlayer == null || nextPlayer == potatoHolder) return;

        lastHolder = potatoHolder;
        potatoHolder = nextPlayer;
        removePotato(lastHolder);
        givePotato(potatoHolder);
        potatoHolder.sendMessage(Text.of("You have the Hot Potato! Hover over it for info"));
        PlayerUtils.sendTitle(
                potatoHolder,
                Text.literal("You have the Hot Potato!").formatted(Formatting.RED),
                20, 40, 20
        );
    }

    private void explode() {
        if (potatoHolder != null) {
            removePotato(potatoHolder);

            // Play creeper primed build-up sound 5 times with 5-tick intervals
            for (int i = 0; i < 5; i++) {
                int delay = i * 5; // 5 ticks apart
                TaskScheduler.scheduleTask(delay, () -> {
                    potatoHolder.getWorld().playSound(
                        null, // only nearby players will hear
                        potatoHolder.getBlockPos(),
                        SoundEvents.ENTITY_CREEPER_PRIMED,
                        SoundCategory.PLAYERS,
                        1.0f, // volume
                        1.0f  // pitch
                    );
                });
            }

            // Show centered title
            PlayerUtils.sendTitle(
                    potatoHolder,
                    Text.literal("The Hot Potato\nexploded!").formatted(Formatting.RED),
                    20, 40, 20
            );

            // Broadcast to others
            PlayerUtils.broadcastMessage(
                    Text.literal(potatoHolder.getName().getString() + " didn't want to get rid of the Potato")
                            .formatted(Formatting.RED)
            );
        }

        reset();
    }

    private void givePotato(ServerPlayerEntity player) {
        if (player == null) return;

        ItemStack potato = new ItemStack(Items.POTATO);
        potato.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Hot Potato").formatted(Formatting.RED, Formatting.BOLD));
        potato.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("You have been given the Hot Potato").formatted(Formatting.GRAY),
                Text.literal("It will explode during this session").formatted(Formatting.DARK_RED),
                Text.literal("Don't be the last player holding it").formatted(Formatting.RED)
        )));
        NbtCompound tag = new NbtCompound();
        tag.putString(NBT_KEY, potatoUuid.toString());
        potato.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(tag));

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
        NbtComponent comp = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (comp == null) return false;
        NbtCompound nbt = comp.copyNbt();
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
}