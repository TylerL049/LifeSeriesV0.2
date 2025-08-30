package net.mat0u5.lifeseries.registries;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.mat0u5.lifeseries.command.*;
import net.mat0u5.lifeseries.dependencies.DependencyManager;
import net.mat0u5.lifeseries.events.Events;
import net.mat0u5.lifeseries.seasons.season.doublelife.DoubleLifeCommands;
import net.mat0u5.lifeseries.seasons.season.secretlife.SecretLifeCommands;
import net.mat0u5.lifeseries.seasons.season.wildlife.WildLifeCommands;
import net.mat0u5.lifeseries.utils.other.TaskScheduler;
import net.mat0u5.lifeseries.utils.other.TextUtils;

public class ModRegistries {
    public static void registerModStuff() {
        registerCommands();
        registerEvents();
        TextUtils.setEmotes();

        if (DependencyManager.polymerLoaded()) MobRegistry.registerMobs();
    }

    private static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(DoubleLifeCommands::register);
        CommandRegistrationCallback.EVENT.register(SecretLifeCommands::register);
        CommandRegistrationCallback.EVENT.register(WildLifeCommands::register);

        CommandRegistrationCallback.EVENT.register(LivesCommand::register);
        CommandRegistrationCallback.EVENT.register(SessionCommand::register);
        CommandRegistrationCallback.EVENT.register(BoogeymanCommand::register);
        CommandRegistrationCallback.EVENT.register(ClaimKillCommand::register);
        CommandRegistrationCallback.EVENT.register(LifeSeriesCommand::register);
        CommandRegistrationCallback.EVENT.register(GivelifeCommand::register);
        CommandRegistrationCallback.EVENT.register(SelfMessageCommand::register);
        CommandRegistrationCallback.EVENT.register(WatcherCommand::register);
    }

    private static void registerEvents() {
        Events.register();
        TaskScheduler.registerTickHandler();
    }
}
