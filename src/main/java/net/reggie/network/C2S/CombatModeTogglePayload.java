package net.reggie.network.C2S;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.reggie.NewWorld;

public record CombatModeTogglePayload() implements CustomPayload {
    public static final Id<CombatModeTogglePayload> ID = new Id<>(Identifier.of(NewWorld.MOD_ID, "combat_mode_toggle"));

    // Da wir keine extra Daten mitschicken müssen (nur den Knopfdruck), ist der Codec leer
    public static final PacketCodec<RegistryByteBuf, CombatModeTogglePayload> CODEC = PacketCodec.unit(new CombatModeTogglePayload());

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
