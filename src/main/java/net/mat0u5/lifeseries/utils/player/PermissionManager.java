package net.mat0u5.lifeseries.utils.player;

import net.mat0u5.lifeseries.Main;
import net.minecraft.server.network.ServerPlayerEntity;

public class PermissionManager {
    public static boolean isAdmin(ServerPlayerEntity player) {
        if (player == null) return false;
        if (Main.isClientPlayer(player.getUuid())) return true;
        return player.hasPermissionLevel(2);
    }
}
