package net.mat0u5.lifeseries.seasons.season.secretlife;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.mat0u5.lifeseries.seasons.season.Seasons;
import net.mat0u5.lifeseries.utils.other.OtherUtils;
import net.mat0u5.lifeseries.utils.other.TextUtils;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.mat0u5.lifeseries.utils.world.AnimationUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static net.mat0u5.lifeseries.Main.*;
import static net.mat0u5.lifeseries.utils.player.PermissionManager.isAdmin;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SecretLifeCommands {

    public static boolean isAllowed() {
        return currentSeason.getSeason() == Seasons.SECRET_LIFE;
    }

    public static boolean checkBanned(ServerCommandSource source) {
        if (isAllowed()) return false;
        source.sendError(Text.of("This command is only available when playing Secret Life."));
        return true;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess commandRegistryAccess,
                                CommandManager.RegistrationEnvironment registrationEnvironment) {

        dispatcher.register(
            literal("health")
                .requires(source -> isAllowed())
                .executes(context -> showHealth(context.getSource()))
                .then(literal("sync")
                    .requires(source -> (isAdmin(source.getPlayer()) || (source.getEntity() == null)))
                    .executes(context -> syncHealth(
                        context.getSource())
                    )
                )
                .then(literal("add")
                    .requires(source -> (isAdmin(source.getPlayer()) || (source.getEntity() == null)))
                    .then(argument("player", EntityArgumentType.players())
                        .executes(context -> healthManager(
                            context.getSource(), EntityArgumentType.getPlayers(context, "player"), 1, false)
                        )
                        .then(argument("amount", DoubleArgumentType.doubleArg(0))
                            .executes(context -> healthManager(
                                context.getSource(), EntityArgumentType.getPlayers(context, "player"), DoubleArgumentType.getDouble(context, "amount"), false)
                            )
                        )
                    )
                )
                .then(literal("remove")
                    .requires(source -> (isAdmin(source.getPlayer()) || (source.getEntity() == null)))
                    .then(argument("player", EntityArgumentType.players())
                        .executes(context -> healthManager(
                            context.getSource(), EntityArgumentType.getPlayers(context, "player"), -1, false)
                        )
                        .then(argument("amount", DoubleArgumentType.doubleArg(0))
                            .executes(context -> healthManager(
                                context.getSource(), EntityArgumentType.getPlayers(context, "player"), -DoubleArgumentType.getDouble(context, "amount"), false)
                            )
                        )
                    )
                )
                .then(literal("set")
                    .requires(source -> (isAdmin(source.getPlayer()) || (source.getEntity() == null)))
                    .then(argument("player", EntityArgumentType.players())
                        .then(argument("amount", DoubleArgumentType.doubleArg(0))
                            .executes(context -> healthManager(
                                context.getSource(), EntityArgumentType.getPlayers(context, "player"), DoubleArgumentType.getDouble(context, "amount"), true)
                            )
                        )
                    )
                )
                .then(literal("get")
                    .requires(source -> (isAdmin(source.getPlayer()) || (source.getEntity() == null)))
                    .then(argument("player", EntityArgumentType.player())
                        .executes(context -> getHealthFor(
                            context.getSource(), EntityArgumentType.getPlayer(context, "player"))
                        )
                    )
                )
                .then(literal("reset")
                    .requires(source -> (isAdmin(source.getPlayer()) || (source.getEntity() == null)))
                    .then(argument("player", EntityArgumentType.players())
                        .executes(context -> resetHealth(
                            context.getSource(), EntityArgumentType.getPlayers(context, "player"))
                        )
                    )
                )
        );
        dispatcher.register(
            literal("task")
                .requires(source -> isAllowed() && (isAdmin(source.getPlayer()) || (source.getEntity() == null)))
                    .then(literal("succeed")
                            .then(argument("player", EntityArgumentType.players())
                                    .executes(context -> succeedTask(
                                            context.getSource(), EntityArgumentType.getPlayers(context, "player"))
                                    )
                            )
                    )
                    .then(literal("fail")
                            .then(argument("player", EntityArgumentType.players())
                                    .executes(context -> failTask(
                                            context.getSource(), EntityArgumentType.getPlayers(context, "player"))
                                    )
                            )
                    )
                    .then(literal("reroll")
                            .then(argument("player", EntityArgumentType.players())
                                    .executes(context -> rerollTask(
                                            context.getSource(), EntityArgumentType.getPlayers(context, "player"))
                                    )
                            )
                    )
                    .then(literal("assignRandom")
                            .then(argument("player", EntityArgumentType.players())
                                    .executes(context -> assignTask(
                                            context.getSource(), EntityArgumentType.getPlayers(context, "player"))
                                    )
                            )
                    )
                    .then(literal("clearTask")
                            .then(argument("player", EntityArgumentType.players())
                                    .executes(context -> clearTask(
                                            context.getSource(), EntityArgumentType.getPlayers(context, "player"))
                                    )
                            )
                    )
                    .then(literal("set")
                            .then(argument("player", EntityArgumentType.players())
                                    .then(argument("type", StringArgumentType.string())
                                            .suggests((context, builder) -> CommandSource.suggestMatching(List.of("easy","hard","red"), builder))
                                            .then(argument("task", StringArgumentType.greedyString())
                                                    .executes(context -> setTask(
                                                            context.getSource(),
                                                            EntityArgumentType.getPlayers(context, "player"),
                                                            StringArgumentType.getString(context, "type"),
                                                            StringArgumentType.getString(context, "task")
                                                            )
                                                    )
                                            )
                                    )
                            )
                    )
        );
        dispatcher.register(
            literal("gift")
                .requires(source -> isAllowed())
                .then(argument("player", EntityArgumentType.player())
                    .executes(context -> gift(
                        context.getSource(), EntityArgumentType.getPlayer(context, "player"))
                    )
                )
        );
        dispatcher.register(
            literal("secretlife")
                .requires(source -> isAllowed() && (isAdmin(source.getPlayer()) || (source.getEntity() == null)))
                .then(literal("changeLocations")
                    .executes(context -> changeLocations(
                        context.getSource())
                    )
                )
        );
    }

    public static int setTask(ServerCommandSource source, Collection<ServerPlayerEntity> targets, String type, String task) {
        if (checkBanned(source)) return -1;
        if (targets == null || targets.isEmpty()) return -1;

        TaskTypes taskType = TaskTypes.EASY;

        if (type.equalsIgnoreCase("hard")) taskType = TaskTypes.HARD;
        if (type.equalsIgnoreCase("red")) taskType = TaskTypes.RED;

        task = task.replaceAll("\\\\n","\n");

        for (ServerPlayerEntity player : targets) {
            TaskManager.preAssignedTasks.put(player.getUuid(), new Task(task, taskType));

            if (TaskManager.removePlayersTaskBook(player) || TaskManager.tasksChosen) {
                TaskManager.assignRandomTaskToPlayer(player, taskType);
                AnimationUtils.playTotemAnimation(player);
                if (targets.size() == 1) {
                    OtherUtils.sendCommandFeedback(source, TextUtils.format("Changed {}'s task", player));
                }
            }
            else if (targets.size() == 1) {
                OtherUtils.sendCommandFeedback(source, TextUtils.format("Pre-assigned {}'s task for randomization", player));
                OtherUtils.sendCommandFeedbackQuiet(source, Text.of("§7They will be given the task book once you / the game rolls the tasks"));
            }
        }

        if (targets.size() != 1) {
            OtherUtils.sendCommandFeedback(source, TextUtils.format("Changed or pre-assigned task of {} targets", targets.size()));
        }

        return 1;
    }

    public static int changeLocations(ServerCommandSource source) {
        if (checkBanned(source)) return -1;
        OtherUtils.sendCommandFeedback(source, Text.of("Changing Secret Life locations..."));
        TaskManager.deleteLocations();
        TaskManager.checkSecretLifePositions();
        return 1;
    }

    public static int clearTask(ServerCommandSource source, Collection<ServerPlayerEntity> targets) {
        if (checkBanned(source)) return -1;
        List<ServerPlayerEntity> affected = new ArrayList<>();
        for (ServerPlayerEntity player : targets) {
            if (TaskManager.removePlayersTaskBook(player)) {
                affected.add(player);
            }
        }

        if (affected.isEmpty()) {
            source.sendError(Text.of("No task books were found"));
            return -1;
        }
        if (affected.size() == 1) {
            OtherUtils.sendCommandFeedback(source, TextUtils.format("Removed task book from {}", affected.getFirst()));
        }
        else {
            OtherUtils.sendCommandFeedback(source, TextUtils.format("Removed task book from {} targets", affected.size()));
        }
        return 1;
    }

    public static int assignTask(ServerCommandSource source, Collection<ServerPlayerEntity> targets) {
        if (checkBanned(source)) return -1;

        if (targets.size() == 1) {
            OtherUtils.sendCommandFeedback(source, TextUtils.format("Assigning random task to {}", targets.iterator().next()));
        }
        else {
            OtherUtils.sendCommandFeedback(source, TextUtils.format("Assigning random tasks to {} targets", targets.size()));
        }

        TaskManager.chooseTasks(targets.stream().toList(), null);

        return 1;
    }

    public static int succeedTask(ServerCommandSource source, Collection<ServerPlayerEntity> targets) {
        if (checkBanned(source)) return -1;
        if (targets == null || targets.isEmpty()) return -1;

        if (targets.size() == 1) {
            OtherUtils.sendCommandFeedback(source, TextUtils.format("§7Succeeding task for {}§7...", targets.iterator().next()));
        }
        else {
            OtherUtils.sendCommandFeedback(source, TextUtils.format("§7Succeeding task for {}§7 targets...", targets.size()));
        }

        for (ServerPlayerEntity player : targets) {
            TaskManager.succeedTask(player, true);
        }

        return 1;
    }

    public static int failTask(ServerCommandSource source, Collection<ServerPlayerEntity> targets) {
        if (checkBanned(source)) return -1;
        if (targets == null || targets.isEmpty()) return -1;

        if (targets.size() == 1) {
            OtherUtils.sendCommandFeedback(source, TextUtils.format("§7Failing task for {}§7...", targets.iterator().next()));
        }
        else {
            OtherUtils.sendCommandFeedback(source, TextUtils.format("§7Failing task for {}§7 targets...", targets.size()));
        }

        for (ServerPlayerEntity player : targets) {
            TaskManager.failTask(player, true);
        }

        return 1;
    }

    public static int rerollTask(ServerCommandSource source, Collection<ServerPlayerEntity> targets) {
        if (checkBanned(source)) return -1;
        if (targets == null || targets.isEmpty()) return -1;

        if (targets.size() == 1) {
            OtherUtils.sendCommandFeedback(source, TextUtils.format("§7Rerolling task for {}§7...", targets.iterator().next()));
        }
        else {
            OtherUtils.sendCommandFeedback(source, TextUtils.format("§7Rerolling task for {}§7 targets...", targets.size()));
        }

        for (ServerPlayerEntity player : targets) {
            TaskManager.rerollTask(player, true);
        }

        return 1;
    }

    public static final List<UUID> playersGiven = new ArrayList<>();
    public static int gift(ServerCommandSource source, ServerPlayerEntity target) {
        if (checkBanned(source)) return -1;
        final ServerPlayerEntity self = source.getPlayer();
        if (self == null) return -1;
        if (target == null) return -1;
        SecretLife secretLife = (SecretLife) currentSeason;

        if (target == self) {
            source.sendError(Text.of("Nice Try."));
            return -1;
        }
        if (playersGiven.contains(self.getUuid())) {
            source.sendError(Text.of("You have already gifted a heart this session"));
            return -1;
        }
        if (!livesManager.isAlive(target)) {
            source.sendError(Text.of("That player is not alive"));
            return -1;
        }
        if (!currentSession.statusStarted()) {
            source.sendError(Text.of("The session has not started"));
            return -1;
        }
        playersGiven.add(self.getUuid());
        secretLife.addPlayerHealth(target, 2);
        Text senderMessage = TextUtils.format("You have gifted a heart to {}", target);
        Text recipientMessage = TextUtils.format("{} gave you a heart", self);

        self.sendMessage(senderMessage);
        PlayerUtils.sendTitle(target, recipientMessage, 20, 20, 20);
        target.sendMessage(recipientMessage);
        AnimationUtils.createSpiral(target, 40);

        PlayerUtils.playSoundToPlayers(List.of(self,target), SoundEvent.of(Identifier.of("minecraft","secretlife_life")));

        return 1;
    }

    public static int showHealth(ServerCommandSource source) {
        if (checkBanned(source)) return -1;

        final ServerPlayerEntity self = source.getPlayer();

        if (self == null) return -1;

        SecretLife secretLife = (SecretLife) currentSeason;

        if (!livesManager.isAlive(self)) {
            OtherUtils.sendCommandFeedbackQuiet(source, Text.of("You're dead..."));
            return -1;
        }

        double playerHealth = secretLife.getRoundedHealth(self);
        OtherUtils.sendCommandFeedbackQuiet(source, TextUtils.format("You have {} health", playerHealth));

        return 1;
    }

    public static int getHealthFor(ServerCommandSource source, ServerPlayerEntity target) {
        if (checkBanned(source)) return -1;
        if (target == null) return -1;

        SecretLife secretLife = (SecretLife) currentSeason;
        if (!livesManager.isAlive(target)) {
            OtherUtils.sendCommandFeedbackQuiet(source, TextUtils.format("{} is dead", target));
            return -1;
        }

        double playerHealth = secretLife.getRoundedHealth(target);
        OtherUtils.sendCommandFeedbackQuiet(source, TextUtils.format("{} has {} health", target, playerHealth));
        return 1;
    }

    public static int syncHealth(ServerCommandSource source) {
        if (checkBanned(source)) return -1;
        SecretLife secretLife = (SecretLife) currentSeason;
        secretLife.syncAllPlayerHealth();
        return 1;
    }

    public static int healthManager(ServerCommandSource source, Collection<ServerPlayerEntity> targets, double amount, boolean setNotGive) {
        if (checkBanned(source)) return -1;
        if (targets == null || targets.isEmpty()) return -1;

        SecretLife secretLife = (SecretLife) currentSeason;
        if (setNotGive) {
            for (ServerPlayerEntity player : targets) {
                secretLife.setPlayerHealth(player, amount);
            }
            if (targets.size() == 1) {
                OtherUtils.sendCommandFeedback(source, TextUtils.format("Set {}'s health to {}", targets.iterator().next(), amount));
            }
            else {
                OtherUtils.sendCommandFeedback(source, TextUtils.format("Set the health of {} targets to {}", targets.size(), amount));
            }
        }
        else {
            for (ServerPlayerEntity player : targets) {
                secretLife.addPlayerHealth(player, amount);
            }
            String addOrRemove = amount >= 0 ? "Added" : "Removed";
            String toOrFrom = amount >= 0 ? "to" : "from";
            if (targets.size() == 1) {
                OtherUtils.sendCommandFeedback(source, TextUtils.format("{} {} health {} {}", addOrRemove, Math.abs(amount), toOrFrom, targets.iterator().next()));
            }
            else {
                OtherUtils.sendCommandFeedback(source, TextUtils.format("{} {} health {} {} targets", addOrRemove, Math.abs(amount), toOrFrom, targets.size()));
            }
        }

        return 1;
    }

    public static int resetHealth(ServerCommandSource source, Collection<ServerPlayerEntity> targets) {
        if (checkBanned(source)) return -1;
        if (targets == null || targets.isEmpty()) return -1;

        for (ServerPlayerEntity player : targets) {
            SecretLife secretLife = (SecretLife) currentSeason;
            secretLife.resetPlayerHealth(player);
        }

        if (targets.size() == 1) {
            OtherUtils.sendCommandFeedback(source, TextUtils.format("Reset {}'s health to the default", targets.iterator().next()));
        }
        else {
            OtherUtils.sendCommandFeedback(source, TextUtils.format("Reset the health to default for {} targets", targets.size()));
        }

        return 1;
    }
}
