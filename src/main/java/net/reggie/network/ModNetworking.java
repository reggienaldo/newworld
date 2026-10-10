package net.reggie.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.reggie.NewWorld;
import net.reggie.game.system.haki_energy.HakiEnergyComponent;
import net.reggie.game.system.race.PlayerRace;
import net.reggie.game.system.race.RacePassiveHandler;
import net.reggie.network.C2S.CombatModeTogglePayload;
import net.reggie.network.C2S.HakiTogglePayload;
import net.reggie.network.C2S.SelectRacePayload;
import net.reggie.network.S2C.OpenRaceScreenPayload;
import net.reggie.sound.ModSounds;

public class ModNetworking {

    public static void registerC2SPackets() {
        PayloadTypeRegistry.playC2S().register(HakiTogglePayload.ID, HakiTogglePayload.CODEC);

        // =========================================================================
        // RECEIVER: Haki an- und ausschalten (Tasten G und V vom KeyInputHandler)
        // =========================================================================
        ServerPlayNetworking.registerGlobalReceiver(HakiTogglePayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                HakiEnergyComponent haki = NewWorld.HAKI_ENERGY.get(player);

                if (haki == null) return;

                String type = payload.hakiType();

                // -----------------------------------------------------------------
                // A. RÜSTUNGSHAKI (BUSOSHOKU) STEUERUNG
                // -----------------------------------------------------------------
                if (type.equalsIgnoreCase("buso")) {
                    // Check: Hat der Spieler das Haki überhaupt schon gelernt?
                    if (!haki.hasUnlockedBuso()) {
                        player.sendMessage(Text.literal("§cDu habt das Rüstungshaki noch nicht freigeschaltet!"), true);
                        return;
                    }

                    boolean nextState = !haki.isBusoActive();
                    haki.setBusoActive(nextState);

                    if (nextState) {
                        player.sendMessage(Text.literal("§aRüstungshaki (Busoshoku) aktiviert!"), true);

                        player.getServerWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                                ModSounds.HAKI_ACTIVATE, SoundCategory.PLAYERS, 1.0f, 1.0f);
                    } else {
                        player.sendMessage(Text.literal("§cRüstungshaki deaktiviert."), true);
                        player.getServerWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.5f, 0.5f);
                    }
                }

                // -----------------------------------------------------------------
                // B. OBSERVIERUNGSHAKI (KENBUNSHOKU) STEUERUNG
                // -----------------------------------------------------------------
                else if (type.equalsIgnoreCase("ken")) {
                    // Check: Hat der Spieler das Haki überhaupt schon gelernt?
                    if (!haki.hasUnlockedKenbun()) {
                        player.sendMessage(Text.literal("§cDu hast das Observierungshaki noch nicht freigeschaltet!"), true);
                        return;
                    }

                    boolean nextState = !haki.isKenbunActive();
                    haki.setKenbunActive(nextState);

                    if (nextState) {
                        player.sendMessage(Text.literal("§aObservierungshaki (Kenbunshoku) aktiviert!"), true);

                        // MYSTISCHER ANIME-SOUND (Verzerrter Beacon-Klang für geschärfte Sinne)
                        player.getServerWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                                ModSounds.KEN_HAKI_ACTIVATE, SoundCategory.PLAYERS, 1.0f, 1.0f);
                    } else {
                        player.sendMessage(Text.literal("§cObservierungshaki deaktiviert."), true);
                        player.getServerWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                                ModSounds.KEN_HAKI_DEACTIVATE, SoundCategory.PLAYERS, 1.0f, 1.0f);
                    }
                }
            });
        });

        PayloadTypeRegistry.playC2S().register(CombatModeTogglePayload.ID, CombatModeTogglePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(CombatModeTogglePayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                HakiEnergyComponent haki = NewWorld.HAKI_ENERGY.get(player);

                if (haki == null) return;

                // Modus umschalten
                boolean currentMode = haki.isInCombatMode();
                boolean nextMode = !currentMode;
                haki.setCombatMode(nextMode);

                // Feedback an den Spieler
                if (nextMode) {
                    player.sendMessage(Text.translatable("message.newworld.combat_mode.on"), true);
                } else {
                    // === NEU: Haki sicher ausschalten, wenn man den Kampfmodus verlässt ===
                    if (haki.isBusoActive()) {
                        haki.setBusoActive(false);
                    }
                    if (haki.isKenbunActive()) {
                        haki.setKenbunActive(false);
                    }

                    player.sendMessage(Text.translatable("message.newworld.combat_mode.off"), true);
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

                player.sendMessage(Text.translatable("message.newworld.race.chosen", selectedRace.name()), false);
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
