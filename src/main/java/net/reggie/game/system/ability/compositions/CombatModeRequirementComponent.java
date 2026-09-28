package net.reggie.game.system.ability.compositions;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.reggie.NewWorld;

import java.util.List;

public class CombatModeRequirementComponent implements AbilityComponent {

    @Override
    public boolean execute(ServerWorld world, ServerPlayerEntity user, Vec3d targetPos, List<Entity> targets) {
        boolean inCombat = NewWorld.HAKI_ENERGY.get(user).isInCombatMode();

        if (inCombat) {
            return true; // Kampfmodus ist aktiv, Fähigkeit wird ausgeführt!
        }

        user.sendMessage(Text.literal("§cDu musst im Kampfmodus (Taste R) sein, um Fähigkeiten zu nutzen!"), true);
        return false; // Bricht die Kette ab
    }
}
