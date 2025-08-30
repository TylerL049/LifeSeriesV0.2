package net.mat0u5.lifeseries.mixin.client;

import net.mat0u5.lifeseries.MainClient;
import net.mat0u5.lifeseries.render.ClientRenderer;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard.TimeDilation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.tick.TickManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MinecraftClient.class, priority = 1)
public abstract class MinecraftClientMixin {

    @Inject(method = "getTargetMillisPerTick", at = @At("HEAD"), cancellable = true)
    private void getTargetMillisPerTick(float millis, CallbackInfoReturnable<Float> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null) {
            TickManager tickManager = client.world.getTickManager();
            if (MainClient.TIME_DILATION_TIMESTAMP != 0) {
                long timeSinceDilationActivate = System.currentTimeMillis() - MainClient.TIME_DILATION_TIMESTAMP;
                if (timeSinceDilationActivate < 14000) {
                    if (timeSinceDilationActivate < 1000) {
                        float tickRate = 1000/(1-(timeSinceDilationActivate / 4050.0f));
                        cir.setReturnValue(tickRate);
                        ClientRenderer.isGameFullyFrozen = false;
                        return;
                    }
                    if (timeSinceDilationActivate <= 10000) {
                        cir.setReturnValue(500000f);
                        ClientRenderer.isGameFullyFrozen = true;
                        return;
                    }
                    float tickRate = 1000/((timeSinceDilationActivate-10000) / 4050.0f);
                    cir.setReturnValue(tickRate);
                    ClientRenderer.isGameFullyFrozen = false;
                    return;
                }
            }
            if (tickManager.shouldTick()) {
                float mspt = Math.max(TimeDilation.MIN_PLAYER_MSPT, tickManager.getMillisPerTick());
                cir.setReturnValue(mspt);
                ClientRenderer.isGameFullyFrozen = false;
                return;
            }
        }
        cir.setReturnValue(millis);
        ClientRenderer.isGameFullyFrozen = false;
    }
}
