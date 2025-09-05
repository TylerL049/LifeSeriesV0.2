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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FloorIsLava extends Wildcard {
    private boolean active = false;
    private static final int TICKS_PER_SECOND = 20;
    private static final int EFFECT_CHECK_INTERVAL = 10; // Check every 10 ticks (0.5 seconds)
    private int tickCounter = 0;
    
    // Track when players last received the effect to prevent spam
    private final Map<UUID, Long> lastEffectTime = new HashMap<>();
    private static final long EFFECT_COOLDOWN = 1000; // 1 second cooldown in milliseconds
    
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
            Blocks.GRANITE
    );

    @Override
    public Wildcards getType() {
        return Wildcards.FLOOR_IS_LAVA;
    }

    @Override
    public void activate() {
        this.active = true;
        this.tickCounter = 0;
        this.lastEffectTime.clear();
    }

    @Override
    public void deactivate() {
        this.active = false;
        this.tickCounter = 0;
        this.lastEffectTime.clear();
    }

    @Override
    public void tick() {
        if (!active) return;
        
        tickCounter++;
        
        // Only check every EFFECT_CHECK_INTERVAL ticks for performance
        if (tickCounter % EFFECT_CHECK_INTERVAL != 0) return;

        List<ServerPlayerEntity> players = PlayerUtils.getAllFunctioningPlayers();
        if (players == null || players.isEmpty()) return;

        for (ServerPlayerEntity player : players) {
            if (player == null || player.isSpectator() || player.isCreative()) continue;
            
            checkPlayerPosition(player);
        }
    }
    
    private void checkPlayerPosition(ServerPlayerEntity player) {
        // Check if player is on ground (not flying/swimming/etc)
        if (!player.isOnGround()) return;
        
        // Get the block position directly below the player's feet
        BlockPos playerPos = player.getBlockPos();
        BlockPos posBelow = playerPos.down();
        
        // Also check the block the player is standing in (for slabs, carpets, etc.)
        BlockPos currentPos = playerPos;
        
        Block blockBelow = player.getWorld().getBlockState(posBelow).getBlock();
        Block blockCurrent = player.getWorld().getBlockState(currentPos).getBlock();
        
        // Check if either the block below or the block they're standing in is a natural block
        boolean standingOnNaturalBlock = NATURAL_BLOCKS.contains(blockBelow) || NATURAL_BLOCKS.contains(blockCurrent);
        
        if (standingOnNaturalBlock) {
            applyWitherEffect(player);
            spawnParticles(player);
            playSound(player);
        }
    }
    
    private void applyWitherEffect(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        long currentTime = System.currentTimeMillis();
        
        // Check cooldown to prevent effect spam
        if (lastEffectTime.containsKey(playerId)) {
            long lastTime = lastEffectTime.get(playerId);
            if (currentTime - lastTime < EFFECT_COOLDOWN) {
                return; // Still on cooldown
            }
        }
        
        // Apply Wither effect for 3 seconds (level 0 = Wither I)
        StatusEffectInstance witherEffect = new StatusEffectInstance(
                StatusEffects.WITHER,
                3 * TICKS_PER_SECOND, // 3 seconds
                0, // Level 0 (Wither I)
                false, // Not ambient
                true,  // Show particles
                true   // Show icon
        );
        
        player.addStatusEffect(witherEffect);
        lastEffectTime.put(playerId, currentTime);
    }
    
    private void spawnParticles(ServerPlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return;
        
        // Spawn particles around the player's feet
        serverWorld.spawnParticles(
                ParticleTypes.SMOKE,
                player.getX(),
                player.getY() + 0.1, // Just above feet
                player.getZ(),
                15, // Number of particles
                0.5, // X spread
                0.1, // Y spread
                0.5, // Z spread
                0.02 // Speed
        );
        
        // Also spawn some flame particles for dramatic effect
        serverWorld.spawnParticles(
                ParticleTypes.FLAME,
                player.getX(),
                player.getY() + 0.1,
                player.getZ(),
                5,
                0.3, 0.1, 0.3,
                0.01
        );
    }
    
    private void playSound(ServerPlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return;
        
        // Play a burning sound effect
        serverWorld.playSound(
                null, // No specific player (everyone nearby can hear)
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.BLOCK_FIRE_AMBIENT,
                SoundCategory.PLAYERS,
                0.5F, // Volume
                1.2F  // Pitch
        );
    }
}