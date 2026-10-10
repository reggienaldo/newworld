package net.reggie.game.system.stats;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.reggie.NewWorld;
import net.reggie.game.system.haki_energy.HakiEnergyComponent;
import net.reggie.game.system.haki_energy.HakiProgression;
import net.reggie.game.system.race.PlayerRace;

import java.util.HashMap;
import java.util.Map;

public class IntStatsComponent implements StatsComponent {
    private final PlayerEntity provider;

    // Daten-Variablen
    private PlayerRace race = PlayerRace.HUMAN;
    private int doriki = 0;
    private int availablePoints = 0;

    // Map für die einzelnen Attribute (HAKI, DEVIL_FRUIT, SWORDSMANSHIP, DEFENSE)
    private final Map<StatType, Integer> stats = new HashMap<>();

    public IntStatsComponent(PlayerEntity provider) {
        this.provider = provider;
        // Alle Stats standardmäßig auf Stufe 0 initialisieren
        for (StatType type : StatType.values()) {
            stats.put(type, 0);
        }
    }

    // --- RASSEN MANAGEMENT ---
    @Override
    public PlayerRace getRace() {
        return this.race;
    }

    @Override
    public void setRace(PlayerRace race) {
        this.race = race;
        this.sync();
    }

    // --- DORIKI MANAGEMENT ---
    @Override
    public int getDoriki() {
        return this.doriki;
    }

    @Override
    public void addDoriki(int amount) {
        int oldDoriki = this.doriki;

        if (amount > 0 && this.doriki > Integer.MAX_VALUE - amount) {
            this.doriki = Integer.MAX_VALUE; // Setze fest auf das absolute Maximum (2.147.483.647)
        }
        // Unterlauf-Schutz (falls negatives Doriki abgezogen wird)
        else if (amount < 0 && this.doriki < Integer.MIN_VALUE - amount) {
            this.doriki = 0;
        }
        // Normale sichere Berechnung, wenn alles im Rahmen ist
        else {
            this.doriki = Math.max(0, this.doriki + amount);
        } // Verhindert negatives Doriki

        // Meilenstein-Logik: Alle 100 Doriki gibt es 1 Stat-Punkt
        int oldMilestones = oldDoriki / 100;
        int newMilestones = this.doriki / 100;

        if (newMilestones > oldMilestones) {
            int earnedPoints = newMilestones - oldMilestones;
            this.addAvailablePoints(earnedPoints);
            this.provider.sendMessage(Text.translatable("message.newworld.stats.points_earned", earnedPoints, this.doriki));
        }

        if (this.doriki >= 1500) {
            HakiEnergyComponent haki = NewWorld.HAKI_ENERGY.get(this.provider);
            if (!haki.hasUnlockedBuso()) {
                haki.setBusoUnlocked(true);
                this.provider.sendMessage(Text.translatable("haki.newworld.buso_unlocked"), false);
            }
        }

        if (this.doriki >= 10000) {
            // Erwacht nur, wenn er das Potenzial von Geburt an hat und es noch schläft
            HakiProgression.tryAwakenHaoshoku(this.provider);
        }

        this.sync();
    }

    // --- STAT-PUNKTE MANAGEMENT ---
    @Override
    public int getAvailablePoints() {
        return this.availablePoints;
    }

    @Override
    public void addAvailablePoints(int amount) {
        this.availablePoints = Math.max(0, this.availablePoints + amount);
        this.sync();
    }

    // --- ATTRIBUT MANAGEMENT ---
    @Override
    public int getStat(StatType type) {
        return this.stats.getOrDefault(type, 0);
    }

    @Override
    public void upgradeStat(StatType type) {
        if (this.availablePoints > 0) {
            this.availablePoints--;
            this.stats.put(type, this.getStat(type) + 1);
            this.provider.sendMessage(Text.translatable("message.newworld.stats.upgraded", type.name(), this.getStat(type)));
            this.sync();
        } else {
            this.provider.sendMessage(Text.translatable("message.newworld.stats.no_points"), true);
        }
    }

    // --- SPEICHERN (SERVER -> DATEI) ---
    @Override
    public void writeToNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putString("PlayerRace", this.race.name());
        nbt.putInt("Doriki", this.doriki);
        nbt.putInt("AvailablePoints", this.availablePoints);

        // Unter-Tag für die Stats-Map erstellen
        NbtCompound statsNbt = new NbtCompound();
        this.stats.forEach((type, level) -> statsNbt.putInt(type.name(), level));
        nbt.put("Attributes", statsNbt);
    }

    // --- LADEN (DATEI -> SERVER) ---
    @Override
    public void readFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        if (nbt.contains("PlayerRace")) {
            try {
                this.race = PlayerRace.valueOf(nbt.getString("PlayerRace"));
            } catch (IllegalArgumentException e) {
                this.race = PlayerRace.HUMAN; // Fallback, falls die Rasse ungültig ist
            }
        }

        this.doriki = nbt.getInt("Doriki");
        this.availablePoints = nbt.getInt("AvailablePoints");

        if (nbt.contains("Attributes")) {
            NbtCompound statsNbt = nbt.getCompound("Attributes");
            for (StatType type : StatType.values()) {
                if (statsNbt.contains(type.name())) {
                    this.stats.put(type, statsNbt.getInt(type.name()));
                }
            }
        }
    }

    // Helper-Methode um Code-Duplikate bei der Synchronisation zu vermeiden
    private void sync() {
        // Ersetze 'MyModComponents.ONE_PIECE_STATS' durch das echte Registrierungsfeld aus deinem EntityComponentInitializer!
        NewWorld.STATS.sync(this.provider);
    }
}
