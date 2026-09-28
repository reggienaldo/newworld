package net.reggie.game.system.haki_energy.compositions;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.reggie.NewWorld;
import net.reggie.game.system.ability.compositions.AbilityComponent;
import net.reggie.game.system.haki_energy.HakiEnergyComponent;

import java.util.List;

public class HaoshokuRequirementComponent implements AbilityComponent {

    @Override
    public boolean execute(ServerWorld world, ServerPlayerEntity user, Vec3d targetPos, List<Entity> targets) {
        // 1. Hole die Haki-Komponente vom Spieler
        HakiEnergyComponent haki = NewWorld.HAKI_ENERGY.get(user);

        // 2. FIX: Wir prüfen jetzt auf das echte, auserwählte ERWACHTE Königshaki!
        boolean isKing = haki.hasAwakenedHaoshoku();

        if (isKing) {
            return true; // Bedingung erfüllt! Die Fähigkeit darf ausgeführt werden.
        }

        // Wenn das Königshaki noch schläft oder der Spieler nicht das Gen besitzt
        user.sendMessage(Text.literal("§cDu besitzt nicht das Zeug zum König! Das Königshaki (Haoshoku) bleibt dir verwehrt."), true);
        return false; // Bricht die komplette Fähigkeiten-Kette ab!
    }
}
