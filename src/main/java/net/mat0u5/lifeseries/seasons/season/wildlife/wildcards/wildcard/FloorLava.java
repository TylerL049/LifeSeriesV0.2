package net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FloorLava extends Wildcard {
    private static final int TICKS_PER_SECOND = 20;
    private static final int EFFECT_CHECK_INTERVAL = 20;
    private int tickCounter = 0;

    private final Map<UUID, Integer> lastEffectTick = new HashMap<>();
    private static final int EFFECT_COOLDOWN_TICKS = 4;

    private static final Set<Block> DAMAGING_BLOCKS = Set.of(
            Blocks.GRASS_BLOCK,
            Blocks.DIRT,
            Blocks.STONE,
            Blocks.SAND,
            Blocks.DIRT_PATH,
            Blocks.SNOW,
            Blocks.SNOW_BLOCK,
            Blocks.MOSS_BLOCK,
            Blocks.MOSS_CARPET,
            Blocks.DEEPSLATE,
            Blocks.COARSE_DIRT,
            Blocks.PODZOL,
            Blocks.MYCELIUM,
            Blocks.GRAVEL,
            Blocks.COBBLESTONE,
            Blocks.ANDESITE,
            Blocks.DIORITE,
            Blocks.GRANITE,
            Blocks.CALCITE,
            Blocks.TUFF,
            Blocks.DRIPSTONE_BLOCK,
            Blocks.ROOTED_DIRT,
            Blocks.MUD,
            Blocks.CLAY
    );

    @Override
    public Wildcards getType() {
        return Wildcards.FLOOR_LAVA;
    }

    @Override
    public void activate() {
        super.activate();
        tickCounter = 0;
        lastEffectTick.clear();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        tickCounter = 0;
        lastEffectTick.clear();
    }

    @Override
    public void tick() {
        if (!active) return;

        tickCounter++;
        if (tickCounter % EFFECT_CHECK_INTERVAL != 0) return;

        List<ServerPlayerEntity> players = PlayerUtils.getAllFunctioningPlayers();
        if (players == null || players.isEmpty()) return;

        for (ServerPlayerEntity player : players) {
            if (player == null || player.isSpectator() || player.isCreative()) continue;
            checkPlayerPosition(player);
        }
    }

    private void checkPlayerPosition(ServerPlayerEntity player) {
        BlockPos blockStandingOn = player.getBlockPos().down(); // block player is standing on
        BlockPos blockAboveStandingOn = blockStandingOn.up();   // block above it, e.g. carpet or snow

        Block blockAtFeet = player.getWorld().getBlockState(blockStandingOn).getBlock();
        Block blockAboveFeet = player.getWorld().getBlockState(blockAboveStandingOn).getBlock();

        boolean damaging = DAMAGING_BLOCKS.contains(blockAtFeet) || DAMAGING_BLOCKS.contains(blockAboveFeet);

        if (damaging) {
            applyWitherEffect(player);
            spawnParticles(player);
            playSound(player);
        }
    }

    private void applyWitherEffect(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();

        if (lastEffectTick.containsKey(playerId)) {
            int lastTick = lastEffectTick.get(playerId);
            if (tickCounter - lastTick < EFFECT_COOLDOWN_TICKS) return;
        }

        // Calculate wither amplifier based on elapsed time since activation
        int elapsedSeconds = tickCounter / TICKS_PER_SECOND;
        int rampStart = 10 * 60; // 10 minutes in seconds
        int rampEnd = 20 * 60;   // 20 minutes in seconds
        int amplifier = 0;

        if (elapsedSeconds >= rampStart) {
            int rampProgress = Math.min(elapsedSeconds - rampStart, rampEnd - rampStart);
            amplifier = (int) ((rampProgress / (double)(rampEnd - rampStart)) * 39);
        }

        StatusEffectInstance witherEffect = new StatusEffectInstance(
                StatusEffects.WITHER,
                25, // ticks duration
                amplifier,
                false,
                true,
                true
        );

        if (player.addStatusEffect(witherEffect)) {
            lastEffectTick.put(playerId, tickCounter);
        }

        // ?? Show flames visually, no fire damage
        showBurningVisual(player);
    }

    private void showBurningVisual(ServerPlayerEntity player) {
        // Send client-only packet to show fire animation without damage
        player.networkHandler.sendPacket(new EntityStatusS2CPacket(player, (byte) 0x01));
    }

    private void spawnParticles(ServerPlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return;

        serverWorld.spawnParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 0.1, player.getZ(), 10, 0.3, 0.1, 0.3, 0.02);
        serverWorld.spawnParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.1, player.getZ(), 5, 0.2, 0.05, 0.2, 0.01);
        serverWorld.spawnParticles(ParticleTypes.LAVA, player.getX(), player.getY() + 0.1, player.getZ(), 2, 0.1, 0.05, 0.1, 0.01);
    }

    private void playSound(ServerPlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return;

        serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.PLAYERS, 0.7F,
                1.0F + (float)(Math.random() * 0.4 - 0.2));

        serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.PLAYERS, 0.3F, 1.5F);
    }
}