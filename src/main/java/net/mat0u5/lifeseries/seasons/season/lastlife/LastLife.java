package net.mat0u5.lifeseries.seasons.season.lastlife;

import net.mat0u5.lifeseries.config.ConfigManager;
import net.mat0u5.lifeseries.seasons.boogeyman.Boogeyman;
import net.mat0u5.lifeseries.seasons.boogeyman.BoogeymanManager;
import net.mat0u5.lifeseries.seasons.other.LivesManager;
import net.mat0u5.lifeseries.seasons.season.Season;
import net.mat0u5.lifeseries.seasons.season.Seasons;
import net.mat0u5.lifeseries.seasons.season.limitedlife.LimitedLifeLivesManager;
import net.mat0u5.lifeseries.utils.other.OtherUtils;
import net.mat0u5.lifeseries.utils.player.PermissionManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

import static net.mat0u5.lifeseries.Main.currentSession;
import static net.mat0u5.lifeseries.Main.seasonConfig;

public class LastLife extends Season {
    public static final String COMMANDS_ADMIN_TEXT = "/lifeseries, /session, /claimkill, /lives, /givelife, /boogeyman, /lastlife";
    public static final String COMMANDS_TEXT = "/claimkill, /lives, /givelife";
    public static int ROLL_MAX_LIVES = 6;
    public static int ROLL_MIN_LIVES = 2;

    @Override
    public Seasons getSeason() {
        return Seasons.LAST_LIFE;
    }

    @Override
    public ConfigManager getConfig() {
        return new LastLifeConfig();
    }

    @Override
    public LivesManager createLivesManager() {
        return new LastLifeLivesManager();
    }

    @Override
    public String getAdminCommands() {
        return COMMANDS_ADMIN_TEXT;
    }

    @Override
    public String getNonAdminCommands() {
        return COMMANDS_TEXT;
    }

    @Override
    public boolean sessionStart() {
        super.sessionStart();
        if (livesManager instanceof LastLifeLivesManager lastLifeLivesManager) {
            currentSession.activeActions.add(
                    lastLifeLivesManager.actionChooseLives
            );
        }
        return true;
    }

    @Override
    public void reload() {
        super.reload();
        if (!(seasonConfig instanceof LastLifeConfig config)) return;
        ROLL_MIN_LIVES = config.RANDOM_LIVES_MIN.get(config);
        ROLL_MAX_LIVES = config.RANDOM_LIVES_MAX.get(config);
    }

    @Override
    public Integer getDefaultLives() {
        return null;
    }
}
