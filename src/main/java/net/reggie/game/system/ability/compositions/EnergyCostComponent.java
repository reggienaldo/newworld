package net.reggie.game.system.ability.compositions;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.reggie.NewWorld;
import net.reggie.game.system.haki_energy.HakiEnergyComponent;

import java.util.List;

public class EnergyCostComponent implements AbilityComponent {
    private final int cost;

    public EnergyCostComponent(int cost) {
        this.cost = cost;
    }

    @Override
    public boolean execute(ServerWorld world, ServerPlayerEntity user, Vec3d targetPos, List<Entity> targets) {
        // Hole die Fluchenergie-Daten über Cardinal Components vom Spieler
        HakiEnergyComponent energyComp = NewWorld.HAKI_ENERGY.get(user);

        if (energyComp.getEnergy() >= cost) {
            energyComp.changeEnergy(-cost); // Ziehe die Energie ab (wird autom. zum Client synchronisiert)
            return true; // Genug Energie da, Kette geht weiter!
        }

        // Nicht genug Energie -> Sende Nachricht und brich die Attacke ab
        user.sendMessage(Text.literal("Nicht genug Haki!"), true);
        return false;
    }
}
