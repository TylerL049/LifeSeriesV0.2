package net.mat0u5.lifeseries.command;

import com.mojang.brigadier.CommandDispatcher;
import net.mat0u5.lifeseries.seasons.other.WatcherManager;
import net.mat0u5.lifeseries.utils.other.OtherUtils;
import net.mat0u5.lifeseries.utils.other.TextUtils;
import net.mat0u5.lifeseries.utils.player.ScoreboardUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import java.util.Collection;

import static net.mat0u5.lifeseries.utils.player.PermissionManager.isAdmin;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class WatcherCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess commandRegistryAccess,
                                CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(
            literal("watcher")
                .requires(source -> (isAdmin(source.getPlayer()) || (source.getEntity() == null)))
                .executes(context -> info(context.getSource()))
                .then(literal("info")
                    .executes(context -> info(context.getSource()))
                )
                .then(literal("list")
                        .executes(context -> listWatchers(context.getSource()))
                )
                .then(literal("add")
                    .then(argument("player", EntityArgumentType.players())
                        .executes(context -> addWatchers(
                            context.getSource(), EntityArgumentType.getPlayers(context, "player"))
                        )
                    )
                )
                .then(literal("remove")
                    .then(argument("player", EntityArgumentType.players())
                        .executes(context -> removeWatchers(
                            context.getSource(), EntityArgumentType.getPlayers(context, "player"))
                        )
                    )
                )
        );
    }

    private static int info(ServerCommandSource source) {
        OtherUtils.sendCommandFeedbackQuiet(source, Text.of("§7Watchers are players that are online, but are not affected by most season mechanics. They can only observe."));
        OtherUtils.sendCommandFeedbackQuiet(source, Text.of("§7This is very useful for spectators and for admins."));
        OtherUtils.sendCommandFeedbackQuiet(source, Text.of("§8§oNOTE: This is an experimental feature, report any bugs you find!"));
        return 1;
    }

    private static int listWatchers(ServerCommandSource source) {
        if (WatcherManager.getWatchers().isEmpty()) {
            source.sendError(Text.of("There are no Watchers right now"));
            return -1;
        }
        OtherUtils.sendCommandFeedbackQuiet(source, TextUtils.formatLoosely("Current Watchers: §7{}", WatcherManager.getWatchers()));
        return 1;
    }

    private static int addWatchers(ServerCommandSource source, Collection<ServerPlayerEntity> targets) {
        if (targets == null || targets.isEmpty()) return -1;

        targets.forEach(WatcherManager::addWatcher);
        WatcherManager.reloadWatchers();

        if (targets.size() == 1) {
            OtherUtils.sendCommandFeedback(source, TextUtils.format("{} is now a Watcher", targets.iterator().next()));
        }
        else {
            OtherUtils.sendCommandFeedback(source, TextUtils.format("{} targets are now Watchers", targets.size()));
        }

        return 1;
    }

    private static int removeWatchers(ServerCommandSource source, Collection<ServerPlayerEntity> targets) {
        if (targets == null || targets.isEmpty()) return -1;

        targets.forEach(WatcherManager::removeWatcher);
        WatcherManager.reloadWatchers();

        if (targets.size() == 1) {
            OtherUtils.sendCommandFeedback(source, TextUtils.format("{} is no longer a Watcher", targets.iterator().next()));
        }
        else {
            OtherUtils.sendCommandFeedback(source, TextUtils.format("{} targets are no longer Watchers", targets.size()));
        }

        return 1;
    }
}
