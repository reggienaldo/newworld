package net.reggie.mixin;

import net.minecraft.block.spawner.MobSpawnerLogic;
import net.minecraft.entity.Entity;
import net.reggie.game.system.haki_energy.SpawnerTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(MobSpawnerLogic.class)
public abstract class MobSpawnerLogicMixin {

    // Markiert jeden Mob, bevor der Spawner ihn in die Welt setzt
    @ModifyArg(
            method = "serverTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnNewEntityAndPassengers(Lnet/minecraft/entity/Entity;)Z")
    )
    private Entity newworld$markSpawnerMob(Entity entity) {
        SpawnerTracker.mark(entity);
        return entity;
    }
}
