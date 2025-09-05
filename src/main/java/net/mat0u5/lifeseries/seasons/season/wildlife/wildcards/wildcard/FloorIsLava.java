package net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.tag.TagKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.List;

public class FloorIsLava extends Wildcard {

    private boolean active = false;
    private static final int TICKS_PER_SECOND = 20;

    /** Reference the custom tag we created: lifeseries:natural_blocks */
    private static final TagKey<Block> NATURAL_BLOCKS_TAG = TagKey.of(
            RegistryKeys.BLOCK,
            Registry.makeKey(RegistryKeys.BLOCK, new Identifier("lifeseries", "natural_blocks"))
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

            // Check the block just below the player's feet
            BlockPos posBelow = new BlockPos(player.getX(), player.getY() - 0.1, player.getZ());
            Block blockBelow = player.getWorld().getBlockState(posBelow).getBlock();

            if (blockBelow != null && blockBelow.isIn(NATURAL_BLOCKS_TAG)) {
                applyWither(player);
                spawnLavaParticles(player);
            }
        }
    }

    /** Apply a short Wither effect that refreshes every tick */
    private void applyWither(ServerPlayerEntity player) {
        StatusEffectInstance wither = new StatusEffectInstance(
                StatusEffects.WITHER,
                5,      // 5 ticks = 0.25s, refreshes each tick
                1,
                true,   // show particles
                true,   // show icon
                true    // ambient effect
        );
        player.addStatusEffect(wither);
    }

    /** Spawn purple portal particles above the player */
    private void spawnLavaParticles(ServerPlayerEntity player) {
        if (player.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(
                    ParticleTypes.PORTAL,
                    player.getX(),
                    player.getY() + 1.0,  // slightly above the player
                    player.getZ(),
                    10,      // number of particles
                    0.3, 0.5, 0.3,  // x, y, z offsets
                    0.05     // speed
            );
        }
    }
}