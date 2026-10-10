package net.reggie.game.system.haki_energy;

import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public interface HakiEnergyComponent extends Component, AutoSyncedComponent, ServerTickingComponent {
    int getEnergy();
    void setEnergy(int amount);
    void changeEnergy(int amount);
    int getMaxEnergy();          // wächst mit den Haki-Leveln
    double getRegenPerSecond();  // wächst mit den Haki-Leveln

    boolean isReady(String ability);
    void setCooldown(String ability, int ticks);

    // --- PROGRESSIONS-SYSTEM (Haki-XP, ohne Limit) ---
    long getTotalXp(HakiType type);
    int getLevel(HakiType type);

    // Gesamt-XP aus Buso + Kenbun (Grundlage für das Erwachen des Königshaki)
    long getTotalHakiXp();

    // XP aus dem Spielgeschehen: nur wenn die Haki-Art freigeschaltet ist, inkl. Rassen-Multiplikator.
    // Gibt die tatsächlich gutgeschriebene Menge zurück.
    long gainXp(HakiType type, long baseAmount);

    // Direktes Setzen (Admin-Befehle): ohne Freischalt-Prüfung und ohne Multiplikator
    void setTotalXp(HakiType type, long totalXp);

    default int getBusoLevel() { return getLevel(HakiType.BUSO); }
    default int getKenbunLevel() { return getLevel(HakiType.KENBUN); }

    boolean isInCombatMode();
    void setCombatMode(boolean active);

    boolean isUnlocked(HakiType type);

    boolean hasUnlockedBuso();
    void setBusoUnlocked(boolean unlocked);

    boolean hasUnlockedKenbun();
    void setKenbunUnlocked(boolean unlocked);

    boolean hasPotentialHaoshoku(); // Hat der Spieler das Gen/Potenzial von Geburt an?
    void setPotentialHaoshoku(boolean potential);

    boolean hasRolledHaoshokuGene(); // Wurde das Gen bereits (einmalig) ausgewürfelt?
    void setRolledHaoshokuGene(boolean rolled);

    boolean hasAwakenedHaoshoku(); // Ist das Königshaki bereits erwacht?
    void setHaoshokuAwakened(boolean awakened);

    boolean isBusoActive();
    void setBusoActive(boolean active);

    boolean isKenbunActive();
    void setKenbunActive(boolean active);
}
