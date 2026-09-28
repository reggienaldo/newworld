package net.reggie.game.system.stats;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.reggie.NewWorld;
import net.reggie.game.system.haki_energy.HakiEnergyComponent;
import net.reggie.game.system.race.PlayerRace;

import java.util.HashMap;
import java.util.List;
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
            this.provider.sendMessage(Text.literal("§6+ " + earnedPoints + " Stat-Punkt(e) erhalten! §7(Doriki: " + this.doriki + ")"));
        }

        if (this.doriki >= 1500) {
            HakiEnergyComponent haki = NewWorld.HAKI_ENERGY.get(this.provider);
            if (!haki.hasUnlockedBuso()) {
                haki.setBusoUnlocked(true);
                this.provider.sendMessage(Text.literal("§5 Dein Wille verhärtet sich... Rüstungshaki (Busoshoku) freigeschaltet!"), false);
            }
        }

        if (this.doriki >= 10000) {
            HakiEnergyComponent haki = NewWorld.HAKI_ENERGY.get(this.provider);

            // Hat er das Potenzial von Geburt an und ist noch nicht erwacht?
            if (haki.hasPotentialHaoshoku() && !haki.hasAwakenedHaoshoku()) {
                haki.setHaoshokuAwakened(true);
                this.provider.sendMessage(Text.literal("§d DIE STIMME ALLER DINGE... Das Königshaki (Haoshoku) ist in dir erwacht!"), false);

                // Welt sicher zum Server-Thread casten
                net.minecraft.server.world.ServerWorld serverWorld = (net.minecraft.server.world.ServerWorld) this.provider.getWorld();

                // Erstelle die Schockwellen-Box (15 Blöcke Radius)
                net.minecraft.util.math.Box area = this.provider.getBoundingBox().expand(15.0);

                // Wir holen die Liste der Mobs im Umkreis
                List nearby = serverWorld.getEntitiesByClass(
                        LivingEntity.class,
                        area,
                        entity -> entity != this.provider
                );

                // FIX: Die Schleife liest 'Object' aus (löst den Required Type Object Fehler auf)
                for (Object obj : nearby) {
                    // Wir casten das Objekt manuell und sicher zu einer LivingEntity
                    if (obj instanceof LivingEntity living) {
                        Vec3d push = living.getPos().subtract(this.provider.getPos()).normalize().multiply(2.5);

                        // Wegstoßen (Knockback) berechnen
                        living.setVelocity(push.x, 0.5, push.z);
                        living.velocityModified = true;
                    }
                }
            }
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
            this.provider.sendMessage(Text.literal("§a" + type.name() + " erfolgreich auf Stufe " + this.getStat(type) + " erhöht!"));
            this.sync();
        } else {
            this.provider.sendMessage(Text.literal("§cDu hast keine verfügbaren Stat-Punkte mehr!"), true);
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
