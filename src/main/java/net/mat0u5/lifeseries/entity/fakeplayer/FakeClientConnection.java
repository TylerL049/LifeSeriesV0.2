package net.mat0u5.lifeseries.entity.fakeplayer;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.listener.PacketListener;
//? if <= 1.21.4
import net.minecraft.network.NetworkState;
//? if >= 1.21.5
/*import net.minecraft.network.state.NetworkState;*/

/*
 * This file includes code from the Fabric Carpet project: https://github.com/gnembon/fabric-carpet
 *
 * Used and modified under the MIT License.
 */
public class FakeClientConnection extends ClientConnection {
    public FakeClientConnection(NetworkSide side) {
        super(side);
    }
    @Override
    public void tryDisableAutoRead() {}
    @Override
    public void handleDisconnection() {}
    @Override
    public void setInitialPacketListener(PacketListener packetListener) {}
    @Override
    public <T extends PacketListener> void transitionInbound(NetworkState<T> protocolInfo, T packetListener) {}
}