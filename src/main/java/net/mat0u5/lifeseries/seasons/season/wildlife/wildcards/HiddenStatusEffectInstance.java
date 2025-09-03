package net.mat0u5.lifeseries.seasons.season.wildlife.wildcards;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;

public class HiddenStatusEffectInstance extends StatusEffectInstance {

    public HiddenStatusEffectInstance(StatusEffect type, int duration, int amplifier) {
        super(type, duration, amplifier, false, false, false);
    }

    @Override
    public boolean shouldShowIcon() {
        return false;
    }
}