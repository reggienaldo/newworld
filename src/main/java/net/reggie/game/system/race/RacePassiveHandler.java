package net.reggie.game.system.race;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.reggie.NewWorld;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

public class RacePassiveHandler {

    //BUFFS
    private static final Identifier GIANT_HEALTH_ID = Identifier.of(NewWorld.MOD_ID, "giant_health");

    private static final Identifier ONI_STRENGTH_ID = Identifier.of(NewWorld.MOD_ID, "oni_strength");
    private static final Identifier ONI_ARMOR_ID = Identifier.of(NewWorld.MOD_ID, "oni_armor");

    private static final Identifier LUNARIAN_MOVEMENT_SPEED_ID = Identifier.of(NewWorld.MOD_ID, "lunarian_movement_speed");

    private static final Identifier FISHMAN_WATER_MOVEMENT_EFFIENCY_ID = Identifier.of(NewWorld.MOD_ID, "fishman_water_movement_effinency");

    private static final Identifier MINK_MOVEMENT_SPEED_ID = Identifier.of(NewWorld.MOD_ID, "mink_movement_speed");

    //DEBUFFS
    private static final Identifier GIANT_SLOWNESS_ID = Identifier.of(NewWorld.MOD_ID, "giant_slowness");

    private static final Identifier VAMPIRE_WEAKNESS_ID = Identifier.of(NewWorld.MOD_ID, "vampire_weakness");

    private static final Identifier FISHMAN_LOW_HP_SLOWNESS_ID = Identifier.of(NewWorld.MOD_ID, "fishman_low_hp_slowness");

    private static final Identifier LUNARIAN_LOW_HP_SLOWNESS_ID = Identifier.of(NewWorld.MOD_ID, "lunarian_low_hp_slowness");
    private static final Identifier LUNARIAN_LOW_HP_WEAKNESS_ID = Identifier.of(NewWorld.MOD_ID, "lunarian_low_hp_weakness");

    // Diese Methode wird aufgerufen, um die nativen MC-Stats an die Rasse anzupassen
    public static void applyRaceAttributes(ServerPlayerEntity player) {
        // 1. Zuerst alle alten Rassen-Modifier entfernen, um Dopplungen zu vermeiden
        removeRaceAttributes(player);

        // 2. Pehkui-Daten für den Basis-Größentyp holen
        ScaleData scaleData = ScaleTypes.BASE.getScaleData(player);

        // 2. Aktuelle Rasse aus deinen Cardinal Components auslesen
        PlayerRace race = NewWorld.STATS.get(player).getRace();

        // 3. Je nach Rasse die echten Minecraft-Attribute modifizieren
        switch (race) {
            case GIANT -> {
                // Riesen bekommen +20 zusätzliche Lebenspunkte (entspricht 10 vollen Herzen)
                // ADDITION sorgt dafür, dass es einfach oben drauf gerechnet wird.
                if (player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH) != null) {
                    player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).addTemporaryModifier(
                            new EntityAttributeModifier(GIANT_HEALTH_ID, 20.0, EntityAttributeModifier.Operation.ADD_VALUE)
                    );
                    // Den Spieler vollheilen, damit die neuen Herzen direkt gefüllt sind
                    player.setHealth(player.getMaxHealth());
                }
                if (player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED) != null) {
                    player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).addTemporaryModifier(
                            new EntityAttributeModifier(GIANT_SLOWNESS_ID, -0.02, EntityAttributeModifier.Operation.ADD_VALUE)
                    );
                }
                scaleData.setTargetScale(3.0f); // Zielgröße setzen
                scaleData.setScale(3.0f);       // WICHTIG: Überspringt die Wachs-Animation sofort
            }
            case ONI -> {
                // Oni bekommen +3 permanenten Angriffsschaden auf jeden Schlag (auch ohne Waffe)
                if (player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE) != null) {
                    player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).addTemporaryModifier(
                            new EntityAttributeModifier(ONI_STRENGTH_ID, 3.0, EntityAttributeModifier.Operation.ADD_VALUE)
                    );
                }
                if (player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR) != null) {
                    player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).addTemporaryModifier(
                            new EntityAttributeModifier(ONI_ARMOR_ID, 3.0, EntityAttributeModifier.Operation.ADD_VALUE)
                    );
                }
                scaleData.setTargetScale(1.5f);
                scaleData.setScale(1.5f);       // Direkt auf 1.5f springen
            }
            case LUNARIAN -> {
                // Lunarier bekommen +6 permanenten Rüstungswert (als hätten sie unsichtbare Rüstung an)
                if (player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED) != null) {
                    player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).addTemporaryModifier(
                            new EntityAttributeModifier(LUNARIAN_MOVEMENT_SPEED_ID, 0.03, EntityAttributeModifier.Operation.ADD_VALUE)
                    );
                }
                scaleData.setTargetScale(1.0f);
                scaleData.setScale(1.0f);       // Direkt auf 1.0f zurückspringen (kein unschönes Zusammenschrumpfen)
            }
            case FISHMAN -> {
                // Lunarier bekommen +6 permanenten Rüstungswert (als hätten sie unsichtbare Rüstung an)
                if (player.getAttributeInstance(EntityAttributes.GENERIC_WATER_MOVEMENT_EFFICIENCY) != null) {
                    player.getAttributeInstance(EntityAttributes.GENERIC_WATER_MOVEMENT_EFFICIENCY).addTemporaryModifier(
                            new EntityAttributeModifier(FISHMAN_WATER_MOVEMENT_EFFIENCY_ID, 5.0, EntityAttributeModifier.Operation.ADD_VALUE)
                    );
                }
                scaleData.setTargetScale(1.0f);
                scaleData.setScale(1.0f);       // Direkt auf 1.0f zurückspringen (kein unschönes Zusammenschrumpfen)
            }
            case MINK -> {
                // Lunarier bekommen +6 permanenten Rüstungswert (als hätten sie unsichtbare Rüstung an)
                if (player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED) != null) {
                    player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).addTemporaryModifier(
                        new EntityAttributeModifier(MINK_MOVEMENT_SPEED_ID, 0.05, EntityAttributeModifier.Operation.ADD_VALUE)
                    );
                }
                scaleData.setTargetScale(1.0f);
                scaleData.setScale(1.0f);       // Direkt auf 1.0f zurückspringen (kein unschönes Zusammenschrumpfen)
            }
            default -> {
                // Menschen, Minks etc. haben keine nativen Basis-Attribut-Veränderungen
                scaleData.setTargetScale(1.0f);
                scaleData.setScale(1.0f);       // Direkt auf 1.0f zurückspringen (kein unschönes Zusammenschrumpfen)
            }
        }
    }

    // Entfernt alle Rassen-Modifier (wichtig bei Rassenwechsel)
    public static void removeRaceAttributes(ServerPlayerEntity player) {
        if (player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH) != null) {
            player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).removeModifier(GIANT_HEALTH_ID);
        }
        if (player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE) != null) {
            player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).removeModifier(ONI_STRENGTH_ID);
            player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).removeModifier(LUNARIAN_LOW_HP_WEAKNESS_ID);
        }
        if (player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR) != null) {
            player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).removeModifier(ONI_ARMOR_ID);
        }
        if (player.getAttributeInstance(EntityAttributes.GENERIC_WATER_MOVEMENT_EFFICIENCY) != null) {
            player.getAttributeInstance(EntityAttributes.GENERIC_WATER_MOVEMENT_EFFICIENCY).removeModifier(FISHMAN_WATER_MOVEMENT_EFFIENCY_ID);
        }
        if (player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED) != null) {
            player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).removeModifier(LUNARIAN_MOVEMENT_SPEED_ID);
            player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).removeModifier(MINK_MOVEMENT_SPEED_ID);
            player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).removeModifier(FISHMAN_LOW_HP_SLOWNESS_ID);
            player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).removeModifier(LUNARIAN_LOW_HP_SLOWNESS_ID);
        }
    }
    // 2. TICK-BASIERTE DEBUFFS (Wird 20-mal pro Sekunde über das Player-Tick-Event ausgeführt!)
    public static void handlePlayerPassiveTicks(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        PlayerRace race = NewWorld.STATS.get(player).getRace();

        switch (race) {

            case SKYPIEAN -> {

                if (!player.isOnGround() && player.fallDistance > 0.5f && player.isSneaking()) {
                    // Gibt Slow Falling für 1 Sekunde (wird permanent neu appliziert, solange er in der Luft sneakt)
                    player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.SLOW_FALLING,
                            20,    // 20 Ticks = 1 Sekunde duration (verhindert das Flackern, da es permanent neu triggert)
                            0,     // Stufe 1
                            true,  // Ambient
                            false, // Keine Partikel
                            false  // Kein Icon
                    ));
                }
            }
            case LUNARIAN -> {

                if (!player.isOnGround() && player.fallDistance > 0.5f && player.isSneaking()) {
                    // Gibt Slow Falling für 1 Sekunde (wird permanent neu appliziert, solange er in der Luft sneakt)
                    player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.SLOW_FALLING,
                            20,    // 20 Ticks = 1 Sekunde duration (verhindert das Flackern, da es permanent neu triggert)
                            0,     // Stufe 1
                            true,  // Ambient
                            false, // Keine Partikel
                            false  // Kein Icon
                    ));
                }

                var speedAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
                var damageAttr = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);

                // Wenn der Spieler 3 Herzen (6 HP) oder weniger hat
                if (player.getHealth() <= 6.0f) {

                    // 1. Verlangsamung (Slowness) anwenden
                    if (speedAttr != null && speedAttr.getModifier(LUNARIAN_LOW_HP_SLOWNESS_ID) == null) {
                        speedAttr.addTemporaryModifier(new EntityAttributeModifier(
                                LUNARIAN_LOW_HP_SLOWNESS_ID,
                                -0.03, // Macht den Spieler ca. 20-25% langsamer
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ));
                    }

                    // 2. Schwäche (Weaker / Weniger Schaden) anwenden
                    if (damageAttr != null && damageAttr.getModifier(LUNARIAN_LOW_HP_WEAKNESS_ID) == null) {
                        damageAttr.addTemporaryModifier(new EntityAttributeModifier(
                                LUNARIAN_LOW_HP_WEAKNESS_ID,
                                -3.0, // Zieht 3 Schaden (1,5 Herzen) von jedem Nahkampfangriff ab
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ));

                        // Einmalige Nachricht im Actionbar-Slot
                        player.sendMessage(net.minecraft.text.Text.literal("§cDeine Flamme erlischt... Du wirst schwächer!"), true);
                    }
                } else {
                    // Sobald sich der Lunarier wieder über 3 Herzen hochheilt, werden beide Debuffs sofort gelöscht
                    if (speedAttr != null && speedAttr.getModifier(LUNARIAN_LOW_HP_SLOWNESS_ID) != null) {
                        speedAttr.removeModifier(LUNARIAN_LOW_HP_SLOWNESS_ID);
                    }
                    if (damageAttr != null && damageAttr.getModifier(LUNARIAN_LOW_HP_WEAKNESS_ID) != null) {
                        damageAttr.removeModifier(LUNARIAN_LOW_HP_WEAKNESS_ID);
                    }
                }
            }
            case ONI -> {
                // DEBUFF: Erhöhter Hunger für den Oni
                // Der Hunger-Level (Exhaustion) bestimmt, wie schnell die Hungerkeulen sinken.
                // Ein Wert von 0.005f pro Tick sorgt dafür, dass ein Oni ca. doppelt so schnell Hunger bekommt!
                player.getHungerManager().addExhaustion(0.03f);
            }
            case VAMPIRE -> {
                // DEBUFF: Vampir verbrennt/schwächt im direkten Sonnenlicht
                // Prüft ob es Tag ist, es nicht regnet und der Spieler unter freiem Himmel steht
                if (world.isDay() && !world.isRaining() && world.isSkyVisible(player.getBlockPos())) {
                    // Verursacht alle 20 Ticks (1 Sekunde) 1 Schadenspunkt (1/2 Herz) durch Verbrennen
                    if (player.age % 20 == 0) {
                        player.damage(world.getDamageSources().onFire(), 1.0f);
                    }
                }
            }
            case MINK -> {
                // Wenn es im Minecraft-Server aktuell Nacht ist (Mond steht am Himmel)
                if (world.isNight()) {
                    // Gibt dem Mink unsichtbare Nachtsicht (keine Partikel, kein Icon)
                    player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.NIGHT_VISION,
                            220,   // Über 200 Ticks flackert Minecraft nicht mehr
                            0,
                            true,  // Ambient
                            false, // Keine Partikel
                            false  // Kein Icon
                    ));
                }
            }
            case FISHMAN -> {
                if (player.isSubmergedInWater()) {
                    // Setzt die verbleibende Luft permanent auf den Maximalwert (300 = volle Bläschen)
                    player.setAir(player.getMaxAir());

                    player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.NIGHT_VISION,
                            40,    // Dauer: 1 Sekunde (erneuert sich permanent)
                            0,
                            true,  // Ambient
                            false, // Keine Partikel
                            false  // Kein Icon oben rechts
                    ));
                    // 3. NEU: LOW-HP-VERLANGSAMUNG MECHANIK
                    // Wir prüfen das aktuelle Leben des Spielers. 6.0f entspricht exakt 3 vollen Herzen.
                    var speedAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED); // In neueren MC-Versionen: EntityAttributes.GENERIC_MOVEMENT_SPEED

                    if (speedAttr != null) {
                        if (player.getHealth() <= 6.0f) {
                            // Wenn der Spieler Low HP hat und der Modifier noch nicht aktiv ist, fügen wir ihn hinzu
                            if (speedAttr.getModifier(FISHMAN_LOW_HP_SLOWNESS_ID) == null) {
                                // -0.04 entspricht in Minecraft etwa dem spürbaren Verlangsamungs-Effekt von Slowness II (ca. 30% langsamer)
                                speedAttr.addTemporaryModifier(new EntityAttributeModifier(
                                        FISHMAN_LOW_HP_SLOWNESS_ID,
                                        -0.04,
                                        EntityAttributeModifier.Operation.ADD_VALUE
                                ));
                                // Kurze Feedback-Nachricht im Actionbar-Slot
                                player.sendMessage(Text.literal("§cDu bist schwer verwundet und wirst langsamer!"), true);
                            }
                        } else {
                            // Sobald sich der Fischmensch wieder über 3 Herzen hochheilt,
                            // wird der Verlangsamungs-Modifier sofort wieder gelöscht.
                            if (speedAttr.getModifier(FISHMAN_LOW_HP_SLOWNESS_ID) != null) {
                                speedAttr.removeModifier(FISHMAN_LOW_HP_SLOWNESS_ID);
                            }
                        }
                    }

                    // Optional: Dem Fischmenschen im Wasser zusätzliche Schwimmgeschwindigkeit geben (Dolphins Grace)
                    if (!player.hasStatusEffect(StatusEffects.DOLPHINS_GRACE)) {
                        player.addStatusEffect(new StatusEffectInstance(
                                StatusEffects.DOLPHINS_GRACE, 40, 10, false, false, false
                        ));
                    }
                }
                // DEBUFF: Fischmenschen sind außerhalb des Wassers oder Regens etwas schwächer
                if (!player.isSubmergedInWater() && !world.isRaining()) {
                    // Fügt jede Sekunde Erschöpfung hinzu oder verlangsamt sie minimal auf Land
                    player.getHungerManager().addExhaustion(0.001f);
                }
            }
            default -> {}
        }
    }
}