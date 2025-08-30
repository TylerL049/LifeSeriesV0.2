package net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard.superpowers.superpower;

import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard.TimeDilation;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard.superpowers.Superpower;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard.superpowers.Superpowers;
import net.mat0u5.lifeseries.utils.other.TaskScheduler;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class TimeControl extends Superpower {
    public static int changedSpeedFor = 0;
    public TimeControl(ServerPlayerEntity player) {
        super(player);
    }

    @Override
    public Superpowers getSuperpower() {
        return Superpowers.TIME_CONTROL;
    }

    @Override
    public int getCooldownMillis() {
        return 300000;
    }

    @Override
    public void activate() {
        super.activate();
        float previousSpeed = TimeDilation.getWorldSpeed();
        if (previousSpeed <= 4) return;
        changedSpeedFor += 90;
        TimeDilation.slowlySetWorldSpeed(4, 20);
        PlayerUtils.playSoundToPlayers(PlayerUtils.getAllPlayers(), SoundEvent.of(Identifier.ofVanilla("wildlife_time_slow_down")));
        TaskScheduler.scheduleTask(70, () -> {
            PlayerUtils.playSoundToPlayers(PlayerUtils.getAllPlayers(), SoundEvent.of(Identifier.ofVanilla("wildlife_time_speed_up")), 0.65f, 1);
            TimeDilation.slowlySetWorldSpeed(previousSpeed, 20);
        });
    }
}