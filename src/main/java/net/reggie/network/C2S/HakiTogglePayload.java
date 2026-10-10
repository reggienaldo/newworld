package net.reggie.network.C2S;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.reggie.NewWorld;

public record HakiTogglePayload(String hakiType) implements CustomPayload {
    public static final Id<HakiTogglePayload> ID = new Id<>(Identifier.of(NewWorld.MOD_ID, "haki_toggle"));

    // Nutzen von PacketCodecs.STRING für die 1.21 Yarn-Schnittstelle
    public static final PacketCodec<RegistryByteBuf, HakiTogglePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, HakiTogglePayload::hakiType,
            HakiTogglePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
