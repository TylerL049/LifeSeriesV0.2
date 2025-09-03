package net.mat0u5.lifeseries.seasons.season.wildlife.wildcards;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;

public class HiddenStatusEffectInstance extends StatusEffectInstance {
    public HiddenStatusEffectInstance(StatusEffect type, int duration, int amplifier) {
        super(Registry.STATUS_EFFECT.getEntry(type), duration, amplifier, false, false, false);
    }
}