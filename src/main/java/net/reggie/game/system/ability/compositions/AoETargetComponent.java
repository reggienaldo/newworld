package net.reggie.game.system.ability.compositions;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class AoETargetComponent implements AbilityComponent {
    private final double radius;

    public AoETargetComponent(double radius) {
        this.radius = radius;
    }

    @Override
    public boolean execute(ServerWorld world, ServerPlayerEntity user, Vec3d targetPos, List<Entity> targets) {
        // Sucht alle Entities im Radius um die Zielposition und fügt sie der Liste hinzu
        Box box = new Box(targetPos.subtract(radius, radius, radius), targetPos.add(radius, radius, radius));
        List<Entity> found = world.getOtherEntities(user, box);
        targets.addAll(found);
        return true;
    }
}
