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
    private static final int LEVITATION_DURATION_START = 5 * TICKS_PER_SECOND;
    private static final int LEVITATION_DURATION_INCREMENT = 5 * TICKS_PER_SECOND;
    private static final int LEVITATION_DURATION_CAP = 40 * TICKS_PER_SECOND;

    private static final int FIRST_RANDOM_DELAY_MIN = 7 * 60 * TICKS_PER_SECOND;
    private static final int FIRST_RANDOM_DELAY_MAX = 15 * 60 * TICKS_PER_SECOND;
    private static final int SUBSEQUENT_RANDOM_DELAY_MIN = 5 * 60 * TICKS_PER_SECOND;
    private static final int SUBSEQUENT_RANDOM_DELAY_MAX = 10 * 60 * TICKS_PER_SECOND;

    private static final int MAX_JUMP_BUILDUP = 35;

    private final Random random = new Random();

    private int tickCounter = 0;
    private int phaseStartTick = 0;
    private int nextEventTick = -1;
    private int levitationLevel = 0;
    private boolean levitating = false;
    private int currentLevitationDuration = LEVITATION_DURATION_START;
    private boolean firstLevitation = true;
    private static final int INITIAL_JUMP_DELAY = 60 * TICKS_PER_SECOND;

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
                if (tickCounter > INITIAL_JUMP_DELAY) {
                    double progress = (double) (tickCounter - INITIAL_JUMP_DELAY) / (JUMP_BUILDUP_TICKS - INITIAL_JUMP_DELAY);
                    int jumpLevel = 1 + (int) Math.floor(progress * (MAX_JUMP_BUILDUP - 1));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 40, jumpLevel - 1, false, false, false));
                }
            } else {
                if (levitating) {
                    if (tickCounter < phaseStartTick + currentLevitationDuration) {
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 40, levitationLevel - 1, false, false, false));
                    } else {
                        levitating = false;
                        int jumpLevel = 3 + random.nextInt(MAX_JUMP_BUILDUP - 2);
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 40, jumpLevel - 1, false, false, false));
                        phaseStartTick = tickCounter;

                        if (firstLevitation) {
                            nextEventTick = tickCounter + FIRST_RANDOM_DELAY_MIN + random.nextInt(FIRST_RANDOM_DELAY_MAX - FIRST_RANDOM_DELAY_MIN + 1);
                            firstLevitation = false;
                        } else {
                            nextEventTick = tickCounter + SUBSEQUENT_RANDOM_DELAY_MIN + random.nextInt(SUBSEQUENT_RANDOM_DELAY_MAX - SUBSEQUENT_RANDOM_DELAY_MIN + 1);
                        }

                        currentLevitationDuration = Math.min(currentLevitationDuration + LEVITATION_DURATION_INCREMENT, LEVITATION_DURATION_CAP);
                    }
                } else {
                    int currentJump = player.hasStatusEffect(StatusEffects.JUMP_BOOST)
                            ? player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier()
                            : MAX_JUMP_BUILDUP - 1;

                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 40, currentJump, false, false, false));

                    if (nextEventTick == -1) {
                        nextEventTick = tickCounter + FIRST_RANDOM_DELAY_MIN + random.nextInt(FIRST_RANDOM_DELAY_MAX - FIRST_RANDOM_DELAY_MIN + 1);
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
        firstLevitation = true;
    }
}