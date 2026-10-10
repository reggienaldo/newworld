package net.reggie.game.system.ability.compositions;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class DamageComponent implements AbilityComponent {
    private final float damageAmount;

    public DamageComponent(float damageAmount) {
        this.damageAmount = damageAmount;
    }

    @Override
    public boolean execute(ServerWorld world, ServerPlayerEntity user, Vec3d targetPos, List<Entity> targets) {
        for (Entity target : targets) {
            target.damage(world.getDamageSources().playerAttack(user), this.damageAmount);
        }
        return true; // Weiter zum nächsten Effekt
    }
}
