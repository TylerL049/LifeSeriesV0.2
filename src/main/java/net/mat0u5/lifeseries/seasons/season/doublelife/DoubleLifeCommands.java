package net.mat0u5.lifeseries.seasons.season.doublelife;

import com.mojang.brigadier.CommandDispatcher;
import net.mat0u5.lifeseries.seasons.season.Seasons;
import net.mat0u5.lifeseries.utils.other.OtherUtils;
import net.mat0u5.lifeseries.utils.other.TextUtils;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.*;

import static net.mat0u5.lifeseries.Main.currentSeason;
import static net.mat0u5.lifeseries.utils.player.PermissionManager.isAdmin;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DoubleLifeCommands {

    public static boolean isAllowed() {
        return currentSeason.getSeason() == Seasons.DOUBLE_LIFE;
    }

    public static boolean checkBanned(ServerCommandSource source) {
        if (isAllowed()) return false;
        source.sendError(Text.of("This command is only available when playing Double Life."));
        return true;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess commandRegistryAccess,
                                CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(
            literal("soulmate")
                .requires(source -> isAllowed() && (isAdmin(source.getPlayer()) || (source.getEntity() == null)))
                .then(literal("get")
                    .then(argument("player", EntityArgumentType.player())
                        .executes(context -> getSoulmate(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                    )
                )
                .then(literal("set")
                    .then(argument("player", EntityArgumentType.player())
                        .then(argument("soulmate", EntityArgumentType.player())
                            .executes(context -> setSoulmate(
                                context.getSource(),
                                EntityArgumentType.getPlayer(context, "player"),
                                EntityArgumentType.getPlayer(context, "soulmate")
                            ))
                        )
                    )
                )
                .then(literal("list")
                    .executes(context -> listSoulmates(context.getSource()))
                )
                .then(literal("reset")
                    .then(argument("player", EntityArgumentType.players())
                        .executes(context -> resetSoulmate(context.getSource(), EntityArgumentType.getPlayers(context, "player")))
                    )
                )
                .then(literal("resetAll")
                    .executes(context -> resetAllSoulmates(context.getSource()))
                )
                .then(literal("rollRandom")
                    .executes(context -> rollSoulmates(context.getSource()))
                )
        );
    }

    public static int setSoulmate(ServerCommandSource source, ServerPlayerEntity player, ServerPlayerEntity soulmate) {
        if (checkBanned(source)) return -1;
        if (player == null) return -1;

        DoubleLife season = ((DoubleLife) currentSeason);

        if (season.hasSoulmate(player)) {
            source.sendError(TextUtils.formatPlain("{} already has a soulmate", player));
            return -1;
        }

        if (season.hasSoulmate(soulmate)) {
            source.sendError(TextUtils.formatPlain("{} already has a soulmate", player));
            return -1;
        }

        season.setSoulmate(player,soulmate);
        season.saveSoulmates();

        OtherUtils.sendCommandFeedback(source, TextUtils.format("{}'s soulmate is now {}", player, soulmate));
        return 1;
    }

    public static int getSoulmate(ServerCommandSource source, ServerPlayerEntity player) {
        if (checkBanned(source)) return -1;
        if (player == null) return -1;

        DoubleLife season = ((DoubleLife) currentSeason);

        if (!season.hasSoulmate(player)) {
            source.sendError(TextUtils.formatPlain("{} does not have a soulmate", player));
            return -1;
        }
        if (!season.isSoulmateOnline(player)) {
            source.sendError(TextUtils.formatPlain("{} 's soulmate is not online right now", player));
            return -1;
        }

        ServerPlayerEntity soulmate = season.getSoulmate(player);
        if (soulmate == null) return -1;

        OtherUtils.sendCommandFeedback(source, TextUtils.format("{}'s soulmate is {}", player, soulmate));
        return 1;
    }

    public static int resetSoulmate(ServerCommandSource source, Collection<ServerPlayerEntity> players) {
        if (checkBanned(source)) return -1;
        if (players == null) return -1;
        if (players.isEmpty()) return -1;

        DoubleLife season = ((DoubleLife) currentSeason);

        List<ServerPlayerEntity> affected = new ArrayList<>();
        for (ServerPlayerEntity player : players) {
            if (season.hasSoulmate(player)) {
                season.resetSoulmate(player);
                season.saveSoulmates();
                affected.add(player);
            }
        }

        if (affected.isEmpty()) {
            source.sendError(Text.of("No target was found"));
            return -1;
        }
        if (affected.size() == 1) {
            OtherUtils.sendCommandFeedback(source, TextUtils.format("{}'s soulmate was reset", affected.getFirst()));
            return 1;
        }
        OtherUtils.sendCommandFeedback(source, TextUtils.format("Soulmate was reset for {} targets", affected.size()));
        return 1;
    }

    public static int resetAllSoulmates(ServerCommandSource source) {
        if (checkBanned(source)) return -1;

        DoubleLife season = ((DoubleLife) currentSeason);

        season.resetAllSoulmates();

        OtherUtils.sendCommandFeedback(source, Text.of("All soulmate entries were reset"));
        return 1;
    }

    public static int listSoulmates(ServerCommandSource source) {
        if (checkBanned(source)) return -1;

        DoubleLife season = ((DoubleLife) currentSeason);

        boolean noSoulmates = true;
        for (Map.Entry<UUID, UUID> entry : season.soulmatesOrdered.entrySet()) {
            noSoulmates = false;
            Object text1 = entry.getKey();
            Object text2 = entry.getValue();
            ServerPlayerEntity player = PlayerUtils.getPlayer(entry.getKey());
            ServerPlayerEntity soulmate = PlayerUtils.getPlayer(entry.getValue());
            if (player != null) text1 = player;
            if (soulmate != null) text2 = soulmate;

            OtherUtils.sendCommandFeedbackQuiet(source, TextUtils.format("{}'s soulmate is {}", text1, text2));
        }

        if (noSoulmates) {
            source.sendError(Text.of("There are no soulmates currently assigned"));
        }
        return 1;
    }

    public static int rollSoulmates(ServerCommandSource source) {
        if (checkBanned(source)) return -1;
        DoubleLife season = ((DoubleLife) currentSeason);
        OtherUtils.sendCommandFeedback(source, Text.of("§7Rolling soulmates..."));
        season.rollSoulmates();
        return 1;
    }
}
