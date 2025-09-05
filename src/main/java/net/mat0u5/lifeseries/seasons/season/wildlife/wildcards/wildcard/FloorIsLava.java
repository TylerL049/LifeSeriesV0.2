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
import net.minecraft.registry.tag.TagKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import java.util.List;

public class FloorIsLava extends Wildcard {

    private boolean active = false;
    private static final int TICKS_PER_SECOND = 20;

    /** Reference the custom tag: lifeseries:natural_blocks */
    private static final TagKey<Block> NATURAL_BLOCKS_TAG = TagKey.of(
            RegistryKeys.BLOCK,
            RegistryKey.of(RegistryKeys.BLOCK, new Identifier("lifeseries", "natural_blocks"))
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

            // BlockPos requires integers
            BlockPos posBelow = new BlockPos(
                    Math.floor(player.getX()),
                    Math.floor(player.getY() - 0.1),
                    Math.floor(player.getZ())
            );

            // Check block against tag
            if (player.getWorld().getBlockState(posBelow).isIn(NATURAL_BLOCKS_TAG)) {
                applyWither(player);
                spawnLavaParticles(player);
            }
        }
    }

    /** Apply a short Wither effect that refreshes every tick */
    private void applyWither(ServerPlayerEntity player) {
        StatusEffectInstance wither = new StatusEffectInstance(
                StatusEffects.WITHER,
                5,  // 5 ticks = 0.25s
                1,
                true,
                true,
                true
        );
        player.addStatusEffect(wither);
    }

    /** Spawn purple portal particles above the player */
    private void spawnLavaParticles(ServerPlayerEntity player) {
        if (player.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(
                    ParticleTypes.PORTAL,
                    player.getX(),
                    player.getY() + 1.0,
                    player.getZ(),
                    10,      // particle count
                    0.3, 0.5, 0.3, // x, y, z offsets
                    0.05     // speed
            );
        }
    }
}