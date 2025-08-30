package net.mat0u5.lifeseries.mixin.client;

import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.MainClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Entity.class, priority = 2)
public class EntityMixin {

    @Inject(method = "getAir", at = @At("RETURN"), cancellable = true)
    public void getAir(CallbackInfoReturnable<Integer> cir) {
        if (Main.isLogicalSide()) return;
        if (System.currentTimeMillis() - MainClient.snailAirTimestamp > 5000) return;
        if (MainClient.snailAir >= 300) return;

        Entity entity = (Entity) (Object) this;
        if (entity instanceof PlayerEntity player && !player.hasStatusEffect(StatusEffects.WATER_BREATHING)) {
            int initialAir = cir.getReturnValue();
            if (MainClient.snailAir < initialAir) {
                cir.setReturnValue(MainClient.snailAir);
            }
        }
    }
}
