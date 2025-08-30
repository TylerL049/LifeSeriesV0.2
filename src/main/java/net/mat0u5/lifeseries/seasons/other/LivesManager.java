package net.mat0u5.lifeseries.seasons.other;

import net.mat0u5.lifeseries.seasons.season.doublelife.DoubleLife;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard.superpowers.superpower.Necromancy;
import net.mat0u5.lifeseries.seasons.session.SessionTranscript;
import net.mat0u5.lifeseries.utils.other.TextUtils;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.mat0u5.lifeseries.utils.player.ScoreboardUtils;
import net.mat0u5.lifeseries.utils.player.TeamUtils;
import net.mat0u5.lifeseries.utils.world.AnimationUtils;
import net.mat0u5.lifeseries.utils.world.WorldUitls;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.mat0u5.lifeseries.Main.currentSeason;
import static net.mat0u5.lifeseries.Main.seasonConfig;
import static net.mat0u5.lifeseries.seasons.other.WatcherManager.isWatcher;

public class LivesManager {
    public static final String SCOREBOARD_NAME = "Lives";
    public static boolean FINAL_DEATH_LIGHTNING = true;
    public static SoundEvent FINAL_DEATH_SOUND = SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER;
    public static boolean SHOW_DEATH_TITLE = false;

    public void createTeams() {
        TeamUtils.createTeam("lives_null", "Unassigned", Formatting.GRAY);
        TeamUtils.createTeam("lives_0", "Dead", Formatting.DARK_GRAY);
        TeamUtils.createTeam("lives_1", "Red", Formatting.RED);
        TeamUtils.createTeam("lives_2", "Yellow", Formatting.YELLOW);
        TeamUtils.createTeam("lives_3", "Green", Formatting.GREEN);
        TeamUtils.createTeam("lives_4", "Dark Green", Formatting.DARK_GREEN);
    }

    public void createScoreboards() {
        ScoreboardUtils.createObjective(SCOREBOARD_NAME);
    }

    public Formatting getColorForLives(ServerPlayerEntity player) {
        return getColorForLives(getPlayerLives(player));
    }

    public Formatting getColorForLives(Integer lives) {
        Team team = TeamUtils.getTeam(getTeamForLives(lives));
        if (team != null) {
            Formatting color = team.getColor();
            if (color != null) {
                return color;
            }
        }
        return Formatting.DARK_GRAY;
    }

    public Text getFormattedLives(ServerPlayerEntity player) {
        return getFormattedLives(getPlayerLives(player));
    }

    public Text getFormattedLives(@Nullable Integer lives) {
        if (lives == null) {
            lives = 0;
        }
        Formatting color = getColorForLives(lives);
        return Text.literal(String.valueOf(lives)).formatted(color);
    }
    public String getTeamForPlayer(ServerPlayerEntity player) {
        Integer lives = getPlayerLives(player);
        return getTeamForLives(lives);
    }
    public String getTeamForLives(Integer lives) {
        String prefix = "lives_";
        String nullTeam = prefix+"null";
        if (lives == null) {
            return nullTeam;
        }
        List<Integer> livesTeams = new ArrayList<>();
        Collection<Team> allTeams = TeamUtils.getAllTeams();
        if (allTeams != null) {
            for (Team team : allTeams) {
                String name = team.getName();
                if (name.startsWith(prefix)) {
                    try {
                        int index = Integer.parseInt(name.replaceAll(prefix,""));
                        if (index == lives) {
                            return name;
                        }
                        livesTeams.add(index);
                    }catch(Exception ignored) {}
                }
            }
        }
        if (!livesTeams.isEmpty()) {
            Collections.sort(livesTeams);

            if (lives <= livesTeams.getFirst()) {
                return prefix + livesTeams.getFirst();
            }
            Collections.reverse(livesTeams);
            for (int i : livesTeams) {
                if (lives >= i) {
                    return prefix + i;
                }
            }
        }
        return nullTeam;
    }

    @Nullable
    public Integer getPlayerLives(ServerPlayerEntity player) {
        if (isWatcher(player)) return null;
        return ScoreboardUtils.getScore(player, SCOREBOARD_NAME);
    }

    public boolean hasAssignedLives(ServerPlayerEntity player) {
        Integer lives = getPlayerLives(player);
        return lives != null;
    }

    public boolean isAlive(ServerPlayerEntity player) {
        Integer lives = getPlayerLives(player);
        if (lives == null) return false;
        if (!hasAssignedLives(player)) return false;
        return lives > 0;
    }

    public void removePlayerLife(ServerPlayerEntity player) {
        addToPlayerLives(player,-1);
    }

    public void resetPlayerLife(ServerPlayerEntity player) {
        ScoreboardUtils.resetScore(player, SCOREBOARD_NAME);
        currentSeason.reloadPlayerTeam(player);
        currentSeason.assignDefaultLives(player);
        if (currentSeason instanceof DoubleLife doubleLife) {
            doubleLife.syncSoulboundLives(player);
        }
    }

    public void resetAllPlayerLivesInner() {
        ScoreboardUtils.removeObjective(SCOREBOARD_NAME);
        createScoreboards();
        currentSeason.reloadAllPlayerTeams();
    }

    public void resetAllPlayerLives() {
        resetAllPlayerLivesInner();
        PlayerUtils.getAllPlayers().forEach(currentSeason::assignDefaultLives);
    }

    public void addPlayerLife(ServerPlayerEntity player) {
        addToPlayerLives(player,1);
    }

    public void addToPlayerLives(ServerPlayerEntity player, int amount) {
        Integer currentLives = getPlayerLives(player);
        if (currentLives == null) currentLives = 0;
        int lives = currentLives + amount;
        if (lives < 0 && !Necromancy.isRessurectedPlayer(player)) lives = 0;
        setPlayerLives(player, lives);
    }

    public void addToLifeNoUpdate(ServerPlayerEntity player) {
        if (isWatcher(player)) return;
        Integer currentLives = getPlayerLives(player);
        if (currentLives == null) currentLives = 0;
        int lives = currentLives + 1;
        if (lives < 0) lives = 0;
        ScoreboardUtils.setScore(player, SCOREBOARD_NAME, lives);
    }

    public void receiveLifeFromOtherPlayer(Text playerName, ServerPlayerEntity target) {
        target.playSoundToPlayer(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.MASTER, 10, 1);
        if (seasonConfig.GIVELIFE_BROADCAST.get(seasonConfig)) {
            PlayerUtils.broadcastMessageExcept(TextUtils.format("{} received a life from {}", target, playerName), target);
        }
        target.sendMessage(TextUtils.format("You received a life from {}", playerName));
        PlayerUtils.sendTitleWithSubtitle(target, Text.of("You received a life"), TextUtils.format("from {}", playerName), 10, 60, 10);
        AnimationUtils.createSpiral(target, 175);
        currentSeason.reloadPlayerTeam(target);
        SessionTranscript.givelife(playerName, target);
        if (currentSeason instanceof DoubleLife doubleLife) {
            doubleLife.syncSoulboundLives(target);
        }
    }

    public void setPlayerLives(ServerPlayerEntity player, int lives) {
        if (isWatcher(player)) return;
        Integer livesBefore = getPlayerLives(player);
        ScoreboardUtils.setScore(player, SCOREBOARD_NAME, lives);
        if (lives <= 0) {
            playerLostAllLives(player, livesBefore);
        }
        else if (player.isSpectator()) {
            PlayerUtils.safelyPutIntoSurvival(player);
        }
        currentSeason.reloadPlayerTeam(player);
    }

    @Nullable
    public Boolean isOnLastLife(ServerPlayerEntity player) {
        return isOnSpecificLives(player, 1);
    }

    public boolean isOnLastLife(ServerPlayerEntity player, boolean fallback) {
        Boolean isOnLastLife = isOnLastLife(player);
        if (isOnLastLife == null) return fallback;
        return isOnLastLife;
    }

    @Nullable
    public Boolean isOnSpecificLives(ServerPlayerEntity player, int check) {
        if (!isAlive(player)) return null;
        Integer lives = getPlayerLives(player);
        if (lives == null) return null;
        return lives == check;
    }

    public boolean isOnSpecificLives(ServerPlayerEntity player, int check, boolean fallback) {
        Boolean isOnLife = isOnSpecificLives(player, check);
        if (isOnLife == null) return fallback;
        return isOnLife;
    }

    @Nullable
    public Boolean isOnAtLeastLives(ServerPlayerEntity player, int check) {
        if (!isAlive(player)) return null;
        Integer lives = getPlayerLives(player);
        if (lives == null) return null;
        return lives >= check;
    }

    public boolean isOnAtLeastLives(ServerPlayerEntity player, int check, boolean fallback) {
        Boolean isOnAtLeast = isOnAtLeastLives(player, check);
        if (isOnAtLeast == null) return fallback;
        return isOnAtLeast;
    }


    public void playerLostAllLives(ServerPlayerEntity player, Integer livesBefore) {
        player.changeGameMode(GameMode.SPECTATOR);
        Vec3d pos = player.getPos();
        HashMap<Vec3d, List<Float>> info = new HashMap<>();
        info.put(pos, List.of(player.getYaw(),player.getPitch()));
        currentSeason.respawnPositions.put(player.getUuid(), info);
        currentSeason.dropItemsOnLastDeath(player);
        if (livesBefore != null && livesBefore > 0) {
            if (FINAL_DEATH_SOUND != null) {
                PlayerUtils.playSoundToPlayers(PlayerUtils.getAllPlayers(), FINAL_DEATH_SOUND);
            }
            if (FINAL_DEATH_LIGHTNING) {
                WorldUitls.summonHarmlessLightning(player);
            }
            showDeathTitle(player);
        }
        SessionTranscript.onPlayerLostAllLives(player);
        currentSeason.boogeymanManager.playerLostAllLives(player);
    }

    public void showDeathTitle(ServerPlayerEntity player) {
        if (SHOW_DEATH_TITLE) {
            String subtitle = seasonConfig.FINAL_DEATH_TITLE_SUBTITLE.get(seasonConfig);
            PlayerUtils.sendTitleWithSubtitleToPlayers(PlayerUtils.getAllPlayers(), player.getStyledDisplayName(), Text.literal(subtitle), 20, 80, 20);
        }
        Text deathMessage = getDeathMessage(player);
        if (!deathMessage.getString().isEmpty()) {
            PlayerUtils.broadcastMessage(deathMessage);
        }
    }

    public Text getDeathMessage(ServerPlayerEntity player) {
        String message = seasonConfig.FINAL_DEATH_MESSAGE.get(seasonConfig);
        if (message.contains("${player}")) {
            return TextUtils.format(message.replace("${player}", "{}"), player);
        }
        return Text.literal(message);
    }

    public List<ServerPlayerEntity> getNonRedPlayers() {
        List<ServerPlayerEntity> players = PlayerUtils.getAllFunctioningPlayers();
        players.removeIf(player -> isOnLastLife(player, true));
        return players;
    }

    public List<ServerPlayerEntity> getAlivePlayers() {
        List<ServerPlayerEntity> players = PlayerUtils.getAllFunctioningPlayers();
        players.removeIf(player -> !isAlive(player));
        return players;
    }

    public List<ServerPlayerEntity> getDeadPlayers() {
        List<ServerPlayerEntity> players = PlayerUtils.getAllFunctioningPlayers();
        players.removeIf(this::isAlive);
        return players;
    }

    public boolean anyGreenPlayers() {
        for (ServerPlayerEntity player : PlayerUtils.getAllFunctioningPlayers()) {
            if (isOnSpecificLives(player, 3, false)) return true;
        }
        return false;
    }

    public boolean anyYellowPlayers() {
        for (ServerPlayerEntity player : PlayerUtils.getAllFunctioningPlayers()) {
            if (isOnSpecificLives(player, 2, false)) return true;
        }
        return false;
    }
}
