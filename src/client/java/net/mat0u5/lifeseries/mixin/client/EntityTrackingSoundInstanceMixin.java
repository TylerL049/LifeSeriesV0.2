
package net.mat0u5.lifeseries.mixin.client;

import net.minecraft.client.sound.EntityTrackingSoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityTrackingSoundInstance.class, priority = 2)
public abstract class EntityTrackingSoundInstanceMixin {
    @Inject(method = "canPlay", at = @At("HEAD"), cancellable = true)
    public void canPlay(CallbackInfoReturnable<Boolean> cir) {
        EntityTrackingSoundInstance instance = (EntityTrackingSoundInstance) (Object) this;
        if (instance.getId().getPath().contains("wildlife_trivia")) {
            cir.setReturnValue(true);
        }
    }
}
