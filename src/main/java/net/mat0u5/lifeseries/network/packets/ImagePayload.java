package net.mat0u5.lifeseries.network.packets;

import net.mat0u5.lifeseries.Main;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ImagePayload(String name, int index, int maxIndex, byte[] bytes) implements CustomPayload {
    public static final CustomPayload.Id<ImagePayload> ID = new CustomPayload.Id<>(Identifier.of(Main.MOD_ID, "image"));
    public static final PacketCodec<RegistryByteBuf, ImagePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, ImagePayload::name,
            PacketCodecs.INTEGER, ImagePayload::index,
            PacketCodecs.INTEGER, ImagePayload::maxIndex,
            PacketCodecs.BYTE_ARRAY, ImagePayload::bytes,
            ImagePayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}