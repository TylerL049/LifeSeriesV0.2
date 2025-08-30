package net.mat0u5.lifeseries.mixin;

import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.seasons.session.SessionTranscript;
import net.mat0u5.lifeseries.utils.other.TextUtils;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(value = ServerCommandSource.class, priority = 1)
public class ServerCommandSourceMixin {

    @Inject(method = "sendFeedback", at = @At("HEAD"))
    public void sendFeedback(Supplier<Text> feedbackSupplier, boolean broadcastToOps, CallbackInfo ci) {
        if (!Main.isLogicalSide()) return;
        if (!broadcastToOps) return;
        Text text = feedbackSupplier.get();
        String sourceStr = "null";
        ServerCommandSource source = (ServerCommandSource) (Object) this;
        if (source.isSilent()) return;
        if (!source.isExecutedByPlayer()) {
            sourceStr = "console";
        }
        if (source.getPlayer() != null) {
            sourceStr = source.getPlayer().getName().getString();
        }
        SessionTranscript.addMessageWithTime(TextUtils.formatString("[COMMAND (source: {})] {}", sourceStr, text.getString()));
    }
}
