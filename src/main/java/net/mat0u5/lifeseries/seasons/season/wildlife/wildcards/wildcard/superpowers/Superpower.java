package net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard.superpowers;

import net.mat0u5.lifeseries.network.NetworkHandlerServer;
import net.mat0u5.lifeseries.seasons.session.SessionTranscript;
import net.mat0u5.lifeseries.utils.enums.PacketNames;
import net.mat0u5.lifeseries.utils.other.OtherUtils;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class Superpower {
    public boolean active = false;
    public long cooldown = 0;
    private final UUID playerUUID;
    public Superpower(ServerPlayerEntity player) {
        playerUUID = player.getUuid();
        SessionTranscript.newSuperpower(player, getSuperpower());
    }

    @Nullable
    public ServerPlayerEntity getPlayer() {
        return PlayerUtils.getPlayer(playerUUID);
    }

    public abstract Superpowers getSuperpower();

    public int getCooldownMillis() {
        return 1000;
    }

    public void tick() {}

    public void onKeyPressed() {
        if (System.currentTimeMillis() < cooldown) {
            sendCooldownPacket();
            return;
        }
        activate();
    }

    public void activate() {
        active = true;
        cooldown(getCooldownMillis());
    }

    public void deactivate() {
        active = false;
    }

    public void turnOff() {
        deactivate();
        NetworkHandlerServer.sendLongPacket(getPlayer(), PacketNames.SUPERPOWER_COOLDOWN, 0);
    }

    public void cooldown(int millis) {
        cooldown = System.currentTimeMillis() + millis;
    }

    public void sendCooldownPacket() {
        NetworkHandlerServer.sendLongPacket(getPlayer(), PacketNames.SUPERPOWER_COOLDOWN, cooldown);
    }
}
