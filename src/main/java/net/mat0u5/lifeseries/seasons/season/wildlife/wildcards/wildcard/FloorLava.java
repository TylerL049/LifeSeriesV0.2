package net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;

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
    private static final int EFFECT_COOLDOWN_TICKS = 4; // very short, reapplied every tick on contact

    private static final Set<Block> NATURAL_BLOCKS = Set.of(
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

        List<ServerPlayerEntity> players = PlayerUtils.getAllFunctioningPlayers();
        if (players != null) {
            for (ServerPlayerEntity player : players) {
                if (player != null) {
                    player.sendMessage(Text.literal("§cFloor is Lava wildcard has been activated!"), false);
                }
            }
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        tickCounter = 0;
        lastEffectTick.clear();

        List<ServerPlayerEntity> players = PlayerUtils.getAllFunctioningPlayers();
        if (players != null) {
            for (ServerPlayerEntity player : players) {
                if (player != null) {
                    player.sendMessage(Text.literal("§aFloor is Lava wildcard has been deactivated!"), false);
                }
            }
        }
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
        BlockPos blockBelow = player.getBlockPos().down();
        Block block = player.getWorld().getBlockState(blockBelow).getBlock();

        if (NATURAL_BLOCKS.contains(block)) {
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

        StatusEffectInstance witherEffect = new StatusEffectInstance(
                StatusEffects.WITHER,
                2, // 2 ticks, will refresh if standing on block
                1, // level 2 Wither (amplifier 1)
                false,
                true,
                true
        );

        boolean applied = player.addStatusEffect(witherEffect);
        if (applied) lastEffectTick.put(playerId, tickCounter);
    }

    private void spawnParticles(ServerPlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return;

        serverWorld.spawnParticles(
                ParticleTypes.SMOKE,
                player.getX(),
                player.getY() + 0.1,
                player.getZ(),
                10,
                0.3,
                0.1,
                0.3,
                0.02
        );

        serverWorld.spawnParticles(
                ParticleTypes.FLAME,
                player.getX(),
                player.getY() + 0.1,
                player.getZ(),
                5,
                0.2, 0.05, 0.2,
                0.01
        );

        serverWorld.spawnParticles(
                ParticleTypes.LAVA,
                player.getX(),
                player.getY() + 0.1,
                player.getZ(),
                2,
                0.1, 0.05, 0.1,
                0.01
        );
    }

    private void playSound(ServerPlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return;

        serverWorld.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.BLOCK_FIRE_AMBIENT,
                SoundCategory.PLAYERS,
                0.7F,
                1.0F + (float)(Math.random() * 0.4 - 0.2)
        );

        serverWorld.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.BLOCK_LAVA_EXTINGUISH,
                SoundCategory.PLAYERS,
                0.3F,
                1.5F
        );
    }
}