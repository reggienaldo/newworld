package net.reggie.game.system.stats;

import net.reggie.game.system.race.PlayerRace;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public interface StatsComponent extends Component, AutoSyncedComponent {
    // Doriki (Der globale Fortschritt)
    int getDoriki();
    void addDoriki(int amount);

    // Stat-Punkte zum Verteilen
    int getAvailablePoints();
    void addAvailablePoints(int amount);

    // Die einzelnen Attribute
    int getStat(StatType type);
    void upgradeStat(StatType type);

    PlayerRace getRace();
    void setRace(PlayerRace race);
}