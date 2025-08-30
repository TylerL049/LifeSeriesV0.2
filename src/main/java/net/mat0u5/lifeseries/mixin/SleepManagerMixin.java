package net.mat0u5.lifeseries.mixin;

import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.seasons.season.Seasons;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard.superpowers.Superpowers;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard.superpowers.SuperpowersWildcard;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.SleepManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static net.mat0u5.lifeseries.Main.currentSeason;

@Mixin(value = SleepManager.class, priority = 1)
public abstract class SleepManagerMixin {
    @Inject(method = "canResetTime", at = @At("RETURN"), cancellable = true)
    public void canResetTime(int percentage, List<ServerPlayerEntity> players, CallbackInfoReturnable<Boolean> cir) {
        if (!Main.isLogicalSide()) return;
        if (currentSeason.getSeason() != Seasons.WILD_LIFE) return;
        for (ServerPlayerEntity player : players) {
            if (!player.canResetTimeBySleeping()) return;
            if (SuperpowersWildcard.hasActivePower(player, Superpowers.TIME_CONTROL)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "canSkipNight", at = @At("RETURN"), cancellable = true)
    public void canSkipNight(int percentage, CallbackInfoReturnable<Boolean> cir) {
        if (!Main.isLogicalSide()) return;
        if (currentSeason.getSeason() != Seasons.WILD_LIFE) return;
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            if (!player.isSleeping()) return;
            if (SuperpowersWildcard.hasActivePower(player, Superpowers.TIME_CONTROL)) {
                cir.setReturnValue(true);
            }
        }
    }
}
