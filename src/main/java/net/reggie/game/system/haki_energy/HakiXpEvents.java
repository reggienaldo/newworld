package net.reggie.game.system.haki_energy;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.reggie.NewWorld;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Alle Quellen für Haki-XP:
// - Rüstungshaki: feindliche Mobs töten, Schaden von feindlichen Mobs nehmen, Haki-Clash (PvP)
// - Observierungshaki: Schaden von feindlichen Mobs nehmen
// Mobs aus Spawnern und friedliche Tiere geben keine XP.
public final class HakiXpEvents {

    private record ClashWindow(int lastHitTick, int windowStartTick, long xpInWindow) {}

    // Anti-Farm für PvP: Zustand pro Spielerpaar (nur im Speicher, wird bei Neustart zurückgesetzt)
    private static final Map<String, ClashWindow> CLASHES = new HashMap<>();

    private HakiXpEvents() {}

    public static void register() {
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, attacker, killedEntity) -> {
            if (attacker instanceof ServerPlayerEntity player && isXpMob(killedEntity)) {
                long xp = Math.max(1, Math.round(killedEntity.getMaxHealth() * HakiBalance.KILL_XP_PER_MAX_HP));
                NewWorld.HAKI_ENERGY.get(player).gainXp(HakiType.BUSO, xp);
            }
        });

        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
            if (damageTaken <= 0 || !(entity instanceof ServerPlayerEntity victim)) return;
            Entity attacker = source.getAttacker(); // bei Pfeilen der Schütze

            if (attacker instanceof LivingEntity mob && isXpMob(mob)) {
                HakiEnergyComponent haki = NewWorld.HAKI_ENERGY.get(victim);
                haki.gainXp(HakiType.KENBUN, perHit(damageTaken * HakiBalance.DAMAGE_TAKEN_KENBUN_XP, HakiBalance.MAX_XP_PER_HIT));
                haki.gainXp(HakiType.BUSO, perHit(damageTaken * HakiBalance.DAMAGE_TAKEN_BUSO_XP, HakiBalance.MAX_XP_PER_HIT));
            } else if (attacker instanceof ServerPlayerEntity hitter && hitter != victim) {
                handleClash(hitter, victim, damageTaken);
            }
        });

        // Königshaki-Gen: genau EINMAL pro Spieler auswürfeln (vorher bei jedem Join mit 0 Doriki)
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            HakiEnergyComponent haki = NewWorld.HAKI_ENERGY.get(player);
            if (haki.hasRolledHaoshokuGene()) return;

            haki.setRolledHaoshokuGene(true);
            if (!haki.hasPotentialHaoshoku() && player.getRandom().nextFloat() < HakiBalance.HAOSHOKU_GENE_CHANCE) {
                haki.setPotentialHaoshoku(true);
                // Nachricht nur in die Server-Konsole, damit es für den Spieler eine Überraschung bleibt!
                server.sendMessage(Text.literal("Player " + player.getName().getString() + " carries the potential for Conqueror's Haki!"));
            }
        });
    }

    // Nur feindliche Mobs, die nicht aus einem Spawner stammen
    private static boolean isXpMob(LivingEntity entity) {
        return entity instanceof Monster && !SpawnerTracker.isFromSpawner(entity);
    }

    private static long perHit(double xp, long cap) {
        return Math.min(cap, Math.max(1, Math.round(xp)));
    }

    // Haki-Clash: beide Spieler im Kampfmodus mit Rüstungshaki -> beide bekommen Buso-XP
    private static void handleClash(ServerPlayerEntity hitter, ServerPlayerEntity victim, float damage) {
        HakiEnergyComponent hitterHaki = NewWorld.HAKI_ENERGY.get(hitter);
        HakiEnergyComponent victimHaki = NewWorld.HAKI_ENERGY.get(victim);
        if (!isHakiActive(hitterHaki) || !isHakiActive(victimHaki)) return;

        int now = hitter.getServer().getTicks();
        String key = pairKey(hitter.getUuid(), victim.getUuid());
        ClashWindow window = CLASHES.get(key);

        if (window != null && now - window.lastHitTick() < HakiBalance.CLASH_COOLDOWN_TICKS) return;
        if (window == null || now - window.windowStartTick() >= HakiBalance.CLASH_WINDOW_TICKS) {
            window = new ClashWindow(now, now, 0);
        }

        long remaining = HakiBalance.CLASH_WINDOW_CAP - window.xpInWindow();
        long xp = Math.min(remaining, perHit(damage * HakiBalance.CLASH_XP_PER_DAMAGE, HakiBalance.CLASH_MAX_XP_PER_HIT));
        CLASHES.put(key, new ClashWindow(now, window.windowStartTick(), window.xpInWindow() + Math.max(0, xp)));
        if (xp <= 0) return;

        hitterHaki.gainXp(HakiType.BUSO, xp);
        victimHaki.gainXp(HakiType.BUSO, xp);
    }

    private static boolean isHakiActive(HakiEnergyComponent haki) {
        return haki.isInCombatMode() && haki.hasUnlockedBuso();
    }

    private static String pairKey(UUID a, UUID b) {
        return a.compareTo(b) < 0 ? a + ":" + b : b + ":" + a;
    }
}
