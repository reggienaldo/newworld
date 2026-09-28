package net.reggie.game.system.haki_energy.compositions;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.reggie.NewWorld;
import net.reggie.game.system.ability.compositions.AbilityComponent;

import java.util.List;

public class HakiDamageComponent implements AbilityComponent {
    private final float baseDamage;

    public HakiDamageComponent(float baseDamage) {
        this.baseDamage = baseDamage;
    }

    @Override
    public boolean execute(ServerWorld world, ServerPlayerEntity user, Vec3d targetPos, List<Entity> targets) {
        // Hole das aktuelle Rüstungshaki-Level über Cardinal Components
        int busoLevel = NewWorld.HAKI_ENERGY.get(user).getBusoLevel();

        // Berechnung: Basis-Schaden + (Haki-Level * 3)
        // Level 1 = +3 Schaden | Level 5 = +15 Schaden!
        float finalDamage = this.baseDamage + (busoLevel * 3.0f);

        for (Entity target : targets) {
            target.damage(world.getDamageSources().playerAttack(user), finalDamage);
        }
        return true;
    }
}
