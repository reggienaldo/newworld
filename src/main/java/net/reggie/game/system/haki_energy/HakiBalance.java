package net.reggie.game.system.haki_energy;

import net.minecraft.entity.player.PlayerEntity;
import net.reggie.NewWorld;
import net.reggie.game.system.race.PlayerRace;

// Alle Balancing-Werte und Formeln des Haki-XP-Systems an einem Ort.
// Haki-XP hat kein Limit: Level werden immer teurer und jedes Level bringt etwas weniger Stärke.
public final class HakiBalance {

    private HakiBalance() {}

    // --- XP-QUELLEN ---
    public static final double KILL_XP_PER_MAX_HP = 0.5;       // Zombie (20 HP) = 10 XP, Wither (300 HP) = 150 XP
    public static final double DAMAGE_TAKEN_KENBUN_XP = 2.0;   // pro Schadenspunkt nach Rüstung
    public static final double DAMAGE_TAKEN_BUSO_XP = 1.0;
    public static final long MAX_XP_PER_HIT = 40;

    // Haki-Clash (PvP): beide Spieler im Kampfmodus mit Rüstungshaki
    public static final double CLASH_XP_PER_DAMAGE = 1.5;
    public static final long CLASH_MAX_XP_PER_HIT = 30;
    public static final int CLASH_COOLDOWN_TICKS = 20;          // max. 1x pro Sekunde pro Spielerpaar
    public static final int CLASH_WINDOW_TICKS = 20 * 60 * 10;  // 10 Minuten
    public static final long CLASH_WINDOW_CAP = 500;            // max. XP pro Paar und Zeitfenster

    // --- KÖNIGSHAKI ---
    public static final float HAOSHOKU_GENE_CHANCE = 0.05f;
    public static final long HAOSHOKU_AWAKEN_TOTAL_XP = 25_000;          // Buso + Kenbun zusammen, mit Gen (~beide Stufe 10-11)
    public static final long HAOSHOKU_AWAKEN_TOTAL_XP_NO_GENE = 250_000; // ohne Gen, sehr hoch (~beide Stufe 26)

    public static double busoEnergyCostPerSecond(int busoLevel, int kenbunLevel) {
        // Das Haki verbraucht das 1.5-fache deiner aktuellen Regenerationskraft
        return regenPerSecond(busoLevel, kenbunLevel) * 1.5;
    }

    public static double kenbunEnergyCostPerSecond(int busoLevel, int kenbunLevel) {
        // Gleiche Balance für das Observierungshaki
        return regenPerSecond(busoLevel, kenbunLevel) * 1.5;
    }
    // --- LEVEL-KURVE ---
    // Gesamt-XP, um Level L zu erreichen: 40 * (L-1)^2.5 + 60 * (L-1)
    // Level 2 = 100 XP, Level 10 = ~10.3k, Level 25 = ~113k, Level 100 = ~3.9M
    public static long totalXpForLevel(int level) {
        double xp = rawXpForLevel(level);
        return xp >= Long.MAX_VALUE ? Long.MAX_VALUE : (long) xp;
    }

    // Ungekappte Schwelle, damit auch riesige XP-Werte das richtige Level ergeben
    private static double rawXpForLevel(int level) {
        if (level <= 1) return 0;
        double n = level - 1;
        return Math.floor(40.0 * Math.pow(n, 2.5) + 60.0 * n);
    }

    public static int levelForTotalXp(long totalXp) {
        if (totalXp <= 0) return 1;
        // Binäre Suche: höchstes Level, dessen Schwelle <= totalXp ist
        int low = 1, high = 1;
        while (high < Integer.MAX_VALUE / 2 && rawXpForLevel(high) <= totalXp) {
            high *= 2;
        }
        while (low < high) {
            int mid = low + (high - low + 1) / 2;
            if (rawXpForLevel(mid) <= totalXp) low = mid;
            else high = mid - 1;
        }
        return low;
    }

    // --- STÄRKE ---
    // Level 1 = +3 (wie vorher), Level 10 = +16, Level 25 = +27, Level 100 = +57
    public static float busoDamageBonus(int busoLevel) {
        return (float) (6.0 * Math.sqrt(Math.max(1, busoLevel)) - 3.0);
    }

    // T = gesammelte Level über Stufe 1 hinaus (Buso + Kenbun)
    private static double growth(int busoLevel, int kenbunLevel) {
        return Math.sqrt(Math.max(0, busoLevel + kenbunLevel - 2));
    }

    // T=0: 1000 | T=10: 1790 | T=50: 2768 | T=200: 4536
    public static int maxEnergy(int busoLevel, int kenbunLevel) {
        return (int) (1000 + 250 * growth(busoLevel, kenbunLevel));
    }

    // T=0: 5/s | T=10: 9.7/s | T=50: 15.6/s | T=200: 26.2/s
    public static double regenPerSecond(int busoLevel, int kenbunLevel) {
        return 5.0 + 1.5 * growth(busoLevel, kenbunLevel);
    }

    // --- MULTIPLIKATOR ---
    // Einstiegspunkt für spätere Boni (z. B. HAKI-Stat aus dem Stats-System).
    public static double xpMultiplier(PlayerEntity player) {
        PlayerRace race = NewWorld.STATS.get(player).getRace();
        if (race == PlayerRace.HUMAN) return 1.5;     // +50% für Menschen
        if (race == PlayerRace.LUNARIAN) return 0.75; // -25% für Lunarier
        return 1.0;
    }
}
