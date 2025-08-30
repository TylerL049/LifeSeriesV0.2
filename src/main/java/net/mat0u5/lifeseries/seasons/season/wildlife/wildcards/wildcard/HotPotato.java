package net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Random;
import java.util.UUID;

public class HotPotato extends Wildcard {

    private static final int TICKS_PER_SECOND = 20;
    private static final int DURATION_TICKS = 60 * TICKS_PER_SECOND * 60;
    private final Item HOT_POTATO_ITEM = Items.POTATO;
    private final Random random = new Random();
    private ServerPlayerEntity potatoHolder = null;
    private ServerPlayerEntity lastHolder = null;
    private int tickCounter = 0;
    private boolean active = false;

    @Override
    public Wildcards getType() {
        return Wildcards.HOT_POTATO;
    }

    @Override
    public void activate() {
        active = true;
        potatoHolder = PlayerUtils.getRandomAlivePlayer();
        if (potatoHolder != null) {
            potatoHolder.getInventory().insertStack(new ItemStack(HOT_POTATO_ITEM));
            potatoHolder.sendMessage(
                    net.minecraft.text.Text.literal("You have the Hot Potato - Give it to someone else, or else"),
                    false
            );
            lastHolder = potatoHolder;
        }
    }

    @Override
    public void tick() {
        if (!active || potatoHolder == null) return;
        tickCounter++;
        if (tickCounter >= DURATION_TICKS) {
            if (potatoHolder != null) {
                potatoHolder.kill((ServerWorld) potatoHolder.getWorld());
            } else if (lastHolder != null) {
                lastHolder.kill((ServerWorld) lastHolder.getWorld());
            }
            deactivate();
        } else {
            for (ServerPlayerEntity player : PlayerUtils.getAllAlivePlayers()) {
                if (player.getInventory().contains(new ItemStack(HOT_POTATO_ITEM))) {
                    if (player != potatoHolder) {
                        lastHolder = potatoHolder;
                        potatoHolder = player;
                        potatoHolder.sendMessage(
                                net.minecraft.text.Text.literal("You have the Hot Potato - Give it to someone else, or else"),
                                false
                        );
                    }
                }
            }
        }
    }

    @Override
    public void deactivate() {
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            player.getInventory().remove(stack -> stack.isOf(HOT_POTATO_ITEM), Integer.MAX_VALUE, player.getInventory());
        }
        potatoHolder = null;
        lastHolder = null;
        tickCounter = 0;
        active = false;
    }
}