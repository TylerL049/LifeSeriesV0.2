package net.mat0u5.lifeseries.network.packets;


import net.mat0u5.lifeseries.Main;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.List;

public record StringListPayload(String name, List<String> value) implements CustomPayload {

    public static final CustomPayload.Id<StringListPayload> ID = new CustomPayload.Id<>(Identifier.of(Main.MOD_ID, "stringlist"));
    public static final PacketCodec<RegistryByteBuf, StringListPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, StringListPayload::name,
            PacketCodecs.STRING.collect(PacketCodecs.toList()), StringListPayload::value,
            StringListPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}