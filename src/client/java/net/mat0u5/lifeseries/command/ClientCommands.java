package net.mat0u5.lifeseries.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.network.NetworkHandlerClient;
import net.mat0u5.lifeseries.utils.enums.PacketNames;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ClientCommands {
    public static MinecraftClient client = MinecraftClient.getInstance();
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher,
                                CommandRegistryAccess commandRegistryAccess) {
        if (Main.DEBUG) {
            dispatcher.register(
                    literal("lsc")
                            .executes(context -> execute(context.getSource()))
                            .then(literal("packet")
                                    .then(literal("string")
                                            .then(argument("name", StringArgumentType.string())
                                                    .then(argument("value", StringArgumentType.string())
                                                            .executes(context -> sendStringPacket(
                                                                    context.getSource(),
                                                                    StringArgumentType.getString(context, "name"),
                                                                    StringArgumentType.getString(context, "value"))
                                                            )
                                                    )
                                                    .executes(context -> sendStringPacket(
                                                            context.getSource(),
                                                            StringArgumentType.getString(context, "name"),
                                                            "")
                                                    )
                                            )
                                    )
                                    .then(literal("number")
                                            .then(argument("name", StringArgumentType.string())
                                                    .then(argument("value", DoubleArgumentType.doubleArg())
                                                            .executes(context -> sendNumberPacket(
                                                                    context.getSource(),
                                                                    StringArgumentType.getString(context, "name"),
                                                                    DoubleArgumentType.getDouble(context, "value"))
                                                            )
                                                    )
                                            )
                                    )
                                    .then(literal("handshake")
                                            .executes(context -> sendHandshakePacket(
                                                    context.getSource())
                                            )
                                    )
                                    .then(literal("config")
                                            .then(argument("configType", StringArgumentType.string())
                                                    .then(argument("id", StringArgumentType.string())
                                                            .then(argument("args", StringArgumentType.string())
                                                                    .executes(context -> sendConfigPacket(
                                                                            context.getSource(),
                                                                            StringArgumentType.getString(context, "configType"),
                                                                            StringArgumentType.getString(context, "id"),
                                                                            StringArgumentType.getString(context, "args"))
                                                                    )
                                                            )
                                                    )
                                            )
                                    )
                            )
            );
        }
    }
    public static int execute(FabricClientCommandSource source)  {
        source.sendFeedback(Text.of("Life Series client command text."));
        return 1;
    }

    public static int sendStringPacket(FabricClientCommandSource source, String name, String value)  {
        final PlayerEntity self = source.getPlayer();
        NetworkHandlerClient.sendStringPacket(PacketNames.fromName(name), value);
        self.sendMessage(Text.of("String packet sent."), false);
        return 1;
    }

    public static int sendNumberPacket(FabricClientCommandSource source, String name, double value)  {
        final PlayerEntity self = source.getPlayer();
        NetworkHandlerClient.sendNumberPacket(PacketNames.fromName(name), value);
        self.sendMessage(Text.of("Number packet sent."), false);
        return 1;
    }

    public static int sendHandshakePacket(FabricClientCommandSource source)  {
        final PlayerEntity self = source.getPlayer();
        NetworkHandlerClient.sendHandshake();
        self.sendMessage(Text.of("Handshake packet sent."), false);
        return 1;
    }

    public static int sendConfigPacket(FabricClientCommandSource source, String configType, String id, String argsStr)  {
        final PlayerEntity self = source.getPlayer();
        List<String> args = new ArrayList<>();
        if (argsStr.contains(";")) {
            for (String arg : argsStr.split(";")) {
                if (!arg.isEmpty()) {
                    args.add(arg);
                }
            }
        }
        else {
            args = List.of(argsStr);
        }

        NetworkHandlerClient.sendConfigUpdate(configType, id, args);
        self.sendMessage(Text.of("Config packet sent."), false);
        return 1;
    }

}
