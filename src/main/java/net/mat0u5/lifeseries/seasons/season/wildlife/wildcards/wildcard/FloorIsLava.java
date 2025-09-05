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

import java.util.List;
import java.util.Set;

public class FloorIsLava extends Wildcard {

    private boolean active = false;
    private static final int TICKS_PER_SECOND = 20;

    // Blocks considered "natural" and unsafe
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

        // Check all active players
        List<ServerPlayerEntity> players = PlayerUtils.getAllFunctioningPlayers();
        for (ServerPlayerEntity player : players) {
            if (player.isSpectator()) continue;

            BlockPos posBelow = player.getBlockPos().down(); // Block under the player's feet
            Block blockBelow = player.getWorld().getBlockState(posBelow).getBlock();

            if (NATURAL_BLOCKS.contains(blockBelow)) {
                // Apply Wither effect for slow damage
                StatusEffectInstance effect = new StatusEffectInstance(StatusEffects.WITHER, TICKS_PER_SECOND, 0, false, false, false);
                player.addStatusEffect(effect);
            }
        }
    }
}