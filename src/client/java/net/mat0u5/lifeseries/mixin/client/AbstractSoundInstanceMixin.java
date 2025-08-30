package net.mat0u5.lifeseries.mixin.client;

import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.EntityTrackingSoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractSoundInstance.class, priority = 1)
public class AbstractSoundInstanceMixin {

    @Inject(method = "isRepeatable", at = @At("HEAD"), cancellable = true)
    private void isRepeatable(CallbackInfoReturnable<Boolean> cir) {
        AbstractSoundInstance soundInstance = (AbstractSoundInstance) (Object) this;
        if (soundInstance instanceof EntityTrackingSoundInstance entityTrackingSound) {
            if (entityTrackingSound.getId().getPath().equalsIgnoreCase("wildlife_trivia_suspense")) {
                cir.setReturnValue(true);
            }
        }
    }
}
