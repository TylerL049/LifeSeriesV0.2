package net.mat0u5.lifeseries.mixin;

import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.events.Events;
import net.mat0u5.lifeseries.seasons.other.WatcherManager;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard.superpowers.Superpowers;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard.superpowers.SuperpowersWildcard;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard.superpowers.superpower.WindCharge;
import net.mat0u5.lifeseries.utils.world.ItemStackUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.mat0u5.lifeseries.Main.blacklist;
import static net.mat0u5.lifeseries.Main.currentSeason;

//? if = 1.21.2 {
/*import java.util.function.DoubleSupplier;
import java.util.function.Predicate;
*///?}
//? if >= 1.21.2
/*import net.minecraft.entity.mob.CreakingEntity;*/

@Mixin(value = LivingEntity.class, priority = 1)
public abstract class LivingEntityMixin {
    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    private void onHealHead(float amount, CallbackInfo info) {
        if (!Main.isLogicalSide()) return;
        if (!currentSeason.NO_HEALING) return;

        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof ServerPlayerEntity) {
            info.cancel();
        }
    }

    @Inject(method = "heal", at = @At("TAIL"))
    private void onHeal(float amount, CallbackInfo info) {
        if (!Main.isLogicalSide()) return;
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof ServerPlayerEntity player) {
            if (WatcherManager.isWatcher(player)) return;
            currentSeason.onPlayerHeal(player, amount);
        }
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    //? if <= 1.21 {
    public void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
     //?} else
    /*public void damage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {*/
        if (!Main.isLogicalSide()) return;

        LivingEntity entity = (LivingEntity) (Object) this;
        //? if >= 1.21.2 {
        /*if (entity instanceof CreakingEntity creaking) {
            creaking.hurtTime = 20;
        }
        *///?}

        ItemStack weapon = source.getWeaponStack();
        if (amount <= WindCharge.MAX_MACE_DAMAGE) return;
        if (weapon == null) return;
        if (weapon.isEmpty()) return;
        if (!weapon.isOf(Items.MACE)) return;
        if (!ItemStackUtils.hasCustomComponentEntry(weapon, "WindChargeSuperpower")) return;
        //? if <= 1.21 {
        cir.setReturnValue(entity.damage(source, WindCharge.MAX_MACE_DAMAGE));
         //?} else
        /*cir.setReturnValue(entity.damage(world, source, WindCharge.MAX_MACE_DAMAGE));*/
    }

    //? if = 1.21.2 {
    /*@Inject(method = "isEntityLookingAtMe", at = @At("HEAD"), cancellable = true)
    public void isEntityLookingAtMe(LivingEntity entity, double d, boolean bl, boolean visualShape
            , Predicate<LivingEntity> predicate, DoubleSupplier[] entityYChecks, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity me = (LivingEntity) (Object) this;
        if (me instanceof CreakingEntity creaking) {
            if (creaking.isTeammate(entity)) cir.setReturnValue(false);
        }
    }
    *///?}

    @Inject(method = "addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    public void addStatusEffect(StatusEffectInstance effect, Entity source, CallbackInfoReturnable<Boolean> cir) {
        if (!Main.isLogicalSide()) return;
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof ServerPlayerEntity) {
            if (!effect.isAmbient() && !effect.shouldShowIcon() && !effect.shouldShowParticles()) return;
            if (blacklist.getBannedEffects().contains(effect.getEffectType())) {
                cir.setReturnValue(false);
            }
        }
    }

    /*
        Superpowers
     */

    @Unique
    private DamageSource ls$lastDamageSource;


    //? if <= 1.21 {
    @Inject(method = "damage", at = @At("HEAD"))
    private void captureDamageSource(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        this.ls$lastDamageSource = source;
    }
    //?} else {
    /*@Inject(method = "damage", at = @At("HEAD"))
    private void captureDamageSource(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        this.ls$lastDamageSource = source;
    }
    *///?}

    @ModifyArg(
            method = "damage",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;takeKnockback(DDD)V"),
            index = 0
    )
    private double modifyKnockback(double strength) {
        if (!Main.isLogicalSide()) return strength;
        if (ls$lastDamageSource == null) return strength;

        DamageSource source = ls$lastDamageSource;
        if (source.getAttacker() instanceof ServerPlayerEntity attacker &&
                source.getType() == attacker.getDamageSources().playerAttack(attacker).getType() &&
                SuperpowersWildcard.hasActivatedPower(attacker, Superpowers.SUPER_PUNCH)) {
            return 3;
        }
        return strength;
    }

    @Inject(method = "drop", at = @At("HEAD"))
    private void onDrop(ServerWorld world, DamageSource damageSource, CallbackInfo ci) {
        if (!Main.isLogicalSide()) return;
        Events.onEntityDropItems((LivingEntity) (Object) this, damageSource);
    }
}
