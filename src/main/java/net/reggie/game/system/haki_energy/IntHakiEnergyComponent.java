package net.reggie.game.system.haki_energy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.reggie.NewWorld;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class IntHakiEnergyComponent implements HakiEnergyComponent {

    private final Map<String, Integer> cooldowns = new HashMap<>();

    private final PlayerEntity provider;
    private int energy = 100; // Startwert

    private boolean combatMode = false;

    private boolean busoUnlocked = false;
    private boolean kenbunUnlocked = false;
    private boolean potentialHaoshoku = false;
    private boolean haoshokuGeneRolled = false;
    private boolean haoshokuAwakened = false;

    private boolean busoActive = false;
    private boolean kenbunActive = false;

    private int regenDelayTimer = 0;

    // Gesamt-XP pro Haki-Art (kein Limit). Das Level wird daraus berechnet und zwischengespeichert.
    private final Map<HakiType, Long> totalXp = new EnumMap<>(HakiType.class);
    private final Map<HakiType, Integer> levels = new EnumMap<>(HakiType.class);

    private int regenerationTimer = 0;
    private double regenerationRemainder = 0; // Nachkommastellen der Regeneration

    public IntHakiEnergyComponent(PlayerEntity provider) {
        this.provider = provider;
        for (HakiType type : HakiType.values()) {
            this.totalXp.put(type, 0L);
            this.levels.put(type, 1);
        }
    }

    @Override
    public int getEnergy() { return this.energy; }

    @Override
    public int getMaxEnergy() {
        return HakiBalance.maxEnergy(getLevel(HakiType.BUSO), getLevel(HakiType.KENBUN));
    }

    @Override
    public double getRegenPerSecond() {
        return HakiBalance.regenPerSecond(getLevel(HakiType.BUSO), getLevel(HakiType.KENBUN));
    }

    @Override
    public void setEnergy(int amount) {
        this.energy = Math.max(0, Math.min(amount, getMaxEnergy()));
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
        if (this.busoActive) {
            this.setEnergy(Math.max(0, this.getEnergy() - 1));
            this.regenDelayTimer = 60; // Setzt die Sperre auf 60 Ticks (3 Sekunden)
            if (this.getEnergy() <= 0) {
                this.setBusoActive(false);
                this.provider.sendMessage(Text.literal("§cDeine Haki-Energie ist erschöpft!"), true);
            }
        }

        // --- 2. OBSERVIERUNGSHAKI ENERGIE-VERBRAUCH ---
        if (this.kenbunActive) {
            this.setEnergy(Math.max(0, this.getEnergy() - 1));
            this.regenDelayTimer = 60; // Setzt die Sperre auf 60 Ticks (3 Sekunden)
            if (this.getEnergy() <= 0) {
                this.setKenbunActive(false);
                this.provider.sendMessage(Text.literal("§cDeine Haki-Energie ist erschöpft!"), true);
            }
        }

        // Cooldowns runterticken
        cooldowns.replaceAll((ability, ticks) -> ticks > 0 ? ticks - 1 : 0);

        // --- 3. REGENERATIONS-LOGIK MIT SPERR-TIMER ---
        // Regeneration läuft NUR, wenn beide Hakis AUS sind UND der Sperr-Timer abgelaufen ist!
        if (!this.busoActive && !this.kenbunActive) {

            if (this.regenDelayTimer > 0) {
                this.regenDelayTimer--; // Sperre tickt langsam runter, wenn kein Haki aktiv ist
                this.regenerationTimer = 0;
                this.regenerationRemainder = 0;
            } else if (this.energy < getMaxEnergy()) {
                this.regenerationTimer++;

                if (this.regenerationTimer >= 20) { // Jede Sekunde
                    this.regenerationTimer = 0;
                    double regen = getRegenPerSecond() + this.regenerationRemainder;
                    int regenAmount = (int) regen;
                    this.regenerationRemainder = regen - regenAmount;
                    this.setEnergy(this.energy + regenAmount);
                }
            } else {
                this.regenerationTimer = 0;
                this.regenerationRemainder = 0;
            }

        }
    }

    // --- HAKI-XP PROGRESSION ---
    @Override
    public long getTotalXp(HakiType type) { return this.totalXp.get(type); }

    @Override
    public int getLevel(HakiType type) { return this.levels.get(type); }

    @Override
    public long getTotalHakiXp() {
        long sum = 0;
        for (long xp : this.totalXp.values()) {
            sum = sum > Long.MAX_VALUE - xp ? Long.MAX_VALUE : sum + xp;
        }
        return sum;
    }

    @Override
    public long gainXp(HakiType type, long baseAmount) {
        if (baseAmount <= 0 || !isUnlocked(type)) return 0;

        long amount = Math.max(1, Math.round(baseAmount * HakiBalance.xpMultiplier(this.provider)));
        long current = getTotalXp(type);
        long newTotal = current > Long.MAX_VALUE - amount ? Long.MAX_VALUE : current + amount;
        applyTotalXp(type, newTotal);
        return amount;
    }

    @Override
    public void setTotalXp(HakiType type, long totalXp) {
        applyTotalXp(type, Math.max(0, totalXp));
    }

    private void applyTotalXp(HakiType type, long newTotal) {
        Set<HakiSkill> skillsBefore = HakiProgression.unlockedSkills(this);
        int oldLevel = getLevel(type);

        this.totalXp.put(type, newTotal);
        int newLevel = HakiBalance.levelForTotalXp(newTotal);
        this.levels.put(type, newLevel);

        if (newLevel > oldLevel) {
            this.provider.sendMessage(Text.translatable("haki.newworld.level_up", type.displayName(), newLevel).formatted(Formatting.DARK_PURPLE), false);
        }

        // Energie darf das (evtl. gesunkene) Maximum nicht überschreiten
        this.energy = Math.min(this.energy, getMaxEnergy());

        HakiProgression.announceNewSkills(this.provider, skillsBefore);

        // Königshaki erwacht bei genug Gesamt-Haki-XP: mit Gen früh, ohne Gen erst bei sehr hohen XP
        long totalHakiXp = getTotalHakiXp();
        if (totalHakiXp >= HakiBalance.HAOSHOKU_AWAKEN_TOTAL_XP_NO_GENE) {
            HakiProgression.awakenHaoshoku(this.provider);
        } else if (totalHakiXp >= HakiBalance.HAOSHOKU_AWAKEN_TOTAL_XP) {
            HakiProgression.tryAwakenHaoshoku(this.provider);
        }

        NewWorld.HAKI_ENERGY.sync(this.provider); // HUD aktualisieren
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

    @Override
    public boolean isUnlocked(HakiType type) {
        return switch (type) {
            case BUSO -> this.busoUnlocked;
            case KENBUN -> this.kenbunUnlocked;
        };
    }

    @Override public boolean hasUnlockedBuso() { return this.busoUnlocked; }
    @Override public void setBusoUnlocked(boolean unlocked) { this.busoUnlocked = unlocked; this.sync(); }

    @Override public boolean hasUnlockedKenbun() { return this.kenbunUnlocked; }
    @Override public void setKenbunUnlocked(boolean unlocked) { this.kenbunUnlocked = unlocked; this.sync(); }

    @Override public boolean hasPotentialHaoshoku() { return this.potentialHaoshoku; }
    @Override public void setPotentialHaoshoku(boolean potential) { this.potentialHaoshoku = potential; this.sync(); }

    @Override public boolean hasRolledHaoshokuGene() { return this.haoshokuGeneRolled; }
    @Override public void setRolledHaoshokuGene(boolean rolled) { this.haoshokuGeneRolled = rolled; this.sync(); }

    @Override public boolean hasAwakenedHaoshoku() { return this.haoshokuAwakened; }
    @Override public void setHaoshokuAwakened(boolean awakened) { this.haoshokuAwakened = awakened; this.sync(); }

    @Override public boolean isBusoActive() { return this.busoActive; }
    @Override public void setBusoActive(boolean active) {
        this.busoActive = active;
        this.sync();
    }

    @Override public boolean isKenbunActive() { return this.kenbunActive; }
    @Override public void setKenbunActive(boolean active) {
        this.kenbunActive = active;
        // Wenn Kenbunshoku deaktiviert wird, schalten wir auch das Umgebungsglühen der Mobs ab!
        this.sync();
    }


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
        nbt.putBoolean("HaoshokuGeneRolled", this.haoshokuGeneRolled);
        nbt.putBoolean("HaoshokuAwakened", this.haoshokuAwakened);

        for (HakiType type : HakiType.values()) {
            nbt.putLong(type.nbtKey(), getTotalXp(type));
        }

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
        this.haoshokuGeneRolled = nbt.getBoolean("HaoshokuGeneRolled");
        this.haoshokuAwakened = nbt.getBoolean("HaoshokuAwakened");

        if (nbt.contains("CombatMode")) {
            this.combatMode = nbt.getBoolean("CombatMode");
        }

        // Fehlende Keys (alte Spielstände) ergeben 0 XP / Level 1
        for (HakiType type : HakiType.values()) {
            long xp = Math.max(0, nbt.getLong(type.nbtKey()));
            this.totalXp.put(type, xp);
            this.levels.put(type, HakiBalance.levelForTotalXp(xp));
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
