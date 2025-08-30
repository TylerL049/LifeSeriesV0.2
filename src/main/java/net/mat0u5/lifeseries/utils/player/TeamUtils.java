package net.mat0u5.lifeseries.utils.player;

import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.utils.other.TextUtils;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collection;

import static net.mat0u5.lifeseries.Main.server;

public class TeamUtils {

    public static void createTeam(String teamName, Formatting color) {
        createTeam(teamName, teamName, color);
    }

    public static void createTeam(String teamName, String displayName, Formatting color) {
        if (server == null) return;
        Scoreboard scoreboard = server.getScoreboard();
        if (scoreboard.getTeam(teamName) != null) {
            // A team with this name already exists
            return;
        }
        Team team = scoreboard.addTeam(teamName);
        team.setDisplayName(Text.literal(displayName).formatted(color));
        team.setColor(color);
    }

    public static void addEntityToTeam(String teamName, Entity entity) {
        if (server == null) return;
        Scoreboard scoreboard = server.getScoreboard();
        Team team = scoreboard.getTeam(teamName);

        if (team == null) {
            // A team with this name does not exist
            return;
        }

        scoreboard.addScoreHolderToTeam(entity.getNameForScoreboard(), team);
    }

    public static boolean removePlayerFromTeam(ServerPlayerEntity player) {
        if (server == null) return false;
        Scoreboard scoreboard = server.getScoreboard();
        String playerName = player.getNameForScoreboard();

        Team team = scoreboard.getScoreHolderTeam(playerName);
        if (team == null) {
            Main.LOGGER.warn(TextUtils.formatString("Player {} is not part of any team!", playerName));
            return false;
        }

        scoreboard.removeScoreHolderFromTeam(playerName, team);
        return true;
    }

    public static boolean deleteTeam(String teamName) {
        if (server == null) return false;
        Scoreboard scoreboard = server.getScoreboard();
        Team team = scoreboard.getTeam(teamName);

        if (team == null) {
            return false;
        }

        scoreboard.removeTeam(team);
        return true;
    }

    public static Team getTeam(String teamName) {
        if (server == null) return null;
        Scoreboard scoreboard = server.getScoreboard();
        return scoreboard.getTeam(teamName);
    }

    public static Team getPlayerTeam(ServerPlayerEntity player) {
        if (server == null) return null;
        Scoreboard scoreboard = server.getScoreboard();
        return scoreboard.getScoreHolderTeam(player.getNameForScoreboard());
    }

    public static Collection<Team> getAllTeams() {
        if (server == null) return null;
        Scoreboard scoreboard = server.getScoreboard();
        return scoreboard.getTeams();
    }
}
