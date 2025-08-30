package net.mat0u5.lifeseries.seasons.session;

import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.seasons.season.secretlife.Task;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard.superpowers.Superpowers;
import net.mat0u5.lifeseries.utils.other.OtherUtils;
import net.mat0u5.lifeseries.utils.other.TextUtils;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

import static net.mat0u5.lifeseries.Main.currentSeason;
import static net.mat0u5.lifeseries.Main.currentSession;

public class SessionTranscript {
    public static final List<String> messages = new ArrayList<>();

    public static void newSuperpower(ServerPlayerEntity player, Superpowers superpower) {
        addMessageWithTime(TextUtils.formatString("{} has been assigned the {} superpower.", player, superpower.getString()));
    }

    public static void newTriviaBot(ServerPlayerEntity player) {
        addMessageWithTime(TextUtils.formatString("Spawned trivia bot for {}", player));
    }

    public static void endingIsYours() {
        addMessageWithTime("The ending is yours... Make it WILD.");
    }

    public static void newHungerRule() {
        addMessageWithTime("[Wildcard] Food has been randomized.");
    }

    public static void mobSwap() {
        addMessageWithTime("[Wildcard] Mobs have been swapped.");
    }

    public static void deactivateWildcard(Wildcards type) {
        addMessageWithTime(TextUtils.formatString("Deactivated Wildcard: {}", type));
    }

    public static void activateWildcard(Wildcards type) {
        addMessageWithTime(TextUtils.formatString("Activated Wildcard: {}", type));
    }

    public static void logPlayers() {
        List<String> names = new ArrayList<>();
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            names.add(player.getNameForScoreboard());
        }
        addMessageWithTime(TextUtils.formatString("Players online: {}", names));
    }

    public static void rerollTask(ServerPlayerEntity player) {
        addMessageWithTime(TextUtils.formatString("{} has rerolled their task.", player));
    }

    public static void successTask(ServerPlayerEntity player) {
        addMessageWithTime(TextUtils.formatString("{} has passed their task.", player));
    }

    public static void failTask(ServerPlayerEntity player) {
        addMessageWithTime(TextUtils.formatString("{} has failed their task.", player));
    }

    public static void assignTask(ServerPlayerEntity player, Task task, List<String> linesStr) {
        addMessageWithTime(TextUtils.formatString("{} has been given a {} task: {}", player, task.type.name(), String.join(" ", linesStr)));
    }

    public static void claimKill(ServerPlayerEntity killer, ServerPlayerEntity victim) {
        addMessageWithTime(TextUtils.formatString("{}'s kill claim of {} has been accepted.", killer, victim));
    }

    public static void soulmate(ServerPlayerEntity player, ServerPlayerEntity soulmate) {
        addMessageWithTime(TextUtils.formatString("{}'s soulmate has been chosen to be {}", player, soulmate));
    }

    public static void assignRandomLives(ServerPlayerEntity player, int amount) {
        addMessageWithTime(TextUtils.formatString("{} has been randomly assigned {} lives", player, amount));
    }

    public static void givelife(Text playerName, ServerPlayerEntity target) {
        addMessageWithTime("<@","> ",TextUtils.formatString("{} gave a life to {}", playerName, target));
    }

    public static void playerLeave(ServerPlayerEntity player) {
        addMessageWithTime("<@","> ",TextUtils.formatString("{} left the game.", player));
    }

    public static void playerJoin(ServerPlayerEntity player) {
        addMessageWithTime("<@","> ",TextUtils.formatString("{} joined the game.", player));
    }

    public static void triggerSessionAction(String message) {
        if (message == null || message.isEmpty()) return;
        addMessageWithTime("TRIGGERED_SESSION_ACTION: " + message);
    }

    public static void onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        addMessageWithTime("<@","> ",source.getDeathMessage(player).getString());
    }

    public static void onPlayerLostAllLives(ServerPlayerEntity player) {
        addMessageWithTime(TextUtils.formatString("{} lost all lives.", player));
    }

    public static void boogeymenChosen(List<ServerPlayerEntity> players) {
        List<String> names = new ArrayList<>();
        for (ServerPlayerEntity player : players) {
            names.add(player.getNameForScoreboard());
        }
        addMessageWithTime(TextUtils.formatString("Boogeymen chosen: {}", names));
    }

    public static void sessionStart() {
        addMessageWithTime("-----  Session started!  -----");
    }

    public static void sessionEnd() {
        addMessageWithTime("-----  The session has ended!  -----");
    }
    public static void addMessageWithTime(String message) {
        addMessageWithTime("[@","] ", message);
    }

    private static void addMessageWithTime(String start, String end, String message) {
        String time = currentSession.getPassedTime();
        String finalMessage = start+time+end+message;

        if (currentSession.statusNotStarted() || currentSession.statusFinished()) {
            finalMessage = message;
        }

        if (messages.isEmpty()) {
            addDefaultMessages();
        }
        messages.add(finalMessage);
    }

    public static void resetStats() {
        messages.clear();
        addDefaultMessages();
    }

    public static void addDefaultMessages() {
        messages.add(TextUtils.formatString("-----  Life Series Mod by Mat0u5  |  Mod version: {}  -----", Main.MOD_VERSION));
        messages.add(TextUtils.formatString("-----  {}  |  Time and date: {}  -----", currentSeason.getSeason().name(), OtherUtils.getTimeAndDate()));
        messages.add("-----  Session Transcript  -----");
    }

    public static String getStats() {
        return String.join("\n", messages);
    }

    public static void sendTranscriptToAdmins() {
        Text sessionTranscript = getTranscriptMessage();
        PlayerUtils.broadcastMessageToAdmins(sessionTranscript);
    }

    public static Text getTranscriptMessage() {
        return Text.literal("§7Click ").append(
                Text.literal("here")
                        .styled(style -> style
                                .withColor(Formatting.BLUE)
                                .withClickEvent(TextUtils.copyClipboardClickEvent(SessionTranscript.getStats()))
                                .withUnderline(true)
                        )).append(Text.of("§7 to copy the session transcript."));
    }
}
