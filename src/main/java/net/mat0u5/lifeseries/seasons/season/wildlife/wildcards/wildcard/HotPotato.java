package net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Random;

public class HotPotato extends Wildcard {

    private static final int TICKS_PER_SECOND = 20;
    private static final int DURATION_TICKS = 60 * 60 * TICKS_PER_SECOND;

    private ServerPlayerEntity potatoHolder = null;
    private ServerPlayerEntity lastHolder = null;
    private int tickCounter = 0;
    private final Random random = new Random();

    private final Item HOT_POTATO_ITEM = Items.POTATO;

    @Override
    public Wildcards getType() {
        return Wildcards.HOT_POTATO;
    }

    @Override
    public void tick() {
        tickCounter++;

        if (!active) return;

        if (tickCounter == 1) {
            assignRandomPlayer();
        }

        if (tickCounter >= DURATION_TICKS) {
            endHotPotato();
        }

        for (ServerPlayerEntity player : PlayerUtils.getAllFunctioningPlayers()) {
            if (player.isSpectator()) continue;

            if (player.getInventory().contains(new ItemStack(HOT_POTATO_ITEM))) {
                if (potatoHolder == null || !potatoHolder.equals(player)) {
                    lastHolder = potatoHolder;
                    potatoHolder = player;
                    player.sendMessage(Text.literal("You have the Hot Potato - Give it to another player before it explodes!").formatted(Formatting.RED), false);
                }
            } else {
                if (player.equals(potatoHolder)) {
                    lastHolder = potatoHolder;
                    potatoHolder = null;
                }
            }
        }
    }

    private void assignRandomPlayer() {
        var players = PlayerUtils.getAllFunctioningPlayers();
        if (players.isEmpty()) return;
        potatoHolder = players.get(random.nextInt(players.size()));
        lastHolder = potatoHolder;
        giveHotPotato(potatoHolder);
    }

    private void giveHotPotato(ServerPlayerEntity player) {
        player.getInventory().insertStack(new ItemStack(HOT_POTATO_ITEM));
        player.sendMessage(Text.literal("You have the Hot Potato - Give it to another player before it explodes!").formatted(Formatting.RED), false);
    }

    private void endHotPotato() {
        if (potatoHolder != null) {
            potatoHolder.kill();
        } else if (lastHolder != null) {
            lastHolder.kill();
        }
        deactivate();
    }

    @Override
    public void deactivate() {
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            player.getInventory().remove(new ItemStack(HOT_POTATO_ITEM));
        }
        tickCounter = 0;
        potatoHolder = null;
        lastHolder = null;
    }
}