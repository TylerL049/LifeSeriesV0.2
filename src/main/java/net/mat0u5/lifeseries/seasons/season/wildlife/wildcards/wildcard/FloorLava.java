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
    private static final int EFFECT_COOLDOWN_TICKS = 40;

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
        this.tickCounter = 0;
        this.lastEffectTick.clear();

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
        this.tickCounter = 0;
        this.lastEffectTick.clear();

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
        BlockPos playerPos = player.getBlockPos();

        BlockPos[] positionsToCheck = {
                playerPos.down(),
                playerPos.down(2),
                playerPos,
                playerPos.add(0, -1, 0)
        };

        boolean standingOnNaturalBlock = false;
        Block foundBlock = null;

        for (BlockPos pos : positionsToCheck) {
            Block block = player.getWorld().getBlockState(pos).getBlock();
            if (NATURAL_BLOCKS.contains(block)) {
                standingOnNaturalBlock = true;
                foundBlock = block;
                break;
            }
        }

        if (standingOnNaturalBlock) {
            applyWitherEffect(player, foundBlock);
            spawnParticles(player);
            playSound(player);
        }
    }

    private void applyWitherEffect(ServerPlayerEntity player, Block block) {
        UUID playerId = player.getUuid();

        if (lastEffectTick.containsKey(playerId)) {
            int lastTick = lastEffectTick.get(playerId);
            if (tickCounter - lastTick < EFFECT_COOLDOWN_TICKS) {
                return;
            }
        }

        StatusEffectInstance witherEffect = new StatusEffectInstance(
                StatusEffects.WITHER,
                4 * TICKS_PER_SECOND,
                0,
                false,
                true,
                true
        );

        boolean applied = player.addStatusEffect(witherEffect);

        if (applied) {
            lastEffectTick.put(playerId, tickCounter);
            player.sendMessage(Text.literal("§c§lOUCH! The " + block.getName().getString() +
                    " burns your feet!"), true);
        }
    }

    private void spawnParticles(ServerPlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return;

        try {
            serverWorld.spawnParticles(
                    ParticleTypes.SMOKE,
                    player.getX(),
                    player.getY() + 0.1,
                    player.getZ(),
                    20,
                    0.5,
                    0.2,
                    0.5,
                    0.05
            );

            serverWorld.spawnParticles(
                    ParticleTypes.FLAME,
                    player.getX(),
                    player.getY() + 0.1,
                    player.getZ(),
                    8,
                    0.3, 0.1, 0.3,
                    0.02
            );

            serverWorld.spawnParticles(
                    ParticleTypes.LAVA,
                    player.getX(),
                    player.getY() + 0.1,
                    player.getZ(),
                    3,
                    0.2, 0.1, 0.2,
                    0.01
            );
        } catch (Exception e) {
            System.err.println("Error spawning particles for FloorIsLava: " + e.getMessage());
        }
    }

    private void playSound(ServerPlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return;

        try {
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
        } catch (Exception e) {
            System.err.println("Error playing sound for FloorIsLava: " + e.getMessage());
        }
    }
}