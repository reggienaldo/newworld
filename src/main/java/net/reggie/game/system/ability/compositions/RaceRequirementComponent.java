package net.reggie.game.system.ability.compositions;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.reggie.NewWorld;
import net.reggie.game.system.race.PlayerRace;

import java.util.List;

public class RaceRequirementComponent implements AbilityComponent {
    private final PlayerRace requiredRace;

    public RaceRequirementComponent(PlayerRace requiredRace) {
        this.requiredRace = requiredRace;
    }

    @Override
    public boolean execute(ServerWorld world, ServerPlayerEntity user, Vec3d targetPos, List<Entity> targets) {
        PlayerRace playerRace = NewWorld.STATS.get(user).getRace();

        if (playerRace == this.requiredRace) {
            return true; // Rasse stimmt, Kette läuft weiter!
        }

        user.sendMessage(Text.literal("§cDiese Fähigkeit ist exklusiv für die Rasse: " + requiredRace.name()), true);
        return false; // Falsche Rasse -> Abbruch!
    }
}
