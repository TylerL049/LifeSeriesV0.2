package net.mat0u5.lifeseries.network.packets;

import net.mat0u5.lifeseries.Main;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.List;

public record ConfigPayload(String configType, String id, int index, String name, String description, List<String> args) implements CustomPayload {

    public static final CustomPayload.Id<ConfigPayload> ID = new CustomPayload.Id<>(Identifier.of(Main.MOD_ID, "config"));
    public static final PacketCodec<RegistryByteBuf, ConfigPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, ConfigPayload::configType,
            PacketCodecs.STRING, ConfigPayload::id,
            PacketCodecs.INTEGER, ConfigPayload::index,
            PacketCodecs.STRING, ConfigPayload::name,
            PacketCodecs.STRING, ConfigPayload::description,
            PacketCodecs.STRING.collect(PacketCodecs.toList()), ConfigPayload::args,
            ConfigPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}