package net.mat0u5.lifeseries.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.mat0u5.lifeseries.network.NetworkHandlerServer;
import net.mat0u5.lifeseries.seasons.season.Seasons;
import net.mat0u5.lifeseries.seasons.session.SessionTranscript;
import net.mat0u5.lifeseries.utils.enums.PacketNames;
import net.mat0u5.lifeseries.utils.other.OtherUtils;
import net.mat0u5.lifeseries.utils.other.TextUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

import static net.mat0u5.lifeseries.Main.currentSeason;
import static net.mat0u5.lifeseries.Main.currentSession;
import static net.mat0u5.lifeseries.utils.player.PermissionManager.isAdmin;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SessionCommand {
    public static final String INVALID_TIME_FORMAT_ERROR = "Invalid time format. Use h, m, s for hours, minutes, and seconds.";

    public static boolean isAllowed() {
        return currentSeason.getSeason() != Seasons.UNASSIGNED;
    }

    public static boolean checkBanned(ServerCommandSource source) {
        if (isAllowed()) return false;
        source.sendError(Text.of("This command is only available when you have selected a Season."));
        return true;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess commandRegistryAccess,
                                CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(
            literal("session")
                .then(literal("start")
                    .requires(source -> (isAdmin(source.getPlayer()) || (source.getEntity() == null)))
                    .executes(context -> SessionCommand.startSession(
                        context.getSource()
                    ))
                )
                .then(literal("stop")
                    .requires(source -> (isAdmin(source.getPlayer()) || (source.getEntity() == null)))
                    .executes(context -> SessionCommand.stopSession(
                        context.getSource()
                    ))
                )
                .then(literal("pause")
                    .requires(source -> (isAdmin(source.getPlayer()) || (source.getEntity() == null)))
                    .executes(context -> SessionCommand.pauseSession(
                        context.getSource()
                    ))
                )
                .then(literal("timer")
                    .then(literal("set")
                        .requires(source -> (isAdmin(source.getPlayer()) || (source.getEntity() == null)))
                        .then(argument("time", StringArgumentType.greedyString())
                            .suggests((context, builder) -> CommandSource.suggestMatching(List.of("1h","1h30m","2h"), builder))
                            .executes(context -> SessionCommand.setTime(
                                context.getSource(), StringArgumentType.getString(context, "time")
                            ))
                        )
                    )
                    .then(literal("add")
                        .requires(source -> (isAdmin(source.getPlayer()) || (source.getEntity() == null)))
                        .then(argument("time", StringArgumentType.greedyString())
                            .suggests((context, builder) -> CommandSource.suggestMatching(List.of("30m", "1h"), builder))
                            .executes(context -> SessionCommand.addTime(
                                context.getSource(), StringArgumentType.getString(context, "time")
                            ))
                        )
                    )
                    .then(literal("fastforward")
                        .requires(source -> (isAdmin(source.getPlayer()) || (source.getEntity() == null)))
                        .then(argument("time", StringArgumentType.greedyString())
                            .suggests((context, builder) -> CommandSource.suggestMatching(List.of("5m"), builder))
                            .executes(context -> SessionCommand.skipTime(
                                context.getSource(), StringArgumentType.getString(context, "time")
                            ))
                        )
                    )
                    .then(literal("remove")
                        .requires(source -> (isAdmin(source.getPlayer()) || (source.getEntity() == null)))
                            .then(argument("time", StringArgumentType.greedyString())
                                    .suggests((context, builder) -> CommandSource.suggestMatching(List.of("5m"), builder))
                                    .executes(context -> SessionCommand.removeTime(
                                            context.getSource(), StringArgumentType.getString(context, "time")
                                    ))
                            )
                    )
                    .then(literal("remaining")
                        .executes(context -> SessionCommand.getTime(
                            context.getSource()
                        ))
                    )
                    .then(literal("showDisplay")
                        .executes(context -> SessionCommand.displayTimer(
                            context.getSource()
                        ))
                    )
                )

        );
    }

    public static int getTime(ServerCommandSource source) {
        if (checkBanned(source)) return -1;

        if (!currentSession.validTime()) {
            source.sendError(Text.of("The session time has not been set yet"));
            return -1;
        }
        OtherUtils.sendCommandFeedbackQuiet(source, TextUtils.format("The session ends in {}",currentSession.getRemainingTime()));
        return 1;
    }

    public static int displayTimer(ServerCommandSource source) {
        if (checkBanned(source)) return -1;
        final ServerPlayerEntity self = source.getPlayer();

        if (self == null) return -1;
        if (NetworkHandlerServer.wasHandshakeSuccessful(self)) {
            NetworkHandlerServer.sendStringPacket(self, PacketNames.TOGGLE_TIMER, "");
        }

        boolean isInDisplayTimer = currentSession.isInDisplayTimer(self);
        if (isInDisplayTimer) currentSession.removeFromDisplayTimer(self);
        else currentSession.addToDisplayTimer(self);
        return 1;
    }

    public static int startSession(ServerCommandSource source) {
        if (checkBanned(source)) return -1;

        if (!currentSession.validTime()) {
            source.sendError(Text.of("The session time is not set! Use '/session timer set <time>' to set the session time."));
            return -1;
        }
        if (currentSession.statusStarted()) {
            source.sendError(Text.of("The session has already started"));
            return -1;
        }
        if (currentSession.statusPaused()) {
            OtherUtils.sendCommandFeedback(source, Text.of("§7Unpausing session..."));
            currentSession.sessionPause();
            return 1;
        }

        OtherUtils.sendCommandFeedback(source, Text.of("Starting session..."));
        if (!currentSession.sessionStart()) {
            source.sendError(Text.of("Could not start session"));
            return -1;
        }

        return 1;
    }

    public static int stopSession(ServerCommandSource source) {
        if (checkBanned(source)) return -1;

        if (currentSession.statusNotStarted() || currentSession.statusFinished()) {
            source.sendError(Text.of("The session has not yet started"));
            return -1;
        }

        OtherUtils.sendCommandFeedback(source, Text.of("§7Stopping session..."));
        currentSession.sessionEnd();
        return 1;
    }

    public static int pauseSession(ServerCommandSource source) {
        if (checkBanned(source)) return -1;

        if (currentSession.statusNotStarted() || currentSession.statusFinished()) {
            source.sendError(Text.of("The session has not yet started"));
            return -1;
        }

        if (currentSession.statusPaused()) {
            OtherUtils.sendCommandFeedback(source, Text.of("§7Unpausing session..."));
        }
        else {
            OtherUtils.sendCommandFeedback(source, Text.of("§7Pausing session..."));
        }
        currentSession.sessionPause();

        return 1;
    }

    public static int skipTime(ServerCommandSource source, String timeArgument) {
        if (checkBanned(source)) return -1;

        Integer totalTicks = OtherUtils.parseTimeFromArgument(timeArgument);
        if (totalTicks == null) {
            source.sendError(Text.literal(INVALID_TIME_FORMAT_ERROR));
            return -1;
        }
        OtherUtils.sendCommandFeedback(source, TextUtils.format("Skipped {} in the session length", OtherUtils.formatTime(totalTicks)));
        currentSession.passedTime+=totalTicks;
        return 1;
    }

    public static int setTime(ServerCommandSource source, String timeArgument) {
        if (checkBanned(source)) return -1;

        Integer totalTicks = OtherUtils.parseTimeFromArgument(timeArgument);
        if (totalTicks == null) {
            source.sendError(Text.literal(INVALID_TIME_FORMAT_ERROR));
            return -1;
        }
        currentSession.setSessionLength(totalTicks);

        OtherUtils.sendCommandFeedback(source, TextUtils.format("The session length has been set to {}", OtherUtils.formatTime(totalTicks)));
        return 1;
    }

    public static int addTime(ServerCommandSource source, String timeArgument) {
        if (checkBanned(source)) return -1;

        Integer totalTicks = OtherUtils.parseTimeFromArgument(timeArgument);
        if (totalTicks == null) {
            source.sendError(Text.literal(INVALID_TIME_FORMAT_ERROR));
            return -1;
        }
        currentSession.addSessionLength(totalTicks);

        OtherUtils.sendCommandFeedback(source, TextUtils.format("Added {} to the session length", OtherUtils.formatTime(totalTicks)));
        return 1;
    }

    public static int removeTime(ServerCommandSource source, String timeArgument) {
        if (checkBanned(source)) return -1;

        Integer totalTicks = OtherUtils.parseTimeFromArgument(timeArgument);
        if (totalTicks == null) {
            source.sendError(Text.literal(INVALID_TIME_FORMAT_ERROR));
            return -1;
        }
        currentSession.removeSessionLength(totalTicks);

        OtherUtils.sendCommandFeedback(source, TextUtils.format("Removed {} from the session length", OtherUtils.formatTime(totalTicks)));
        return 1;
    }
}

