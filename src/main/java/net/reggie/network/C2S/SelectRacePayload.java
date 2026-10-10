package net.reggie.network.C2S;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.reggie.NewWorld;

public record SelectRacePayload(String raceName) implements CustomPayload {
    public static final Id<SelectRacePayload> ID = new Id<>(Identifier.of(NewWorld.MOD_ID, "select_race"));

    // FIX: PacketCodecs.STRING anstatt PacketCodec.STRING nutzen!
    public static final PacketCodec<RegistryByteBuf, SelectRacePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, SelectRacePayload::raceName,
            SelectRacePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
