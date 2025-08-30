package net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard.superpowers.superpower;

import net.mat0u5.lifeseries.network.NetworkHandlerServer;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard.superpowers.Superpowers;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard.superpowers.ToggleableSuperpower;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public class TripleJump extends ToggleableSuperpower {
    public boolean isInAir = false;
    private int onGroundTicks = 0;

    public TripleJump(ServerPlayerEntity player) {
        super(player);
    }

    @Override
    public Superpowers getSuperpower() {
        return Superpowers.TRIPLE_JUMP;
    }

    @Override
    public void tick() {
        ServerPlayerEntity player = getPlayer();
        if (!active || player == null) {
            onGroundTicks = 0;
            return;
        }

        if (!player.isOnGround()) {
            StatusEffectInstance jump = new StatusEffectInstance(StatusEffects.JUMP_BOOST, 219, 2, false, false, false);
            player.addStatusEffect(jump);
            onGroundTicks = 0;
        }
        else {
            player.removeStatusEffect(StatusEffects.JUMP_BOOST);
            onGroundTicks++;
        }

        if (!isInAir) {
            onGroundTicks = 0;
            return;
        }

        if (onGroundTicks >= 10) {
            isInAir = false;
            onGroundTicks = 0;
        }
    }

    @Override
    public void activate() {
        super.activate();
        ServerPlayerEntity player = getPlayer();
        if (player == null) return;
        player.playSoundToPlayer(SoundEvents.ENTITY_SLIME_JUMP, SoundCategory.MASTER, 1, 1);
        NetworkHandlerServer.sendVignette(player, -1);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        ServerPlayerEntity player = getPlayer();
        if (player == null) return;
        player.removeStatusEffect(StatusEffects.JUMP_BOOST);
        player.playSoundToPlayer(SoundEvents.ENTITY_SLIME_SQUISH, SoundCategory.MASTER, 1, 1);
        NetworkHandlerServer.sendVignette(player, 0);
    }
}
