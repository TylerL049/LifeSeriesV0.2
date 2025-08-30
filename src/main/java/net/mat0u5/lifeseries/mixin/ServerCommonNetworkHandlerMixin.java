package net.mat0u5.lifeseries.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mat0u5.lifeseries.entity.fakeplayer.FakeClientConnection;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if <= 1.21.5
import net.minecraft.network.PacketCallbacks;
//? if >= 1.21.6
/*import io.netty.channel.ChannelFutureListener;*/

@Mixin(value = ServerCommonNetworkHandler.class, priority = 1)
public class ServerCommonNetworkHandlerMixin {

    @Final
    @Shadow
    protected ClientConnection connection;

    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
    public void sendPacket(Packet<?> packet, CallbackInfo ci) {
        if (connection instanceof FakeClientConnection) {
            ci.cancel();
        }
    }

    //? if <= 1.21.5 {
    @WrapOperation(
            method = "send",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;Z)V")
    )
    public void send(ClientConnection instance, Packet<?> packet, PacketCallbacks callbacks, boolean flush, Operation<Void> original) {
        if (connection instanceof FakeClientConnection) return;
        original.call(instance, packet, callbacks, flush);
    }
    //?} else {
    /*@WrapOperation(
            method = "send",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;send(Lnet/minecraft/network/packet/Packet;Lio/netty/channel/ChannelFutureListener;Z)V")
    )
    public void send(ClientConnection instance, Packet packet, ChannelFutureListener channelFutureListener, boolean b, Operation<Void> original) {
        if (connection instanceof FakeClientConnection) return;
        original.call(instance, packet, channelFutureListener, b);
    }
    *///?}

    @Inject(method = "disconnect(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), cancellable = true)
    public void disconnect(Text reason, CallbackInfo ci) {
        if (connection instanceof FakeClientConnection) {
            ci.cancel();
        }
    }
}
