package net.mat0u5.lifeseries.mixin.client;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
//? if >= 1.21.2 {
/*import net.mat0u5.lifeseries.utils.ClientUtils;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
*///?}

@Mixin(value = PlayerEntity.class, priority = 2)
public class PlayerEntityMixin {
    //? if >= 1.21.2 {
    /*@Inject(method = "canGlide", at = @At("HEAD"), cancellable = true)
    protected void canGlide(CallbackInfoReturnable<Boolean> cir) {
        if (ClientUtils.shouldPreventGliding()) {
            cir.setReturnValue(false);
        }
    }
    *///?}
}
