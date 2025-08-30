package net.mat0u5.lifeseries.mixin.client;

import com.google.common.collect.Maps;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.UUID;

@Mixin(value = ClientPlayNetworkHandler.class, priority = 1)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "handlePlayerListAction", at = @At("HEAD"), cancellable = true)
    private void handlePlayerListAction(PlayerListS2CPacket.Action action, PlayerListS2CPacket.Entry receivedEntry, PlayerListEntry currentEntry, CallbackInfo ci) {
        if (action == PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME) return;
        if (receivedEntry.profile() == null) return;

        if (receivedEntry.profile().getName().startsWith("`")) {
            ci.cancel();
        }
    }

    @Final
    @Shadow
    private Map<UUID, PlayerListEntry> playerListEntries;

    @Unique
    private final Map<UUID, PlayerListEntry> ls$copy_playerListEntries = Maps.newHashMap();

    @Inject(method = "getPlayerListEntry(Ljava/util/UUID;)Lnet/minecraft/client/network/PlayerListEntry;", at = @At("RETURN"), cancellable = true)
    public void getPlayerListEntry(UUID uuid, CallbackInfoReturnable<PlayerListEntry> cir) {
        if (cir.getReturnValue() != null) return;
        cir.setReturnValue(ls$copy_playerListEntries.get(uuid));
    }

    @Inject(method = "onPlayerRemove", at = @At("HEAD"))
    public void onPlayerRemove(PlayerRemoveS2CPacket packet, CallbackInfo ci) {
        for (UUID uUID : packet.profileIds()) {
            PlayerListEntry playerListEntry = playerListEntries.get(uUID);
            if (playerListEntry != null) {
                ls$copy_playerListEntries.put(uUID, playerListEntry);
            }
        }
    }
}
