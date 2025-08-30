package net.mat0u5.lifeseries.seasons.boogeyman;

import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class Boogeyman {
    public UUID uuid;
    public String name;
    public boolean cured = false;
    public boolean failed = false;
    public boolean died = false;

    public Boogeyman(ServerPlayerEntity player) {
        uuid = player.getUuid();
        name = player.getNameForScoreboard();
    }

    public ServerPlayerEntity getPlayer() {
        return PlayerUtils.getPlayer(uuid);
    }
}
