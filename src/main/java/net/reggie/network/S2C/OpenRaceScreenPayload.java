package net.reggie.network.S2C;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.reggie.NewWorld;

public record OpenRaceScreenPayload() implements CustomPayload {
    public static final Id<OpenRaceScreenPayload> ID = new Id<>(Identifier.of(NewWorld.MOD_ID, "open_race_screen"));
    public static final PacketCodec<RegistryByteBuf, OpenRaceScreenPayload> CODEC = PacketCodec.unit(new OpenRaceScreenPayload());
    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
