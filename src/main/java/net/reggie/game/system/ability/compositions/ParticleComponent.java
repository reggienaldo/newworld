package net.reggie.game.system.ability.compositions;

import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class ParticleComponent implements AbilityComponent {
    private final ParticleEffect particle;
    private final int count;

    public ParticleComponent(ParticleEffect particle, int count) {
        this.particle = particle;
        this.count = count;
    }

    @Override
    public boolean execute(ServerWorld world, ServerPlayerEntity user, Vec3d targetPos, List<Entity> targets) {
        // Erzeugt Partikel am Zielort
        world.spawnParticles(particle, targetPos.x, targetPos.y, targetPos.z, count, 0.5, 0.5, 0.5, 0.1);
        return true;
    }
}
