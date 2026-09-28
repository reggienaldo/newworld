package net.reggie.game.system.ability.compositions;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.List;

@FunctionalInterface
public interface AbilityComponent {
    // Führt die Logik der Komponente aus.
    // Gibt true zurück, wenn die Kette fortgesetzt werden soll.
    boolean execute(ServerWorld world, ServerPlayerEntity user, Vec3d targetPos, List<Entity> targets);
}