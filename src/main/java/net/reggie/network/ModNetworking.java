package net.reggie.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.reggie.NewWorld;
import net.reggie.game.system.haki_energy.HakiEnergyComponent;
import net.reggie.game.system.race.PlayerRace;
import net.reggie.game.system.race.RacePassiveHandler;
import net.reggie.network.C2S.CombatModeTogglePayload;
import net.reggie.network.C2S.SelectRacePayload;
import net.reggie.network.S2C.OpenRaceScreenPayload;

public class ModNetworking {

    public static void registerC2SPackets() {
        PayloadTypeRegistry.playC2S().register(CombatModeTogglePayload.ID, CombatModeTogglePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(CombatModeTogglePayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                HakiEnergyComponent haki = NewWorld.HAKI_ENERGY.get(player);

                // Modus umschalten
                boolean currentMode = haki.isInCombatMode();
                haki.setCombatMode(!currentMode);

                // Feedback an den Spieler
                if (!currentMode) {
                    player.sendMessage(Text.literal("combatmode: §aon"), true);
                } else {
                    player.sendMessage(Text.literal("combatmode: §coff"), true);
                }
            });
        });

        PayloadTypeRegistry.playC2S().register(SelectRacePayload.ID, SelectRacePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SelectRacePayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                PlayerRace selectedRace = PlayerRace.valueOf(payload.raceName().toUpperCase());

                // Rasse setzen und Attribute/Pehkui-Größe sofort triggern!
                NewWorld.STATS.get(player).setRace(selectedRace);
                RacePassiveHandler.applyRaceAttributes(player);

                player.sendMessage(Text.literal("§aDu hast die Rasse §6" + selectedRace.name() + " §agewählt!"), false);
            });
        });
    }
    @Environment(EnvType.CLIENT)
    public static void registerS2CPackets() {

        PayloadTypeRegistry.playS2C().register(OpenRaceScreenPayload.ID, OpenRaceScreenPayload.CODEC);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();

            // Prüfen, ob der Spieler ein brandneuer Spieler ist (z.B. Doriki == 0 und Rasse HUMAN)
            // Wenn du eine extra NBT-Variable "HasChosenRace" in deiner Komponente nutzt, ist das noch sicherer:
            var stats = NewWorld.STATS.get(player);
            if (stats.getDoriki() == 0 && stats.getRace() == PlayerRace.HUMAN) {
                // Schicke den Befehl zum GUI-Öffnen an den Client
                server.execute(() -> ServerPlayNetworking.send(player, new OpenRaceScreenPayload()));
            }
        });

    }
}
