package net.reggie;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.reggie.command.DorikiCommand;
import net.reggie.command.HakiAdminCommands;
import net.reggie.command.RaceCommand;
import net.reggie.game.system.ability.ModAbilities;
import net.reggie.game.system.haki_energy.HakiEnergyComponent;
import net.reggie.game.system.haki_energy.IntHakiEnergyComponent;
import net.reggie.game.system.race.PlayerRace;
import net.reggie.game.system.race.RacePassiveHandler;
import net.reggie.game.system.stats.IntStatsComponent;
import net.reggie.game.system.stats.StatsComponent;
import net.reggie.item.ModItemGroups;
import net.reggie.item.ModItems;
import net.reggie.network.ModNetworking;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewWorld implements ModInitializer, EntityComponentInitializer {
	public static final String MOD_ID = "newworld";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		ModItems.registerModItems();
		ModItemGroups.registerItemGroups();

		ModNetworking.registerC2SPackets();

		ModAbilities.registerAbilities();

		DorikiCommand.register();
		RaceCommand.register();
		HakiAdminCommands.register();


		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			RacePassiveHandler.applyRaceAttributes(handler.getPlayer());
		});

		// Nach dem Sterben (beim Respawn) die Stats erneuern
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			RacePassiveHandler.applyRaceAttributes(newPlayer);
		});

		// Dieser Event-Hook triggert die Tick-Debuffs für jeden Spieler auf dem Server
		ServerTickEvents.START_SERVER_TICK.register(server -> {
			// Wir gehen jeden einzelnen Spieler durch, der gerade online ist
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				// Führt die Tick-Debuffs (wie Oni-Hunger oder Vampir-Sonne) aus
				RacePassiveHandler.handlePlayerPassiveTicks(player);
			}
		});

		ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, attacker, killedEntity) -> {
			if (attacker instanceof ServerPlayerEntity player) {
				int dorikiReward = 1; // Standardwert

				// Doriki je nach Gegnertyp anpassen
				if (killedEntity instanceof ZombieEntity) dorikiReward = 2;
				if (killedEntity instanceof WitherEntity) dorikiReward = 500;
				// if (killedEntity instanceof PacifistaEntity) dorikiReward = 1000;

				// Punkte gutschreiben
				NewWorld.STATS.get(player).addDoriki(dorikiReward);
			}
		});

		// Event: Immer wenn ein Entity Schaden nimmt
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			// Prüfen, ob der Angreifer ein Spieler ist
			if (source.getAttacker() instanceof ServerPlayerEntity attacker) {
				HakiEnergyComponent haki = NewWorld.HAKI_ENERGY.get(attacker);

				// BEISPIEL-LOGIK: Wenn der Spieler eine Rüstungshaki-Fähigkeit aktiv hat
				// (Das kannst du über eine Boolean-Variable in deiner Komponente abfragen, z.B. haki.isBusoActive())
				if (haki.isReady("buso_cooldown")) { // Oder ein anderer Marker für "aktiviert"
					// Spieler bekommt XP basierend auf dem gemachten Schaden!
					haki.addBusoXp((int) amount);
				}
			}
			return true;
		});

		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (entity instanceof ServerPlayerEntity player) {
				HakiEnergyComponent haki = NewWorld.HAKI_ENERGY.get(player);

				// Bedingung für Observierungshaki: Unter 2 Herzen (4 HP)
				if (haki.hasUnlockedBuso() && !haki.hasUnlockedKenbun() && (player.getHealth() - amount) <= 4.0f) {

					// 5% Chance (Zufallszahl zwischen 0.0 und 1.0 kleiner als 0.05)
					if (player.getRandom().nextFloat() < 0.05f) {
						haki.setKenbunUnlocked(true);
						player.sendMessage(Text.literal("§e Deine Sinne schärfen sich im Angesicht des Todes... Observierungshaki (Kenbunshoku) erwacht!"), false);
						player.getServerWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
								net.minecraft.sound.SoundEvents.BLOCK_BEACON_ACTIVATE, net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 1.5f);
					}
				}
			}
			return true;
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			HakiEnergyComponent haki = NewWorld.HAKI_ENERGY.get(player);
			var stats = NewWorld.STATS.get(player);

			// Wenn der Spieler brandneu ist (Doriki 0) und noch nie gewürfelt wurde
			// Wir nutzen hier einen Kontroll-Check, z.B. wenn er noch keine Rasse gewählt hat
			if (stats.getDoriki() == 0 && stats.getRace() == PlayerRace.HUMAN) {

				// 10% Chance, das Potenzial für Königshaki zu besitzen
				if (player.getRandom().nextFloat() < 0.10f) {
					haki.setPotentialHaoshoku(true);
					// Nachricht nur in die Server-Konsole, damit es für den Spieler eine Überraschung bleibt!
					server.sendMessage(Text.literal("Spieler " + player.getName().getString() + " besitzt das Potenzial für Königshaki!"));
				}
			}
		});

	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	public static final ComponentKey<HakiEnergyComponent> HAKI_ENERGY =
			ComponentRegistry.getOrCreate(Identifier.of("newworld", "haki_energy"), HakiEnergyComponent.class);

	public static final ComponentKey<StatsComponent> STATS =
			ComponentRegistry.getOrCreate(Identifier.of("newworld", "stats"), StatsComponent.class);

	@Override
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {

		registry.registerForPlayers(HAKI_ENERGY, IntHakiEnergyComponent::new, RespawnCopyStrategy.ALWAYS_COPY);

		registry.registerForPlayers(STATS, IntStatsComponent::new, RespawnCopyStrategy.ALWAYS_COPY);

	}
}
