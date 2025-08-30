package net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class HotPotato extends Wildcard {

    private static final int TICKS_PER_SECOND = 20;
    private static final int DURATION_TICKS = 60 * 60 * TICKS_PER_SECOND;
    private final ItemStack HOT_POTATO_ITEM = new ItemStack(Items.POTATO);
    private final Random random = new Random();
    private ServerPlayerEntity potatoHolder = null;
    private ServerPlayerEntity lastHolder = null;
    private int tickCounter = 0;
    private boolean active = false;

    @Override
    public Wildcards getType() {
        return Wildcards.HOT_POTATO;
    }

    private List<ServerPlayerEntity> getAlivePlayers() {
        return PlayerUtils.getAllPlayers().stream()
                .filter(PlayerUtils::isAlive)
                .collect(Collectors.toList());
    }

    private ServerPlayerEntity getRandomAlivePlayer() {
        List<ServerPlayerEntity> alive = getAlivePlayers();
        if (alive.isEmpty()) return null;
        return alive.get(random.nextInt(alive.size()));
    }

    @Override
    public void activate() {
        active = true;
        potatoHolder = getRandomAlivePlayer();
        if (potatoHolder != null) {
            potatoHolder.getInventory().insertStack(HOT_POTATO_ITEM.copy());
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
            for (ServerPlayerEntity player : getAlivePlayers()) {
                boolean hasPotato = false;
                for (ItemStack stack : player.getInventory().main) {
                    if (stack.isOf(Items.POTATO)) {
                        hasPotato = true;
                        break;
                    }
                }
                if (hasPotato && player != potatoHolder) {
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

    @Override
    public void deactivate() {
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            player.getInventory().remove(stack -> stack.isOf(Items.POTATO), Integer.MAX_VALUE, player.getInventory());
        }
        potatoHolder = null;
        lastHolder = null;
        tickCounter = 0;
        active = false;
    }
}