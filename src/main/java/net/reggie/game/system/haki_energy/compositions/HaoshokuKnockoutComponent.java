package net.reggie.game.system.haki_energy.compositions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.reggie.game.system.ability.compositions.AbilityComponent;

import java.util.List;

public class HaoshokuKnockoutComponent implements AbilityComponent {
    private final double maxHealthThreshold; // Mobs mit weniger als diesem Max-Leben kippen sofort um

    public HaoshokuKnockoutComponent(double maxHealthThreshold) {
        this.maxHealthThreshold = maxHealthThreshold;
    }

    @Override
    public boolean execute(ServerWorld world, ServerPlayerEntity user, Vec3d targetPos, List<Entity> targets) {
        for (Entity entity : targets) {
            if (entity instanceof LivingEntity living && entity != user) {

                // Wenn es ein schwacher Standard-Mob ist (z.B. Zombie, Skelett, Huhn)
                if (living.getMaxHealth() <= this.maxHealthThreshold) {

                    // Wir verpassen dem Mob extreme Effekte, damit er "umkippt" / kampfunfähig wird:
                    // Slowness 10 (Einfrieren) und Dunkelheit/Blindheit
                    living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 9, false, false));
                    living.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 200, 9, false, false));

                    // Wenn es eine steuerbare KI ist (Zombie etc.), schalten wir sie temporär stumm
                    if (living instanceof MobEntity mob) {
                        mob.setTarget(null); // Vergisst sein aktuelles Angriffsziel
                    }

                    // Visuelles Feedback über dem Kopf des Mobs
                    living.setCustomName(Text.literal("§7💤 Ohnmächtig"));
                    living.setCustomNameVisible(true);
                } else {
                    // Starke Mobs oder Bosse werden nicht ohnmächtig, werden aber ein Stück zurückgeworfen (Präsenz-Druck)
                    Vec3d pushVector = living.getPos().subtract(user.getPos()).normalize().multiply(1.5);
                    living.setVelocity(pushVector.x, 0.4, pushVector.z);
                    living.velocityModified = true;
                }
            }
        }
        return true;
    }
}
