package net.mat0u5.lifeseries.mixin;

import net.mat0u5.lifeseries.Main;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.mat0u5.lifeseries.Main.blacklist;

//? if >= 1.21.2
/*import net.minecraft.server.world.ServerWorld;*/

@Mixin(value = StatusEffect.class, priority = 1)
public class StatusEffectMixin {
    @Inject(method = "applyInstantEffect", at = @At("HEAD"), cancellable = true)
    //? if <= 1.21 {
    public void applyInstantEffect(Entity source, Entity attacker, LivingEntity target, int amplifier, double proximity, CallbackInfo ci) {
    //?} else {
    /*public void applyInstantEffect(ServerWorld world, Entity effectEntity, Entity attacker, LivingEntity target, int amplifier, double proximity, CallbackInfo ci) {
    *///?}
        if (!Main.isLogicalSide()) return;
        StatusEffect effect = (StatusEffect) (Object) this;
        if (target instanceof ServerPlayerEntity) {
            if (blacklist.getBannedEffects().contains(Registries.STATUS_EFFECT.getEntry(effect))) {
                ci.cancel();
            }
        }
    }
    @Inject(method = "applyUpdateEffect", at = @At("HEAD"), cancellable = true)
    //? if <= 1.21 {
    public void applyInstantEffect(LivingEntity entity, int amplifier, CallbackInfoReturnable<Boolean> cir) {
    //?} else {
    /*public void applyInstantEffect(ServerWorld world, LivingEntity entity, int amplifier, CallbackInfoReturnable<Boolean> cir) {
    *///?}
        if (!Main.isLogicalSide()) return;
        StatusEffect effect = (StatusEffect) (Object) this;
        if (entity instanceof ServerPlayerEntity) {
            if (blacklist.getBannedEffects().contains(Registries.STATUS_EFFECT.getEntry(effect))) {
                cir.setReturnValue(false);
            }
        }
    }
}
