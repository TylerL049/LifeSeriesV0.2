package net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Random;

import static net.mat0u5.lifeseries.utils.player.PermissionManager.isAdmin;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class PlayerSwap extends Wildcard {

    private static final int TICKS_PER_SECOND = 20;
    private static final int INITIAL_DELAY = 120 * TICKS_PER_SECOND;
    private static final int MIN_DELAY_FIRST_HOUR = 5 * 60 * TICKS_PER_SECOND;
    private static final int MAX_DELAY_FIRST_HOUR = 10 * 60 * TICKS_PER_SECOND;
    private static final int MIN_DELAY_AFTER_HOUR = 60 * TICKS_PER_SECOND;
    private static final int MAX_DELAY_AFTER_HOUR = 5 * 60 * TICKS_PER_SECOND;

    private static final double MOB_SWAP_CHANCE_INITIAL = 0.20;
    private static final double MOB_SWAP_CHANCE_AFTER_HOUR = 0.35;

    private final Random random = new Random();
    private int tickCounter = 0;
    private int nextSwapTick = -1;
    private boolean active = false;

    @Override
    public Wildcards getType() {
        return Wildcards.PLAYER_SWAP;
    }

    @Override
    public void activate() {
        this.active = true;
        this.tickCounter = 0;
        this.nextSwapTick = INITIAL_DELAY;
    }

    @Override
    public void tick() {
        if (!active) return;

        tickCounter++;
        if (nextSwapTick > 0 && tickCounter >= nextSwapTick) {
            doSwap(null);

            int elapsed = tickCounter;
            if (elapsed < 60 * 60 * TICKS_PER_SECOND) {
                nextSwapTick = tickCounter + MIN_DELAY_FIRST_HOUR +
                        random.nextInt(MAX_DELAY_FIRST_HOUR - MIN_DELAY_FIRST_HOUR + 1);
            } else {
                nextSwapTick = tickCounter + MIN_DELAY_AFTER_HOUR +
                        random.nextInt(MAX_DELAY_AFTER_HOUR - MIN_DELAY_AFTER_HOUR + 1);
            }
        }
    }

    /** Main swap method */
    public void doSwap(String forceType) {
        List<ServerPlayerEntity> players = PlayerUtils.getAllFunctioningPlayers();
        if (players.isEmpty()) return;

        ServerPlayerEntity player = players.get(random.nextInt(players.size()));
        boolean useMob;

        if ("players".equalsIgnoreCase(forceType)) useMob = false;
        else if ("mob".equalsIgnoreCase(forceType)) useMob = true;
        else useMob = shouldSwapWithMob();

        if (useMob) swapWithMob(player);
        else if (players.size() > 1) {
            ServerPlayerEntity other = players.get(random.nextInt(players.size()));
            while (other == player && players.size() > 1) {
                other = players.get(random.nextInt(players.size()));
            }
            swapPlayers(player, other);
        }
    }

    private boolean shouldSwapWithMob() {
        double chance = (tickCounter < 60 * 60 * TICKS_PER_SECOND)
                ? MOB_SWAP_CHANCE_INITIAL
                : MOB_SWAP_CHANCE_AFTER_HOUR;
        return random.nextDouble() < chance;
    }

    private void swapPlayers(ServerPlayerEntity p1, ServerPlayerEntity p2) {
        if (p1 == null || p2 == null) return;

        double p1X = p1.getX(), p1Y = p1.getY(), p1Z = p1.getZ();
        double p2X = p2.getX(), p2Y = p2.getY(), p2Z = p2.getZ();

        ServerPlayerEntity executor = PlayerUtils.getPlayer("Talis04");
        if (executor == null) return;

        var source = executor.getCommandSource();
        executor.getServer().getCommandManager().executeWithPrefix(source,
                "tp " + p1.getName().getString() + " " + p2X + " " + p2Y + " " + p2Z);
        executor.getServer().getCommandManager().executeWithPrefix(source,
                "tp " + p2.getName().getString() + " " + p1X + " " + p1Y + " " + p1Z);

        applyNegativeEffects(p1);
        applyNegativeEffects(p2);
    }

    private void swapWithMob(ServerPlayerEntity player) {
        if (player == null) return;

        MobEntity mob = getNearestMob(player, 50);
        if (mob == null) return;

        double playerX = player.getX(), playerY = player.getY(), playerZ = player.getZ();
        double mobX = mob.getX(), mobY = mob.getY(), mobZ = mob.getZ();

        ServerPlayerEntity executor = PlayerUtils.getPlayer("Talis04");
        if (executor == null) return;

        var source = executor.getCommandSource();
        executor.getServer().getCommandManager().executeWithPrefix(source,
                "tp " + player.getName().getString() + " " + mobX + " " + mobY + " " + mobZ);
        executor.getServer().getCommandManager().executeWithPrefix(source,
                "tp " + mob.getUuidAsString() + " " + playerX + " " + playerY + " " + playerZ);

        applyNegativeEffects(player);
    }

    private MobEntity getNearestMob(ServerPlayerEntity player, double radius) {
        List<MobEntity> mobs = player.getWorld().getEntitiesByClass(
                MobEntity.class, player.getBoundingBox().expand(radius), mob -> true);
        if (mobs.isEmpty()) return null;
        return mobs.get(random.nextInt(mobs.size()));
    }

    private void applyNegativeEffects(ServerPlayerEntity player) {
        if (player == null || player.isSpectator()) return;

        int duration = 5 * TICKS_PER_SECOND;
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, duration, 0, false, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, duration, 1, false, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, duration, 0, false, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, duration, 0, false, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, duration, 0, false, false, false));
    }

    @Override
    public void deactivate() {
        this.active = false;
        this.tickCounter = 0;
        this.nextSwapTick = -1;
    }

    /** Register /playerswap command */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment,
                                PlayerSwap instance) {

        dispatcher.register(
                literal("playerswap")
                        .requires(source -> {
                            ServerPlayerEntity player = source.getPlayer();
                            return player == null || isAdmin(player); // console or admin
                        })
                        .then(literal("activateswap")
                                .executes(context -> runSwapCommand(context, instance, null))
                                .then(literal("players")
                                        .executes(context -> runSwapCommand(context, instance, "players"))
                                )
                                .then(literal("mob")
                                        .executes(context -> runSwapCommand(context, instance, "mob"))
                                )
                        )
        );
    }

    private static int runSwapCommand(CommandContext<ServerCommandSource> context,
                                      PlayerSwap instance, String forceType) {

        ServerCommandSource source = context.getSource();
        ServerPlayerEntity executor = source.getPlayer();

        instance.doSwap(forceType);

        if (executor != null) {
            executor.sendMessage(Text.literal("PlayerSwap triggered manually."), false);
            if (forceType != null) {
                executor.sendMessage(Text.literal("Forced swap type: " + forceType), false);
            }
        } else {
            source.sendFeedback(Text.literal("PlayerSwap triggered manually."), false);
        }

        return 1;
    }
}