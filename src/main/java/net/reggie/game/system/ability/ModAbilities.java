package net.reggie.game.system.ability;

import net.minecraft.particle.ParticleTypes;
import net.reggie.game.system.ability.compositions.*;
import net.reggie.game.system.cooldown.CooldownComponent;
import net.reggie.game.system.haki_energy.compositions.HaoshokuKnockoutComponent;
import net.reggie.game.system.haki_energy.compositions.HaoshokuRequirementComponent;

public class ModAbilities {

    // Die fertige, zusammengesteckte Königshaki-Fähigkeit
    public static ComposedAbility HAOSHOKU_BURST;

    public static void registerAbilities() {
        HAOSHOKU_BURST = new ComposedAbility()
                // 1. CHECK: Muss im Kampfmodus sein (Taste R)
                .add(new CombatModeRequirementComponent())

                // 2. CHECK: Muss das Königshaki im RPG-System freigeschaltet haben
                .add(new HaoshokuRequirementComponent())

                // 3. KOSTEN: Zieht dem Spieler 50 Haki-Energie ab
                .add(new EnergyCostComponent(50))

                // 4. COOLDOWN: Setzt einen Cooldown von 10 Sekunden (200 Ticks) auf diesen Move
                .add(new CooldownComponent("haoshoku_burst", 200))

                // 5. ZIELSUCHE: Erfasst alle Mobs/Spieler in einem riesigen Radius von 12 Blöcken
                .add(new AoETargetComponent(12.0))

                // 6. EFFEKT: Schwache Mobs (bis 30 maxHP) werden ohnmächtig, Bosse fliegen zurück
                .add(new HaoshokuKnockoutComponent(30.0))

                // 7. VISUELL: Spawnt die neuen, großen 1.21 Gust-Partikel als Schockwelle vor dem Spieler
                .add(new ParticleComponent(ParticleTypes.GUST, 5));



    }
}