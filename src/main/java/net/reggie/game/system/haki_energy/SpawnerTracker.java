package net.reggie.game.system.haki_energy;

import net.minecraft.entity.Entity;

// Markiert Mobs aus (Trial-)Spawnern, damit sie keine Haki-XP geben.
// Der Tag wird mit dem Mob gespeichert und überlebt Chunk-Neuladen.
public final class SpawnerTracker {

    private static final String SPAWNER_TAG = "newworld.spawner";

    private SpawnerTracker() {}

    public static void mark(Entity entity) {
        // Auch Reiter (z. B. Spinnenjockey) mitmarkieren
        entity.streamSelfAndPassengers().forEach(e -> e.addCommandTag(SPAWNER_TAG));
    }

    public static boolean isFromSpawner(Entity entity) {
        return entity.getCommandTags().contains(SPAWNER_TAG);
    }
}
