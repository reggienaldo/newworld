package net.reggie.game.system.ability.compositions;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.reggie.NewWorld;
import net.reggie.game.system.stats.StatType;

import java.util.List;

public class DevilFruitDamageComponent implements AbilityComponent {
    private final float baseDamage;
    private final float scalingFactor;

    public DevilFruitDamageComponent(float baseDamage, float scalingFactor) {
        this.baseDamage = baseDamage;
        this.scalingFactor = scalingFactor;
    }

    @Override
    public boolean execute(ServerWorld world, ServerPlayerEntity user, Vec3d targetPos, List<Entity> targets) {
        // Hole den Teufelsfrucht-Stat des Spielers (z.B. Level 5)
        int fruitStat = NewWorld.STATS.get(user).getStat(StatType.DEVIL_FRUIT);

        // Berechnung: Basis-Schaden + (Stat-Level * Skalierung)
        // z.B. 10.0 + (5 * 2.0) = 20.0 Schaden
        float finalDamage = this.baseDamage + (fruitStat * this.scalingFactor);

        for (Entity target : targets) {
            target.damage(world.getDamageSources().playerAttack(user), finalDamage);
        }
        return true;
    }
}
