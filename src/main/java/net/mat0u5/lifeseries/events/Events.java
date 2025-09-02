package net.mat0u5.lifeseries.events;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.entity.fakeplayer.FakePlayer;
import net.mat0u5.lifeseries.network.NetworkHandlerServer;
import net.mat0u5.lifeseries.resources.datapack.DatapackManager;
import net.mat0u5.lifeseries.seasons.other.WatcherManager;
import net.mat0u5.lifeseries.seasons.season.Seasons;
import net.mat0u5.lifeseries.seasons.season.doublelife.DoubleLife;
import net.mat0u5.lifeseries.seasons.season.secretlife.SecretLife;
import net.mat0u5.lifeseries.seasons.season.secretlife.TaskManager;
import net.mat0u5.lifeseries.seasons.season.wildlife.morph.MorphManager;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard.snails.SnailSkinsServer;
import net.mat0u5.lifeseries.seasons.session.SessionTranscript;
import net.mat0u5.lifeseries.utils.other.TaskScheduler;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.mat0u5.lifeseries.utils.world.ItemStackUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard.HotPotato;

import java.util.*;
import java.util.List;
import java.util.UUID;

import static net.mat0u5.lifeseries.Main.*;

public class Events {
    public static boolean skipNextTickReload = false;
    public static boolean updatePlayerListsNextTick = false;

    public static final List<UUID> joiningPlayers = new ArrayList<>();
    private static final Map<UUID, Vec3d> joiningPlayersPos = new HashMap<>();
    private static final Map<UUID, Float> joiningPlayersYaw = new HashMap<>();
    private static final Map<UUID, Float> joiningPlayersPitch = new HashMap<>();

    public static void register() {
        // Server lifecycle
        ServerLifecycleEvents.SERVER_STARTING.register(Events::onServerStarting);
        ServerLifecycleEvents.SERVER_STARTED.register(Events::onServerStart);
        ServerLifecycleEvents.SERVER_STOPPING.register(Events::onServerStopping);

        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register(Events::onReloadStart);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(Events::onReloadEnd);

        // Player interactions
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
            return onBlockAttack(serverPlayer, world, pos);
        });
        UseBlockCallback.EVENT.register(Events::onBlockUse);
        UseEntityCallback.EVENT.register(Events::onRightClickEntity);
        AttackEntityCallback.EVENT.register(Events::onAttackEntity);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onPlayerJoin(handler.getPlayer()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> onPlayerDisconnect(handler.getPlayer()));

        // Server tick
        ServerTickEvents.END_SERVER_TICK.register(Events::onServerTickEnd);

        // Living entity death
        ServerLivingEntityEvents.AFTER_DEATH.register(Events::onEntityDeath);
    }

    // Hot Potato pickup scan (called each server tick)
    public static void scanHotPotatoPickup(MinecraftServer server) {
        HotPotato hotPotato = HotPotato.getInstance();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            for (int i = 0; i < player.getInventory().size(); i++) {
                ItemStack stack = player.getInventory().getStack(i);
                if (hotPotato.isHotPotato(stack)) {
                    hotPotato.onPlayerPickupPotato(player, stack);
                    player.getInventory().removeStack(i);
                    break;
                }
            }
        }
    }

    private static void onReloadStart(MinecraftServer server, LifecycledResourceManager resourceManager) {
        try {
            if (!Main.isLogicalSide()) return;
            Main.reloadStart();
        } catch(Exception e) {Main.LOGGER.error(e.getMessage());}
    }

    private static void onReloadEnd(MinecraftServer server, LifecycledResourceManager resourceManager, boolean success) {
        try {
            if (!Main.isLogicalSide()) return;
            Main.reloadEnd();
        } catch(Exception e) {Main.LOGGER.error(e.getMessage());}
    }

    private static void onPlayerJoin(ServerPlayerEntity player) {
        if (isFakePlayer(player)) return;

        try {
            playerStartJoining(player);
            currentSeason.onPlayerJoin(player);
            currentSeason.onUpdatedInventory(player);
            SessionTranscript.playerJoin(player);
            MorphManager.onPlayerJoin(player);
        } catch(Exception e) {Main.LOGGER.error(e.getMessage());}
    }

    private static void onPlayerFinishJoining(ServerPlayerEntity player) {
        if (isFakePlayer(player)) return;

        try {
            currentSeason.onPlayerFinishJoining(player);
            TaskScheduler.scheduleTask(20, () -> {
                NetworkHandlerServer.tryKickFailedHandshake(player);
                PlayerUtils.resendCommandTree(player);
            });
            MorphManager.onPlayerDisconnect(player);
            MorphManager.syncToPlayer(player);
        } catch(Exception e) {Main.LOGGER.error(e.getMessage());}
    }

    private static void onPlayerDisconnect(ServerPlayerEntity player) {
        if (isFakePlayer(player)) return;

        try {
            currentSeason.onPlayerDisconnect(player);
            SessionTranscript.playerLeave(player);
        } catch(Exception e) {Main.LOGGER.error(e.getMessage());}
    }

    private static void onServerStopping(MinecraftServer server) {
        try {
            currentSession.sessionEnd();
        }catch (Exception e) {Main.LOGGER.error(e.getMessage());}
    }

    private static void onServerStarting(MinecraftServer server) {
        Main.server = server;
    }

    private static void onServerStart(MinecraftServer server) {
        try {
            Main.server = server;
            currentSeason.initialize();
            blacklist.reloadBlacklist();
            if (currentSeason.getSeason() == Seasons.DOUBLE_LIFE) {
                ((DoubleLife) currentSeason).loadSoulmates();
            }
            DatapackManager.onServerStarted(server);
        } catch(Exception e) {Main.LOGGER.error(e.getMessage());}
    }

    private static void onServerTickEnd(MinecraftServer server) {
        try {
            skipNextTickReload = false;
            if (!Main.isLogicalSide()) return;
            if (updatePlayerListsNextTick) {
                updatePlayerListsNextTick = false;
                PlayerUtils.updatePlayerLists();
            }
            checkPlayerFinishJoiningTick();
            if (server.getTickManager().isFrozen()) return;
            if (Main.currentSession != null) {
                Main.currentSession.tick(server);
                currentSeason.tick(server);
            }
            PlayerUtils.onTick();
            if (NetworkHandlerServer.updatedConfigThisTick) {
                NetworkHandlerServer.onUpdatedConfig();
            }

            // Hot Potato pickup scan
            scanHotPotatoPickup(server);

        } catch(Exception e) {
            Main.LOGGER.error(e.getMessage());
        }
    }

    public static void onEntityDeath(LivingEntity entity, DamageSource source) {
        if (isFakePlayer(entity)) return;
        try {
            if (!Main.isLogicalSide()) return;
            if (entity instanceof ServerPlayerEntity player) {
                onPlayerDeath(player, source);
                return;
            }
            currentSeason.onMobDeath(entity, source);
        } catch(Exception e) {Main.LOGGER.error(e.getMessage());}
    }

    public static void onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        if (isExcludedPlayer(player)) return;
        try {
            if (!Main.isLogicalSide()) return;
            currentSeason.onPlayerDeath(player, source);
        } catch(Exception e) {Main.LOGGER.error(e.getMessage());}
    }

    public static ActionResult onBlockUse(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (isFakePlayer(player)) return ActionResult.PASS;
        return ActionResult.PASS;
    }

    public static ActionResult onBlockAttack(ServerPlayerEntity player, World world, BlockPos pos) {
        if (isFakePlayer(player)) return ActionResult.PASS;
        return ActionResult.PASS;
    }

    private static ActionResult onRightClickEntity(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult) {
        if (isFakePlayer(player)) return ActionResult.PASS;
        return ActionResult.PASS;
    }

    private static ActionResult onAttackEntity(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult) {
        if (isFakePlayer(player)) return ActionResult.PASS;
        return ActionResult.PASS;
    }

    // Joining player helpers
    public static void playerStartJoining(ServerPlayerEntity player) {
        joiningPlayers.add(player.getUuid());
        joiningPlayersPos.put(player.getUuid(), player.getPos());
        joiningPlayersYaw.put(player.getUuid(), player.getYaw());
        joiningPlayersPitch.put(player.getUuid(), player.getPitch());
    }

    public static void checkPlayerFinishJoiningTick() {
        for (UUID uuid : new ArrayList<>(joiningPlayers)) {
            ServerPlayerEntity player = PlayerUtils.getPlayer(uuid);
            if (player == null) continue;
            boolean finished = false;
            if (!player.getPos().equals(joiningPlayersPos.get(uuid))) finished = true;
            if (player.getYaw() != joiningPlayersYaw.get(uuid)) finished = true;
            if (player.getPitch() != joiningPlayersPitch.get(uuid)) finished = true;
            if (finished) {
                onPlayerFinishJoining(player);
                finishedJoining(uuid);
            }
        }
    }

    public static void finishedJoining(UUID uuid) {
        joiningPlayers.remove(uuid);
        joiningPlayersPos.remove(uuid);
        joiningPlayersYaw.remove(uuid);
        joiningPlayersPitch.remove(uuid);
    }

    public static boolean isExcludedPlayer(Entity entity) {
        if (entity instanceof ServerPlayerEntity player && WatcherManager.isWatcher(player)) return true;
        return isFakePlayer(entity);
    }

    public static boolean isFakePlayer(Entity entity) {
        return entity instanceof FakePlayer;
    }
}
