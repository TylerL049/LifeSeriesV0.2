package net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.WildcardManager;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;

public class GravityManipulation extends Wildcard {

    private static final int TICKS_PER_SECOND = 20;
    private static final int SESSION_TICKS = 2 * 60 * 60 * TICKS_PER_SECOND;
    private static final int LEVITATION_START_TICKS = SESSION_TICKS - (20 * 60 * TICKS_PER_SECOND);
    private static final int MAX_JUMP_LEVEL = 30;
    private static final int MAX_LEVITATION_LEVEL = 10;

    private int tickCounter = 0;

    @Override
    public Wildcards getType() {
        return Wildcards.GRAVITY_MANIPULATION;
    }

    @Override
    public void tick() {
        tickCounter++;

        double jumpProgress = Math.min(1.0, (double) tickCounter / LEVITATION_START_TICKS);
        int jumpLevel = 1 + (int) Math.floor(jumpProgress * (MAX_JUMP_LEVEL - 1));

        double levitationProgress = Math.max(0, (tickCounter - LEVITATION_START_TICKS) / (double)(SESSION_TICKS - LEVITATION_START_TICKS));
        int levitationLevel = (int) Math.floor(levitationProgress * MAX_LEVITATION_LEVEL);

        for (ServerPlayerEntity player : PlayerUtils.getAllFunctioningPlayers()) {
            if (player.isSpectator()) continue;

            // Apply Jump Boost
            StatusEffectInstance jumpEffect = new StatusEffectInstance(StatusEffects.JUMP_BOOST, 40, jumpLevel - 1, false, false, false);
            player.addStatusEffect(jumpEffect);

            // Apply Levitation after LEVITATION_START_TICKS
            if (tickCounter >= LEVITATION_START_TICKS) {
                StatusEffectInstance levitationEffect = new StatusEffectInstance(StatusEffects.LEVITATION, 40, levitationLevel, false, false, false);
                player.addStatusEffect(levitationEffect);
            }
        }
    }

    @Override
    public void stop() {
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            player.removeStatusEffect(StatusEffects.JUMP_BOOST);
            player.removeStatusEffect(StatusEffects.LEVITATION);
        }
        tickCounter = 0;
    }
}