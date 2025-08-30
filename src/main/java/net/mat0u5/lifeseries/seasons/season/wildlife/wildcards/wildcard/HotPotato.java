package net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Random;

public class HotPotato extends Wildcard {

    private static final int TICKS_PER_SECOND = 20;
    private static final int DURATION_TICKS = 60 * 60 * TICKS_PER_SECOND;

    private static final Item HOT_POTATO_ITEM = Items.POTATO;
    private ServerPlayerEntity potatoHolder;
    private ServerPlayerEntity lastHolder;
    private int tickCounter = 0;
    private final Random random = new Random();

    @Override
    public Wildcards getType() {
        return Wildcards.HOT_POTATO;
    }

    @Override
    public void activate() {
        List<ServerPlayerEntity> players = PlayerUtils.getAllFunctioningPlayers();
        if (players.isEmpty()) return;
        potatoHolder = players.get(random.nextInt(players.size()));
        givePotato(potatoHolder);
    }

    private void givePotato(ServerPlayerEntity player) {
        player.getInventory().insertStack(new ItemStack(HOT_POTATO_ITEM));
        lastHolder = player;
        PlayerUtils.displayMessageToPlayer(player, Text.literal("You have the Hot Potato - Give it to someone else, or else!"), 100);
    }

    @Override
    public void tick() {
        tickCounter++;
        if (tickCounter >= DURATION_TICKS) {
            if (lastHolder != null && lastHolder.isAlive()) {
                lastHolder.kill(PlayerUtils.getServerWorld(lastHolder));
            }
            deactivate();
        }

        if (potatoHolder != null) {
            if (!potatoHolder.getInventory().contains(new ItemStack(HOT_POTATO_ITEM))) {
                if (lastHolder != null && lastHolder.isAlive()) {
                    lastHolder.kill(PlayerUtils.getServerWorld(lastHolder));
                }
                deactivate();
            }
        }
    }

    public void playerPickedUp(ServerPlayerEntity player, ItemStack stack) {
        if (!stack.isOf(HOT_POTATO_ITEM)) return;
        potatoHolder = player;
        lastHolder = player;
        PlayerUtils.displayMessageToPlayer(player, Text.literal("You have the Hot Potato - Give it to someone else, or else!"), 100);
    }

    public void playerDropped(ServerPlayerEntity player, ItemStack stack) {
        if (!stack.isOf(HOT_POTATO_ITEM)) return;
        if (lastHolder != null && lastHolder.isAlive()) {
            lastHolder.kill(PlayerUtils.getServerWorld(lastHolder));
        }
        potatoHolder = null;
    }

    @Override
    public void deactivate() {
        if (potatoHolder != null) {
            PlayerUtils.clearItemStack(potatoHolder, new ItemStack(HOT_POTATO_ITEM));
        }
        potatoHolder = null;
        lastHolder = null;
        tickCounter = 0;
    }
}