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

public class FloorIsLava extends Wildcard {
    private boolean active = false;
    private static final int TICKS_PER_SECOND = 20;
    private static final int EFFECT_CHECK_INTERVAL = 20; // Check every 20 ticks (1 second) for better performance
    private int tickCounter = 0;
    
    // Track when players last received the effect to prevent spam
    private final Map<UUID, Integer> lastEffectTick = new HashMap<>();
    private static final int EFFECT_COOLDOWN_TICKS = 40; // 2 seconds cooldown in ticks
    
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
        return Wildcards.FLOOR_IS_LAVA;
    }

    @Override
    public void activate() {
        this.active = true;
        this.tickCounter = 0;
        this.lastEffectTick.clear();
        
        // Send activation message to all players for debugging
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
        this.active = false;
        this.tickCounter = 0;
        this.lastEffectTick.clear();
        
        // Send deactivation message to all players for debugging
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
        System.out.println("FloorIsLava: Checking position for player " + player.getName().getString() + " - CONSOLE DEBUG");
        
        // Get player's current position
        BlockPos playerPos = player.getBlockPos();
        
        // Check multiple positions around the player's feet
        BlockPos[] positionsToCheck = {
            playerPos.down(),           // Directly below
            playerPos.down(2),          // Two blocks below (in case standing on slab)
            playerPos,                  // Current position (in case standing in tall grass, etc.)
            playerPos.add(0, -1, 0)     // Alternative way to get position below
        };
        
        boolean standingOnNaturalBlock = false;
        Block foundBlock = null;
        
        for (BlockPos pos : positionsToCheck) {
            Block block = player.getWorld().getBlockState(pos).getBlock();
            System.out.println("FloorIsLava: Checking block at " + pos + ": " + block.getName().getString() + " - CONSOLE DEBUG");
            if (NATURAL_BLOCKS.contains(block)) {
                standingOnNaturalBlock = true;
                foundBlock = block;
                System.out.println("FloorIsLava: Found natural block: " + block.getName().getString() + " - CONSOLE DEBUG");
                break;
            }
        }
        
        // Debug: Send message to player about what block they're standing on
        Block blockBelow = player.getWorld().getBlockState(playerPos.down()).getBlock();
        player.sendMessage(Text.literal("§7Debug: Standing on " + blockBelow.getName().getString() + 
            " | Natural: " + standingOnNaturalBlock + " | On Ground: " + player.isOnGround()), true);
        
        if (standingOnNaturalBlock) {
            System.out.println("FloorIsLava: Applying effects to " + player.getName().getString() + " - CONSOLE DEBUG");
            applyWitherEffect(player, foundBlock);
            spawnParticles(player);
            playSound(player);
        } else {
            System.out.println("FloorIsLava: Player " + player.getName().getString() + " not on natural block - CONSOLE DEBUG");
        }
    }
    
    private void applyWitherEffect(ServerPlayerEntity player, Block block) {
        UUID playerId = player.getUuid();
        
        // Check cooldown to prevent effect spam
        if (lastEffectTick.containsKey(playerId)) {
            int lastTick = lastEffectTick.get(playerId);
            if (tickCounter - lastTick < EFFECT_COOLDOWN_TICKS) {
                return; // Still on cooldown
            }
        }
        
        // Apply Wither effect for 4 seconds (level 0 = Wither I)
        StatusEffectInstance witherEffect = new StatusEffectInstance(
                StatusEffects.WITHER,
                4 * TICKS_PER_SECOND, // 4 seconds
                0, // Level 0 (Wither I)
                false, // Not ambient
                true,  // Show particles
                true   // Show icon
        );
        
        boolean applied = player.addStatusEffect(witherEffect);
        
        if (applied) {
            lastEffectTick.put(playerId, tickCounter);
            
            // Send message to player for debugging
            player.sendMessage(Text.literal("§c§lOUCH! The " + block.getName().getString() + 
                " burns your feet!"), true);
        }
    }
    
    private void spawnParticles(ServerPlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return;
        
        try {
            // Spawn smoke particles around the player's feet
            serverWorld.spawnParticles(
                    ParticleTypes.SMOKE,
                    player.getX(),
                    player.getY() + 0.1, // Just above feet
                    player.getZ(),
                    20, // Number of particles
                    0.5, // X spread
                    0.2, // Y spread  
                    0.5, // Z spread
                    0.05 // Speed
            );
            
            // Spawn flame particles for dramatic effect
            serverWorld.spawnParticles(
                    ParticleTypes.FLAME,
                    player.getX(),
                    player.getY() + 0.1,
                    player.getZ(),
                    8,
                    0.3, 0.1, 0.3,
                    0.02
            );
            
            // Spawn lava particles for extra effect
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
            // Log error but don't crash
            System.err.println("Error spawning particles for FloorIsLava: " + e.getMessage());
        }
    }
    
    private void playSound(ServerPlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return;
        
        try {
            // Play multiple sound effects for better feedback
            serverWorld.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.BLOCK_FIRE_AMBIENT,
                    SoundCategory.PLAYERS,
                    0.7F, // Volume
                    1.0F + (float)(Math.random() * 0.4 - 0.2) // Random pitch variation
            );
            
            // Also play a sizzling sound
            serverWorld.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.BLOCK_LAVA_EXTINGUISH,
                    SoundCategory.PLAYERS,
                    0.3F, // Lower volume
                    1.5F  // Higher pitch
            );
        } catch (Exception e) {
            // Log error but don't crash  
            System.err.println("Error playing sound for FloorIsLava: " + e.getMessage());
        }
    }
}