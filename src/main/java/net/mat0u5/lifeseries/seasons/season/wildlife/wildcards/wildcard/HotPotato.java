package net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.mat0u5.lifeseries.seasons.other.LivesManager;
import net.mat0u5.lifeseries.network.NetworkHandlerClient;
import net.mat0u5.lifeseries.utils.enums.PacketNames;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.mat0u5.lifeseries.Main.*;

public class HotPotato {

    private static final String HOT_POTATO_ITEM = "hot_potato";
    private ServerPlayerEntity potatoHolder;
    private ServerPlayerEntity lastHolder;
    private int ticksUntilExplode;
    private boolean active;

    public HotPotato() {
        this.active = false;
    }

    public void start(ServerPlayerEntity starter, int fuseTicks) {
        this.potatoHolder = starter;
        this.lastHolder = null;
        this.ticksUntilExplode = fuseTicks;
        this.active = true;
        broadcastPotatoHolder();
    }

    public void tick() {
        if (!active) return;

        ticksUntilExplode--;
        if (ticksUntilExplode <= 0) {
            explode();
        }
    }

    public void passTo(ServerPlayerEntity nextPlayer) {
        if (!active) return;
        if (nextPlayer == null) return;
        lastHolder = potatoHolder;
        potatoHolder = nextPlayer;
        broadcastPotatoHolder();
    }

    private void explode() {
        if (potatoHolder != null) {
            livesManager.setPlayerLives(potatoHolder, livesManager.getPlayerLives(potatoHolder) - 1);
            PlayerUtils.sendTitle(potatoHolder, Text.of("§cThe Hot Potato exploded!"), 20, 40, 20);
        }
        if (lastHolder != null) {
            PlayerUtils.sendTitle(lastHolder, Text.of("§7You just passed the Hot Potato."), 20, 40, 20);
        }
        reset();
    }

    private void broadcastPotatoHolder() {
        if (potatoHolder == null) return;
        NetworkHandlerClient.sendStringPacket(PacketNames.SELECTED_WILDCARD, "hot_potato:" + potatoHolder.getUuid());
    }

    public void reset() {
        this.potatoHolder = null;
        this.lastHolder = null;
        this.ticksUntilExplode = 0;
        this.active = false;
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