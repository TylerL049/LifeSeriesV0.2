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

import java.util.List;
import java.util.Set;

public class FloorIsLava extends Wildcard {

    private boolean active = false;
    private static final int TICKS_PER_SECOND = 20;

    // Use a static set instead of a TagKey
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
            Blocks.DEEPSLATE
    );

    @Override
    public Wildcards getType() {
        return Wildcards.FLOOR_IS_LAVA;
    }

    @Override
    public void activate() {
        this.active = true;
    }

    @Override
    public void deactivate() {
        this.active = false;
    }

    @Override
    public void tick() {
        if (!active) return;

        List<ServerPlayerEntity> players = PlayerUtils.getAllFunctioningPlayers();
        for (ServerPlayerEntity player : players) {
            if (player.isSpectator()) continue;

            BlockPos posBelow = player.getBlockPos().down(); // simple and works fine
            Block blockBelow = player.getWorld().getBlockState(posBelow).getBlock();

            if (NATURAL_BLOCKS.contains(blockBelow)) {
                applyWither(player);
                spawnLavaParticles(player);
            }
        }
    }

    private void applyWither(ServerPlayerEntity player) {
        StatusEffectInstance wither = new StatusEffectInstance(
                StatusEffects.WITHER,
                2 * TICKS_PER_SECOND, // 2 seconds
                1,
                true,  // show particles
                true,  // show icon
                true   // ambient
        );
        player.addStatusEffect(wither);
    }

    private void spawnLavaParticles(ServerPlayerEntity player) {
        if (player.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(
                    ParticleTypes.PORTAL,
                    player.getX(),
                    player.getY() + 0.5,
                    player.getZ(),
                    10,
                    0.3, 0.5, 0.3,
                    0.05
            );
        }
    }
}