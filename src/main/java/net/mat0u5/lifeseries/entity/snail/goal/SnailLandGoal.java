package net.mat0u5.lifeseries.entity.snail.goal;

import net.mat0u5.lifeseries.entity.snail.Snail;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public final class SnailLandGoal extends Goal {

    @NotNull
    private final Snail mob;
    private int noTargetTicks;

    public SnailLandGoal(@NotNull Snail mob) {
        this.mob = mob;
    }

    @Override
    public boolean canStart() {
        if (!mob.flying || mob.gliding) {
            return false;
        }

        ServerPlayerEntity boundPlayer = mob.getBoundPlayer();
        if (boundPlayer == null) {
            noTargetTicks++;
        } else {
            noTargetTicks = 0;
        }

        if (noTargetTicks >= 40) {
            return true;
        }

        if (boundPlayer == null) {
            return false;
        }

        boolean isMobAboveTarget = mob.getY() - boundPlayer.getY() > 0.0D;

        if (!isMobAboveTarget) {
            return false;
        }

        if (!mob.isValidBlockOnGround()) {
            return false;
        }

        return mob.canPathToPlayerFromGround(false);
    }

    @Override
    public boolean shouldContinue() {
        if (!mob.isValidBlockOnGround()) {
            return false;
        }
        return mob.getDistanceToGroundBlock() > 1.5D;
    }

    @Override
    public void tick() {
        land();
    }

    @Override
    public void start() {
        mob.landing = true;
        mob.flying = false;
        mob.gliding = false;
    }

    @Override
    public void stop() {
        mob.landing = false;
        mob.gliding = false;
        mob.flying = false;
        mob.updateNavigation();
        mob.updateMoveControl();

        mob.playStopFlyAnimation();
    }

    private void land() {
        mob.setVelocity(0.0D, -0.15D, 0.0D);
    }
}