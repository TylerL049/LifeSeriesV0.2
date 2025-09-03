package net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.WildcardManager;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.mat0u5.lifeseries.seasons.other.WatcherManager;
import net.mat0u5.lifeseries.utils.other.TaskScheduler;
import net.mat0u5.lifeseries.utils.world.ItemStackUtils;
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

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
    private UUID potatoHolderUuid;
    private String potatoHolderName; // ✅ store name for rejoin / offline tracking

    private static final int DELAY_TICKS = 200;
    private static final int FUSE_DURATION = 12000;
    private static final int CHECK_INTERVAL = 20; // 1 second
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

        // Assign potato to a random player after delay
        TaskScheduler.scheduleTask(DELAY_TICKS, this::assignPotatoToRandomPlayer);

        // Start inventory checking (self-rescheduling)
        checkPotatoHolder();
    }

    private void assignPotatoToRandomPlayer() {
        List<ServerPlayerEntity> candidates = livesManager.getAlivePlayers();
        candidates.removeIf(WatcherManager::isWatcher);
        if (candidates.isEmpty()) {
            reset();
            return;
        }

        potatoHolder = candidates.get(new Random().nextInt(candidates.size()));
        potatoHolderUuid = potatoHolder.getUuid();
        potatoHolderName = potatoHolder.getGameProfile().getName(); // ✅ store name

        givePotato(potatoHolder);
        potatoAssigned = true;

        PlayerUtils.sendTitle(
                potatoHolder,
                Text.literal("You have the Hot Potato!").formatted(Formatting.RED),
                20, 40, 20
        );
        potatoHolder.sendMessage(
            Text.literal("You have the Hot Potato! It will explode during this session. Don't be the last player holding it.")
                .formatted(Formatting.AQUA)
        );

        // Schedule fuse countdown
        TaskScheduler.scheduleTask(FUSE_DURATION, this::explode);
    }

    private void checkPotatoHolder() {
        if (!active) return;

        ServerPlayerEntity foundHolder = null;

        for (ServerPlayerEntity player : livesManager.getAlivePlayers()) {
            for (ItemStack stack : PlayerUtils.getPlayerInventory(player)) {
                if (isHotPotato(stack)) {
                    foundHolder = player;
                    break;
                }
            }
            if (foundHolder != null) break;
        }

        // If holder changed, pass potato
        if (foundHolder != null && foundHolder != potatoHolder) {
            passTo(foundHolder);
        }

        // Reschedule itself
        TaskScheduler.scheduleTask(CHECK_INTERVAL, this::checkPotatoHolder);
    }

    public void passTo(ServerPlayerEntity nextPlayer) {
        if (!active || !potatoAssigned || nextPlayer == null || nextPlayer == potatoHolder) return;

        lastHolder = potatoHolder;
        potatoHolder = nextPlayer;
        potatoHolderUuid = potatoHolder.getUuid();
        potatoHolderName = potatoHolder.getGameProfile().getName(); // ✅ update name


        potatoHolder.sendMessage(
            Text.literal("You have the Hot Potato! It will explode during this session. Don't be the last player holding it.")
                .formatted(Formatting.AQUA)
        );
        PlayerUtils.sendTitle(
            potatoHolder,
            Text.literal("You have the Hot Potato!").formatted(Formatting.RED),
            20, 40, 20
        );
    }

    private void explode() {
        if (potatoHolder != null) {
            potatoHolder.getWorld().playSound(
                null,
                potatoHolder.getBlockPos(),
                SoundEvents.ENTITY_GENERIC_EXPLODE.value(),
                SoundCategory.PLAYERS,
                1.0f,
                1.0f
            );

            PlayerUtils.sendTitle(
                potatoHolder,
                Text.literal("The Potato exploded!").formatted(Formatting.RED),
                20, 40, 20
            );

            PlayerUtils.broadcastMessage(
                Text.literal(potatoHolderName + " didn't want to get rid of the Potato")
                        .formatted(Formatting.RED)
            );
            MinecraftServer server = potatoHolder.getServer();
            if (server != null) {
                ServerCommandSource source = potatoHolder.getCommandSource(); // The player themselves is the source
                server.getCommandManager().executeWithPrefix(source, "damage " + potatoHolder.getName().getString() + " 1000 minecraft:explosion");
            }
        } else if (potatoHolderUuid != null) {
            // Holder logged out, still blame them
            PlayerUtils.broadcastMessage(
                Text.literal(potatoHolderName + " didn't want to get rid of the Potato")
                        .formatted(Formatting.RED)
            );
        }

        reset(); // safely remove all potatoes & deactivate wildcard
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

        for (ItemStack stack : PlayerUtils.getPlayerInventory(player)) {
            if (isHotPotato(stack)) {
                PlayerUtils.clearItemStack(player, stack); // ✅ removes & syncs properly
            }
        }

        // Remove cursor stack if holding potato
        if (isHotPotato(player.currentScreenHandler.getCursorStack())) {
            player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
            player.currentScreenHandler.sendContentUpdates();
        }
    }

    public boolean isHotPotato(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return ItemStackUtils.hasCustomComponentEntry(stack, NBT_KEY);
    }

    private void reset() {
        for (ServerPlayerEntity player : livesManager.getAlivePlayers()) {
            removePotato(player);
        }

        potatoHolder = null;
        lastHolder = null;
        potatoHolderUuid = null;
        potatoHolderName = null;
        active = false;
        potatoAssigned = false;
        potatoUuid = null;

        // Remove from wildcard manager
        WildcardManager.activeWildcards.remove(Wildcards.HOT_POTATO);
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
    public void deactivate() {
        reset();
    }
}