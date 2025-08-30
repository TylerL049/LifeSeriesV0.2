package net.mat0u5.lifeseries.seasons.season.limitedlife;

import net.mat0u5.lifeseries.seasons.boogeyman.Boogeyman;
import net.mat0u5.lifeseries.seasons.boogeyman.BoogeymanManager;
import net.mat0u5.lifeseries.seasons.other.LivesManager;
import net.mat0u5.lifeseries.utils.other.TextUtils;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.mat0u5.lifeseries.utils.player.ScoreboardUtils;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

import static net.mat0u5.lifeseries.Main.*;

public class LimitedLifeBoogeymanManager extends BoogeymanManager {
    @Override
    public void sessionEnd() {
        if (!BOOGEYMAN_ENABLED) return;
        if (server == null) return;
        for (Boogeyman boogeyman : boogeymen) {
            if (boogeyman.died) continue;

            if (!boogeyman.cured) {
                ServerPlayerEntity player = PlayerUtils.getPlayer(boogeyman.uuid);
                if (player == null) {
                    Integer currentLives = ScoreboardUtils.getScore(ScoreHolder.fromName(boogeyman.name), LivesManager.SCOREBOARD_NAME);
                    if (currentLives == null) continue;
                    if (currentLives <= LimitedLifeLivesManager.RED_TIME) continue;

                    if (BOOGEYMAN_ANNOUNCE_OUTCOME) {
                        PlayerUtils.broadcastMessage(TextUtils.format("{}§7 failed to kill a player while being the §cBoogeyman§7. They have been dropped to their §cLast Life§7.", boogeyman.name));
                    }
                    if (currentLives > LimitedLifeLivesManager.RED_TIME && currentLives <= LimitedLifeLivesManager.YELLOW_TIME) {
                        ScoreboardUtils.setScore(ScoreHolder.fromName(boogeyman.name), LivesManager.SCOREBOARD_NAME, LimitedLifeLivesManager.RED_TIME);
                    }
                    if (currentLives > LimitedLifeLivesManager.YELLOW_TIME) {
                        ScoreboardUtils.setScore(ScoreHolder.fromName(boogeyman.name), LivesManager.SCOREBOARD_NAME, LimitedLifeLivesManager.YELLOW_TIME);
                    }
                    continue;
                }
                playerFailBoogeyman(player);
            }
        }
    }
    @Override
    public void playerFailBoogeyman(ServerPlayerEntity player) {
        if (!livesManager.isAlive(player)) return;
        if (livesManager.isOnLastLife(player, true)) return;
        if (livesManager.isOnSpecificLives(player, 3, false)) {
            livesManager.setPlayerLives(player, LimitedLifeLivesManager.YELLOW_TIME);
        }
        else if (livesManager.isOnSpecificLives(player, 2, false)) {
            livesManager.setPlayerLives(player, LimitedLifeLivesManager.RED_TIME);
        }
        Text setTo = livesManager.getFormattedLives(player);

        PlayerUtils.sendTitle(player,Text.of("§cYou have failed."), 20, 30, 20);
        PlayerUtils.playSoundToPlayer(player, SoundEvent.of(Identifier.of("minecraft","lastlife_boogeyman_fail")));
        if (BOOGEYMAN_ANNOUNCE_OUTCOME) {
            PlayerUtils.broadcastMessage(TextUtils.format("{}§7 failed to kill a player while being the §cBoogeyman§7. Their time has been dropped to {}", player, setTo));
        }
    }
}
