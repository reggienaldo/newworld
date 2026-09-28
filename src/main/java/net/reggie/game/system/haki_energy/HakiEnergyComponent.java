package net.reggie.game.system.haki_energy;

import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.List;

public interface HakiEnergyComponent extends Component, AutoSyncedComponent, ServerTickingComponent {
    int getEnergy();
    void setEnergy(int amount);
    void changeEnergy(int amount);
    int getMaxEnergy();

    boolean isReady(String ability);
    void setCooldown(String ability, int ticks);

    // --- NEU: PROGRESSIONS-SYSTEM ---
    // Rüstungshaki (Busoshoku)
    int getBusoLevel();
    int getBusoXp();
    void addBusoXp(int amount);

    // Observierungshaki (Kenbunshoku)
    int getKenbunLevel();
    int getKenbunXp();
    void addKenbunXp(int amount);

    // Königshaki (Haoshoku) - Kann z. B. direkt an Doriki gekoppelt werden
    boolean hasHaoshoku();

    boolean isInCombatMode();
    void setCombatMode(boolean active);

    boolean hasUnlockedBuso();
    void setBusoUnlocked(boolean unlocked);

    boolean hasUnlockedKenbun();
    void setKenbunUnlocked(boolean unlocked);

    boolean hasPotentialHaoshoku(); // Hat der Spieler das Gen/Potenzial von Geburt an?
    void setPotentialHaoshoku(boolean potential);

    boolean hasAwakenedHaoshoku(); // Ist das Königshaki bereits erwacht?
    void setHaoshokuAwakened(boolean awakened);
}
