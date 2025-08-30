package net.mat0u5.lifeseries.seasons.season.lastlife;

import net.mat0u5.lifeseries.seasons.other.LivesManager;
import net.mat0u5.lifeseries.seasons.other.WatcherManager;
import net.mat0u5.lifeseries.seasons.session.SessionAction;
import net.mat0u5.lifeseries.seasons.session.SessionTranscript;
import net.mat0u5.lifeseries.utils.other.OtherUtils;
import net.mat0u5.lifeseries.utils.other.TaskScheduler;
import net.mat0u5.lifeseries.utils.other.TextUtils;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

import static net.mat0u5.lifeseries.Main.currentSeason;
import static net.mat0u5.lifeseries.Main.livesManager;

public class LastLifeLivesManager extends LivesManager {
    public SessionAction actionChooseLives = new SessionAction(
            OtherUtils.minutesToTicks(1),"§7Assign lives if necessary §f[00:01:00]", "Assign lives if necessary"
    ) {
        @Override
        public void trigger() {
            assignRandomLivesToUnassignedPlayers();
        }
    };
    Random rnd = new Random();

    public void assignRandomLivesToUnassignedPlayers() {
        List<ServerPlayerEntity> assignTo = new ArrayList<>();
        for (ServerPlayerEntity player : PlayerUtils.getAllFunctioningPlayers()) {
            if (livesManager.hasAssignedLives(player)) continue;
            assignTo.add(player);
        }
        if (assignTo.isEmpty()) return;
        assignRandomLives(assignTo);
    }

    public void assignRandomLives(Collection<ServerPlayerEntity> players) {
        HashMap<ServerPlayerEntity, Integer> lives = new HashMap<>();
        for (ServerPlayerEntity player : players) {
            if (WatcherManager.isWatcher(player)) continue;
            if (lives.containsKey(player)) continue;
            lives.put(player,-1);
        }
        PlayerUtils.sendTitleToPlayers(players, Text.literal("You will have...").formatted(Formatting.GRAY), 10, 40, 10);
        int delay = 60;
        TaskScheduler.scheduleTask(delay, ()-> lifeRoll( 0, -1, lives));
    }

    public void lifeRoll(int currentStep, int lastNum,  Map<ServerPlayerEntity, Integer> lives) {
        //TODO refactor
        int delay = 1;
        if (currentStep >= 30) delay = 2;
        if (currentStep >= 50) delay = 4;
        if (currentStep >= 65) delay = 8;
        if (currentStep >= 75) delay = 20;
        if (currentStep == 80) {
            //Choose the amount of lives a player will have

            int totalSize = lives.size();
            int chosenNotRandomly = LastLife.ROLL_MIN_LIVES;
            for (ServerPlayerEntity player : lives.keySet()) {
                Integer currentLives = livesManager.getPlayerLives(player);
                if (currentLives != null) {
                    lives.put(player, currentLives);
                    continue;
                }
                int diff = LastLife.ROLL_MAX_LIVES-LastLife.ROLL_MIN_LIVES+2;
                if (chosenNotRandomly <= LastLife.ROLL_MAX_LIVES && totalSize > diff) {
                    lives.put(player, chosenNotRandomly);
                    chosenNotRandomly++;
                    continue;
                }

                int minLives = LastLife.ROLL_MIN_LIVES;
                int maxLives = LastLife.ROLL_MAX_LIVES;
                int randomLives = rnd.nextInt(minLives, maxLives+1);

                lives.put(player, randomLives);
            }


            //Show the actual amount of lives for one cycle
            for (Map.Entry<ServerPlayerEntity, Integer> playerEntry : lives.entrySet()) {
                Integer livesNum = playerEntry.getValue();
                ServerPlayerEntity player = playerEntry.getKey();
                Text textLives = livesManager.getFormattedLives(livesNum);
                PlayerUtils.sendTitle(player, textLives, 0, 25, 0);
            }
            PlayerUtils.playSoundToPlayers(lives.keySet(), SoundEvents.UI_BUTTON_CLICK.value());
            TaskScheduler.scheduleTask(delay, ()-> lifeRoll( currentStep+1, -1, lives));
            return;
        }
        if (currentStep == 81) {
            //Show "x lives." screen
            for (Map.Entry<ServerPlayerEntity, Integer> playerEntry : lives.entrySet()) {
                Integer livesNum = playerEntry.getValue();
                ServerPlayerEntity player = playerEntry.getKey();
                Text textLives = TextUtils.format("{}§a lives.", livesManager.getFormattedLives(livesNum));
                PlayerUtils.sendTitle(player, textLives, 0, 60, 20);
                if (livesManager.hasAssignedLives(player)) continue;
                SessionTranscript.assignRandomLives(player, livesNum);
                livesManager.setPlayerLives(player, livesNum);
            }
            PlayerUtils.playSoundToPlayers(lives.keySet(), SoundEvents.BLOCK_END_PORTAL_SPAWN);
            currentSeason. reloadAllPlayerTeams();
            return;
        }
        int minLives = LastLife.ROLL_MIN_LIVES;
        int maxLives = LastLife.ROLL_MAX_LIVES;
        int displayLives = rnd.nextInt(minLives, maxLives+1);
        while (displayLives == lastNum) {
            // Just so that the random cycle can't have two of the same number in a row
            displayLives = rnd.nextInt(minLives, maxLives+1);
        }
        int finalDisplayLives = displayLives;
        PlayerUtils.sendTitleToPlayers(lives.keySet(), livesManager.getFormattedLives(finalDisplayLives), 0, 25, 0);
        PlayerUtils.playSoundToPlayers(lives.keySet(), SoundEvents.UI_BUTTON_CLICK.value());
        TaskScheduler.scheduleTask(delay, ()-> lifeRoll( currentStep+1, finalDisplayLives, lives));
    }
}
