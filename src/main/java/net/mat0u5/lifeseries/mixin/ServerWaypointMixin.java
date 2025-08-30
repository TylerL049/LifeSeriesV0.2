package net.mat0u5.lifeseries.mixin;
//? if < 1.21.6 {
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = MinecraftServer.class)
public class ServerWaypointMixin {
    //Empty class to avoid mixin errors
}
//?} else {
/*import net.mat0u5.lifeseries.seasons.season.doublelife.DoubleLife;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.waypoint.ServerWaypoint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

import static net.mat0u5.lifeseries.Main.currentSeason;

@Mixin(value = ServerWaypoint.class, priority = 1)
public interface ServerWaypointMixin {

    @Inject(method = "cannotReceive", at = @At("HEAD"), cancellable = true)
    private static void cannotReceive(LivingEntity source, ServerPlayerEntity receiver, CallbackInfoReturnable<Boolean> cir) {
        if (source instanceof ServerPlayerEntity sender) {
            if (currentSeason instanceof DoubleLife doubleLife && DoubleLife.SOULMATE_LOCATOR_BAR) {
                UUID receiverSoulmateUUID = doubleLife.getSoulmateUUID(receiver.getUuid());
                if (sender.getUuid().equals(receiverSoulmateUUID)) {
                    cir.setReturnValue(false);
                }
                else {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}
*///?}