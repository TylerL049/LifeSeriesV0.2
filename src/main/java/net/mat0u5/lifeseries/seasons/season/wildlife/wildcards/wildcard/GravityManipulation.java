package net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Random;

public class GravityManipulation extends Wildcard {

    private static final int TICKS_PER_SECOND = 20;
    private static final int JUMP_BUILDUP_TICKS = 45 * 60 * TICKS_PER_SECOND;
    private static final int LEVITATION_DURATION_START = 30 * TICKS_PER_SECOND;
    private static final int LEVITATION_DURATION_INCREMENT = 60 * TICKS_PER_SECOND;
    private static final int RANDOM_DELAY_MIN = 7 * 60 * TICKS_PER_SECOND;
    private static final int RANDOM_DELAY_MAX = 15 * 60 * TICKS_PER_SECOND;
    private static final int MAX_JUMP_BUILDUP = 20;

    private final Random random = new Random();

    private int tickCounter = 0;
    private int phaseStartTick = 0;
    private int nextEventTick = -1;
    private int levitationLevel = 0;
    private boolean levitating = false;
    private int currentLevitationDuration = LEVITATION_DURATION_START;

    @Override
    public Wildcards getType() {
        return Wildcards.GRAVITY_MANIPULATION;
    }

    @Override
    public void tick() {
        tickCounter++;

        for (ServerPlayerEntity player : PlayerUtils.getAllFunctioningPlayers()) {
            if (player.isSpectator()) continue;

            if (tickCounter <= JUMP_BUILDUP_TICKS) {
                double progress = (double) tickCounter / JUMP_BUILDUP_TICKS;
                int jumpLevel = 1 + (int) Math.floor(progress * (MAX_JUMP_BUILDUP - 1));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 40, jumpLevel - 1, false, false, false));
            } else {
                if (levitating) {
                    if (tickCounter < phaseStartTick + currentLevitationDuration) {
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 40, levitationLevel - 1, false, false, false));
                    } else {
                        levitating = false;
                        int jumpLevel = 3 + random.nextInt(28);
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 40, jumpLevel - 1, false, false, false));
                        phaseStartTick = tickCounter;
                        nextEventTick = tickCounter + RANDOM_DELAY_MIN + random.nextInt(RANDOM_DELAY_MAX - RANDOM_DELAY_MIN + 1);
                        currentLevitationDuration += LEVITATION_DURATION_INCREMENT;
                    }
                } else {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 40, player.hasStatusEffect(StatusEffects.JUMP_BOOST)
                            ? player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier()
                            : MAX_JUMP_BUILDUP - 1, false, false, false));

                    if (nextEventTick == -1) {
                        nextEventTick = tickCounter + RANDOM_DELAY_MIN + random.nextInt(RANDOM_DELAY_MAX - RANDOM_DELAY_MIN + 1);
                        phaseStartTick = tickCounter;
                    } else if (tickCounter >= nextEventTick) {
                        levitating = true;
                        levitationLevel++;
                        phaseStartTick = tickCounter;
                    }
                }
            }
        }
    }

    @Override
    public void deactivate() {
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            player.removeStatusEffect(StatusEffects.JUMP_BOOST);
            player.removeStatusEffect(StatusEffects.LEVITATION);
        }
        tickCounter = 0;
        phaseStartTick = 0;
        nextEventTick = -1;
        levitationLevel = 0;
        levitating = false;
        currentLevitationDuration = LEVITATION_DURATION_START;
    }
}