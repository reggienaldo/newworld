package net.reggie.game.system.haki_energy.compositions;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.reggie.NewWorld;
import net.reggie.game.system.ability.compositions.AbilityComponent;
import net.reggie.game.system.haki_energy.HakiBalance;

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

        // Berechnung: Basis-Schaden + Bonus, der pro Level langsamer wächst (siehe HakiBalance)
        // Level 1 = +3 | Level 10 = +16 | Level 25 = +27 | Level 100 = +57
        float finalDamage = this.baseDamage + HakiBalance.busoDamageBonus(busoLevel);

        for (Entity target : targets) {
            target.damage(world.getDamageSources().playerAttack(user), finalDamage);
        }
        return true;
    }
}
