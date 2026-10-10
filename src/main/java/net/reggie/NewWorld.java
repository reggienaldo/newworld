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
import net.reggie.block.ModBlocks;
import net.reggie.command.DorikiCommand;
import net.reggie.command.HakiAdminCommands;
import net.reggie.command.HakiCommand;
import net.reggie.command.RaceCommand;
import net.reggie.game.system.ability.ModAbilities;
import net.reggie.game.system.haki_energy.HakiEnergyComponent;
import net.reggie.game.system.haki_energy.HakiXpEvents;
import net.reggie.game.system.haki_energy.IntHakiEnergyComponent;
import net.reggie.game.system.race.PlayerRace;
import net.reggie.game.system.race.RacePassiveHandler;
import net.reggie.game.system.stats.IntStatsComponent;
import net.reggie.game.system.stats.StatsComponent;
import net.reggie.item.ModItemGroups;
import net.reggie.item.ModItems;
import net.reggie.network.ModNetworking;
import net.reggie.sound.ModSounds;
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

		ModBlocks.registerModBlocks();

		ModSounds.registerSounds();

		ModNetworking.registerC2SPackets();

		ModAbilities.registerAbilities();

		DorikiCommand.register();
		RaceCommand.register();
		HakiAdminCommands.register();
		HakiCommand.register();


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

		// Haki-XP (Kills, erlittener Schaden, Haki-Clash) und das einmalige Königshaki-Gen
		HakiXpEvents.register();

		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (entity instanceof ServerPlayerEntity player) {
				HakiEnergyComponent haki = NewWorld.HAKI_ENERGY.get(player);

				// Bedingung für Observierungshaki: Unter 2 Herzen (4 HP)
				if (haki.hasUnlockedBuso() && !haki.hasUnlockedKenbun() && (player.getHealth() - amount) <= 4.0f) {

					// 5% Chance (Zufallszahl zwischen 0.0 und 1.0 kleiner als 0.05)
					if (player.getRandom().nextFloat() < 0.05f) {
						haki.setKenbunUnlocked(true);
						player.sendMessage(Text.translatable("haki.newworld.kenbun_awakened"), false);
						player.getServerWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
								net.minecraft.sound.SoundEvents.BLOCK_BEACON_ACTIVATE, net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 1.5f);
					}
				}
			}
			return true;
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
