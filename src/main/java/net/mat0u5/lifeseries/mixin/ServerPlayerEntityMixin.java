package net.mat0u5.lifeseries.mixin;

import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.entity.fakeplayer.FakePlayer;
import net.mat0u5.lifeseries.seasons.other.WatcherManager;
import net.mat0u5.lifeseries.seasons.season.doublelife.DoubleLife;
import net.mat0u5.lifeseries.utils.other.OtherUtils;
import net.mat0u5.lifeseries.utils.other.TaskScheduler;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.TeleportTarget;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.UUID;

import static net.mat0u5.lifeseries.Main.blacklist;
import static net.mat0u5.lifeseries.Main.currentSeason;

@Mixin(value = ServerPlayerEntity.class, priority = 1)
public class ServerPlayerEntityMixin {
    @Inject(method = "getRespawnTarget", at = @At("HEAD"))
    private void getRespawnTarget(boolean alive, TeleportTarget.PostDimensionTransition postDimensionTransition, CallbackInfoReturnable<TeleportTarget> cir) {
        if (!Main.isLogicalSide()) return;
        ServerPlayerEntity player = ls$get();
        if (WatcherManager.isWatcher(player)) return;
        UUID uuid = player.getUuid();
        currentSeason.onPlayerRespawn(Objects.requireNonNull(PlayerUtils.getPlayer(uuid)));
        TaskScheduler.scheduleTask(1, () -> currentSeason.postPlayerRespawn(Objects.requireNonNull(PlayerUtils.getPlayer(uuid))));
    }

    @Inject(method = "openHandledScreen", at = @At("HEAD"))
    private void onInventoryOpen(@Nullable NamedScreenHandlerFactory factory, CallbackInfoReturnable<OptionalInt> cir) {
        if (!Main.isLogicalSide()) return;
        ServerPlayerEntity player = ls$get();
        if (blacklist == null) return;
        
        TaskScheduler.scheduleTask(1, () -> {
            player.currentScreenHandler.getStacks().forEach(itemStack -> blacklist.processItemStack(player, itemStack));
            PlayerUtils.updatePlayerInventory(player);
        });
    }

    @Inject(method = "sendMessageToClient", at = @At("HEAD"), cancellable = true)
    private void sendMessageToClient(Text message, boolean overlay, CallbackInfo ci) {
        ServerPlayerEntity player = ls$get();
        if (player instanceof FakePlayer) {
            ci.cancel();
        }
    }

    @Inject(method = "acceptsMessage", at = @At("HEAD"), cancellable = true)
    private void acceptsMessage(boolean overlay, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerEntity player = ls$get();
        if (player instanceof FakePlayer) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "acceptsChatMessage", at = @At("HEAD"), cancellable = true)
    private void acceptsChatMessage(CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerEntity player = ls$get();
        if (player instanceof FakePlayer) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "attack", at = @At("HEAD"))
    private void onAttackEntity(Entity target, CallbackInfo ci) {
        ServerPlayerEntity player = ls$get();
        currentSeason.onUpdatedInventory(player);
    }

    @Inject(method = "onStatusEffectApplied", at = @At("TAIL"))
    private void onStatusEffectApplied(StatusEffectInstance effect, Entity source, CallbackInfo ci) {
        ls$onUpdatedEffects(effect, true);
    }

    //? if <= 1.21 {
    @Inject(method = "onStatusEffectRemoved", at = @At("TAIL"))
    private void onStatusEffectRemoved(StatusEffectInstance effect, CallbackInfo ci) {
        ls$onUpdatedEffects(effect, false);
    }
    //?} else {
    /*@Inject(method = "onStatusEffectsRemoved", at = @At("TAIL"))
    private void onStatusEffectRemoved(Collection<StatusEffectInstance> effects, CallbackInfo ci) {
        for (StatusEffectInstance effect : effects) {
            ls$onUpdatedEffects(effect, false);
        }
    }
    *///?}

    @Inject(method = "onStatusEffectUpgraded", at = @At("TAIL"))
    private void onStatusEffectUpgraded(StatusEffectInstance effect, boolean reapplyEffect, Entity source, CallbackInfo ci) {
        ls$onUpdatedEffects(effect, true);
    }


    @Unique
    private boolean ls$processing = false;

    @Unique
    private void ls$onUpdatedEffects(StatusEffectInstance effect, boolean add) {
        if (ls$processing) {
            return;
        }
        ServerPlayerEntity player = ls$get();
        ls$processing = true;
        try {
            if (currentSeason instanceof DoubleLife doubleLife) {
                doubleLife.syncStatusEffectsFrom(player, effect, add);
            }
        }finally {
            ls$processing = false;
        }
    }
    
    @Unique
    private ServerPlayerEntity ls$get() {
        return (ServerPlayerEntity) (Object) this;
    }
}
