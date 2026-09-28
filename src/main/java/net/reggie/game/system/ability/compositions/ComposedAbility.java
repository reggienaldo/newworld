package net.reggie.game.system.ability.compositions;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class ComposedAbility {
    private final List<AbilityComponent> components = new ArrayList<>();

    public ComposedAbility add(AbilityComponent component) {
        this.components.add(component);
        return this; // Ermöglicht Chaining (.add().add())
    }

    public void cast(ServerWorld world, ServerPlayerEntity user, Vec3d targetPos) {
        List<Entity> targets = new ArrayList<>();

        // Führe alle Bausteine nacheinander aus
        for (AbilityComponent component : components) {
            boolean continueChain = component.execute(world, user, targetPos, targets);
            if (!continueChain) break; // Falls eine Komponente die Kette stoppt
        }
    }
}
