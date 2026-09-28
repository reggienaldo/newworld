package net.reggie.game.system.haki_energy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.reggie.NewWorld;
import net.reggie.game.system.race.PlayerRace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntHakiEnergyComponent implements HakiEnergyComponent {

    private final Map<String, Integer> cooldowns = new HashMap<>();

    private final PlayerEntity provider;
    private int energy = 100; // Startwert
    private final int maxEnergy = 1000;

    private boolean combatMode = false;

    private boolean busoUnlocked = false;
    private boolean kenbunUnlocked = false;
    private boolean potentialHaoshoku = false;
    private boolean haoshokuAwakened = false;

    // XP und Level Variablen
    private int busoLevel = 1;
    private int busoXp = 0;

    private int kenbunLevel = 1;
    private int kenbunXp = 0;

    private int regenerationTimer = 0;

    public IntHakiEnergyComponent(PlayerEntity provider) {
        this.provider = provider;
    }

    @Override
    public int getEnergy() { return this.energy; }

    @Override
    public int getMaxEnergy() { return this.maxEnergy; }

    @Override
    public void setEnergy(int amount) {
        this.energy = Math.max(0, Math.min(amount, maxEnergy));
        // Synchronisiert die Änderung sofort mit dem Client
        NewWorld.HAKI_ENERGY.sync(this.provider);
    }

    @Override
    public void changeEnergy(int amount) {
        setEnergy(this.energy + amount);
    }

    // Wird jeden Tick aufgerufen (wir müssen dies in der Mod registrieren)
    public void tick() {
        cooldowns.replaceAll((ability, ticks) -> ticks > 0 ? ticks - 1 : 0);
    }

    public boolean isReady(String ability) {
        return cooldowns.getOrDefault(ability, 0) <= 0;
    }

    public void setCooldown(String ability, int ticks) {
        cooldowns.put(ability, ticks);
        NewWorld.HAKI_ENERGY.sync(this.provider); // Client benachrichtigen
    }

    @Override
    public void serverTick() {
        // Cooldowns runterticken
        cooldowns.replaceAll((ability, ticks) -> ticks > 0 ? ticks - 1 : 0);

        // Regenerations-Logik
        if (this.energy < this.maxEnergy) {
            this.regenerationTimer++;

            if (this.regenerationTimer >= 20) { // Jede Sekunde
                this.regenerationTimer = 0;
                int regenAmount = 5;
                this.setEnergy(this.energy + regenAmount);
            }
        } else {
            this.regenerationTimer = 0;
        }
    }

    // --- RÜSTUNGSHAKI PROGRESSION ---
    @Override public int getBusoLevel() { return this.busoLevel; }
    @Override public int getBusoXp() { return this.busoXp; }

    @Override
    public void addBusoXp(int amount) {

        PlayerRace race = NewWorld.STATS.get(this.provider).getRace();

        // 2. Wenn der Spieler ein MENSCH ist, bekommt er +50% mehr XP (Faktor 1.5)
        if (race == PlayerRace.HUMAN) {
            amount = (int) (amount * 1.5f);
        } else if (race == PlayerRace.LUNARIAN) {
            amount = (int) (amount * 0.75f); // -25% Malus für Lunarier
        }

        this.busoXp += amount;
        int xpNeeded = this.busoLevel * 100; // Jedes Level braucht mehr XP (z.B. Lvl 1 = 100, Lvl 2 = 200)

        if (this.busoXp >= xpNeeded) {
            this.busoXp -= xpNeeded;
            this.busoLevel++;
            this.provider.sendMessage(net.minecraft.text.Text.literal("§5 Dein Rüstungshaki (Busoshoku) ist auf Stufe " + this.busoLevel + " aufgestiegen!"), false);
        }
        NewWorld.HAKI_ENERGY.sync(this.provider); // HUD aktualisieren
    }

    // --- OBSERVIERUNGSHAKI PROGRESSION ---
    @Override public int getKenbunLevel() { return this.kenbunLevel; }
    @Override public int getKenbunXp() { return this.kenbunXp; }

    @Override
    public void addKenbunXp(int amount) {

        PlayerRace race = NewWorld.STATS.get(this.provider).getRace();

        // +50% Haki-XP Boost für Menschen
        if (race == PlayerRace.HUMAN) {
            amount = (int) (amount * 1.5f);
        } else if (race == PlayerRace.LUNARIAN) {
            amount = (int) (amount * 0.75f); // -25% Malus für Lunarier
        }

        this.kenbunXp += amount;
        int xpNeeded = this.kenbunLevel * 100;

        if (this.kenbunXp >= xpNeeded) {
            this.kenbunXp -= xpNeeded;
            this.kenbunLevel++;
            this.provider.sendMessage(net.minecraft.text.Text.literal("§e Dein Observierungshaki (Kenbunshoku) ist auf Stufe " + this.kenbunLevel + " aufgestiegen!"), false);
        }
        NewWorld.HAKI_ENERGY.sync(this.provider);
    }

    // --- KÖNIGSHAKI BEDINGUNG ---
    @Override
    public boolean hasHaoshoku() {
        // Gekoppelt an dein One-Piece globales Doriki-System! Freischaltung ab 5000 Doriki
        int currentDoriki = NewWorld.STATS.get(this.provider).getDoriki();
        return currentDoriki >= 5000;
    }
    @Override
    public boolean isInCombatMode() {
        return this.combatMode;
    }

    @Override
    public void setCombatMode(boolean active) {
        this.combatMode = active;
        // WICHTIG: Synchronisiert den Zustand sofort mit dem Client (für das HUD/Animationen)
        NewWorld.HAKI_ENERGY.sync(this.provider);
    }

    @Override public boolean hasUnlockedBuso() { return this.busoUnlocked; }
    @Override public void setBusoUnlocked(boolean unlocked) { this.busoUnlocked = unlocked; this.sync(); }

    @Override public boolean hasUnlockedKenbun() { return this.kenbunUnlocked; }
    @Override public void setKenbunUnlocked(boolean unlocked) { this.kenbunUnlocked = unlocked; this.sync(); }

    @Override public boolean hasPotentialHaoshoku() { return this.potentialHaoshoku; }
    @Override public void setPotentialHaoshoku(boolean potential) { this.potentialHaoshoku = potential; this.sync(); }

    @Override public boolean hasAwakenedHaoshoku() { return this.haoshokuAwakened; }
    @Override public void setHaoshokuAwakened(boolean awakened) { this.haoshokuAwakened = awakened; this.sync(); }

    private void sync() {
        NewWorld.HAKI_ENERGY.sync(this.provider);
    }


    // Speichern beim Welt-Verlassen
    @Override
    public void writeToNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putInt("HakiEnergy", this.energy);
        nbt.putBoolean("CombatMode", this.combatMode);
        nbt.putBoolean("BusoUnlocked", this.busoUnlocked);
        nbt.putBoolean("KenbunUnlocked", this.kenbunUnlocked);
        nbt.putBoolean("PotentialHaoshoku", this.potentialHaoshoku);
        nbt.putBoolean("HaoshokuAwakened", this.haoshokuAwakened);

        // 1. Wir erstellen ein separates Unter-Tag nur für die Cooldowns
        NbtCompound cooldownsNbt = new NbtCompound();

        // 2. Wir loopen durch alle Einträge deiner Map
        this.cooldowns.forEach((ability, ticks) -> {
            // Wir speichern nur Cooldowns, die auch wirklich noch aktiv sind (> 0)
            if (ticks > 0) {
                cooldownsNbt.putInt(ability, ticks);
            }
        });

        // 3. Wir hängen das Unter-Tag an das Haupt-NBT des Spielers an
        nbt.put("AbilityCooldowns", cooldownsNbt);
    }

    // Laden beim Welt-Betreten
    @Override
    public void readFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        this.energy = nbt.getInt("HakiEnergy");
        this.busoUnlocked = nbt.getBoolean("BusoUnlocked");
        this.kenbunUnlocked = nbt.getBoolean("KenbunUnlocked");
        this.potentialHaoshoku = nbt.getBoolean("PotentialHaoshoku");
        this.haoshokuAwakened = nbt.getBoolean("HaoshokuAwakened");

        if (nbt.contains("CombatMode")) {
            this.combatMode = nbt.getBoolean("CombatMode");
        }

        // 1. Wir leeren die aktuelle Map, um alte Daten beim Laden zu überschreiben
        this.cooldowns.clear();

        // 2. Wir prüfen, ob das Tag in der Speicherdatei existiert
        if (nbt.contains("AbilityCooldowns")) {
            NbtCompound cooldownsNbt = nbt.getCompound("AbilityCooldowns");

            // 3. Wir gehen jeden gespeicherten Schlüssel (Fähigkeitsnamen) durch
            for (String ability : cooldownsNbt.getKeys()) {
                int ticks = cooldownsNbt.getInt(ability);

                // Nur hinzufügen, wenn der Cooldown noch nicht abgelaufen ist
                if (ticks > 0) {
                    this.cooldowns.put(ability, ticks);
                }
            }
        }
    }
}
