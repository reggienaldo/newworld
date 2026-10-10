package net.reggie.mixin;

import net.minecraft.block.spawner.TrialSpawnerLogic;
import net.minecraft.entity.Entity;
import net.reggie.game.system.haki_energy.SpawnerTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(TrialSpawnerLogic.class)
public abstract class TrialSpawnerLogicMixin {

    // Markiert jeden Mob, bevor der Trial-Spawner ihn in die Welt setzt
    @ModifyArg(
            method = "trySpawnMob",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnNewEntityAndPassengers(Lnet/minecraft/entity/Entity;)Z")
    )
    private Entity newworld$markSpawnerMob(Entity entity) {
        SpawnerTracker.mark(entity);
        return entity;
    }
}
