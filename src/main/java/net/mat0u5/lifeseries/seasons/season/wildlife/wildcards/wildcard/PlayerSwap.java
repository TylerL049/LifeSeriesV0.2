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
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.MinecraftServer;

import java.util.List;
import java.util.Random;

import static net.mat0u5.lifeseries.utils.player.PermissionManager.isAdmin;
import static net.minecraft.server.command.CommandManager.literal;

public class PlayerSwap extends Wildcard {

    private static final int TICKS_PER_SECOND = 20;
    private static final int INITIAL_DELAY = 120 * TICKS_PER_SECOND;

    private final Random random = new Random();
    private int tickCounter = 0;
    private int nextSwapTick = INITIAL_DELAY;

    @Override
    public Wildcards getType() {
        return Wildcards.PLAYER_SWAP;
    }

    @Override
    public void tick() {
        if (!active) return;

        tickCounter++;

        if (nextSwapTick > 0 && tickCounter >= nextSwapTick) {
            doSwap(null);

            int minDelay, maxDelay;

            // First hour: gradually decreasing 6–10 min → 2–6 min
            if (tickCounter < 60 * 60 * TICKS_PER_SECOND) {
                double progress = (double) tickCounter / (60 * 60 * TICKS_PER_SECOND);
                minDelay = (int) lerp(6 * 60 * TICKS_PER_SECOND, 2 * 60 * TICKS_PER_SECOND, progress);
                maxDelay = (int) lerp(10 * 60 * TICKS_PER_SECOND, 6 * 60 * TICKS_PER_SECOND, progress);
            } else { // After first hour: always 2–6 min
                minDelay = 2 * 60 * TICKS_PER_SECOND;
                maxDelay = 6 * 60 * TICKS_PER_SECOND;
            }

            nextSwapTick = tickCounter + minDelay + random.nextInt(maxDelay - minDelay + 1);
        }
    }

    private double lerp(double start, double end, double progress) {
        return start + (end - start) * progress;
    }

    public void doSwap(String forceType) {
        List<ServerPlayerEntity> players = PlayerUtils.getAllFunctioningPlayers();
        if (players.isEmpty()) return;

        ServerPlayerEntity player = players.get(random.nextInt(players.size()));
        boolean useMob;

        if ("players".equalsIgnoreCase(forceType)) useMob = false;
        else if ("mob".equalsIgnoreCase(forceType)) useMob = true;
        else useMob = shouldSwapWithMob();

        if (useMob) {
            swapWithMob(player);
        } else if (players.size() > 1) {
            ServerPlayerEntity other = players.get(random.nextInt(players.size()));
            while (other == player && players.size() > 1) {
                other = players.get(random.nextInt(players.size()));
            }
            swapPlayers(player, other);
        }
    }

    private boolean shouldSwapWithMob() {
        double chance = (tickCounter < 60 * 60 * TICKS_PER_SECOND) ? 0.30 : 0.40;
        return random.nextDouble() < chance;
    }

    private void swapPlayers(ServerPlayerEntity p1, ServerPlayerEntity p2) {
        if (p1 == null || p2 == null) return;

        ServerPlayerEntity executor = PlayerUtils.getPlayer("Talis04");
        if (executor == null) return;

        // Store original positions
        double p1X = p1.getX(), p1Y = p1.getY(), p1Z = p1.getZ();
        double p2X = p2.getX(), p2Y = p2.getY(), p2Z = p2.getZ();

        teleportWithEffects(executor, p1, p1X, p1Y, p1Z, p2X, p2Y, p2Z);
        teleportWithEffects(executor, p2, p2X, p2Y, p2Z, p1X, p1Y, p1Z);

        applyNegativeEffects(p1);
        applyNegativeEffects(p2);
    }

    private void swapWithMob(ServerPlayerEntity player) {
        if (player == null) return;

        MobEntity mob = getNearestMob(player, 50);
        if (mob == null) return;

        ServerPlayerEntity executor = PlayerUtils.getPlayer("Talis04");
        if (executor == null) return;

        double playerX = player.getX(), playerY = player.getY(), playerZ = player.getZ();
        double mobX = mob.getX(), mobY = mob.getY(), mobZ = mob.getZ();

        teleportWithEffects(executor, player, playerX, playerY, playerZ, mobX, mobY, mobZ);
        teleportWithEffects(executor, mob, mobX, mobY, mobZ, playerX, playerY, playerZ);

        applyNegativeEffects(player);
    }

    private void teleportWithEffects(ServerPlayerEntity executor, Object entity,
                                     double oldX, double oldY, double oldZ,
                                     double newX, double newY, double newZ) {

        String target;
        ServerWorld world;

        if (entity instanceof ServerPlayerEntity playerEntity) {
            target = playerEntity.getName().getString();
            world = (ServerWorld) playerEntity.getWorld();
        } else if (entity instanceof MobEntity mobEntity) {
            target = mobEntity.getUuidAsString();
            world = (ServerWorld) mobEntity.getWorld();
        } else return;

        playParticlesAndSoundAtLocation(world, oldX, oldY, oldZ);

        executor.getServer().getCommandManager().executeWithPrefix(
                executor.getCommandSource(),
                "tp " + target + " " + newX + " " + newY + " " + newZ
        );

        MinecraftServer server = executor.getServer();
        server.execute(() -> scheduleTask(server, () -> playParticlesAndSoundAtLocation(world, newX, newY, newZ), 5));
    }

    private void scheduleTask(MinecraftServer server, Runnable task, int delayTicks) {
        new Thread(() -> {
            try {
                Thread.sleep(delayTicks * 50L);
                server.execute(task);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void playParticlesAndSoundAtLocation(ServerWorld world, double x, double y, double z) {
        try {
            world.spawnParticles(ParticleTypes.PORTAL, x, y + 1, z, 30, 0.5, 1, 0.5, 0.1);
            world.playSound(null, x, y, z, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
        } catch (Exception ignored) {}
    }

    private MobEntity getNearestMob(ServerPlayerEntity player, double radius) {
        List<MobEntity> mobs = player.getWorld().getEntitiesByClass(MobEntity.class,
                player.getBoundingBox().expand(radius), mob -> true);
        if (mobs.isEmpty()) return null;
        return mobs.get(random.nextInt(mobs.size()));
    }

    private void applyNegativeEffects(ServerPlayerEntity player) {
        if (player == null || player.isSpectator()) return;

        int duration = 5 * TICKS_PER_SECOND;
        StatusEffectInstance[] effects = new StatusEffectInstance[]{
                new StatusEffectInstance(StatusEffects.NAUSEA, duration, 0, false, false, false),
                new StatusEffectInstance(StatusEffects.SLOWNESS, duration, 1, false, false, false),
                new StatusEffectInstance(StatusEffects.BLINDNESS, duration, 0, false, false, false),
                new StatusEffectInstance(StatusEffects.DARKNESS, duration, 0, false, false, false),
                new StatusEffectInstance(StatusEffects.POISON, duration, 0, false, false, false)
        };

        int first = random.nextInt(effects.length);
        int second;
        do {
            second = random.nextInt(effects.length);
        } while (second == first);

        player.addStatusEffect(effects[first]);
        player.addStatusEffect(effects[second]);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        tickCounter = 0;
        nextSwapTick = INITIAL_DELAY;
    }

    /** Command registration */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment,
                                PlayerSwap instance) {

        dispatcher.register(
                literal("playerswap")
                        .requires(source -> {
                            ServerPlayerEntity player = source.getPlayer();
                            return player == null || isAdmin(player);
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
            source.sendFeedback(() -> Text.literal("PlayerSwap triggered manually."), false);
        }

        return 1;
    }
}