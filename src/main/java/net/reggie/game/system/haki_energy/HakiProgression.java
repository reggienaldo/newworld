package net.reggie.game.system.haki_energy;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.reggie.NewWorld;

import java.util.EnumSet;
import java.util.Set;

// Zentrale Anlaufstelle für Haki-Fortschritt, die andere Systeme (z. B. das Fähigkeiten-Inventar) nutzen können.
public final class HakiProgression {

    private HakiProgression() {}

    public static boolean isUnlocked(PlayerEntity player, HakiSkill skill) {
        return skill.isUnlocked(NewWorld.HAKI_ENERGY.get(player));
    }

    public static Set<HakiSkill> unlockedSkills(HakiEnergyComponent haki) {
        Set<HakiSkill> unlocked = EnumSet.noneOf(HakiSkill.class);
        for (HakiSkill skill : HakiSkill.values()) {
            if (skill.isUnlocked(haki)) unlocked.add(skill);
        }
        return unlocked;
    }

    // Meldet alle Fähigkeiten, die seit dem Schnappschuss 'before' neu freigeschaltet wurden
    public static void announceNewSkills(PlayerEntity player, Set<HakiSkill> before) {
        for (HakiSkill skill : unlockedSkills(NewWorld.HAKI_ENERGY.get(player))) {
            if (!before.contains(skill)) {
                player.sendMessage(Text.translatable("haki.newworld.skill_unlocked", skill.displayName()).formatted(Formatting.AQUA), false);
            }
        }
    }

    // Erweckt Königshaki, aber nur wenn der Spieler das Gen besitzt und es noch schläft.
    // Wird sowohl über Doriki (10.000) als auch über Haki-XP ausgelöst - was zuerst eintritt.
    public static boolean tryAwakenHaoshoku(PlayerEntity player) {
        if (!NewWorld.HAKI_ENERGY.get(player).hasPotentialHaoshoku()) return false;
        return awakenHaoshoku(player);
    }

    // Erweckt Königshaki ohne Gen-Prüfung (sehr hohe Haki-XP). Nichts passiert, wenn es schon erwacht ist.
    public static boolean awakenHaoshoku(PlayerEntity player) {
        HakiEnergyComponent haki = NewWorld.HAKI_ENERGY.get(player);
        if (haki.hasAwakenedHaoshoku()) return false;
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return false;

        Set<HakiSkill> before = unlockedSkills(haki);
        haki.setHaoshokuAwakened(true);
        player.sendMessage(Text.translatable("haki.newworld.haoshoku_awakened").formatted(Formatting.LIGHT_PURPLE), false);

        // Schockwelle: alle Wesen im Umkreis von 15 Blöcken werden weggestoßen
        Box area = player.getBoundingBox().expand(15.0);
        for (LivingEntity living : serverWorld.getEntitiesByClass(LivingEntity.class, area, entity -> entity != player)) {
            Vec3d push = living.getPos().subtract(player.getPos()).normalize().multiply(2.5);
            living.setVelocity(push.x, 0.5, push.z);
            living.velocityModified = true;
        }

        announceNewSkills(player, before);
        return true;
    }
}
